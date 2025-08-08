package model;

import java.time.LocalDateTime;

public class Partida {
    private int id;
    private Jugador jugador;
    private ModoJuego modo;
    private int tamanoLaberinto;
    private int movimientos;
    private int tiempoRestante;
    private LocalDateTime fecha;
    private boolean completada;

    public Partida(Jugador jugador, ModoJuego modo, int tamanoLaberinto,
                   int movimientos, int tiempoRestante, boolean completada) {
        this.jugador = jugador;
        this.modo = modo;
        this.tamanoLaberinto = tamanoLaberinto;
        this.movimientos = movimientos;
        this.tiempoRestante = tiempoRestante;
        this.fecha = LocalDateTime.now();
        this.completada = completada;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public ModoJuego getModo() {
        return modo;
    }

    public void setModo(ModoJuego modo) {
        this.modo = modo;
    }

    public int getTamanoLaberinto() {
        return tamanoLaberinto;
    }

    public void setTamanoLaberinto(int tamanoLaberinto) {
        this.tamanoLaberinto = tamanoLaberinto;
    }

    public int getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(int movimientos) {
        this.movimientos = movimientos;
    }

    public int getTiempoRestante() {
        return tiempoRestante;
    }

    public void setTiempoRestante(int tiempoRestante) {
        this.tiempoRestante = tiempoRestante;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }
}