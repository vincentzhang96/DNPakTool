/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Vincent Zhang/PhoenixLAB
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
import java.util.HashMap;
import java.util.Map;

public class PakFileReader {

    private final Path path;

    public PakFileReader(Path path) {
        this.path = path;
    }

    public PakFile load() throws IOException {
        if (Files.notExists(path)) {
            throw new FileNotFoundException("The file does not exist: " + path.toString());
        }
        if (Files.isDirectory(path)) {
            //  JavaDoc says this is thrown when RAF can't open the file anyways
            throw new FileNotFoundException("The file specified is a directory: " + path.toString());
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "r");
        PakHeader header = new PakHeader();
        DirEntry root = new DirEntry("\\", null);
        header.read(randomAccessFile);
        randomAccessFile.seek(header.getFileTableOffset());
        Map<String, FileEntry> entries = new HashMap<>((int) header.numFiles);
        for (long l = 0; l < header.numFiles; ++l) {
            FileInfo fileInfo = new FileInfo().load(randomAccessFile);
            FileEntry entry = root.insert(fileInfo.getFullPath(), fileInfo);
            entries.put(fileInfo.getFullPath(), entry);
        }
        return new PakFile(root, entries, header, path, randomAccessFile);
    }
}

