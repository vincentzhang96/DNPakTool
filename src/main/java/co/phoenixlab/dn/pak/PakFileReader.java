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
import java.nio.file.Files;
import java.nio.file.Path;

public class PakFileReader implements AutoCloseable {

    private PakHeader header;
    private DirEntry root;
    private Path path;
    RandomAccessFile randomAccessFile;

    public PakFileReader(Path path) {
        this.path = path;
        header = new PakHeader();
        root = new DirEntry("\\", null);
    }

    public void load() throws IOException {
        if (Files.notExists(path)) {
            throw new FileNotFoundException("The file does not exist: " + path.toString());
        }
        if (Files.isDirectory(path)) {
            //  JavaDoc says this is thrown when RAF can't open the file anyways
            throw new FileNotFoundException("The file specified is a directory: " + path.toString());
        }
        randomAccessFile = new RandomAccessFile(path.toFile(), "r");
        header.read(randomAccessFile);

    }

    public static void insert(String path, DirEntry parent, FileEntry fileEntry) throws IllegalArgumentException {
        String[] strs = path.split("\\\\", 2);
        if (strs.length == 1) {
            parent.children.put(fileEntry.name, fileEntry);
            return;
        }
        Entry newEntry = parent.children.get(strs[0]);
        DirEntry dirEntry;
        if (newEntry instanceof DirEntry) {
            dirEntry = (DirEntry)newEntry;
        } else if (newEntry != null) {
            throw new IllegalArgumentException("Cannot replace an existing file with a directory");
        } else {
            dirEntry = new DirEntry(strs[0], parent);
            parent.children.put(strs[0], dirEntry);
        }
        parent.children.put(strs[0], newEntry);
        insert(strs[1], dirEntry, fileEntry);
    }

    public PakHeader getHeader() {
        return header;
    }

    public DirEntry getRoot() {
        return root;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void close() throws IOException {
        if (randomAccessFile != null) {
            randomAccessFile.close();
        }
    }
}

