import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import javax.swing.*;

/** Universo com coordenadas 3D projetadas em uma janela Swing. */
public class HelloUniverso extends JPanel {
    private static final int QUANTIDADE = 650;
    private final Estrela[] estrelas = new Estrela[QUANTIDADE];
    private final Random random = new Random();
    private boolean pausado, turbo;
    private double mouseX, mouseY, tempo;
    private long ultimoFrame = System.nanoTime();

    public HelloUniverso() {
        setPreferredSize(new Dimension(1100, 720));
        setBackground(new Color(4, 6, 20));
        for (int i = 0; i < estrelas.length; i++) {
            estrelas[i] = new Estrela();
            renascer(estrelas[i], true);
        }
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                mouseX = (e.getX() - getWidth() / 2.0) / Math.max(1, getWidth());
                mouseY = (e.getY() - getHeight() / 2.0) / Math.max(1, getHeight());
            }
        });
        bind("SPACE", "pausar", () -> pausado = !pausado);
        bind("T", "turbo", () -> turbo = !turbo);
        bind("ESCAPE", "sair", () -> SwingUtilities.getWindowAncestor(this).dispose());
        Timer timer = new Timer(16, e -> {
            long agora = System.nanoTime();
            double delta = Math.min((agora - ultimoFrame) / 1e9, 0.05);
            ultimoFrame = agora;
            if (!pausado) {
                tempo += delta;
                for (Estrela estrela : estrelas) {
                    estrela.z -= delta * (turbo ? 720 : 95);
                    if (estrela.z < 15) renascer(estrela, false);
                }
            }
            repaint();
        });
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                if (isDisplayable()) { ultimoFrame = System.nanoTime(); timer.start(); }
                else timer.stop();
            }
        });
    }

    private void bind(String tecla, String nome, Runnable acao) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), nome);
        getActionMap().put(nome, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { acao.run(); }
        });
    }

    private void renascer(Estrela estrela, boolean inicial) {
        estrela.x = (random.nextDouble() - 0.5) * 2400;
        estrela.y = (random.nextDouble() - 0.5) * 1600;
        estrela.z = inicial ? 15 + random.nextDouble() * 1500 : 1500;
        estrela.raio = 0.7 + random.nextDouble() * 1.8;
        estrela.fase = random.nextDouble() * Math.PI * 2;
        estrela.cor = random.nextBoolean() ? new Color(133, 221, 255) : new Color(224, 195, 255);
    }

    @Override protected void paintComponent(Graphics original) {
        super.paintComponent(original);
        Graphics2D g = (Graphics2D) original.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        float escala = Math.max(0.45f, Math.min(w / 1100f, h / 720f));
        g.setPaint(new RadialGradientPaint(w * .5f, h * .48f, Math.max(1, w * .65f),
            new float[]{0, .5f, 1}, new Color[]{new Color(32, 21, 68), new Color(10, 15, 38), new Color(3, 5, 16)}));
        g.fillRect(0, 0, w, h);
        double focal = Math.min(w, h) * .9;
        for (Estrela estrela : estrelas) {
            // Quanto menor Z, maior e mais proxima a estrela aparece.
            double fator = focal / estrela.z;
            double x = w / 2.0 + (estrela.x - mouseX * 190) * fator;
            double y = h / 2.0 + (estrela.y - mouseY * 190) * fator;
            double raio = Math.min(8, estrela.raio * fator);
            if (x < -30 || x > w + 30 || y < -30 || y > h + 30) continue;
            float brilho = (float) (.55 + .45 * Math.sin(tempo * 1.8 + estrela.fase));
            g.setComposite(AlphaComposite.SrcOver.derive(brilho * .16f));
            g.setColor(estrela.cor);
            g.fill(new Ellipse2D.Double(x - raio * 3, y - raio * 3, raio * 6, raio * 6));
            g.setComposite(AlphaComposite.SrcOver.derive(brilho));
            g.fill(new Ellipse2D.Double(x - raio, y - raio, raio * 2, raio * 2));
            if (turbo) {
                double anterior = focal / (estrela.z + 65);
                g.draw(new Line2D.Double(x, y,
                    w / 2.0 + (estrela.x - mouseX * 190) * anterior,
                    h / 2.0 + (estrela.y - mouseY * 190) * anterior));
            }
            if (raio > 2.3) {
                g.draw(new Line2D.Double(x - raio * 2, y, x + raio * 2, y));
                g.draw(new Line2D.Double(x, y - raio * 2, x, y + raio * 2));
            }
        }
        g.setComposite(AlphaComposite.SrcOver);
        double centroY = h * .48 + Math.sin(tempo * 1.2) * 12 * escala;
        g.translate(-mouseX * 18, -mouseY * 18);
        g.setFont(new Font("SansSerif", Font.BOLD, Math.round(14 * escala)));
        centralizar(g, "M E U   P R I M E I R O   U N I V E R S O", w / 2f, (float) centroY - 80 * escala, new Color(145, 207, 241));
        g.setFont(new Font("SansSerif", Font.BOLD, Math.round(84 * escala)));
        for (int camada = 10; camada >= 1; camada--) {
            centralizar(g, "HELLO WORLD", w / 2f + camada * escala,
                (float) centroY + camada * escala, new Color(49, 30, 95));
        }
        centralizar(g, "HELLO WORLD", w / 2f, (float) centroY, new Color(228, 246, 255));
        g.setFont(new Font("SansSerif", Font.PLAIN, Math.round(19 * escala)));
        centralizar(g, "Uma pequena mensagem. Um universo de possibilidades.", w / 2f,
            (float) centroY + 52 * escala, new Color(173, 183, 215));
        g.translate(mouseX * 18, mouseY * 18);
        g.setFont(new Font("SansSerif", Font.PLAIN, Math.max(11, Math.round(13 * escala))));
        centralizar(g, "MOUSE  mover a visao     ESPACO  pausar     T  turbo     ESC  sair", w / 2f, h - 42, new Color(143, 164, 192));
        centralizar(g, pausado ? "PAUSADO" : turbo ? "VIAGEM EM ALTA VELOCIDADE" : "FEITO COM JAVA  /  650 ESTRELAS", w / 2f, 40, new Color(129, 198, 222));
        g.dispose();
    }

    private void centralizar(Graphics2D g, String texto, float x, float y, Color cor) {
        g.setColor(cor);
        g.drawString(texto, x - g.getFontMetrics().stringWidth(texto) / 2f, y);
    }

    private static class Estrela {
        double x, y, z, raio, fase;
        Color cor;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Hello Universo | Meu primeiro Java 3D");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setContentPane(new HelloUniverso());
            janela.pack();
            janela.setMinimumSize(new Dimension(640, 420));
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
        });
    }
}
