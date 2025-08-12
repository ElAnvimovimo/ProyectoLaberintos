package model;

public class Jugador {
    private String nombre;
    private double rachaPartidas;
    private int juegosTerminados;

    public Jugador() {
        this("NO NAME", 0, 0);
    }

    public Jugador(String nombre, double rachaPartidas, int juegosTerminados) {
        this.nombre = nombre;
        this.rachaPartidas = rachaPartidas;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getRachaPartidas() {
        return rachaPartidas;
    }

    public void setRachaPartidas(double rachaPartidas) {
        this.rachaPartidas = rachaPartidas;
    }
}
