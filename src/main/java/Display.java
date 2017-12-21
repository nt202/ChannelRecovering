import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Display {

    private static void showImage(BufferedImage image) {
        try {
            JFrame frame = new JFrame();
            frame.getContentPane().setLayout(new FlowLayout());
            frame.getContentPane().add(new JLabel(new ImageIcon(image)));
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void showImage(int[][] red, int[][] green, int[][] blue) {
        int height = red.length;
        int width = red[0].length;
        BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                temp.setRGB(j, i, red[i][j] << 16 | green[i][j] << 8 | blue[i][j]);
            }
        }
        showImage(temp);
    }
}
