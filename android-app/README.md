# Native Android replica

This is a standalone native Android application, not a WebView and not a PWA.

## Compatibility

- Package: `cz.hipercalc.pro`
- Version: `12.1.4` / versionCode `287`
- minSdk: 29
- targetSdk: 34
- ABI: architecture-independent Java/DEX; no native libraries
- Google Play services: none
- Network permission: none
- Google license verification: omitted
- APK signing: intentionally omitted

The public package identity matches the original. The APK is intentionally unsigned, so the system will not treat it as a valid update to the Play-signed original. Uninstall or otherwise resolve the existing package/signing conflict before installation.

## Build

The build script locates `ANDROID_HOME`/`ANDROID_SDK_ROOT`, selects the highest installed platform and build-tools, and uses JDK 17. Gradle is not required.

```powershell
.\build.ps1 -Clean
```

Output:

```text
..\HiPER-Calc-Pro-12.1.4-replica-debug-signed.apk
```

The build creates a local debug keystore at `keystore/hiper-debug.jks` and signs with APK Signature Scheme v2, which is supported by Android 7+ and Android-compatible HarmonyOS releases. Use `-Unsigned` only when an unsigned intermediate is explicitly required.

The public package name matches the original, but the certificate does not. If the Play-signed original is installed, Android/HarmonyOS will reject this APK with a package/signature conflict. Uninstall the original first or change the package name.

## Differences from the original APK

The proprietary PairIP-wrapped arithmetic core, encrypted resources, Google licensing, upgrade flow, and proprietary OCR service are not copied.

The native replica implements:

- BigDecimal exact arithmetic and trigonometric/transcendental functions up to 100 displayed digits
- six result modes and five floating formats
- polynomial equations through degree six
- complex, matrix/vector, statistics/regression, bitwise, number-theory and limit/table tools
- history, exact variables, Ans/LastX, constants, expanded unit conversion, settings and backup
- system camera/photo input with interactive crop

Known differences and remaining work are tracked in `analysis/GAP_MATRIX.md`.
