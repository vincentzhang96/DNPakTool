package co.phoenixlab.dn.pak;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

public class PakFileWriter {

    private static int BUFFER_SIZE = 8 * 1024 * 1024;  //  8 MB

    private static final byte[] EMPTY = new byte[256];

    private final Path basePath;
    private final Path targetPath;
    private final Map<Path, FileEntry> files;
    private final byte[] bufferIn;
    private final byte[] bufferOut;
    private final ByteBuffer fileHeaderBuffer;

    public PakFileWriter(Path basePath, Path targetPath) {
        this.basePath = basePath;
        this.targetPath = targetPath;
        files = new HashMap<>();
        bufferIn = new byte[BUFFER_SIZE];
        bufferOut = new byte[BUFFER_SIZE];
        fileHeaderBuffer = ByteBuffer.allocate(316);
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
                fileInfo.setDecompressedSize(Files.size(file));
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

    public void write() throws IOException {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(targetPath.toFile(), "rw")) {
            randomAccessFile.seek(1024L);  //  Skip header for now
            Deflater deflater = new Deflater();
            int lenIn;
            int lenOut;
            long startPos;
            for (Map.Entry<Path, FileEntry> entry : files.entrySet()) {
                deflater.reset();
                FileEntry fileEntry = entry.getValue();
                FileInfo fileInfo = fileEntry.getFileInfo();
                startPos = randomAccessFile.getFilePointer();
                fileInfo.setDiskOffset(startPos);
                try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(entry.getKey()), BUFFER_SIZE)) {
                    while ((lenIn = inputStream.read(bufferIn)) != -1) {
                        deflater.setInput(bufferIn, 0, lenIn);
                        while ((lenOut = deflater.deflate(bufferOut)) != 0) {
                            randomAccessFile.write(bufferOut, 0, lenOut);
                        }
                    }
                    deflater.finish();
                    lenOut = deflater.deflate(bufferOut);
                    randomAccessFile.write(bufferOut, 0, lenOut);
                    long diskSize = randomAccessFile.getFilePointer() - startPos;
                    fileInfo.setDiskSize(diskSize);
                    fileInfo.setCompressedSize(diskSize);
                }
            }
            deflater.end();
            deflater = null;
            long tableOffset = randomAccessFile.getFilePointer();
            long numEntries = files.size();
            FileChannel fileChannel = randomAccessFile .getChannel();
            for (FileEntry entry : files.values()) {
                fillFileHeader(entry.getFileInfo());
                while (fileChannel.write(fileHeaderBuffer) != 0);
            }
            randomAccessFile.seek(0);
            writeHeader(randomAccessFile, numEntries, tableOffset);
        }
    }

    private void writeHeader(RandomAccessFile randomAccessFile, long count, long tableOffset) throws IOException {

    }

    private void fillFileHeader(FileInfo fileInfo) {
        fileHeaderBuffer.rewind();
        byte[] str = fileInfo.getFullPath().getBytes(StandardCharsets.UTF_8);
        int pad = 256 - str.length;
        fileHeaderBuffer.write(str);
        fileHeaderBuffer.write(EMPTY, 0, pad);


    }

    private String relativeToBase(Path path) {
        return "\\" + basePath.relativize(path).toString().replace('/', '\\');
    }


}
