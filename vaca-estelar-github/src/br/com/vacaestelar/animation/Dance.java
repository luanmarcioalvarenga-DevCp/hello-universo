package br.com.vacaestelar.animation;

import br.com.vacaestelar.model.Star;

/** Transforma uma estrela sem modificar o modelo original. */
public final class Dance {
    public static double[] pose(Star star, double beat) {
        double x = star.x, y = star.y, z = star.z;
        double angle = 0, px = 0, py = 0;
        switch (star.part) {
            case FRONT_LEG:
            case BACK_LEG:
                px = star.part == Star.Part.FRONT_LEG ? 97 : -110;
                py = 35;
                angle = Math.sin(beat + (star.z > 0 ? 0 : Math.PI)
                    + (star.part == Star.Part.BACK_LEG ? Math.PI : 0)) * .33;
                break;
            case HEAD:
                px = 133; py = -58; angle = Math.sin(beat) * .09; break;
            case TAIL:
                px = -160; py = -45; angle = Math.sin(beat * 1.5) * .35; break;
            default: break;
        }
        double dx = x - px, dy = y - py;
        x = px + dx * Math.cos(angle) - dy * Math.sin(angle);
        y = py + dx * Math.sin(angle) + dy * Math.cos(angle);
        double sway = Math.sin(beat * .5) * .055;
        return new double[]{x * Math.cos(sway) - y * Math.sin(sway),
            x * Math.sin(sway) + y * Math.cos(sway) - Math.abs(Math.sin(beat)) * 15, z};
    }
}
