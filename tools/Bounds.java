import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
public class Bounds {
    public static void main(String[] a) throws Exception {
        BufferedImage in = ImageIO.read(new File(a[0]));
        int W = in.getWidth(), H = in.getHeight();
        boolean[][] seen = new boolean[W][H];
        for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) {
            if (seen[x][y] || ((in.getRGB(x, y) >>> 24) == 0)) continue;
            int x0 = x, x1 = x, y0 = y, y1 = y, n = 0;
            ArrayDeque<int[]> q = new ArrayDeque<>(); q.add(new int[]{x, y}); seen[x][y] = true;
            while (!q.isEmpty()) { int[] p = q.poll(); n++;
                x0 = Math.min(x0, p[0]); x1 = Math.max(x1, p[0]); y0 = Math.min(y0, p[1]); y1 = Math.max(y1, p[1]);
                for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) { int nx = p[0]+d[0], ny = p[1]+d[1];
                    if (nx>=0&&ny>=0&&nx<W&&ny<H&&!seen[nx][ny]&&(in.getRGB(nx,ny)>>>24)!=0){seen[nx][ny]=true;q.add(new int[]{nx,ny});}}}
            if (n > 20) System.out.println("region x=" + x0 + ".." + x1 + " y=" + y0 + ".." + y1 + " px=" + n);
        }
    }
}
