import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

public class Main extends JFrame {
    private BufferedImage original;
    private BufferedImage working;
    private final ImagePanel originalPanel = new ImagePanel();
    private final ImagePanel resultPanel = new ImagePanel();
    private final HistogramPanel histPanel = new HistogramPanel();
    private final JComboBox<String> sampleCombo;
    private final JLabel status = new JLabel("Готов");
    private final java.util.List<String> sampleUrls = Arrays.asList(
           /*пейзаж*/  "https://thumbs.dreamstime.com/b/beautiful-landscape-valley-alpine-mountains-small-houses-seefeld-rural-scene-majestic-picturesque-view-40712070.jpg",
            /*храм*/ "https://illustrarch.com/wp-content/uploads/2025/04/Architecture_Terms-2.jpg",
            /*куб*/   "https://thumbs.dreamstime.com/b/neon-pink-cube-white-background-soft-side-shadow-clean-geometry-contrast-high-quality-photo-396419053.jpg"
    );
    public Main() {
        super("");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton openBtn = new JButton("Открыть файл");
        JButton loadUrlBtn = new JButton("Загрузить ссылку");
        JButton resetBtn = new JButton("Сброс");
        JButton saveBtn = new JButton("Сохранить результат");

        top.add(openBtn);
        top.add(loadUrlBtn);
        top.add(saveBtn);
        top.add(resetBtn);

        sampleCombo = new JComboBox<>(sampleUrls.toArray(new String[0]));
        JButton loadSample = new JButton("Загрузить пример");
        top.add(new JLabel("Примеры:"));
        top.add(sampleCombo);
        top.add(loadSample);

        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        JPanel imagesRow = new JPanel(new GridLayout(1,2));
        originalPanel.setBorder(new TitledBorder("Оригинал"));
        resultPanel.setBorder(new TitledBorder("Результат"));
        imagesRow.add(originalPanel);
        imagesRow.add(resultPanel);

        center.add(imagesRow, BorderLayout.CENTER);
        histPanel.setPreferredSize(new Dimension(800,160));
        center.add(histPanel, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new TitledBorder("Операции"));

        JButton showHistBtn = new JButton("Построить гистограмму");
        JButton linearStretchBtn = new JButton("Линейное контрастирование");
        JButton histEqGrayBtn = new JButton("Выравнивание гистограммы (Gray)");
        JButton histEqRGBBtn = new JButton("Equalize each channel (RGB)");
        JButton histEqHSVBtn = new JButton("Equalize V in HSV");
        JButton sobelBtn = new JButton("Sobel (границы)");
        JButton harrisBtn = new JButton("Harris (углы)");
        JButton houghBtn = new JButton("Hough (линии)");
        JButton compareBtn = new JButton("Сравнить все контрасты (панель)");

        right.add(showHistBtn);
        right.add(Box.createVerticalStrut(6));
        right.add(linearStretchBtn);
        right.add(Box.createVerticalStrut(6));
        right.add(histEqGrayBtn);
        right.add(Box.createVerticalStrut(6));
        right.add(histEqRGBBtn);
        right.add(Box.createVerticalStrut(6));
        right.add(histEqHSVBtn);
        right.add(Box.createVerticalStrut(12));
        right.add(sobelBtn);
        right.add(Box.createVerticalStrut(6));
        right.add(harrisBtn);
        right.add(Box.createVerticalStrut(6));
        right.add(houghBtn);
        right.add(Box.createVerticalStrut(12));
        right.add(compareBtn);

        add(right, BorderLayout.EAST);

        add(status, BorderLayout.SOUTH);

        openBtn.addActionListener(e -> openFile());
        loadUrlBtn.addActionListener(e -> loadUrlDialog());
        loadSample.addActionListener(e -> loadFromUrl((String)sampleCombo.getSelectedItem()));
        resetBtn.addActionListener(e -> { if (original!=null) { working = deepCopy(original); updateDisplay(); }});
        saveBtn.addActionListener(e -> saveResult());

        showHistBtn.addActionListener(e -> { ifLoaded(() -> showHistogram(working)); });
        linearStretchBtn.addActionListener(e -> { ifLoaded(() -> { working = linearContrastStretch(working); updateDisplay(); }); });
        histEqGrayBtn.addActionListener(e -> { ifLoaded(() -> { working = histogramEqualizationGray(working); updateDisplay(); }); });
        histEqRGBBtn.addActionListener(e -> { ifLoaded(() -> { working = histogramEqualizationRGB(working); updateDisplay(); }); });
        histEqHSVBtn.addActionListener(e -> { ifLoaded(() -> { working = histogramEqualizationHSV(working); updateDisplay(); }); });
        sobelBtn.addActionListener(e -> { ifLoaded(() -> { working = sobelEdgeDetect(working); updateDisplay(); }); });
        harrisBtn.addActionListener(e -> { ifLoaded(() -> { BufferedImage vis = deepCopy(working); markHarrisCorners(vis, harrisCorners(working, 1500)); working = vis; updateDisplay(); }); });
        houghBtn.addActionListener(e -> { ifLoaded(() -> { BufferedImage vis = deepCopy(working); BufferedImage edges = sobelEdgeDetect(working); List<Line> lines = houghLines(edges, 150); drawLines(vis, lines); working = vis; updateDisplay(); }); });
        compareBtn.addActionListener(e -> { ifLoaded(this::showComparisonPanel); });

        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void ifLoaded(Runnable r) {
        if (working == null) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите изображение.", "Нет изображения", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            r.run();
        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Ошибка: " + ex.getMessage());
        }
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                File f = chooser.getSelectedFile();
                BufferedImage img = ImageIO.read(f);
                loadImage(img);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Не удалось открыть: " + ex.getMessage());
            }
        }
    }

    private void loadUrlDialog() {
        String url = JOptionPane.showInputDialog(this, "Введите ссылку на картинку:");
        if (url != null && !url.trim().isEmpty()) loadFromUrl(url.trim());
    }

    private void loadFromUrl(String url) {
        try {
            status.setText("Загрузка");
            BufferedImage img = ImageIO.read(new URL(url));
            loadImage(img);
            status.setText("Загружено с URL");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + ex.getMessage());
            status.setText("Ошибка загрузки");
        }
    }

    private void loadImage(BufferedImage img) {
        original = convertToRGB(img);
        working = deepCopy(original);
        updateDisplay();
    }

    private void updateDisplay() {
        originalPanel.setImage(original);
        resultPanel.setImage(working);
        histPanel.setImage(working);
        repaint();
        status.setText("Готов — изображение обновлено");
    }

    private void saveResult() {
        if (working == null) { JOptionPane.showMessageDialog(this, "Нет результата для сохранения."); return; }
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                File out = chooser.getSelectedFile();
                ImageIO.write(working, "png", out);
                status.setText("Сохранено: " + out.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения: " + ex.getMessage());
            }
        }
    }


    private static BufferedImage convertToRGB(BufferedImage src) {
        BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return img;
    }

    private static BufferedImage deepCopy(BufferedImage src) {
        ColorModel cm = src.getColorModel();
        boolean isAlpha = cm.hasAlpha();
        WritableRaster raster = src.copyData(null);
        return new BufferedImage(cm, raster, isAlpha, null);
    }

    private static int[] computeHistogramGray(BufferedImage img) {
        int w=img.getWidth(), h=img.getHeight();
        int[] hist = new int[256];
        Raster r = img.getRaster();
        int[] px = new int[3];
        for (int y=0;y<h;y++) {
            for (int x=0;x<w;x++) {
                img.getRaster().getPixel(x,y,px);
                int gray = (int)Math.round(0.299*px[0] + 0.587*px[1] + 0.114*px[2]);
                hist[gray]++;
            }
        }
        return hist;
    }

    private void showHistogram(BufferedImage img) {
        histPanel.setImage(img);
    }

    private static BufferedImage linearContrastStretch(BufferedImage src) {
        int w=src.getWidth(), h=src.getHeight();
        BufferedImage dst = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        int[] min = {255,255,255}, max = {0,0,0};
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            int rgb = src.getRGB(x,y);
            int r=(rgb>>16)&0xFF, g=(rgb>>8)&0xFF, b=rgb&0xFF;
            if (r<min[0]) min[0]=r; if (g<min[1]) min[1]=g; if (b<min[2]) min[2]=b;
            if (r>max[0]) max[0]=r; if (g>max[1]) max[1]=g; if (b>max[2]) max[2]=b;
        }
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            int rgb = src.getRGB(x,y);
            int r=(rgb>>16)&0xFF, g=(rgb>>8)&0xFF, b=rgb&0xFF;
            int nr = scale(r, min[0], max[0]);
            int ng = scale(g, min[1], max[1]);
            int nb = scale(b, min[2], max[2]);
            dst.setRGB(x,y,(nr<<16)|(ng<<8)|nb);
        }
        return dst;
    }
    private static int scale(int v, int a, int b) {
        if (b==a) return v;
        int res = (v - a) * 255 / (b - a);
        if (res<0) res=0; if (res>255) res=255;
        return res;
    }

    private static BufferedImage histogramEqualizationGray(BufferedImage src) {
        int w=src.getWidth(), h=src.getHeight();
        int[] hist = computeHistogramGray(src);
        int n = w*h;
        int[] cdf = new int[256];
        cdf[0]=hist[0];
        for (int i=1;i<256;i++) cdf[i]=cdf[i-1]+hist[i];
        int[] map = new int[256];
        for (int i=0;i<256;i++) map[i] = Math.round((cdf[i] - cdf[0]) * 255f / (n - cdf[0]));
        BufferedImage dst = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        int[] px = new int[3];
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            src.getRaster().getPixel(x,y,px);
            int gray = (int)Math.round(0.299*px[0] + 0.587*px[1] + 0.114*px[2]);
            int m = map[gray];
            dst.setRGB(x,y,(m<<16)|(m<<8)|m);
        }
        return dst;
    }

    private static BufferedImage histogramEqualizationRGB(BufferedImage src) {
        int w=src.getWidth(), h=src.getHeight();
        int[][] hist = new int[3][256];
        int[] px = new int[3];
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            src.getRaster().getPixel(x,y,px);
            hist[0][px[0]]++; hist[1][px[1]]++; hist[2][px[2]]++;
        }
        int[][] map = new int[3][256];
        for (int c=0;c<3;c++) {
            int[] cdf = new int[256];
            cdf[0]=hist[c][0];
            for (int i=1;i<256;i++) cdf[i]=cdf[i-1]+hist[c][i];
            int n = w*h;
            for (int i=0;i<256;i++) map[c][i]=Math.round((cdf[i]) * 255f / n);
        }
        BufferedImage dst = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            src.getRaster().getPixel(x,y,px);
            int r=map[0][px[0]], g=map[1][px[1]], b=map[2][px[2]];
            dst.setRGB(x,y,(r<<16)|(g<<8)|b);
        }
        return dst;
    }

    private static BufferedImage histogramEqualizationHSV(BufferedImage src) {
        int w=src.getWidth(), h=src.getHeight();
        int[] histV = new int[256];
        float[] hsv = new float[3];
        int[] rgb = new int[3];
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            int px = src.getRGB(x,y);
            rgb[0]=(px>>16)&0xFF; rgb[1]=(px>>8)&0xFF; rgb[2]=px&0xFF;
            Color.RGBtoHSB(rgb[0],rgb[1],rgb[2],hsv);
            int vi = Math.round(hsv[2]*255f);
            histV[vi]++;
        }
        int n = w*h;
        int[] cdf = new int[256]; cdf[0]=histV[0];
        for (int i=1;i<256;i++) cdf[i]=cdf[i-1]+histV[i];
        int[] map = new int[256];
        for (int i=0;i<256;i++) map[i]=Math.round((cdf[i]) * 255f / n);
        BufferedImage dst = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            int px = src.getRGB(x,y);
            rgb[0]=(px>>16)&0xFF; rgb[1]=(px>>8)&0xFF; rgb[2]=px&0xFF;
            Color.RGBtoHSB(rgb[0],rgb[1],rgb[2],hsv);
            int vi = Math.round(hsv[2]*255f);
            int newV = map[vi];
            int rgbNew = Color.HSBtoRGB(hsv[0], hsv[1], newV/255f);
            dst.setRGB(x,y, rgbNew & 0xFFFFFF);
        }
        return dst;
    }

    private static BufferedImage sobelEdgeDetect(BufferedImage src) {
        int w=src.getWidth(), h=src.getHeight();
        BufferedImage gray = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            int rgb = src.getRGB(x,y);
            int r=(rgb>>16)&0xFF, g=(rgb>>8)&0xFF, b=rgb&0xFF;
            int grayv = (int)Math.round(0.299*r + 0.587*g + 0.114*b);
            gray.getRaster().setSample(x,y,0,grayv);
        }
        BufferedImage out = new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
        int[][] gx = {{-1,0,1},{-2,0,2},{-1,0,1}};
        int[][] gy = {{1,2,1},{0,0,0},{-1,-2,-1}};
        int max = 0;
        int[] buff = new int[9];
        for (int y=1;y<h-1;y++) {
            for (int x=1;x<w-1;x++) {
                int gxs=0,gys=0;
                for (int ky=-1;ky<=1;ky++) for (int kx=-1;kx<=1;kx++) {
                    int val = gray.getRaster().getSample(x+kx,y+ky,0);
                    gxs += val * gx[ky+1][kx+1];
                    gys += val * gy[ky+1][kx+1];
                }
                int mag = (int)Math.hypot(gxs,gys);
                if (mag>max) max=mag;
                out.getRaster().setSample(x,y,0,mag);
            }
        }

        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            int v = out.getRaster().getSample(x,y,0);
            int nv = (int)(v * 255.0 / Math.max(1,max));
            if (nv<30) nv=0;
            out.getRaster().setSample(x,y,0,nv);
        }
        BufferedImage rgbOut = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            int v=out.getRaster().getSample(x,y,0);
            rgbOut.setRGB(x,y,(v<<16)|(v<<8)|v);
        }
        return rgbOut;
    }

    private static List<Point> harrisCorners(BufferedImage src, int maxPoints) {
        int w=src.getWidth(), h=src.getHeight();
        double[][] I = new double[h][w];
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            int rgb = src.getRGB(x,y);
            int r=(rgb>>16)&0xFF, g=(rgb>>8)&0xFF, b=rgb&0xFF;
            I[y][x] = 0.299*r + 0.587*g + 0.114*b;
        }

        double[][] Ix = new double[h][w], Iy = new double[h][w];
        for (int y=1;y<h-1;y++) for (int x=1;x<w-1;x++) {
            Ix[y][x] = (I[y][x+1] - I[y][x-1]) / 2.0;
            Iy[y][x] = (I[y+1][x] - I[y-1][x]) / 2.0;
        }

        double k = 0.04;
        List<Corner> corners = new ArrayList<>();
        for (int y=2;y<h-2;y++) for (int x=2;x<w-2;x++) {
            double A=0,B=0,C=0;
            for (int wy=-1;wy<=1;wy++) for (int wx=-1;wx<=1;wx++) {
                double ix = Ix[y+wy][x+wx], iy = Iy[y+wy][x+wx];
                A += ix*ix;
                B += ix*iy;
                C += iy*iy;
            }
            double det = A*C - B*B;
            double trace = A + C;
            double R = det - k * trace * trace;
            corners.add(new Corner(x,y,R));
        }

        corners.sort((a,b)->Double.compare(b.r,a.r));
        boolean[][] taken = new boolean[h][w];
        List<Point> result = new ArrayList<>();
        for (Corner c : corners) {
            if (c.r <= 0) break;
            if (result.size() >= maxPoints) break;
            if (taken[c.y][c.x]) continue;

            for (int dy=-5;dy<=5;dy++) for (int dx=-5;dx<=5;dx++) {
                int ny=c.y+dy, nx=c.x+dx;
                if (ny>=0 && ny<h && nx>=0 && nx<w) taken[ny][nx]=true;
            }
            result.add(new Point(c.x,c.y));
        }
        return result;
    }

    private static class Corner { int x,y; double r; Corner(int x,int y,double r){this.x=x;this.y=y;this.r=r;} }

    private static void markHarrisCorners(BufferedImage vis, List<Point> corners) {
        Graphics2D g = vis.createGraphics();
        g.setColor(Color.MAGENTA);
        for (Point p: corners) {
            g.drawRect(p.x-3,p.y-3,7,7);
        }
        g.dispose();
    }

    private static List<Line> houghLines(BufferedImage edgeImg, int threshold) {
        int w=edgeImg.getWidth(), h=edgeImg.getHeight();

        boolean[][] edge = new boolean[h][w];
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            int v = (edgeImg.getRGB(x,y)) & 0xFF;
            edge[y][x] = v > 50;
        }
        int diag = (int)Math.hypot(w, h);
        int rMax = diag;
        int rSize = rMax*2 + 1;
        int thetaBins = 180;
        int[][] acc = new int[rSize][thetaBins];
        double[] sin = new double[thetaBins], cos = new double[thetaBins];
        for (int t=0;t<thetaBins;t++) {
            double theta = Math.toRadians(t - 90);
            cos[t] = Math.cos(theta);
            sin[t] = Math.sin(theta);
        }
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            if (!edge[y][x]) continue;
            for (int t=0;t<thetaBins;t++) {
                int r = (int)Math.round(x * cos[t] + y * sin[t]) + rMax;
                if (r>=0 && r<rSize) acc[r][t]++;
            }
        }

        List<Line> lines = new ArrayList<>();
        for (int r=0;r<rSize;r++) for (int t=0;t<thetaBins;t++) {
            if (acc[r][t] > threshold) {
                lines.add(new Line(r - rMax, Math.toRadians(t - 90), acc[r][t]));
            }
        }

        lines.sort((a,b)->Integer.compare(b.v,a.v));
        if (lines.size() > 50) lines = lines.subList(0,50);
        return lines;
    }

    private static void drawLines(BufferedImage img, List<Line> lines) {
        Graphics2D g = img.createGraphics();
        g.setStroke(new BasicStroke(2f));
        Random rnd = new Random(0);
        for (Line L : lines) {
            int w = img.getWidth(), h = img.getHeight();
            double r = L.r, theta = L.theta;

            int x1=0, y1=0, x2=w, y2=h;
            if (Math.abs(Math.sin(theta)) > Math.abs(Math.cos(theta))) {

                x1 = (int)Math.round((r - 0*Math.sin(theta))/Math.cos(theta));
                x2 = (int)Math.round((r - h*Math.sin(theta))/Math.cos(theta));
                y1 = 0; y2 = h;
            } else {
                y1 = (int)Math.round((r - 0*Math.cos(theta))/Math.sin(theta));
                y2 = (int)Math.round((r - w*Math.cos(theta))/Math.sin(theta));
                x1 = 0; x2 = w;
            }
            g.setColor(new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
            g.drawLine(x1,y1,x2,y2);
        }
        g.dispose();
    }

    private static class Line { int r; double theta; int v; Line(int r,double theta,int v){this.r=r;this.theta=theta;this.v=v;} }


    static class ImagePanel extends JPanel {
        private BufferedImage img;
        public Dimension getPreferredSize() {
            return new Dimension(400,300);
        }
        public void setImage(BufferedImage img) { this.img = img; repaint(); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(Color.DARK_GRAY);
                g.drawString("No image", 10,20);
                return;
            }

            int iw = img.getWidth(), ih = img.getHeight();
            double rw = (double)getWidth()/iw, rh = (double)getHeight()/ih;
            double r = Math.min(rw, rh);
            int nw = (int)(iw*r), nh = (int)(ih*r);
            g.drawImage(img, (getWidth()-nw)/2, (getHeight()-nh)/2, nw, nh, null);
        }
    }

    static class HistogramPanel extends JPanel {
        private BufferedImage image;
        public void setImage(BufferedImage img) { image = img; repaint(); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image==null) {
                g.setColor(Color.WHITE); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(Color.GRAY); g.drawString("Гистограмма: нет изображения",10,20);
                return;
            }
            int[] hist = computeHistogramGray(image);
            int w = getWidth(), h = getHeight();
            g.setColor(Color.WHITE); g.fillRect(0,0,w,h);
            int max = Arrays.stream(hist).max().orElse(1);
            int barW = Math.max(1, w/256);
            for (int i=0;i<256;i++) {
                int barH = (int)((double)hist[i]/max * (h-20));
                g.setColor(Color.DARK_GRAY);
                g.fillRect(i*barW+10, h-10-barH, barW, barH);
            }
            g.setColor(Color.BLACK);
            g.drawRect(9,9,w-18,h-18);
        }
    }

    private void showComparisonPanel() {
        BufferedImage orig = deepCopy(original);
        BufferedImage stretched = linearContrastStretch(deepCopy(original));
        BufferedImage histGray = histogramEqualizationGray(deepCopy(original));
        BufferedImage histRGB = histogramEqualizationRGB(deepCopy(original));
        BufferedImage histHSV = histogramEqualizationHSV(deepCopy(original));

        JDialog dlg = new JDialog(this, "Сравнение методов повышения контраста", true);
        dlg.setLayout(new BorderLayout());
        JPanel grid = new JPanel(new GridLayout(2,3));
        grid.add(makePanelWithTitle(orig, "Оригинал"));
        grid.add(makePanelWithTitle(stretched, "Линейное растяжение"));
        grid.add(makePanelWithTitle(histGray, "Гистограмма (Gray)"));
        grid.add(makePanelWithTitle(histRGB, "Гистограмма (RGB каналы)"));
        grid.add(makePanelWithTitle(histHSV, "Гистограмма (V в HSV)"));

        grid.add(new JPanel());
        dlg.add(grid, BorderLayout.CENTER);
        JButton close = new JButton("Закрыть");
        close.addActionListener(e->dlg.dispose());
        dlg.add(close, BorderLayout.SOUTH);
        dlg.setSize(1000,700);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel makePanelWithTitle(BufferedImage img, String title) {
        JPanel p = new JPanel(new BorderLayout());
        ImagePanel ip = new ImagePanel();
        ip.setImage(img);
        p.add(ip, BorderLayout.CENTER);
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        p.add(lbl, BorderLayout.SOUTH);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}
