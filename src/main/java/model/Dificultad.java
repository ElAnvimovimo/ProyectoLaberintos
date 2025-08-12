package model;

public enum Dificultad {
    FACIL("FACIL", 10),
    NORMAL("NORMAL", 20),
    DIFICIL("DIFICL", 30);

    private final String nombre;
    private final int sizeLaberinto;

    Dificultad(String nombre, int sizeLaberinto) {
        this.nombre = nombre;
        this.sizeLaberinto = sizeLaberinto;
    }

    public String getNombre() {
        return nombre;
    }

    public int getSizeLaberinto() {
        return sizeLaberinto;
    }

    public int getValor() {
        return sizeLaberinto;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
