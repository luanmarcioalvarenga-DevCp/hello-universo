package br.com.vacaestelar.model;

import java.util.*;
import static br.com.vacaestelar.model.Star.Part.*;

/** Escultura de pontos 3D: volumes elipsoidais e curvas formam a vaca. */
public final class CowModel {
    private final List<Star> stars = new ArrayList<>();
    private final Random random = new Random(42);

    public CowModel() {
        ellipsoid(0, -25, 0, 175, 90, 66, 1800, BODY, 0, true);
        ellipsoid(155, -94, 0, 65, 70, 52, 650, HEAD, 0, true);
        ellipsoid(207, -65, 0, 46, 30, 54, 380, HEAD, 1, false);
        // Orelhas, chifres, olhos e narinas nas duas faces.
        for (int side : new int[]{-1, 1}) {
            ellipsoid(132, -140, side * 64, 38, 12, 25, 130, HEAD, 1, false);
            curve(145, -146, side * 31, 136, -195, side * 38, 30, HEAD, 3);
            ellipsoid(182, -114, side * 45, 7, 9, 4, 36, HEAD, 3, false);
            ellipsoid(235, -66, side * 26, 4, 5, 7, 25, HEAD, 4, false);
        }
        for (int side : new int[]{-1, 1}) {
            ellipsoid(97, 93, side * 42, 15, 69, 15, 220, FRONT_LEG, 0, false);
            ellipsoid(-110, 93, side * 42, 18, 69, 17, 220, BACK_LEG, 0, false);
            ellipsoid(100, 157, side * 42, 25, 12, 21, 80, FRONT_LEG, 2, false);
            ellipsoid(-107, 157, side * 42, 26, 12, 21, 80, BACK_LEG, 2, false);
        }
        ellipsoid(-45, 61, 0, 42, 22, 32, 150, BODY, 1, false);
        curve(-160, -45, 0, -225, 55, 0, 120, TAIL, 0);
        ellipsoid(-225, 60, 0, 16, 25, 14, 100, TAIL, 2, false);
    }

    public List<Star> stars() { return Collections.unmodifiableList(stars); }

    private void ellipsoid(double cx, double cy, double cz, double rx, double ry, double rz,
                           int count, Star.Part part, int color, boolean patches) {
        for (int i = 0; i < count; i++) {
            double u = random.nextDouble() * Math.PI * 2;
            double v = Math.acos(2 * random.nextDouble() - 1);
            double x = cx + rx * Math.sin(v) * Math.cos(u);
            double y = cy + ry * Math.cos(v);
            double z = cz + rz * Math.sin(v) * Math.sin(u);
            boolean patch = patches && (Math.pow((x + 65) / 60, 2) + Math.pow((y + 45) / 49, 2) < 1
                || Math.pow((x - 55) / 40, 2) + Math.pow((y + 5) / 47, 2) < 1
                || (x > 125 && x < 164 && y < -105));
            add(x, y, z, part, patch ? 2 : color);
        }
    }

    private void curve(double ax, double ay, double az, double bx, double by, double bz,
                       int count, Star.Part part, int color) {
        for (int i = 0; i < count; i++) {
            double t = i / (double) (count - 1);
            add(ax + (bx - ax) * t - Math.sin(t * Math.PI) * 13,
                ay + (by - ay) * t, az + (bz - az) * t + random.nextGaussian() * 2, part, color);
        }
    }

    private void add(double x, double y, double z, Star.Part part, int color) {
        stars.add(new Star(x, y, z, .65 + random.nextDouble() * 1.15,
            random.nextDouble() * Math.PI * 2, part, color));
    }
}
