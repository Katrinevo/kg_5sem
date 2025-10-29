import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;

public class Main extends JFrame {

    final JPanel preview = new JPanel();
    final JSlider rSlider = new JSlider(0, 255);
    final JSlider gSlider = new JSlider(0, 255);
    final JSlider bSlider = new JSlider(0, 255);
    final JTextField rField = new JTextField(3);
    final JTextField gField = new JTextField(3);
    final JTextField bField = new JTextField(3);

    final JSlider cSlider = new JSlider(0, 100);
    final JSlider mSlider = new JSlider(0, 100);
    final JSlider ySlider = new JSlider(0, 100);
    final JSlider kSlider = new JSlider(0, 100);
    final JTextField cField = new JTextField(3);
    final JTextField mField = new JTextField(3);
    final JTextField yField = new JTextField(3);
    final JTextField kField = new JTextField(3);

    final JSlider hSlider = new JSlider(0, 360);
    final JSlider sSlider = new JSlider(0, 100);
    final JSlider vSlider = new JSlider(0, 100);
    final JTextField hField = new JTextField(4);
    final JTextField sField = new JTextField(3);
    final JTextField vField = new JTextField(3);

    boolean internalUpdate = false;

    public Main() {
        super("");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        setSize(1000, 650);
        setLocationRelativeTo(null);

        preview.setPreferredSize(new Dimension(200, 400));
        preview.setBorder(BorderFactory.createTitledBorder("Preview"));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout(8, 8));
        leftPanel.add(preview, BorderLayout.NORTH);

        JButton chooseColor = new JButton("My Palette");
        chooseColor.setFont(new Font("", Font.PLAIN, 20));
        chooseColor.addActionListener(e -> MyPalette());
        leftPanel.add(chooseColor, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setLayout(new GridLayout(3, 1, 6, 6));

        center.add(createRGBPanel());
        center.add(createCMYKPanel());
        center.add(createHSVPanel());

        add(center, BorderLayout.CENTER);

        setFromRGB(128, 128, 128);

        setVisible(true);
    }

    private JPanel createRGBPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("RGB (0..255)"));

        JPanel sliders = new JPanel(new GridLayout(3, 1, 4, 4));
        sliders.add(makeSlider("R", rSlider, rField));
        sliders.add(makeSlider("G", gSlider, gField));
        sliders.add(makeSlider("B", bSlider, bField));

        panel.add(sliders, BorderLayout.CENTER);

        ChangeListener rgbChange = e -> {
            if (!internalUpdate && (e.getSource() instanceof JSlider)) {
                internalUpdate = true;
                int r = rSlider.getValue();
                int g = gSlider.getValue();
                int b = bSlider.getValue();
                setFromRGB(r, g, b);
                internalUpdate = false;
            }
        };
        rSlider.addChangeListener(rgbChange);
        gSlider.addChangeListener(rgbChange);
        bSlider.addChangeListener(rgbChange);

        ActionListener rgbFieldListener = e -> {
            if (!internalUpdate) {
                internalUpdate = true;
                try {
                    int r = clamp(Integer.parseInt(rField.getText()), 0, 255);
                    int g = clamp(Integer.parseInt(gField.getText()), 0, 255);
                    int b = clamp(Integer.parseInt(bField.getText()), 0, 255);
                    setFromRGB(r, g, b);
                } catch (NumberFormatException ex) {
                    rField.setText(String.valueOf(rSlider.getValue()));
                    gField.setText(String.valueOf(gSlider.getValue()));
                    bField.setText(String.valueOf(bSlider.getValue()));
                }
                internalUpdate = false;
            }
        };
        rField.addActionListener(rgbFieldListener);
        gField.addActionListener(rgbFieldListener);
        bField.addActionListener(rgbFieldListener);

        return panel;
    }

    private JPanel createCMYKPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("CMYK (0 - 100 %)"));

        JPanel sliders = new JPanel(new GridLayout(4, 1, 4, 4));
        sliders.add(makeSlider("C", cSlider, cField));
        sliders.add(makeSlider("M", mSlider, mField));
        sliders.add(makeSlider("Y", ySlider, yField));
        sliders.add(makeSlider("K", kSlider, kField));

        panel.add(sliders, BorderLayout.CENTER);

        ChangeListener cmykChange = e -> {
            if (!internalUpdate && (e.getSource() instanceof JSlider)) {
                internalUpdate = true;
                double c = cSlider.getValue() / 100.0;
                double m = mSlider.getValue() / 100.0;
                double y = ySlider.getValue() / 100.0;
                double k = kSlider.getValue() / 100.0;
                setFromCMYK(c, m, y, k);
                internalUpdate = false;
            }
        };
        cSlider.addChangeListener(cmykChange);
        mSlider.addChangeListener(cmykChange);
        ySlider.addChangeListener(cmykChange);
        kSlider.addChangeListener(cmykChange);

        ActionListener cmykFieldListener = e -> {
            if (!internalUpdate) {
                internalUpdate = true;
                try {
                    int cValue = clamp(Integer.parseInt(cField.getText()), 0, 100);
                    int mValue = clamp(Integer.parseInt(mField.getText()), 0, 100);
                    int yValue = clamp(Integer.parseInt(yField.getText()), 0, 100);
                    int kValue = clamp(Integer.parseInt(kField.getText()), 0, 100);
                    setFromCMYK(cValue / 100.0, mValue / 100.0, yValue / 100.0, kValue / 100.0);
                } catch (NumberFormatException ex) {
                    cField.setText(String.valueOf(cSlider.getValue()));
                    mField.setText(String.valueOf(mSlider.getValue()));
                    yField.setText(String.valueOf(ySlider.getValue()));
                    kField.setText(String.valueOf(kSlider.getValue()));
                }
                internalUpdate = false;
            }
        };
        cField.addActionListener(cmykFieldListener);
        mField.addActionListener(cmykFieldListener);
        yField.addActionListener(cmykFieldListener);
        kField.addActionListener(cmykFieldListener);

        return panel;
    }

    private JPanel createHSVPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("HSV (H:0-360, S/V:0-100%)"));

        JPanel sliders = new JPanel(new GridLayout(3, 1, 4, 4));
        sliders.add(makeSlider("H", hSlider, hField));
        sliders.add(makeSlider("S", sSlider, sField));
        sliders.add(makeSlider("V", vSlider, vField));

        panel.add(sliders, BorderLayout.CENTER);

        ChangeListener hsvChange = e -> {
            if (!internalUpdate && (e.getSource() instanceof JSlider)) {
                internalUpdate = true;
                int h = hSlider.getValue();
                int s = sSlider.getValue();
                int v = vSlider.getValue();
                setFromHSV(h, s, v);
                internalUpdate = false;
            }
        };
        hSlider.addChangeListener(hsvChange);
        sSlider.addChangeListener(hsvChange);
        vSlider.addChangeListener(hsvChange);

        ActionListener hsvFieldListener = e -> {
            if (!internalUpdate) {
                internalUpdate = true;
                try {
                    int h = clamp(Integer.parseInt(hField.getText()), 0, 360);
                    int s = clamp(Integer.parseInt(sField.getText()), 0, 100);
                    int v = clamp(Integer.parseInt(vField.getText()), 0, 100);
                    setFromHSV(h, s, v);
                } catch (NumberFormatException ex) {
                    hField.setText(String.valueOf(hSlider.getValue()));
                    sField.setText(String.valueOf(sSlider.getValue()));
                    vField.setText(String.valueOf(vSlider.getValue()));
                }
                internalUpdate = false;
            }
        };
        hField.addActionListener(hsvFieldListener);
        sField.addActionListener(hsvFieldListener);
        vField.addActionListener(hsvFieldListener);

        return panel;
    }

    private JPanel makeSlider(String label, JSlider slider, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(20, 20));
        panel.add(lbl, BorderLayout.WEST);

        panel.add(slider, BorderLayout.CENTER);

        field.setHorizontalAlignment(JTextField.CENTER);
        field.setSize(new Dimension(260, 40));
        field.setFont(new Font("", Font.PLAIN, 14));
        panel.add(field, BorderLayout.EAST);

        slider.addChangeListener(e -> {
            if (!internalUpdate) {
                field.setText(String.valueOf(slider.getValue()));
            }
        });

        return panel;
    }

    private void MyPalette() {
        JDialog paletteDialog = new JDialog(this, "My Palette", true);
        paletteDialog.setLayout(new BorderLayout(10, 10));
        paletteDialog.setSize(600, 400);
        paletteDialog.setLocationRelativeTo(this);

        JPanel colorGrid = new JPanel(new GridLayout(8, 8, 2, 2));
        colorGrid.setBorder(BorderFactory.createTitledBorder(""));

        Color[] presetColors = {
                new Color(0, 0, 0), new Color(255, 255, 255),
                new Color(128, 128, 128), new Color(192, 192, 192),
                // RGB
                new Color(255, 0, 0), new Color(0, 255, 0), new Color(0, 0, 255),
                new Color(255, 255, 0), new Color(0, 255, 255), new Color(255, 0, 255),
                new Color(128, 0, 0), new Color(0, 128, 0), new Color(0, 0, 128),
                new Color(200, 128, 128), new Color(128, 255, 128), new Color(128, 128, 255),

                new Color(255, 128, 0), new Color(255, 200, 0),
                new Color(128, 0, 128), new Color(200, 0, 200),
                new Color(64, 64, 64), new Color(160, 160, 160),
                new Color(255, 128, 128), new Color(128, 255, 128),
                new Color(128, 128, 255), new Color(255, 255, 128),
                new Color(128, 255, 255), new Color(255, 128, 255),
                new Color(255, 128, 64), new Color(64, 128, 255),
                new Color(128, 255, 64), new Color(255, 64, 128),
                new Color(64, 255, 128), new Color(128, 64, 255),
                new Color(192, 128, 64), new Color(64, 192, 128),
                new Color(120, 10, 192), new Color(192, 64, 128),
                new Color(64, 128, 192), new Color(128, 60, 64),
                new Color(200, 100, 50), new Color(50, 200, 100),
                new Color(100, 50, 200), new Color(200, 50, 100),
                new Color(50, 100, 200), new Color(100, 200, 50),
                new Color(100, 15, 76), new Color(75, 150, 75),
                new Color(75, 75, 150), new Color(150, 150, 75),
                new Color(75, 150, 150), new Color(150, 75, 150),
                new Color(180, 90, 90), new Color(90, 180, 90),
                new Color(90, 90, 180), new Color(180, 180, 90),
        };

        for (Color color : presetColors) {
            JButton colorButton = getButton(color, paletteDialog);
            colorGrid.add(colorButton);
        }

        JPanel controlPanel = getPanel(paletteDialog);

        paletteDialog.add(colorGrid, BorderLayout.CENTER);
        paletteDialog.add(controlPanel, BorderLayout.SOUTH);

        paletteDialog.setVisible(true);
    }

    private JButton getButton(Color color, JDialog paletteDialog) {
        JButton colorButton = new JButton();
        colorButton.setBackground(color);
        colorButton.setOpaque(true);
        colorButton.setBorderPainted(false);
        colorButton.setFocusPainted(false);
        colorButton.setPreferredSize(new Dimension(30, 30));
        colorButton.setToolTipText(String.format("RGB(%d, %d, %d)",
                color.getRed(), color.getGreen(), color.getBlue()));
        colorButton.addActionListener(e -> {
            internalUpdate = true;
            setFromRGB(color.getRed(), color.getGreen(), color.getBlue());
            internalUpdate = false;
            paletteDialog.dispose();
        });

        if (color.getRed() + color.getGreen() + color.getBlue() > 382) {
            colorButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        } else {
            colorButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        }
        return colorButton;
    }

    private JPanel getPanel(JDialog paletteDialog) {
        JPanel controlPanel = new JPanel(new FlowLayout());

        JButton randomColorButton = new JButton("Random color");
        randomColorButton.addActionListener(e -> {
            internalUpdate = true;
            int r = (int)(Math.random() * 256);
            int g = (int)(Math.random() * 256);
            int b = (int)(Math.random() * 256);
            setFromRGB(r, g, b);
            internalUpdate = false;
            paletteDialog.dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> paletteDialog.dispose());

        controlPanel.add(randomColorButton);
        controlPanel.add(cancelButton);
        return controlPanel;
    }

    private void setFromRGB(int r, int g, int b) {
        preview.setBackground(new Color(r, g, b));

        rSlider.setValue(r);
        gSlider.setValue(g);
        bSlider.setValue(b);
        rField.setText(String.valueOf(r));
        gField.setText(String.valueOf(g));
        bField.setText(String.valueOf(b));

        double[] cmyk = rgbToCmyk(r, g, b);
        int cPercent = (int)Math.round(cmyk[0] * 100);
        int mPercent = (int)Math.round(cmyk[1] * 100);
        int yPercent = (int)Math.round(cmyk[2] * 100);
        int kPercent = (int)Math.round(cmyk[3] * 100);

        cSlider.setValue(cPercent);
        mSlider.setValue(mPercent);
        ySlider.setValue(yPercent);
        kSlider.setValue(kPercent);
        cField.setText(String.valueOf(cPercent));
        mField.setText(String.valueOf(mPercent));
        yField.setText(String.valueOf(yPercent));
        kField.setText(String.valueOf(kPercent));

        double[] hsv = rgbToHsv(r, g, b);
        int hValue = (int)hsv[0];
        int sPercent = (int)hsv[1];
        int vPercent = (int)hsv[2];

        hSlider.setValue(hValue);
        sSlider.setValue(sPercent);
        vSlider.setValue(vPercent);
        hField.setText(String.valueOf(hValue));
        sField.setText(String.valueOf(sPercent));
        vField.setText(String.valueOf(vPercent));
    }

    private void setFromCMYK(double c, double m, double y, double k) {
        int[] rgb = cmykToRgb(c, m, y, k);
        setFromRGB(rgb[0], rgb[1], rgb[2]);
    }

    private void setFromHSV(int h, int s, int v) {
        int[] rgb = hsvToRgb(h, s, v);
        setFromRGB(rgb[0], rgb[1], rgb[2]);
    }

    public static double[] rgbToCmyk(int r, int g, int b) {
        double rd = r / 255.0;
        double gd = g / 255.0;
        double bd = b / 255.0;

        double k = 1 - Math.max(rd, Math.max(gd, bd));
        if (k >= 1.0 - 1e-9) {
            return new double[]{0, 0, 0, 1};
        }
        double c = (1 - rd - k) / (1 - k);
        double m = (1 - gd - k) / (1 - k);
        double y = (1 - bd - k) / (1 - k);
        return new double[]{c, m, y, k};
    }

    public static int[] cmykToRgb(double c, double m, double y, double k) {
        int r = Math.round((float)(255 * (1 - c) * (1 - k)));
        int g = Math.round((float)(255 * (1 - m) * (1 - k)));
        int b = Math.round((float)(255 * (1 - y) * (1 - k)));
        return new int[]{clamp(r, 0, 255), clamp(g, 0, 255), clamp(b, 0, 255)};
    }

    public static double[] rgbToHsv(int r, int g, int b) {
        float[] hsv = new float[3];
        Color.RGBtoHSB(r, g, b, hsv);
        return new double[]{
                Math.round(hsv[0] * 360),  // h
                Math.round(hsv[1] * 100),  // s
                Math.round(hsv[2] * 100)   // v
        };
    }

    public static int[] hsvToRgb(double h, double s, double v) {
        float hJava = (float)(h / 360.0);
        float sJava = (float)(s / 100.0);
        float vJava = (float)(v / 100.0);

        int rgb = Color.HSBtoRGB(hJava, sJava, vJava);
        return new int[]{
                (rgb >> 16) & 0xFF,
                (rgb >> 8) & 0xFF,
                rgb & 0xFF
        };
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
