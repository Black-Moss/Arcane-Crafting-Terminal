import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
public class Crop {
    public static void main(String[] a) throws Exception {
        BufferedImage in = ImageIO.read(new File(a[0]));
        if (a.length == 1) { System.out.println(in.getWidth() + "x" + in.getHeight()); return; }
        int x = Integer.parseInt(a[2]), y = Integer.parseInt(a[3]), w = Integer.parseInt(a[4]), h = Integer.parseInt(a[5]), s = Integer.parseInt(a[6]);
        BufferedImage out = new BufferedImage(w * s, h * s, BufferedImage.TYPE_INT_ARGB);
        for (int j = 0; j < h * s; j++) for (int i = 0; i < w * s; i++) {
            int c = in.getRGB(x + i / s, y + j / s);
            int gx = i / s, gy = j / s;
            if (((c >>> 24) & 0xFF) == 0) c = ((gx / 4 + gy / 4) % 2 == 0) ? 0xFFFF00FF : 0xFF00FFFF;
            if (s >= 4 && (i % (s * 10) == 0 || j % (s * 10) == 0)) c = 0xFFFF0000;
            out.setRGB(i, j, c);
        }
        ImageIO.write(out, "png", new File(a[1]));
    }
}
