package co.phoenixlab.dn.pak;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.Deflater;

public class PakFileWriter {

    private static int BUFFER_SIZE = 8 * 1024 * 1024;  //  8 MB

    private static int UPDATE_INTERVAL = 200;
    private static double SCALAR = 1000.0 / UPDATE_INTERVAL;

    private final Path basePath;
    private final Path targetPath;
    private final Map<Path, FileEntry> files;
    private final byte[] bufferIn;
    private final byte[] bufferOut;
    private final ByteBuffer buffer;

    private long cumulativeSize;

    public PakFileWriter(Path basePath, Path targetPath) {
        this.basePath = basePath;
        this.targetPath = targetPath;
        files = new HashMap<>();
        bufferIn = new byte[BUFFER_SIZE];
        bufferOut = new byte[BUFFER_SIZE];
        buffer = ByteBuffer.allocate(PakHeader.HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        cumulativeSize = PakHeader.HEADER_SIZE;
    }

    public void build() throws IOException {
        Files.walkFileTree(basePath, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                String relative = relativeToBase(file);
                FileInfo fileInfo = new FileInfo();
                long size = Files.size(file);
                fileInfo.setDecompressedSize(size);
                cumulativeSize += size + FileInfo.FILE_INFO_SIZE;
                fileInfo.setFileName(fileName);
                fileInfo.setFullPath(relative);
                FileEntry fileEntry = new FileEntry(fileName, null, fileInfo);
                files.put(file, fileEntry);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void write(Consumer<ProgressUpdate> progressListener) throws IOException {
        ProgressUpdate update = new ProgressUpdate(this);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(targetPath.toFile(), "rw")) {
            randomAccessFile.seek(1024L);  //  Skip header for now
            Deflater deflater = new Deflater();
            int lenIn;
            int lenOut;
            long startPos;
            long time;
            int bytesReadPerSec = 0;
            int filesPerSec = 0;
            time = System.currentTimeMillis();
            for (Map.Entry<Path, FileEntry> entry : files.entrySet()) {
                deflater.reset();
                FileEntry fileEntry = entry.getValue();
                FileInfo fileInfo = fileEntry.getFileInfo();
                startPos = randomAccessFile.getFilePointer();
                fileInfo.setDiskOffset(startPos);
                try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(entry.getKey()), BUFFER_SIZE)) {
                    while ((lenIn = inputStream.read(bufferIn)) != -1) {
                        bytesReadPerSec += lenIn;
                        update.bytesWritten += lenIn;
                        deflater.setInput(bufferIn, 0, lenIn);
                        while ((lenOut = deflater.deflate(bufferOut)) != 0) {
                            randomAccessFile.write(bufferOut, 0, lenOut);
                        }
                        if (System.currentTimeMillis() - time >= UPDATE_INTERVAL) {
                            update.filesPerSec = (int) (filesPerSec * SCALAR);
                            update.kilobytesPerSec = (int) (bytesReadPerSec / 1024 * SCALAR);
                            filesPerSec = 0;
                            bytesReadPerSec = 0;
                            progressListener.accept(update);
                            time = System.currentTimeMillis();
                        }
                    }
                    deflater.finish();
                    lenOut = deflater.deflate(bufferOut);
                    randomAccessFile.write(bufferOut, 0, lenOut);
                    long diskSize = randomAccessFile.getFilePointer() - startPos;
                    fileInfo.setDiskSize(diskSize);
                    fileInfo.setCompressedSize(diskSize);
                    ++filesPerSec;
                    ++update.filesWritten;
                    if (System.currentTimeMillis() - time >= UPDATE_INTERVAL) {
                        update.filesPerSec = (int) (filesPerSec * SCALAR);
                        update.kilobytesPerSec = (int) (bytesReadPerSec / 1024 * SCALAR);
                        filesPerSec = 0;
                        bytesReadPerSec = 0;
                        progressListener.accept(update);
                        time = System.currentTimeMillis();
                    }
                }
            }
            deflater.end();
            long tableOffset = randomAccessFile.getFilePointer();
            long numEntries = files.size();
            FileChannel fileChannel = randomAccessFile .getChannel();
            int filesWrittenPerSec = 0;
            int bytesWrittenPerSec = 0;
            time = System.currentTimeMillis();
            for (FileEntry entry : files.values()) {
                fillFileHeader(entry.getFileInfo());
                int writ;
                while ((writ = fileChannel.write(buffer)) != 0) {
                    bytesWrittenPerSec += writ;
                    update.bytesWritten += writ;
                    if (System.currentTimeMillis() - time >= UPDATE_INTERVAL) {
                        update.filesPerSec = (int) (filesWrittenPerSec * SCALAR);
                        update.kilobytesPerSec = (int) (bytesWrittenPerSec / 1024 * SCALAR);
                        filesWrittenPerSec = 0;
                        bytesWrittenPerSec = 0;
                        progressListener.accept(update);
                        time = System.currentTimeMillis();
                    }
                }
                ++filesWrittenPerSec;
                ++update.filesWritten;
                if (System.currentTimeMillis() - time >= UPDATE_INTERVAL) {
                    update.filesPerSec = (int) (filesWrittenPerSec * SCALAR);
                    update.kilobytesPerSec = (int) (bytesWrittenPerSec / 1024 * SCALAR);
                    filesWrittenPerSec = 0;
                    bytesWrittenPerSec = 0;
                    progressListener.accept(update);
                    time = System.currentTimeMillis();
                }
            }
            fileChannel.position(0);
            fileChannel.write(fillHeader(numEntries, tableOffset));
        }
    }

    private ByteBuffer fillHeader(long count, long tableOffset) throws IOException {
        buffer.rewind();
        buffer.limit(buffer.capacity());
        PakHeader header = new PakHeader();
        header.fileTableOffset = tableOffset;
        header.numFiles = count;
        header.unknown = PakHeader.UNKNOWN_CONST;
        header.magic = PakHeader.MAGIC_WORD;
        header.write(buffer);
        return buffer;
    }

    private void fillFileHeader(FileInfo fileInfo) {
        buffer.rewind();
        buffer.limit(buffer.capacity());
        fileInfo.write(buffer);
    }

    private String relativeToBase(Path path) {
        return "\\" + basePath.relativize(path).toString().replace('/', '\\');
    }

    public long getCumulativeSize() {
        return cumulativeSize;
    }

    public int getNumFiles() {
        return files.size();
    }

    public static class ProgressUpdate {
        long totalBytes;
        long bytesWritten;
        int kilobytesPerSec;
        int totalFiles;
        int filesWritten;
        int filesPerSec;

        ProgressUpdate(PakFileWriter writer) {
            totalBytes = writer.cumulativeSize;
            bytesWritten = 0;
            kilobytesPerSec = 0;
            totalFiles = writer.files.size();
            filesWritten = 0;
            filesPerSec = 0;
        }

        public String toString(String fmt) {
            return String.format(fmt,
                    filesWritten, totalFiles, (int)(100 * ((double)filesWritten/(double)totalFiles)), filesPerSec,
                    Math.max(1, bytesWritten/1024/1024), Math.max(1, totalBytes/1024/1024),
                    (int)(100 * ((double)bytesWritten/(double)totalBytes)), kilobytesPerSec);
        }
    }

}
