import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static int COUNT_OF_POINTS = 64; // quad of number

    private static int[][] imageR;
    private static int[][] imageG;
    private static int[][] imageB;
    private static int[][] imageRDeformed;
    private static int[][] imageRCorrected;

    private static Map<Integer, Integer[]> points = new HashMap<>(COUNT_OF_POINTS);

    private static INDArray F = Nd4j.create(COUNT_OF_POINTS, 10);
    private static INDArray dX = Nd4j.create(COUNT_OF_POINTS, 1);
    private static INDArray dY = Nd4j.create(COUNT_OF_POINTS, 1);

    public static void main(String[] args) {
        InputStream is = null;
        try {
            is = Main.class.getClass().getResourceAsStream("/image.jpeg");
            BufferedImage imageOriginal = ImageIO.read(is);
            int width = imageOriginal.getWidth();
            int height = imageOriginal.getHeight();
            setPoints(width, height);
            imageR = new int[height][width];
            imageG = new int[height][width];
            imageB = new int[height][width];
            imageRDeformed = new int[height][width];
            imageRCorrected = new int[height][width];
            BufferedImage imageDeform = imageOriginal.getSubimage(8, 2, width - 23, height - 13);
            imageDeform = resize(imageDeform, width, height);

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Color pixelColorOrigin = new Color(imageOriginal.getRGB(j, i));
                    int red = pixelColorOrigin.getRed();
                    int green = pixelColorOrigin.getGreen();
                    int blue = pixelColorOrigin.getBlue();
                    Color pixelColorDeform = new Color(imageDeform.getRGB(j, i));
                    int red2 = pixelColorDeform.getRed();
                    imageR[i][j] = red;
                    imageG[i][j] = green;
                    imageB[i][j] = blue;
                    imageRDeformed[i][j] = red2;
                }
            }

            Display.showImage(imageRDeformed, imageG, imageB);

            for (int i = 0; i < COUNT_OF_POINTS; i++) {
                int[] shifts = calculatedXdY(points.get(i));
                dX.put(i, 0, shifts[0]);
                dY.put(i, 0, shifts[1]);
            }

            MatrixContainer.setdX(dX);
            MatrixContainer.setdY(dY);

            for (int i = 0; i < COUNT_OF_POINTS; i++) {
                int[] vector = calculatedF(points.get(i));
                for (int j = 0; j < 10; j++) {
                    F.put(i, j, vector[j]);
                }
            }

            MatrixContainer.setF(F);

            correctRLayer(MatrixContainer.getAx(), MatrixContainer.getAy());

            Display.showImage(imageRCorrected, imageG, imageB);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void setPoints(int width, int height) {
        int counter = 0;
        int n = (int) Math.sqrt(COUNT_OF_POINTS);
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                Integer[] coordinates = new Integer[2];
                coordinates[0] = (width / (n + 1) * i); // x
                coordinates[1] = (height / (n + 1) * j); // y
                points.put(counter, coordinates);
                counter++;
            }
        }
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    private static int[] calculatedXdY(Integer[] coordinates) {
        int[][] window = new int[7][7];

        int x0 = coordinates[0];
        int y0 = coordinates[1];

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                window[i][j] = imageRDeformed[i - 3 + y0][j - 3 + x0];
            }
        }

        int dy = 0;
        int dx = 0;
        int value = Integer.MAX_VALUE;
        for (int i = y0 - 15; i < y0 + 15; i++) {
            for (int j = x0 - 15; j < x0 + 15; j++) {
                int counter = 0;
                for (int m = i - 3; m < i + 3; m++) {
                    for (int n = j - 3; n < j + 3; n++) {
                        counter += Math.abs(window[m - i + 3][n - j + 3] - imageR[m][n]);
                    }
                }
                if (counter < value) {
                    value = counter;
                    dy = i - y0;
                    dx = j - x0;
                }
            }
        }

        int result[] = new int[2];
        result[0] = dx;
        result[1] = dy;

        return result;
    }

    private static int[] calculatedF(Integer[] coordinates) {
        int[] vector = new int[10];
        int x0 = coordinates[0];
        int y0 = coordinates[1];
        vector[0] = 1;
        vector[1] = x0;
        vector[2] = y0;
        vector[3] = x0 * y0;
        vector[4] = x0 * x0;
        vector[5] = y0 * y0;
        vector[6] = x0 * x0 * y0;
        vector[7] = x0 * y0 * y0;
        vector[8] = x0 * x0 * x0;
        vector[9] = y0 * y0 * y0;
        return vector;
    }

    private static void correctRLayer(INDArray Ax, INDArray Ay) {
        double dx;
        double dy;
        for (int i = 0; i < imageG.length; i++) {
            for (int j = 0; j < imageG[0].length; j++) {
                dx = Ax.getDouble(0) +
                        Ax.getDouble(1) * j +
                        Ax.getDouble(2) * i +
                        Ax.getDouble(3) * j * i +
                        Ax.getDouble(4) * j * j +
                        Ax.getDouble(5) * i * i +
                        Ax.getDouble(6) * j * j * i +
                        Ax.getDouble(7) * j * i * i +
                        Ax.getDouble(8) * j * j * j +
                        Ax.getDouble(9) * i * i * i;
                dy = Ay.getDouble(0) +
                        Ay.getDouble(1) * j +
                        Ay.getDouble(2) * i +
                        Ay.getDouble(3) * j * i +
                        Ay.getDouble(4) * j * j +
                        Ay.getDouble(5) * i * i +
                        Ay.getDouble(6) * j * j * i +
                        Ay.getDouble(7) * j * i * i +
                        Ay.getDouble(8) * j * j * j +
                        Ay.getDouble(9) * i * i * i;
                try {
                    imageRCorrected[i][j] = imageRDeformed[i - (int) dy][j - (int) dx];
                } catch (Exception e) {
                    // NOPE
                }
            }
        }
    }
}