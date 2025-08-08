package model;

public enum ModoJuego {
    NORMAL("Normal (contra el tiempo)", true, 30, 10),
    LIBRE("Libre (sin tiempo limite)", false, 0, 0),
    PESADILLA("Pesadilla... Cuidado :)", true, 5, 30);

    private final String nombre;
    private final boolean tieneTiempo;
    private final int tiempoSegundos;
    private final int dificultad;

    ModoJuego(String nombre, boolean tieneTiempo, int tiempoSegundos, int dificultad) {
        this.nombre = nombre;
        this.tieneTiempo = tieneTiempo;
        this.tiempoSegundos = tiempoSegundos;
        this.dificultad = dificultad;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean tieneTiempo() {
        return tieneTiempo;
    }

    public int getTiempoSegundos() {
        return tiempoSegundos;
    }

    public int getDificultad() {
        return dificultad;
    }

    @Override
    public String toString() {
        return nombre;
    }
}