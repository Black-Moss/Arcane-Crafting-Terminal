import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Builds the mod's textures from Tom's Simple Storage textures.
 * Args: <toms textures dir> <output assets/arcane_terminal/textures dir>
 */
public class GenTextures {
    public static void main(String[] args) throws Exception {
        File toms = new File(args[0]);
        File out = new File(args[1]);
        gui(new File(toms, "gui/crafting_terminal.png"), new File(out, "gui/arcane_crafting_terminal.png"));
        front(new File(toms, "block/crafting_terminal_front.png"), new File(out, "block/arcane_crafting_terminal_front.png"));
    }

    /** Tom's advanced wireless terminal recoloured from netherite to thaumium purple. */
    static void wireless(File in, File outFile) throws Exception {
        BufferedImage src = ImageIO.read(in);
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int[] dark = {0x1A, 0x12, 0x2E};
        int[] mid = {0x4B, 0x3C, 0x7A};
        int[] light = {0xB4, 0xA6, 0xE6};
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int a = argb >>> 24;
                int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
                float lum = (0.299F * r + 0.587F * g + 0.114F * b) / 255F;
                int[] c = lum < 0.30F ? lerp(dark, mid, lum / 0.30F) : lerp(mid, light, Math.min(1F, (lum - 0.30F) / 0.25F));
                dst.setRGB(x, y, (a << 24) | (c[0] << 16) | (c[1] << 8) | c[2]);
            }
        }
        outFile.getParentFile().mkdirs();
        ImageIO.write(dst, "png", outFile);
    }

    /** Header + five storage rows of Tom's terminal, closed by a bottom frame. */
    static void gui(File in, File outFile) throws Exception {
        BufferedImage src = ImageIO.read(in);
        BufferedImage dst = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        int width = 194;
        int storageEnd = 107;
        for (int y = 0; y < storageEnd + 3; y++) {
            for (int x = 0; x < width; x++) {
                dst.setRGB(x, y, src.getRGB(x, y));
            }
        }
        // bottom frame: Tom's panel bottom rows (252..255), right corner shifted out to x=193
        for (int row = 0; row < 4; row++) {
            for (int x = 0; x < width; x++) {
                int sx = x < 168 ? x : x < 176 ? 160 : x - 18;
                dst.setRGB(x, storageEnd + 3 + row, src.getRGB(sx, 252 + row));
            }
        }
        outFile.getParentFile().mkdirs();
        ImageIO.write(dst, "png", outFile);
    }

    /** Tom's crafting terminal front with the screen recoloured to the arcane workbench green. */
    static void front(File in, File outFile) throws Exception {
        BufferedImage src = ImageIO.read(in);
        int size = src.getWidth();
        BufferedImage dst = new BufferedImage(size, src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int lo = size * 4 / 32;
        int hi = size * 28 / 32;
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < size; x++) {
                int argb = src.getRGB(x, y);
                boolean screen = x >= lo && x < hi && y >= lo && y < hi;
                dst.setRGB(x, y, screen ? arcane(argb) : argb);
            }
        }
        outFile.getParentFile().mkdirs();
        ImageIO.write(dst, "png", outFile);
    }

    private static int arcane(int argb) {
        int a = argb >>> 24;
        int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        float lum = (0.299F * r + 0.587F * g + 0.114F * b) / 255F;
        int[] dark = {0x2A, 0x14, 0x3A};   // deep purple for icons and shadows
        int[] mid = {0x1E, 0x5C, 0x24};    // workbench green
        int[] light = {0x7C, 0xD8, 0x6A};  // glowing green highlights
        int[] c;
        if (lum < 0.30F) {
            c = lerp(dark, mid, lum / 0.30F * 0.35F);
        } else {
            float t = Math.min(1F, (lum - 0.30F) / 0.45F);
            c = lerp(mid, light, t);
        }
        return (a << 24) | (c[0] << 16) | (c[1] << 8) | c[2];
    }

    private static int[] lerp(int[] a, int[] b, float t) {
        return new int[] {Math.round(a[0] + (b[0] - a[0]) * t), Math.round(a[1] + (b[1] - a[1]) * t), Math.round(a[2] + (b[2] - a[2]) * t)};
    }
}
