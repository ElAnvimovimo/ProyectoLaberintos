package model;

public class LaberintoInfo {
    private final int id;
    private final String nombreJugador;
    private final String fecha;
    private final Double racha;
    private final String estado;

    public LaberintoInfo(int id, String nombreJugador, String fecha, Double racha, String estado) {
        this.id = id;
        this.nombreJugador = nombreJugador;
        this.fecha = fecha;
        this.racha = racha;
        this.estado = estado;
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

    public Double getRacha() {
        return racha;
    }

    public String getEstado() {
        return estado;
    }

}
