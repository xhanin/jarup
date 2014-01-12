package io.github.xhanin.jarup;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static java.util.Arrays.asList;

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
        unzip(jarPath, root);
        return new WorkingCopy(jarPath, root);
    }

    private final Path jarPath;
    private final File root;
    private boolean updated;

    private WorkingCopy(Path jarPath, File root) {
        this.jarPath = jarPath;
        this.root = root;
    }

    public String readFile(String filePath, String encoding) throws IOException {
        return IOUtils.toString(getFile(filePath), Charset.forName(encoding));
    }

    File getFile(String filePath) {
        return new File(root, filePath.replace(":/", ".$/"));
    }

    public WorkingCopy writeFile(String path, String encoding, String content) throws IOException {
        IOUtils.write(getFile(path), Charset.forName(encoding), content);
        updated = true;
        return this;
    }

    public WorkingCopy copyFileFrom(String from, String to) throws IOException {
        File toFile = getFile(to);
        mkdir(toFile.getParentFile());
        Files.copy(Paths.get(from), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return this;
    }

    public String getDefaultCharsetFor(String path) {
        if (path.endsWith(".properties")) {
            return "ISO-8859-1";
        } else {
            return "UTF-8";
        }
    }

    @Override
    public void close() throws Exception {
        if (updated) {
            zip(root, jarPath);
        }
        IOUtils.delete(root);
    }

    private static void zip(final File from, Path to) throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(to.toFile())))) {
            final byte data[] = new byte[BUFFER];

            final Path root = from.toPath();
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!root.equals(dir) && dir.getFileName().toString().endsWith(".$")) {
                        Path dest = dir.getParent().resolve(dir.getFileName().toString().replaceAll("\\.\\$$", ""));
                        zip(dir.toFile(), dest);
                        addZipEntry(dest);
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!isZipFile(file.toString())) {
                        // zip files are added by compressing corresponding expanded directory
                        addZipEntry(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                private void addZipEntry(Path file) throws IOException {
                    ZipEntry entry = new ZipEntry(root.relativize(file).toString());
                    out.putNextEntry(entry);
                    BufferedInputStream origin = new BufferedInputStream(new FileInputStream(file.toFile()), BUFFER);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                }
            });
        }
    }

    private static void unzip(Path from, File to) throws IOException {
        mkdir(to);

        try (ZipFile zip = new ZipFile(from.toFile())) {
            Enumeration zipFileEntries = zip.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

                String currentEntry = entry.getName();
                File destFile = new File(to, currentEntry);
                mkdir(destFile.getParentFile());

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
                if (isZipFile(currentEntry)) {
                    unzip(destFile.toPath(), new File(destFile.getParentFile(), destFile.getName() + ".$"));
                }
            }
        }
    }

    private static boolean isZipFile(String currentEntry) {
        String s = currentEntry.toLowerCase(Locale.ENGLISH);
        for (String ext : asList(".jar", ".war", ".ear", ".zip")) {
            if (s.endsWith(ext)) {
                return true;
            }
        }

        return false;
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
