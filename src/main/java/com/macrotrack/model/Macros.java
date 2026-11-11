package com.macrotrack.model;

import java.util.Locale;

/**
 * Conjunto de calorías y macronutrientes. Es inmutable: cada operación
 * devuelve un objeto Macros nuevo y no modifica el original.
 */
public class Macros {

    /** Macros en cero, útil como valor inicial al sumar. */
    public static final Macros CERO = new Macros(0, 0, 0, 0);

    private final double kcal;
    private final double proteina;
    private final double carbohidratos;
    private final double grasas;

    public Macros(double kcal, double proteina, double carbohidratos, double grasas) {
        this.kcal = kcal;
        this.proteina = proteina;
        this.carbohidratos = carbohidratos;
        this.grasas = grasas;
    }

    public double getKcal() {
        return kcal;
    }

    public double getProteina() {
        return proteina;
    }

    public double getCarbohidratos() {
        return carbohidratos;
    }

    public double getGrasas() {
        return grasas;
    }

    /** Devuelve la suma de estos macros con los de otro. */
    public Macros sumar(Macros otro) {
        return new Macros(
                kcal + otro.kcal,
                proteina + otro.proteina,
                carbohidratos + otro.carbohidratos,
                grasas + otro.grasas);
    }

    /** Devuelve la resta de estos macros menos los de otro. */
    public Macros restar(Macros otro) {
        return new Macros(
                kcal - otro.kcal,
                proteina - otro.proteina,
                carbohidratos - otro.carbohidratos,
                grasas - otro.grasas);
    }

    /** Multiplica cada valor por el factor y redondea el resultado a 1 decimal. */
    public Macros escalar(double factor) {
        return new Macros(
                redondear(kcal * factor),
                redondear(proteina * factor),
                redondear(carbohidratos * factor),
                redondear(grasas * factor));
    }

    /** Redondea a 1 decimal. */
    private static double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }

    /** Formato: "330.0 kcal | P 62.0 g | C 0.0 g | G 7.2 g" (siempre con punto decimal). */
    @Override
    public String toString() {
        return String.format(Locale.US, "%.1f kcal | P %.1f g | C %.1f g | G %.1f g",
                kcal, proteina, carbohidratos, grasas);
    }
}
