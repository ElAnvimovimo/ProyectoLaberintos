package model;

public class LaberintoInfo {
    private final int id;
    private final String nombreJugador;
    private final String fecha;
    private final Double racha;
    private final String estado;
    private final String laberintoJson;
    private final int sizeLaberinto;
    private final int movimientos;
    private final int rowJugador;
    private final int colJugador;
    private final int rowMeta;
    private final int colMeta;
    private final ModoJuego modoJuego;
    private final int tiempoRestante;
    private final int juegosTerminados;

    public LaberintoInfo(int id, String nombreJugador, String fecha, Double racha, String estado, String laberintoJson, int sizeLaberinto, int movimientos, int rowJugador, int colJugador, int rowMeta, int colMeta, ModoJuego modoJuego, int tiempoRestante, int juegosTerminados) {
        this.id = id;
        this.nombreJugador = nombreJugador;
        this.fecha = fecha;
        this.racha = racha;
        this.estado = estado;
        this.laberintoJson = laberintoJson;
        this.sizeLaberinto = sizeLaberinto;
        this.movimientos = movimientos;
        this.rowJugador = rowJugador;
        this.colJugador = colJugador;
        this.rowMeta = rowMeta;
        this.colMeta = colMeta;
        this.modoJuego = modoJuego;
        this.tiempoRestante = tiempoRestante;
        this.juegosTerminados = juegosTerminados;
    }

    public int getTiempoRestante() {
        return tiempoRestante;
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

    public String getLaberintoJson() {
        return laberintoJson;
    }

    public int getSizeLaberinto() {
        return sizeLaberinto;
    }

    public int getMovimientos() {
        return movimientos;
    }

    public int getRowJugador() {
        return rowJugador;
    }

    public int getColJugador() {
        return colJugador;
    }

    public int getRowMeta() {
        return rowMeta;
    }

    public int getColMeta() {
        return colMeta;
    }

    public ModoJuego getModoJuego() {
        return modoJuego;
    }

    public int getJuegosTerminados() {
        return juegosTerminados;
    }
}
