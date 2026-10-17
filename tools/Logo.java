import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Renders the mod logo: Thaumaturge arcane stone bricks behind a 3D Arcane Crafting Terminal.
 * Args: <background tile> <front texture> <side texture> <output png>
 */
public class Logo {
    static final int SIZE = 512;
    static final double SCALE = 17.0;
    static final double YAW = Math.toRadians(-28);
    static final double PITCH = Math.toRadians(18);
    static final double ROLL = Math.toRadians(-6);

    record Vec(double x, double y, double z) {}

    record Face(Vec[] corners, BufferedImage tex, double u0, double v0, double u1, double v1, boolean rotate, double light) {}

    public static void main(String[] args) throws Exception {
        BufferedImage stone = ImageIO.read(new File(args[0]));
        BufferedImage front = ImageIO.read(new File(args[1]));
        BufferedImage side = ImageIO.read(new File(args[2]));
        BufferedImage out = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);

        background(out, stone);
        glow(out, 0xB070FF, 230, 0.55);

        // block model: 16 x 16 face, 6 deep; front (south) at z = 6
        double d = 6;
        Vec[] p = {
            new Vec(0, 0, 0), new Vec(16, 0, 0), new Vec(16, 16, 0), new Vec(0, 16, 0),
            new Vec(0, 0, d), new Vec(16, 0, d), new Vec(16, 16, d), new Vec(0, 16, d)};
        double sw = 6.0 / 16.0;
        List<Face> faces = new ArrayList<>();
        // corners ordered: top-left, top-right, bottom-right, bottom-left as seen from outside
        faces.add(new Face(new Vec[] {p[7], p[6], p[5], p[4]}, front, 0, 0, 1, 1, false, 1.0));            // south (front)
        faces.add(new Face(new Vec[] {p[6], p[2], p[1], p[5]}, side, 0, 0, sw, 1, false, 0.62));           // east
        faces.add(new Face(new Vec[] {p[3], p[7], p[4], p[0]}, side, 0, 0, sw, 1, false, 0.62));           // west
        faces.add(new Face(new Vec[] {p[3], p[2], p[6], p[7]}, side, 0, 0, sw, 1, true, 0.85));            // top
        faces.add(new Face(new Vec[] {p[4], p[5], p[1], p[0]}, side, 0, 0, sw, 1, true, 0.5));             // bottom

        shadow(out, p);
        List<Face> visible = new ArrayList<>();
        for (Face f : faces) {
            if (facesCamera(f)) {
                visible.add(f);
            }
        }
        visible.sort(Comparator.comparingDouble(Logo::depth));
        for (Face f : visible) {
            drawFace(out, f);
        }
        vignette(out);
        ImageIO.write(out, "png", new File(args[3]));
    }

    static Vec transform(Vec v) {
        double x = v.x() - 8, y = v.y() - 8, z = v.z() - 3;
        double cx = x * Math.cos(YAW) + z * Math.sin(YAW);
        double cz = -x * Math.sin(YAW) + z * Math.cos(YAW);
        double cy = y * Math.cos(PITCH) - cz * Math.sin(PITCH);
        double cz2 = y * Math.sin(PITCH) + cz * Math.cos(PITCH);
        double rx = cx * Math.cos(ROLL) - cy * Math.sin(ROLL);
        double ry = cx * Math.sin(ROLL) + cy * Math.cos(ROLL);
        return new Vec(rx, ry, cz2);
    }

    static double[] screen(Vec v) {
        Vec t = transform(v);
        return new double[] {SIZE / 2.0 + t.x() * SCALE, SIZE / 2.0 - t.y() * SCALE + 8};
    }

    static boolean facesCamera(Face f) {
        double[] a = screen(f.corners()[0]), b = screen(f.corners()[1]), c = screen(f.corners()[2]);
        double cross = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
        return cross > 0;
    }

    static double depth(Face f) {
        double z = 0;
        for (Vec v : f.corners()) {
            z += transform(v).z();
        }
        return z;
    }

    /** Fills the quad by inverting the projective map from the unit square for each pixel. */
    static void drawFace(BufferedImage out, Face f) {
        double[][] q = new double[4][];
        for (int i = 0; i < 4; i++) {
            q[i] = screen(f.corners()[i]);
        }
        double[] h = homography(q);
        int minX = (int) Math.floor(Math.min(Math.min(q[0][0], q[1][0]), Math.min(q[2][0], q[3][0])));
        int maxX = (int) Math.ceil(Math.max(Math.max(q[0][0], q[1][0]), Math.max(q[2][0], q[3][0])));
        int minY = (int) Math.floor(Math.min(Math.min(q[0][1], q[1][1]), Math.min(q[2][1], q[3][1])));
        int maxY = (int) Math.ceil(Math.max(Math.max(q[0][1], q[1][1]), Math.max(q[2][1], q[3][1])));
        BufferedImage tex = f.tex();
        for (int y = Math.max(0, minY); y <= Math.min(SIZE - 1, maxY); y++) {
            for (int x = Math.max(0, minX); x <= Math.min(SIZE - 1, maxX); x++) {
                double px = x + 0.5, py = y + 0.5;
                double w = h[6] * px + h[7] * py + 1;
                double s = (h[0] * px + h[1] * py + h[2]) / w;
                double t = (h[3] * px + h[4] * py + h[5]) / w;
                if (s < 0 || s >= 1 || t < 0 || t >= 1) {
                    continue;
                }
                double su = f.rotate() ? t : s;
                double tv = f.rotate() ? 1 - s : t;
                double u = f.u0() + su * (f.u1() - f.u0());
                double v = f.v0() + tv * (f.v1() - f.v0());
                int tx = Math.min(tex.getWidth() - 1, (int) (u * tex.getWidth()));
                int ty = Math.min(tex.getHeight() - 1, (int) (v * tex.getHeight()));
                int c = tex.getRGB(tx, ty);
                if ((c >>> 24) < 16) {
                    continue;
                }
                out.setRGB(x, y, shade(c, f.light()) | 0xFF000000);
            }
        }
    }

    /** Inverse homography: screen point -> (s, t) in the unit square, corners TL, TR, BR, BL. */
    static double[] homography(double[][] q) {
        double[][] a = new double[8][9];
        double[][] uv = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
        for (int i = 0; i < 4; i++) {
            double x = q[i][0], y = q[i][1], u = uv[i][0], v = uv[i][1];
            a[2 * i] = new double[] {x, y, 1, 0, 0, 0, -u * x, -u * y, u};
            a[2 * i + 1] = new double[] {0, 0, 0, x, y, 1, -v * x, -v * y, v};
        }
        for (int col = 0; col < 8; col++) {
            int pivot = col;
            for (int r = col + 1; r < 8; r++) {
                if (Math.abs(a[r][col]) > Math.abs(a[pivot][col])) {
                    pivot = r;
                }
            }
            double[] tmp = a[col];
            a[col] = a[pivot];
            a[pivot] = tmp;
            for (int r = 0; r < 8; r++) {
                if (r == col) {
                    continue;
                }
                double factor = a[r][col] / a[col][col];
                for (int k = col; k < 9; k++) {
                    a[r][k] -= factor * a[col][k];
                }
            }
        }
        double[] h = new double[8];
        for (int i = 0; i < 8; i++) {
            h[i] = a[i][8] / a[i][i];
        }
        return h;
    }

    static int shade(int c, double light) {
        int r = (int) Math.min(255, ((c >> 16) & 0xFF) * light);
        int g = (int) Math.min(255, ((c >> 8) & 0xFF) * light);
        int b = (int) Math.min(255, (c & 0xFF) * light);
        return (r << 16) | (g << 8) | b;
    }

    static void background(BufferedImage out, BufferedImage stone) {
        int tile = 128;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int c = stone.getRGB((x % tile) * stone.getWidth() / tile, (y % tile) * stone.getHeight() / tile);
                out.setRGB(x, y, shade(c, 0.85) | 0xFF000000);
            }
        }
    }

    static void glow(BufferedImage out, int rgb, double radius, double strength) {
        double cx = SIZE / 2.0, cy = SIZE / 2.0;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - cx, y - cy) / radius;
                if (dist >= 1) {
                    continue;
                }
                double a = strength * (1 - dist) * (1 - dist);
                out.setRGB(x, y, blend(out.getRGB(x, y), rgb, a));
            }
        }
    }

    static void shadow(BufferedImage out, Vec[] p) {
        double[] a = screen(p[3]), b = screen(p[2]), c = screen(p[1]), d = screen(p[0]);
        double cx = (a[0] + b[0] + c[0] + d[0]) / 4 + 14, cy = (a[1] + b[1] + c[1] + d[1]) / 4 + 18;
        double rx = 150, ry = 160;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot((x - cx) / rx, (y - cy) / ry);
                if (dist >= 1) {
                    continue;
                }
                double alpha = 0.55 * Math.pow(1 - dist, 1.5);
                out.setRGB(x, y, blend(out.getRGB(x, y), 0x000000, alpha));
            }
        }
    }

    static void vignette(BufferedImage out) {
        double cx = SIZE / 2.0, cy = SIZE / 2.0, max = Math.hypot(cx, cy);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - cx, y - cy) / max;
                double alpha = Math.max(0, (dist - 0.55) / 0.45) * 0.6;
                out.setRGB(x, y, blend(out.getRGB(x, y), 0x0A0612, alpha));
            }
        }
    }

    static int blend(int base, int rgb, double a) {
        int r = (int) (((base >> 16) & 0xFF) * (1 - a) + ((rgb >> 16) & 0xFF) * a);
        int g = (int) (((base >> 8) & 0xFF) * (1 - a) + ((rgb >> 8) & 0xFF) * a);
        int b = (int) ((base & 0xFF) * (1 - a) + (rgb & 0xFF) * a);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
