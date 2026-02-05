package engine;

import java.io.*;

public class ConfigFiles {

    public static File get(String name) {
        File dir = new File("config");
        dir.mkdirs();
        return new File(dir, name);
    }

    public static InputStream open(String name) throws IOException {
        File f = get(name);
        if (f.exists()) {
            return new FileInputStream(f);
        }
        // Fallback: aus resources
        InputStream in = ConfigFiles.class
                .getClassLoader()
                .getResourceAsStream("config/" + name);
        if (in == null) {
            throw new FileNotFoundException(name);
        }
        return in;
    }

    public static void ensureExists(String name) throws IOException {
        File f = get(name);
        if (f.exists()) return;

        try (InputStream in = open(name);
             OutputStream out = new FileOutputStream(f)) {
            in.transferTo(out);
        }
    }
}
