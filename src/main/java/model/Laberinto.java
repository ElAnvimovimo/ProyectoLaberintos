package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Laberinto {
    private int filas, columnas;
    private Celda[][] laberinto;
    private List<Celda> listaFronteras;
    private Celda celdaInicial;

    public Laberinto() {
        this(10, 10);
    }

    public Laberinto(int filas, int columnas) {
        this.filas = filas + 2;
        this.columnas = columnas + 2;
        this.laberinto = new Celda[this.filas][this.columnas];
        this.listaFronteras = new ArrayList<>();
    }

    public Celda getCeldaInicial() {
        return celdaInicial;
    }

    public void setCeldaInicial(Celda celdaInicial) {
        this.celdaInicial = celdaInicial;
    }

    public void crearLaberinto() {
        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < columnas; col++) {
                laberinto[fila][col] = new Celda(fila, col);
                if (fila == 0 || fila == filas - 1 || col == 0 || col == columnas - 1) {
                    laberinto[fila][col].setEsBorde(true);
                }
            }
        }
    }

    public void recorrerLaberinto() {
        Random random = new Random();
        celdaInicial = laberinto[1][1];

        celdaInicial.setVisitado(true);
        buscarCeldasFrontera(celdaInicial);
        listaFronteras.add(celdaInicial);

        while (!listaFronteras.isEmpty()) {
            int indice = random.nextInt(listaFronteras.size());
            Celda auxCelda = listaFronteras.remove(indice);

            Celda celdaVecina = buscarVecinaVisitada(auxCelda);
            if (celdaVecina != null) {
                hacerCamino(auxCelda, celdaVecina);
                auxCelda.setVisitado(true);
                buscarCeldasFrontera(auxCelda);
            }
        }
    }


    public Celda[][] getLaberinto() {
        return laberinto;
    }

    public void setLaberinto(Celda[][] laberinto) {
        this.laberinto = laberinto;
    }

    private void hacerCamino(Celda auxCelda, Celda celdaVecina) {
        int df = celdaVecina.getFila() - auxCelda.getFila();
        int dc = celdaVecina.getColumna() - auxCelda.getColumna();

        if (df == -1) { // Norte
            auxCelda.setMuroNorte(false);
            celdaVecina.setMuroSur(false);
        } else if (df == 1) { // Sur
            auxCelda.setMuroSur(false);
            celdaVecina.setMuroNorte(false);
        } else if (dc == -1) { // Oeste
            auxCelda.setMuroOeste(false);
            celdaVecina.setMuroEste(false);
        } else if (dc == 1) { // Este
            auxCelda.setMuroEste(false);
            celdaVecina.setMuroOeste(false);
        }
    }

    private Celda buscarVecinaVisitada(Celda auxCelda) {
        List<Celda> vecinos = new ArrayList<>();
        int f = auxCelda.getFila();
        int c = auxCelda.getColumna();

        if (laberinto[f - 1][c].isVisitado()) vecinos.add(laberinto[f - 1][c]); // Norte
        if (laberinto[f + 1][c].isVisitado()) vecinos.add(laberinto[f + 1][c]); // Sur
        if (laberinto[f][c - 1].isVisitado()) vecinos.add(laberinto[f][c - 1]); // Oeste
        if (laberinto[f][c + 1].isVisitado()) vecinos.add(laberinto[f][c + 1]); // Este

        if (vecinos.isEmpty()) return null;
        return vecinos.get(new Random().nextInt(vecinos.size()));
    }

    private void buscarCeldasFrontera(Celda cel) {
        int f = cel.getFila();
        int c = cel.getColumna();

        // Comprueba si el vecino del NORTE está dentro de los límites y no es un borde.
        // La fila debe ser > 1 para que su vecino del norte (f-1) no sea la fila 0 (borde).
        if (f > 1) {
            Celda norte = laberinto[f - 1][c];
            if (!norte.isVisitado() && !listaFronteras.contains(norte)) {
                listaFronteras.add(norte);
            }
        }

        // Comprueba si el vecino del SUR está dentro de los límites y no es un borde.
        // La fila debe ser < filas - 2 para que su vecino sur (f+1) no sea la última fila (borde).
        if (f < this.filas - 2) {
            Celda sur = laberinto[f + 1][c];
            if (!sur.isVisitado() && !listaFronteras.contains(sur)) {
                listaFronteras.add(sur);
            }
        }

        // Comprueba si el vecino del OESTE está dentro de los límites y no es un borde.
        if (c > 1) {
            Celda oeste = laberinto[f][c - 1];
            if (!oeste.isVisitado() && !listaFronteras.contains(oeste)) {
                listaFronteras.add(oeste);
            }
        }

        // Comprueba si el vecino del ESTE está dentro de los límites y no es un borde.
        if (c < this.columnas - 2) {
            Celda este = laberinto[f][c + 1];
            if (!este.isVisitado() && !listaFronteras.contains(este)) {
                listaFronteras.add(este);
            }
        }
    }
}
