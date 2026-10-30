package br.com.vacaestelar.ui;

import br.com.vacaestelar.model.*;
import br.com.vacaestelar.animation.Dance;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/** Interface, camera e renderizacao; todo o estado pertence a thread do Swing. */
public final class UniversePanel extends JPanel {
    private final CowModel cow = new CowModel();
    private final double[][] background = new double[220][3];
    private final javax.swing.Timer timer;
    private double time, speed = 1, yaw = -.35, zoom = 1;
    private boolean paused, lines = true;
    private int lastX;
    private long lastTime;
    private static final Color[] COLORS = {new Color(210, 243, 255), new Color(255, 156, 197),
        new Color(119, 102, 255), new Color(255, 225, 140), new Color(255, 99, 160)};

    public UniversePanel() {
        setPreferredSize(new Dimension(1200, 800));
        Random random = new Random(8);
        for (double[] point : background) {
            point[0] = random.nextDouble(); point[1] = random.nextDouble(); point[2] = random.nextDouble();
        }
        bind("SPACE", "pause", () -> paused = !paused);
        bind("UP", "faster", () -> speed = Math.min(2.5, speed + .25));
        bind("DOWN", "slower", () -> speed = Math.max(.25, speed - .25));
        bind("L", "lines", () -> lines = !lines);
        bind("R", "reset", () -> { yaw = -.35; zoom = 1; speed = 1; });
        bind("ESCAPE", "close", () -> SwingUtilities.getWindowAncestor(this).dispose());
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { lastX = e.getX(); }
            @Override public void mouseDragged(MouseEvent e) {
                yaw += (e.getX() - lastX) * .008; lastX = e.getX();
            }
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                zoom = Math.max(.6, Math.min(1.6, zoom - e.getPreciseWheelRotation() * .08));
            }
        };
        addMouseListener(mouse); addMouseMotionListener(mouse); addMouseWheelListener(mouse);
        timer = new javax.swing.Timer(16, e -> {
            long now = System.nanoTime();
            double dt = Math.min(.05, (now - lastTime) / 1e9); lastTime = now;
            if (!paused) time += dt * speed;
            repaint();
        });
    }

    @Override public void addNotify() { super.addNotify(); lastTime = System.nanoTime(); timer.start(); }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }

    private void bind(String key, String name, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        double scale = Math.min(w / 1000.0, h / 730.0) * zoom;
        g.setPaint(new RadialGradientPaint(w * .55f, h * .5f, Math.max(w, h) * .8f,
            new float[]{0, .55f, 1}, new Color[]{new Color(30, 25, 70), new Color(9, 14, 37), new Color(3, 6, 18)}));
        g.fillRect(0, 0, w, h);
        for (double[] p : background) {
            int alpha = (int) (60 + 100 * (.5 + .5 * Math.sin(time + p[2] * 20)));
            g.setColor(new Color(170, 199, 255, alpha));
            double radius = 1 + p[2] * 1.6;
            g.fill(new Ellipse2D.Double(p[0] * w, p[1] * h, radius, radius));
        }
        // Palco orbital abaixo dos cascos.
        g.setStroke(new BasicStroke(1));
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(97, 132, 247, 65 - i * 12));
            double rw = (540 + i * 35) * scale, rh = (65 + i * 13) * scale;
            g.draw(new Ellipse2D.Double(w / 2.0 - rw / 2, h * .53 + 180 * scale - rh / 2, rw, rh));
        }
        List<Dot> dots = new ArrayList<>(cow.stars().size());
        for (Star star : cow.stars()) {
            double[] p = Dance.pose(star, time * Math.PI * 2 * 1.5);
            double x = p[0] * Math.cos(yaw) + p[2] * Math.sin(yaw);
            double z = -p[0] * Math.sin(yaw) + p[2] * Math.cos(yaw);
            double y = p[1] * Math.cos(.12) - z * Math.sin(.12);
            double depth = p[1] * Math.sin(.12) + z * Math.cos(.12);
            double perspective = 1000 / (1000 + depth);
            dots.add(new Dot(w * .5 + x * scale * perspective,
                h * .53 + y * scale * perspective, depth, star, perspective));
        }
        dots.sort(Comparator.comparingDouble((Dot d) -> d.depth).reversed());
        // Arestas locais curtas sugerem constelacoes, sem ligar partes distantes.
        if (lines) {
            g.setStroke(new BasicStroke(.6f));
            for (int i = 0; i < dots.size(); i += 3) {
                Dot a = dots.get(i);
                for (int j = i + 1; j < Math.min(i + 14, dots.size()); j++) {
                    Dot b = dots.get(j);
                    double distance = Math.hypot(a.x - b.x, a.y - b.y);
                    if (a.star.part == b.star.part && distance > 4 && distance < 22 * scale) {
                        g.setColor(new Color(137, 177, 255, 35));
                        g.draw(new Line2D.Double(a.x, a.y, b.x, b.y)); break;
                    }
                }
            }
        }
        for (Dot dot : dots) {
            Color color = COLORS[dot.star.color];
            double size = Math.max(.55, dot.star.size * scale * dot.perspective);
            int alpha = (int) (145 + 110 * (.5 + .5 * Math.sin(time * 3 + dot.star.phase)));
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 22));
            g.fill(new Ellipse2D.Double(dot.x - size * 3, dot.y - size * 3, size * 6, size * 6));
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g.fill(new Ellipse2D.Double(dot.x - size, dot.y - size, size * 2, size * 2));
            if (dot.star.phase < .12) {
                g.draw(new Line2D.Double(dot.x - size * 3, dot.y, dot.x + size * 3, dot.y));
                g.draw(new Line2D.Double(dot.x, dot.y - size * 3, dot.x, dot.y + size * 3));
            }
        }
        text(g, "C O N S T E L A C A O   E M   M O V I M E N T O", 37, 12, new Color(144, 186, 230));
        text(g, "VACA ESTELAR", 81, 36, new Color(232, 242, 255));
        text(g, "Uma danca desenhada com milhares de estrelas", 109, 15, new Color(174, 166, 207));
        text(g, (paused ? "PAUSADA" : "DANCANDO") + "   /   " + String.format(Locale.ROOT, "%.2fx", speed)
            + "   /   " + cow.stars().size() + " estrelas", h - 81, 13, new Color(169, 211, 244));
        text(g, "ARRASTE: girar   |   RODA: zoom   |   ESPACO: pausar   |   SETAS: ritmo", h - 51, 12, new Color(148, 159, 190));
        text(g, "L: constelacoes   |   R: restaurar visao   |   ESC: sair", h - 29, 12, new Color(148, 159, 190));
        g.dispose();
    }

    private void text(Graphics2D g, String message, int y, int size, Color color) {
        g.setFont(new Font("SansSerif", size > 25 ? Font.BOLD : Font.PLAIN, size));
        g.setColor(color);
        g.drawString(message, (getWidth() - g.getFontMetrics().stringWidth(message)) / 2, y);
    }

    private static final class Dot {
        final double x, y, depth, perspective;
        final Star star;
        Dot(double x, double y, double depth, Star star, double perspective) {
            this.x = x; this.y = y; this.depth = depth; this.star = star; this.perspective = perspective;
        }
    }
}
