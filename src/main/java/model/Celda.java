package model;

public class Celda {
    private int fila, columna;
    private boolean muroNorte, muroSur, muroEste, muroOeste, visitado;
    private boolean isBorde;
    private int pasos;

    public Celda() {
        this(0, 0);
    }

    public Celda(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
        this.muroNorte = true;
        this.muroSur = true;
        this.muroEste = true;
        this.muroOeste = true;
        this.visitado = false;
        this.pasos = 0;
    }

    public int getPasos() {
        return pasos;
    }

    public void setPasos(int pasos) {
        this.pasos = pasos;
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getColumna() {
        return columna;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    public boolean isMuroNorte() {
        return muroNorte;
    }

    public void setMuroNorte(boolean muroNorte) {
        this.muroNorte = muroNorte;
    }

    public boolean isMuroSur() {
        return muroSur;
    }

    public void setMuroSur(boolean muroSur) {
        this.muroSur = muroSur;
    }

    public boolean isMuroEste() {
        return muroEste;
    }

    public void setMuroEste(boolean muroEste) {
        this.muroEste = muroEste;
    }

    public boolean isMuroOeste() {
        return muroOeste;
    }

    public void setMuroOeste(boolean muroOeste) {
        this.muroOeste = muroOeste;
    }

    public boolean isVisitado() {
        return visitado;
    }

    public void setVisitado(boolean visitado) {
        this.visitado = visitado;
    }

    public void setBorde() {
        this.visitado = true;
        this.muroNorte = true;
        this.muroSur = true;
        this.muroEste = true;
        this.muroOeste = true;
    }

    public void setEsBorde(boolean esBorde) {
        this.isBorde = esBorde;
        // Si es borde, forzar todos los muros cerrados
        if (esBorde) {
            this.muroNorte = true;
            this.muroSur = true;
            this.muroEste = true;
            this.muroOeste = true;
        }
    }

    public boolean isIsBorde() {
        return isBorde;
    }

    @Override
    public String toString() {
        return "Celda{" +
                "fila=" + fila +
                ", columna=" + columna +
                ", muroNorte=" + muroNorte +
                ", muroSur=" + muroSur +
                ", muroEste=" + muroEste +
                ", muroOeste=" + muroOeste +
                ", visitado=" + visitado +
                ", isBorde=" + isBorde +
                ", pasos=" + pasos +
                '}';
    }
}
