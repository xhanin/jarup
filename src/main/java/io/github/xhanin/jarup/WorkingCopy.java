package io.github.xhanin.jarup;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Date: 10/1/14
 * Time: 17:40
 */
public class WorkingCopy implements AutoCloseable {
    private static final int BUFFER = 2048;
    private static final long TS = System.currentTimeMillis();
    private static final long R = new Random().nextLong();
    private static final AtomicLong C = new AtomicLong();

    public static WorkingCopy prepareFor(Path jarPath) throws IOException {
        String id = TS + "-" + R + "-" + C.incrementAndGet();

        File root = new File(System.getProperty("java.io.tmpdir") + "/jarup/" + id + "/" + jarPath.getFileName());
        extract(jarPath, root);
        return new WorkingCopy(root);
    }

    public WorkingCopy(File root) {
        this.root = root;
    }

    private final File root;

    public String readFile(String filePath, String encoding) throws IOException {
        return IOUtils.toString(new File(root, filePath), Charset.forName(encoding));
    }

    @Override
    public void close() throws Exception {
        IOUtils.delete(root);
    }

    private static void extract(Path jarPath, File to) throws IOException {
        ZipFile zip = new ZipFile(jarPath.toFile());

        mkdir(to);

        Enumeration zipFileEntries = zip.entries();
        while (zipFileEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

            String currentEntry = entry.getName();
            File destFile = new File(to, currentEntry);
            File destinationParent = destFile.getParentFile();

            mkdir(destinationParent);

            if (!entry.isDirectory()) {
                try (BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                     BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER)) {

                    byte data[] = new byte[BUFFER];
                    int currentByte;

                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                }
            } else {
                mkdir(destFile);
            }

            // recursive
//            if (currentEntry.endsWith(".zip"))
//            {
//                // found a zip file, try to open
//                extractFolder(destFile.getAbsolutePath());
//            }
        }
    }

    private static void mkdir(File to) throws IOException {
        if (to.exists()) {
            if (!to.isDirectory()) {
                throw new IOException("can't create directory " + to.getAbsolutePath() + ": a file of same name already exists");
            } else {
                return;
            }
        }
        if (!to.mkdirs()) {
            throw new IOException("can't create directory " + to.getAbsolutePath());
        }
    }
}
