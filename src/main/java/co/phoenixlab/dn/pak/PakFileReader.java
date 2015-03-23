/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Vince
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package co.phoenixlab.dn.pak;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class PakFileReader implements AutoCloseable {

    private PakHeader header;
    private DirEntry root;
    private Path path;
    private int numFilesRead;
    RandomAccessFile randomAccessFile;

    public PakFileReader(Path path) {
        this.path = path;
        header = new PakHeader();
        root = new DirEntry("\\", null);
    }

    public void load() throws IOException {
        numFilesRead = 0;
        if (Files.notExists(path)) {
            throw new FileNotFoundException("The file does not exist: " + path.toString());
        }
        if (Files.isDirectory(path)) {
            //  JavaDoc says this is thrown when RAF can't open the file anyways
            throw new FileNotFoundException("The file specified is a directory: " + path.toString());
        }
        randomAccessFile = new RandomAccessFile(path.toFile(), "r");
        header.read(randomAccessFile);
        randomAccessFile.seek(header.getFileTableOffset());
        for (long l = 0; l < header.numFiles; ++l) {
            FileInfo fileInfo = new FileInfo().load(randomAccessFile);
            root.insert(fileInfo.getFullPath(), fileInfo);
            ++numFilesRead;
        }
    }

//    public static void insert(String path, DirEntry parent, FileInfo fileInfo) throws IllegalArgumentException {
//        String[] strs = path.split("\\\\", 2);
//        if (strs.length == 1) {
//            parent.getChildren().put(strs[0], new FileEntry(fileInfo.getFileName(), parent, fileInfo));
//            return;
//        }
//        Entry newEntry = parent.getChildren().get(strs[0]);
//        DirEntry dirEntry;
//        if (newEntry instanceof DirEntry) {
//            dirEntry = (DirEntry) newEntry;
//        } else if (newEntry != null) {
//            throw new IllegalArgumentException("Cannot replace an existing file with a directory");
//        } else {
//            dirEntry = new DirEntry(strs[0], parent);
//            parent.getChildren().put(strs[0], dirEntry);
//        }
//        insert(strs[1], dirEntry, fileInfo);
//    }

    public PakHeader getHeader() {
        return header;
    }

    public DirEntry getRoot() {
        return root;
    }

    public Path getPath() {
        return path;
    }

    public int getNumFilesRead() {
        return numFilesRead;
    }

    public FileChannel getFileChannel(FileInfo fileInfo) throws IOException {
        FileChannel fileChannel = randomAccessFile.getChannel();
        fileChannel.position(fileInfo.getDiskOffset());
        return fileChannel;
    }

    public void transferTo(FileInfo fileInfo, WritableByteChannel target) throws IOException {
        FileChannel fileChannel = getFileChannel(fileInfo);
        fileChannel.transferTo(fileInfo.getDiskOffset(), fileInfo.getDiskSize(), target);
    }

    @Override
    public void close() throws IOException {
        if (randomAccessFile != null) {
            randomAccessFile.close();
        }
    }
}

