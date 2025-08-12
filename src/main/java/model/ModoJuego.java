package model;

public enum ModoJuego {
    NORMAL("NORMAL", true, 10),
    LIBRE("LIBRE", false, 0),
    PESADILLA("PESADILLA", true, 5);

    private final String nombre;
    private final boolean tieneTiempo;
    private final int tiempoSegundos;

    ModoJuego(String nombre, boolean tieneTiempo, int tiempoSegundos) {
        this.nombre = nombre;
        this.tieneTiempo = tieneTiempo;
        this.tiempoSegundos = tiempoSegundos;
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

    @Override
    public String toString() {
        return nombre;
    }
}