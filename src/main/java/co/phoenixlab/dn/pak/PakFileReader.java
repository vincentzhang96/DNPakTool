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
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A PakFileReader is the primary way to load and obtain a {@link PakFile}. Instances of this class may be used
 * to load a PakFile from a disk location via {@link PakFileReader#load(Path)}. Instances may be reused to load
 * multiple PakFiles.
 */
@SuppressWarnings("WeakerAccess")
public class PakFileReader {

    /**
     * Constructs a new PakFileReader for reading PakFiles. Instances are reusable.
     */
    public PakFileReader() {
    }

    /**
     * Loads a PakFile from the given {@code Path}.
     * <p>
     * Note that this method opens a RandomAccessFile to read the PakFile but does not close it.
     * Care must be taken to close() on the <b>returned PakFile</b> to release the file. See {@link PakFile#close()}.
     * @param path The Path to the PakFile to load
     * @return A PakFile read from the given path, in the open state. See {@link PakFile}.
     * @throws FileNotFoundException If the given path does not exist or is a directory
     * @throws InvalidPakException If the given path points to a file that is not a valid PakFile
     * @throws IOException If there was an error reading the PakFile
     */
    public PakFile load(Path path) throws IOException {
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
        long bufSize = header.getNumFiles() * FileInfo.FILE_INFO_SIZE;
        MappedByteBuffer buffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY,
                randomAccessFile.getFilePointer(),
                bufSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (long l = 0; l < header.numFiles; ++l) {
            FileInfo fileInfo = new FileInfo().load(buffer);
            FileEntry entry = root.insert(fileInfo.getFullPath(), fileInfo);
            entries.put(fileInfo.getFullPath(), entry);
        }
        buffer.clear();
        buffer = null;
        return new PakFile(root, entries, header, path, randomAccessFile);
    }
}

