import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
    private DrawingPanel drawingPanel;
    private JComboBox<String> algorithmComboBox;
    private JTextField x1Field, y1Field, x2Field, y2Field, radiusField;
    private JButton drawButton;
    private JLabel timeLabel;
    private JCheckBox gridCheckBox, axesCheckBox, coordinatesCheckBox;
    private JSlider scaleSlider;

    private static final int GRID_SIZE = 20;
    private double scale = 1.0;

    public Main() {
        setTitle("Алгоритмы растеризации");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeComponents();
        setupEventListeners();

        pack();
        setLocationRelativeTo(null);
        setSize(1000, 700);
    }

    private void initializeComponents() {
        JPanel controlPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Управление"));

        controlPanel.add(new JLabel("Алгоритм:"));
        algorithmComboBox = new JComboBox<>(new String[]{"Пошаговый алгоритм", "Алгоритм ЦДА", "Алгоритм Брезенхема (отрезок)", "Алгоритм Брезенхема (окружность)"});
        controlPanel.add(algorithmComboBox);

        controlPanel.add(new JLabel("X1:"));
        x1Field = new JTextField("10");
        controlPanel.add(x1Field);

        controlPanel.add(new JLabel("Y1:"));
        y1Field = new JTextField("10");
        controlPanel.add(y1Field);

        controlPanel.add(new JLabel("X2:"));
        x2Field = new JTextField("50");
        controlPanel.add(x2Field);

        controlPanel.add(new JLabel("Y2:"));
        y2Field = new JTextField("50");
        controlPanel.add(y2Field);

        controlPanel.add(new JLabel("Радиус:"));
        radiusField = new JTextField("30");
        controlPanel.add(radiusField);

        drawButton = new JButton("Построить");
        controlPanel.add(drawButton);

        timeLabel = new JLabel("Время: ");
        controlPanel.add(timeLabel);

        JPanel displayPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        displayPanel.setBorder(BorderFactory.createTitledBorder("Резултат"));

        gridCheckBox = new JCheckBox("Сетка", true);
        displayPanel.add(gridCheckBox);

        axesCheckBox = new JCheckBox("Оси координат", true);
        displayPanel.add(axesCheckBox);

        coordinatesCheckBox = new JCheckBox("Подписи координат", true);
        displayPanel.add(coordinatesCheckBox);

        displayPanel.add(new JLabel("Масштаб:"));
        scaleSlider = new JSlider(50, 200, 100);
        displayPanel.add(scaleSlider);

        JPanel mainControlPanel = new JPanel(new BorderLayout());
        mainControlPanel.add(controlPanel, BorderLayout.NORTH);
        mainControlPanel.add(displayPanel, BorderLayout.CENTER);

        add(mainControlPanel, BorderLayout.WEST);
        drawingPanel = new DrawingPanel();
        add(new JScrollPane(drawingPanel), BorderLayout.CENTER);

        updateRadiusFieldVisibility();
    }

    private void setupEventListeners() {
        drawButton.addActionListener(e -> drawFigure());

        algorithmComboBox.addActionListener(e -> updateRadiusFieldVisibility());

        gridCheckBox.addActionListener(e -> drawingPanel.repaint());
        axesCheckBox.addActionListener(e -> drawingPanel.repaint());
        coordinatesCheckBox.addActionListener(e -> drawingPanel.repaint());

        scaleSlider.addChangeListener(e -> {
            scale = scaleSlider.getValue() / 100.0;
            drawingPanel.repaint();
        });
    }

    private void updateRadiusFieldVisibility() {
        String selected = (String) algorithmComboBox.getSelectedItem();
        boolean isCircle = selected.equals("Алгоритм Брезенхема (окружность)");
        radiusField.setEnabled(isCircle);
        x2Field.setEnabled(!isCircle);
        y2Field.setEnabled(!isCircle);
    }

    private void drawFigure() {
        try {
            int x1 = Integer.parseInt(x1Field.getText());
            int y1 = Integer.parseInt(y1Field.getText());
            int x2 = Integer.parseInt(x2Field.getText());
            int y2 = Integer.parseInt(y2Field.getText());
            int radius = Integer.parseInt(radiusField.getText());

            String algorithm = (String) algorithmComboBox.getSelectedItem();

            long startTime = System.nanoTime();

            switch (algorithm) {
                case "Пошаговый алгоритм":
                    drawingPanel.setPoints(stepByStepLine(x1, y1, x2, y2));
                    break;
                case "Алгоритм ЦДА":
                    drawingPanel.setPoints(ddaLine(x1, y1, x2, y2));
                    break;
                case "Алгоритм Брезенхема (отрезок)":
                    drawingPanel.setPoints(bresenhamLine(x1, y1, x2, y2));
                    break;
                case "Алгоритм Брезенхема (окружность)":
                    drawingPanel.setPoints(bresenhamCircle(x1, y1, radius));
                    break;
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            timeLabel.setText(String.format("Время: %d нс", duration));

            drawingPanel.repaint();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Введите корректные числа");
        }
    }

    // пошаговый алгоритм
    private List<Point> stepByStepLine(int x1, int y1, int x2, int y2) {
        List<Point> points = new ArrayList<>();

        int dx = x2 - x1;
        int dy = y2 - y1;

        if (Math.abs(dx) >= Math.abs(dy)) {
            if (x1 > x2) {
                int temp = x1; x1 = x2; x2 = temp;
                temp = y1; y1 = y2; y2 = temp;
            }

            float k = (float) dy / dx;
            float y = y1;

            for (int x = x1; x <= x2; x++) {
                points.add(new Point(x, Math.round(y)));
                y += k;
            }
        } else {
            if (y1 > y2) {
                int temp = x1; x1 = x2; x2 = temp;
                temp = y1; y1 = y2; y2 = temp;
            }

            float k = (float) dx / dy;
            float x = x1;

            for (int y = y1; y <= y2; y++) {
                points.add(new Point(Math.round(x), y));
                x += k;
            }
        }

        return points;
    }

    // ЦДА
    private List<Point> ddaLine(int x1, int y1, int x2, int y2) {
        List<Point> points = new ArrayList<>();

        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        float xInc = (float) dx / steps;
        float yInc = (float) dy / steps;

        float x = x1;
        float y = y1;

        for (int i = 0; i <= steps; i++) {
            points.add(new Point(Math.round(x), Math.round(y)));
            x += xInc;
            y += yInc;
        }

        return points;
    }

    // Брезенхем для отрезка
    private List<Point> bresenhamLine(int x1, int y1, int x2, int y2) {
        List<Point> points = new ArrayList<>();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int x = x1;
        int y = y1;

        while (true) {
            points.add(new Point(x, y));
            if (x == x2 && y == y2) break;

            int err2 = 2 * err;
            if (err2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (err2 < dx) {
                err += dx;
                y += sy;
            }
        }

        return points;
    }

    // Брезенхем для окружности
    private List<Point> bresenhamCircle(int xc, int yc, int r) {
        List<Point> points = new ArrayList<>();

        int x = 0;
        int y = r;
        int d = 3 - 2 * r;

        addCirclePoints(points, xc, yc, x, y);

        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            addCirclePoints(points, xc, yc, x, y);
        }

        return points;
    }

    private void addCirclePoints(List<Point> points, int xc, int yc, int x, int y) {
        points.add(new Point(xc + x, yc + y));
        points.add(new Point(xc - x, yc + y));
        points.add(new Point(xc + x, yc - y));
        points.add(new Point(xc - x, yc - y));
        points.add(new Point(xc + y, yc + x));
        points.add(new Point(xc - y, yc + x));
        points.add(new Point(xc + y, yc - x));
        points.add(new Point(xc - y, yc - x));
    }

    // для хранения точек
    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private class DrawingPanel extends JPanel {
        private List<Point> points = new ArrayList<>();

        public DrawingPanel() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.WHITE);
        }

        public void setPoints(List<Point> points) {
            this.points = points;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;
            int centerY = height / 2;

            //масштаб
            g2d.scale(scale, scale);
            //сетка
            if (gridCheckBox.isSelected()) {
                drawGrid(g2d, width, height, centerX, centerY);
            }
            //оси
            if (axesCheckBox.isSelected()) {
                drawAxes(g2d, width, height, centerX, centerY);
            }
            //подписи координат
            if (coordinatesCheckBox.isSelected()) {
                drawCoordinates(g2d, width, height, centerX, centerY);
            }
            // точки
            drawPoints(g2d, centerX, centerY);
        }

        private void drawGrid(Graphics2D g2d, int width, int height, int centerX, int centerY) {
            g2d.setColor(new Color(200, 200, 200));
            for (int x = centerX % GRID_SIZE; x < width; x += GRID_SIZE) {
                g2d.drawLine(x, 0, x, height);
            }
            for (int y = centerY % GRID_SIZE; y < height; y += GRID_SIZE) {
                g2d.drawLine(0, y, width, y);
            }
        }

        private void drawAxes(Graphics2D g2d, int width, int height, int centerX, int centerY) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));

            g2d.drawLine(0, centerY, width, centerY);
            g2d.drawLine(centerX, 0, centerX, height);

            g2d.fillPolygon(new int[]{width - 10, width - 10, width}, new int[]{centerY - 5, centerY + 5, centerY}, 3);
            g2d.fillPolygon(new int[]{centerX - 5, centerX + 5, centerX}, new int[]{10, 10, 0}, 3);
        }

        private void drawCoordinates(Graphics2D g2d, int width, int height, int centerX, int centerY) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));

            for (int x = centerX + GRID_SIZE; x < width; x += GRID_SIZE) {
                int value = (x - centerX) / GRID_SIZE;
                g2d.drawString(String.valueOf(value), x - 5, centerY + 15);
                g2d.drawString(String.valueOf(-value), centerX - (x - centerX) - 5, centerY + 15);
            }
            for (int y = centerY + GRID_SIZE; y < height; y += GRID_SIZE) {
                int value = (y - centerY) / GRID_SIZE;
                g2d.drawString(String.valueOf(-value), centerX + 5, y + 5);
                g2d.drawString(String.valueOf(value), centerX + 5, centerY - (y - centerY) + 5);
            }
            // подписи осей
            g2d.drawString("X", width - 15, centerY - 10);
            g2d.drawString("Y", centerX + 10, 15);
        }

        private void drawPoints(Graphics2D g2d, int centerX, int centerY) {
            g2d.setColor(Color.RED);

            for (Point p : points) {
                int x = centerX + p.x * GRID_SIZE;
                int y = centerY - p.y * GRID_SIZE; // инвертируем у

                g2d.fillRect(x - 2, y - 2, 5, 5);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Main().setVisible(true);
        });
    }
}