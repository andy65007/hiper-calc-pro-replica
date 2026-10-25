package app.hipercalc.replica;

public final class ComplexMath {
    public final double real;
    public final double imaginary;

    public ComplexMath(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public ComplexMath add(ComplexMath other) {
        return new ComplexMath(real + other.real, imaginary + other.imaginary);
    }

    public ComplexMath subtract(ComplexMath other) {
        return new ComplexMath(real - other.real, imaginary - other.imaginary);
    }

    public ComplexMath multiply(ComplexMath other) {
        return new ComplexMath(real * other.real - imaginary * other.imaginary,
                real * other.imaginary + imaginary * other.real);
    }

    public ComplexMath divide(ComplexMath other) {
        double denominator = other.real * other.real + other.imaginary * other.imaginary;
        return new ComplexMath(
                (real * other.real + imaginary * other.imaginary) / denominator,
                (imaginary * other.real - real * other.imaginary) / denominator
        );
    }

    public ComplexMath power(int exponent) {
        ComplexMath result = new ComplexMath(1, 0);
        ComplexMath base = exponent < 0 ? new ComplexMath(1, 0).divide(this) : this;
        int n = Math.abs(exponent);
        while (n > 0) {
            if ((n & 1) == 1) result = result.multiply(base);
            base = base.multiply(base);
            n >>= 1;
        }
        return result;
    }

    public double abs() {
        return Math.hypot(real, imaginary);
    }

    public double argument() {
        return Math.atan2(imaginary, real);
    }

    public ComplexMath conjugate() {
        return new ComplexMath(real, -imaginary);
    }

    public ComplexMath exp() {
        double exp = Math.exp(real);
        return new ComplexMath(exp * Math.cos(imaginary), exp * Math.sin(imaginary));
    }

    public ComplexMath log() {
        return new ComplexMath(Math.log(abs()), argument());
    }

    public ComplexMath sqrt() {
        double magnitude = Math.sqrt(abs());
        double angle = argument() / 2;
        return new ComplexMath(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
    }

    public ComplexMath sin() {
        return new ComplexMath(Math.sin(real) * Math.cosh(imaginary), Math.cos(real) * Math.sinh(imaginary));
    }

    public ComplexMath cos() {
        return new ComplexMath(Math.cos(real) * Math.cosh(imaginary), -Math.sin(real) * Math.sinh(imaginary));
    }

    public ComplexMath tan() {
        return sin().divide(cos());
    }

    public ComplexMath sinh() {
        return new ComplexMath(Math.sinh(real) * Math.cos(imaginary), Math.cosh(real) * Math.sin(imaginary));
    }

    public ComplexMath cosh() {
        return new ComplexMath(Math.cosh(real) * Math.cos(imaginary), Math.sinh(real) * Math.sin(imaginary));
    }

    public ComplexMath tanh() {
        return sinh().divide(cosh());
    }

    public ComplexMath power(ComplexMath exponent) {
        return exponent.multiply(log()).exp();
    }
}
