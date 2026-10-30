package br.com.vacaestelar.model;

/** Um ponto imutavel do modelo. As coordenadas originais nunca sao alteradas. */
public final class Star {
    public enum Part { BODY, HEAD, FRONT_LEG, BACK_LEG, TAIL }
    public final double x, y, z, size, phase;
    public final Part part;
    public final int color;

    public Star(double x, double y, double z, double size, double phase, Part part, int color) {
        this.x = x; this.y = y; this.z = z;
        this.size = size; this.phase = phase; this.part = part; this.color = color;
    }
}
