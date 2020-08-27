package io.github.xhanin.jarup;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newBufferedWriter;

/**
 * Date: 10/1/14
 * Time: 17:40
 */
public class WorkingCopy implements AutoCloseable {
    private static final int BUFFER = 2048;
    private static final long TS = System.currentTimeMillis();
    private static final long R = new Random().nextLong();
    private static final AtomicLong C = new AtomicLong();

    private static final String MANIFEST = JarFile.MANIFEST_NAME;
    private static final String MANIFEST_DIR = "META-INF/";

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
        return IOUtils.toString(getFile(filePath, FileOperation.READ), Charset.forName(encoding));
    }

    // for tests only
    File getFile(String filePath) {
        return new File(root, filePath);
    }

    public void deleteFile(String path) throws IOException {
        getFile(path, FileOperation.DELETE);
    }

    private File getFile(String filePath, FileOperation operation) throws IOException {
        Path archiveRoot = root.toPath();
        String entryName = filePath;
        while (filePath.contains(":/")) {
            String subPath = filePath.substring(0, filePath.indexOf(":/"));
            Path explodedPath = getExplodedPath(root.toPath().resolve(subPath));
            if (!explodedPath.toFile().exists()) {
                unzip(root.toPath().resolve(subPath), explodedPath.toFile());
            }
            archiveRoot = explodedPath;
            entryName = filePath.substring(filePath.indexOf(":/") + 2);
            filePath = root.toPath().relativize(explodedPath) + "/" + entryName;
        }
        File file = new File(root, filePath);
        if (operation.requiresUpdate()) {
            updated = true;
            operation.updateEntry(file, archiveRoot, entryName);
        }
        return file;
    }

    private static Path getExplodedPath(Path file) {
        return file.resolveSibling(file.getFileName().toString() + ".$");
    }

    private static void addEntry(Path archiveRoot, String entryName) throws IOException {
        try (BufferedWriter entries = newBufferedWriter(entriesPath(archiveRoot),
                UTF_8, StandardOpenOption.APPEND)) {
            entries.write(entryName(archiveRoot.resolve(entryName), entryName));
            entries.newLine();
        }
    }

    private static void removeEntry(Path archiveRoot, String entryName) throws IOException {
        Path path = entriesPath(archiveRoot);
        File temp = Files.createTempFile(archiveRoot, "temp", "jarup").toFile();
        try (BufferedReader reader = newBufferedReader(path, UTF_8);
             BufferedWriter writer = newBufferedWriter(temp.toPath(), UTF_8)) {

            String entryLine = entryName(archiveRoot.resolve(entryName), entryName);
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String current = currentLine.trim();
                if (current.equals(entryLine)) {
                    continue;
                }
                writer.write(current);
                writer.newLine();
            }
        }

        File target = path.toFile();
        if (!temp.renameTo(target)) {
            throw new FileSystemException(
                    String.format("Could not copy temporary entries files <%s> to <%s>", temp.getPath(), target.getPath())
            );
        }
    }

    public WorkingCopy writeFile(String path, String encoding, String content) throws IOException {
        File file = getFile(path, FileOperation.UPDATE);
        IOUtils.write(file, Charset.forName(encoding), content);
        return this;
    }

    public WorkingCopy copyFileFrom(String from, String to) throws IOException {
        File toFile = getFile(to, FileOperation.UPDATE);
        mkdir(toFile.getParentFile());
        Files.copy(Paths.get(from), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return this;
    }

    public WorkingCopy copyFileTo(String from, String to) throws IOException {
        File toFile = Paths.get(to).toFile();
        mkdir(toFile.getParentFile());
        Files.copy(getFile(from, FileOperation.UPDATE).toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
            final Path root = from.toPath();

            // repackaging uncompressed archives
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!root.equals(dir) && dir.getFileName().toString().endsWith(".$")) {
                        Path dest = dir.resolveSibling(dir.getFileName().toString().replaceAll("\\.\\$$", ""));
                        zip(dir.toFile(), dest);
                        IOUtils.delete(dir.toFile());
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }
            });

            // process all entries as listed in entries file
            for (String entry : Files.readAllLines(entriesPath(root), UTF_8)) {
                addZipEntry(out, root, root.resolve(entry));
            }
        }
    }

    /*
     * Adds a new file entry to the ZIP output stream.
     */
    private static void addZipEntry(ZipOutputStream out, Path root, Path path) throws IOException {
        String name = entryName(root, path);

        File file = path.toFile();
        boolean isDir = file.isDirectory();
        if (name.equals("") || name.equals(".")) {
            return;
        }

        long size = isDir ? 0 : file.length();

        ZipEntry e = new ZipEntry(name);
        e.setTime(file.lastModified());
        if (size == 0) {
            e.setMethod(ZipEntry.STORED);
            e.setSize(0);
            e.setCrc(0);
        }
        out.putNextEntry(e);
        if (!isDir) {
            byte[] buf = new byte[1024];
            int len;
            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                while ((len = is.read(buf, 0, buf.length)) != -1) {
                    out.write(buf, 0, len);
                }
            }
        }
        out.closeEntry();
    }

    private static String entryName(Path root, Path path) {
        String name = root.relativize(path).toString();
        return entryName(path, name);
    }

    private static String entryName(Path path, String name) {
        name = name.replace(File.separatorChar, '/');
        if (Files.isDirectory(path)) {
            name = name.endsWith(File.separator) ? name :
                    (name + File.separator);
        }
        return name;
    }

    private static void unzip(Path from, File to) throws IOException {
        to = to.getCanonicalFile();
        mkdir(to);

        try (ZipFile zip = new ZipFile(from.toFile());
             BufferedWriter entries = newBufferedWriter(entriesPath(to.toPath()), UTF_8)
        ) {
            Enumeration zipFileEntries = zip.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                entries.write(entry.getName());
                entries.newLine();

                File destFile = createNewFile(to, entry.getName());
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
                destFile.setLastModified(entry.getTime());
            }
        }
    }

    private static File createNewFile(File to, String entry) throws IOException {
        File destFile = new File(to, entry);
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(to + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + entry);
        }
        return destFile;
    }

    private static Path entriesPath(Path root) {
        return root.resolve("___jarup___entries");
    }

    private static void mkdir(File to) throws IOException {
        if (to == null) {
            return;
        }
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

    private enum FileOperation {
        READ {
            @Override
            public boolean requiresUpdate() {
                return false;
            }

            @Override
            public void updateEntry(File file, Path archiveRoot, String entryName) {
                throw new UnsupportedOperationException();
            }
        },
        UPDATE {
            @Override
            public boolean requiresUpdate() {
                return true;
            }

            @Override
            public void updateEntry(File file, Path archiveRoot, String entryName) throws IOException {
                if (!file.exists()) {
                    addEntry(archiveRoot, entryName);
                }
            }
        },
        DELETE {
            @Override
            public boolean requiresUpdate() {
                return true;
            }

            @Override
            public void updateEntry(File file, Path archiveRoot, String entryName) throws IOException {
                if (file.exists() && file.delete()) {
                    removeEntry(archiveRoot, entryName);
                }
            }
        };

        public abstract boolean requiresUpdate();

        public abstract void updateEntry(File file, Path archiveRoot, String entryName) throws IOException;
    }
}
