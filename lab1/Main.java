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
        setSize(980, 640);
        setLocationRelativeTo(null);

        preview.setPreferredSize(new Dimension(200, 400));
        preview.setBorder(BorderFactory.createTitledBorder("Preview"));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout(8, 8));
        leftPanel.add(preview, BorderLayout.NORTH);

        JButton chooseColor = new JButton("Palette");
        chooseColor.setFont(new Font("", Font.PLAIN, 20));
        chooseColor.addActionListener(e -> openColorChooser());
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
        sliders.add(makeLabeledSlider("R", rSlider, rField));
        sliders.add(makeLabeledSlider("G", gSlider, gField));
        sliders.add(makeLabeledSlider("B", bSlider, bField));

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
        sliders.add(makeLabeledSlider("C", cSlider, cField));
        sliders.add(makeLabeledSlider("M", mSlider, mField));
        sliders.add(makeLabeledSlider("Y", ySlider, yField));
        sliders.add(makeLabeledSlider("K", kSlider, kField));

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
        sliders.add(makeLabeledSlider("H", hSlider, hField));
        sliders.add(makeLabeledSlider("S", sSlider, sField));
        sliders.add(makeLabeledSlider("V", vSlider, vField));

        panel.add(sliders, BorderLayout.CENTER);

        ChangeListener hsvChange = e -> {
            if (!internalUpdate && (e.getSource() instanceof JSlider)) {
                internalUpdate = true;
                double h = hSlider.getValue();
                double s = sSlider.getValue() / 100.0;
                double v = vSlider.getValue() / 100.0;
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
                    double h = clampDouble(Double.parseDouble(hField.getText()), 0, 361);
                    int sValue = clamp(Integer.parseInt(sField.getText()), 0, 100);
                    int vValue = clamp(Integer.parseInt(vField.getText()), 0, 100);
                    setFromHSV(h, sValue / 100.0, vValue / 100.0);
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

    private JPanel makeLabeledSlider(String label, JSlider slider, JTextField field) {
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

    private void openColorChooser() {
        Color chosen = JColorChooser.showDialog(this, "Choose color", preview.getBackground());
        if (chosen != null) {
            internalUpdate = true;
            setFromRGB(chosen.getRed(), chosen.getGreen(), chosen.getBlue());
            internalUpdate = false;
        }
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
        cSlider.setValue((int) Math.round(cmyk[0] * 100));
        mSlider.setValue((int) Math.round(cmyk[1] * 100));
        ySlider.setValue((int) Math.round(cmyk[2] * 100));
        kSlider.setValue((int) Math.round(cmyk[3] * 100));
        cField.setText(String.valueOf((int) Math.round(cmyk[0] * 100)));
        mField.setText(String.valueOf((int) Math.round(cmyk[1] * 100)));
        yField.setText(String.valueOf((int) Math.round(cmyk[2] * 100)));
        kField.setText(String.valueOf((int) Math.round(cmyk[3] * 100)));

        double[] hsv = rgbToHsv(r, g, b);
        hSlider.setValue((int) Math.round(hsv[0]));
        sSlider.setValue((int) Math.round(hsv[1] * 100));
        vSlider.setValue((int) Math.round(hsv[2] * 100));
        hField.setText(String.valueOf((int) Math.round(hsv[0])));
        sField.setText(String.valueOf((int) Math.round(hsv[1] * 100)));
        vField.setText(String.valueOf((int) Math.round(hsv[2] * 100)));
    }

    private void setFromCMYK(double c, double m, double y, double k) {
        int[] rgb = cmykToRgb(c, m, y, k);
        setFromRGB(rgb[0], rgb[1], rgb[2]);
    }

    private void setFromHSV(double h, double s, double v) {
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
        return new double[]{clampDouble(c, 0, 1), clampDouble(m, 0, 1), clampDouble(y, 0, 1), clampDouble(k, 0, 1)};
    }

    public static int[] cmykToRgb(double c, double m, double y, double k) {
        int r = (int) Math.round(255 * (1 - c) * (1 - k));
        int g = (int) Math.round(255 * (1 - m) * (1 - k));
        int b = (int) Math.round(255 * (1 - y) * (1 - k));
        return new int[]{clamp(r, 0, 255), clamp(g, 0, 255), clamp(b, 0, 255)};
    }

    public static double[] rgbToHsv(int r, int g, int b) {
        double rd = r / 255.0;
        double gd = g / 255.0;
        double bd = b / 255.0;

        double max = Math.max(rd, Math.max(gd, bd));
        double min = Math.min(rd, Math.min(gd, bd));
        double delta = max - min;

        double h = 0.0;
        if (delta < 1e-9) {
            h = 0;
        } else if (max == rd) {
            h = 60 * (((gd - bd) / delta) % 6);
        } else if (max == gd) {
            h = 60 * (((bd - rd) / delta) + 2);
        } else if (max == bd) {
            h = 60 * (((rd - gd) / delta) + 4);
        }
        if (h < 0) h += 360;

        double s = (max == 0) ? 0 : delta / max;
        double v = max;

        return new double[]{h, s, v};
    }

    public static int[] hsvToRgb(double h, double s, double v) {
        double C = v * s;
        double X = C * (1 - Math.abs(((h / 60.0) % 2) - 1));
        double m = v - C;

        double rd = 0, gd = 0, bd = 0;
        if (h >= 0 && h < 60) {
            rd = C;
            gd = X;
            bd = 0;
        } else if (h < 120) {
            rd = X;
            gd = C;
            bd = 0;
        } else if (h < 180) {
            rd = 0;
            gd = C;
            bd = X;
        } else if (h < 240) {
            rd = 0;
            gd = X;
            bd = C;
        } else if (h < 300) {
            rd = X;
            gd = 0;
            bd = C;
        } else {
            rd = C;
            gd = 0;
            bd = X;
        }

        int r = (int) Math.round((rd + m) * 255);
        int g = (int) Math.round((gd + m) * 255);
        int b = (int) Math.round((bd + m) * 255);
        return new int[]{clamp(r, 0, 255), clamp(g, 0, 255), clamp(b, 0, 255)};
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double clampDouble(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(Main::new);
    }
}