[CmdletBinding()]
param(
    [switch]$Clean,
    [switch]$Unsigned
)

$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$workspace = Split-Path -Parent $project
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } elseif ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { 'D:\programs\android' }
$javaHome = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { 'D:\programs\java\jdk-17.0.18' }
$build = Join-Path $project '.build'
$resZip = Join-Path $build 'compiled-resources.zip'
$gen = Join-Path $build 'gen'
$classes = Join-Path $build 'classes'
$classesJar = Join-Path $build 'classes.jar'
$dex = Join-Path $build 'dex'
$rawApk = Join-Path $build 'app-unsigned-unaligned.apk'
$finalApk = Join-Path $workspace 'HiPER-Calc-Pro-12.1.4-replica-unsigned.apk'
$signedApk = Join-Path $workspace 'HiPER-Calc-Pro-12.1.4-replica-debug-signed.apk'
$keystore = Join-Path $project 'keystore\hiper-debug.jks'
$platform = Get-ChildItem -LiteralPath (Join-Path $sdk 'platforms') -Directory |
    Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'android.jar') } |
    Sort-Object { [int]($_.Name -replace '^android-', '') } -Descending |
    Select-Object -First 1
$buildTools = Get-ChildItem -LiteralPath (Join-Path $sdk 'build-tools') -Directory |
    Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'aapt2.exe') } |
    Sort-Object { [version]$_.Name } -Descending |
    Select-Object -First 1
if (-not $platform) { throw "No Android platform found under $sdk\platforms" }
if (-not $buildTools) { throw "No Android build-tools found under $sdk\build-tools" }
$androidJar = Join-Path $platform.FullName 'android.jar'
$aapt2 = Join-Path $buildTools.FullName 'aapt2.exe'
$d8 = Join-Path $buildTools.FullName 'd8.bat'
$zipalign = Join-Path $buildTools.FullName 'zipalign.exe'
$apksigner = Join-Path $buildTools.FullName 'apksigner.bat'
$keytool = Join-Path $javaHome 'bin\keytool.exe'
$javac = Join-Path $javaHome 'bin\javac.exe'
$jar = Join-Path $javaHome 'bin\jar.exe'

if ($Clean -and (Test-Path -LiteralPath $build)) {
    $resolvedBuild = [IO.Path]::GetFullPath($build)
    $resolvedProject = [IO.Path]::GetFullPath($project)
    if (-not $resolvedBuild.StartsWith($resolvedProject, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to clean outside project: $resolvedBuild"
    }
    Remove-Item -LiteralPath $resolvedBuild -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $build, $gen, $classes, $dex | Out-Null

& $aapt2 compile --dir (Join-Path $project 'res') -o $resZip
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile failed: $LASTEXITCODE" }

& $aapt2 link -o $rawApk -I $androidJar `
    --manifest (Join-Path $project 'AndroidManifest.xml') `
    -R $resZip --java $gen --auto-add-overlay
if ($LASTEXITCODE -ne 0) { throw "aapt2 link failed: $LASTEXITCODE" }

$sources = @(
    Get-ChildItem -LiteralPath (Join-Path $project 'src') -Recurse -File -Filter '*.java'
    Get-ChildItem -LiteralPath $gen -Recurse -File -Filter '*.java'
) | Select-Object -ExpandProperty FullName

& $javac -encoding UTF-8 -source 17 -target 17 -Xlint:-options -cp $androidJar -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw "javac failed: $LASTEXITCODE" }

& $jar --create --file $classesJar -C $classes .
if ($LASTEXITCODE -ne 0) { throw "jar failed: $LASTEXITCODE" }

& $d8 --lib $androidJar --min-api 29 --output $dex $classesJar
if ($LASTEXITCODE -ne 0) { throw "d8 failed: $LASTEXITCODE" }

& $jar --update --file $rawApk -C $dex classes.dex
if ($LASTEXITCODE -ne 0) { throw "adding classes.dex failed: $LASTEXITCODE" }

# Signing is intentionally skipped. This produces an unsigned install artifact.
& $zipalign -f -p 4 $rawApk $finalApk
if ($LASTEXITCODE -ne 0) { throw "zipalign failed: $LASTEXITCODE" }

if (-not $Unsigned) {
    if (-not (Test-Path -LiteralPath $keystore)) {
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $keystore) | Out-Null
        & $keytool -genkeypair -v -keystore $keystore `
            -storepass hiperdebug -keypass hiperdebug -alias hiper-replica `
            -keyalg RSA -keysize 2048 -validity 10000 `
            -dname 'CN=HiPER Calc Pro Replica, OU=Local Build, O=Independent Replica, L=Shanghai, ST=Shanghai, C=CN'
        if ($LASTEXITCODE -ne 0) { throw "keytool failed: $LASTEXITCODE" }
    }
    & $apksigner sign --ks $keystore --ks-key-alias hiper-replica --min-sdk-version 24 `
        --ks-pass 'pass:hiperdebug' --key-pass 'pass:hiperdebug' `
        --v1-signing-enabled false --v2-signing-enabled true --v3-signing-enabled false `
        --out $signedApk $finalApk
    if ($LASTEXITCODE -ne 0) { throw "apksigner failed: $LASTEXITCODE" }
    & $apksigner verify --verbose $signedApk
    if ($LASTEXITCODE -ne 0) { throw "signed APK verification failed: $LASTEXITCODE" }
    $finalApk = $signedApk
}

$hash = (Get-FileHash -LiteralPath $finalApk -Algorithm SHA256).Hash
Write-Host "Built: $finalApk"
Write-Host "SHA-256: $hash"
Write-Host ($(if ($Unsigned) { "Signing: skipped" } else { "Signing: local debug key" }))
