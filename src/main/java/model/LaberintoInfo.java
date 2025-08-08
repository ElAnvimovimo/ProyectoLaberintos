package model;

public class LaberintoInfo {
    private final int id;
    private final String nombreJugador;
    private final String fecha;

    public LaberintoInfo(int id, String nombreJugador, String fecha) {
        this.id = id;
        this.nombreJugador = nombreJugador;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public String getFecha() {
        return fecha;
    }

}
