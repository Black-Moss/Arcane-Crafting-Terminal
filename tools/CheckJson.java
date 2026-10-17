import java.nio.file.*;
public class CheckJson { public static void main(String[] a) throws Exception { for (String f : a) { String s = Files.readString(Path.of(f)); com.google.gson.JsonParser.parseString(s); System.out.println("OK " + f); } } }
