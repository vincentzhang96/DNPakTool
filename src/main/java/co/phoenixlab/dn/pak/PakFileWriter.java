package co.phoenixlab.dn.pak;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class PakFileWriter {

    private Path basePath;
    private Path targetPath;
    private Map<String, FileEntry> files;

    public PakFileWriter(Path basePath, Path targetPath) {
        this.basePath = basePath;
        this.targetPath = targetPath;
        files = new HashMap<>();
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
                files.put(relative, fileEntry);
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
        
    }


    private String relativeToBase(Path path) {
        return "\\" + basePath.relativize(path).toString().replace('/', '\\');
    }


}
