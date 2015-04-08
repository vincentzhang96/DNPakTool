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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

/**
 * Represents a pak file, providing access to its header information and file entries.
 * <p>
 * Instances of PakFile are meant to be constructed through {@link PakFileReader#load()} and not
 * by user code.
 */
public class PakFile implements AutoCloseable {

    /** Path to the file on disk that this PakFile represents */
    private final Path path;
    /** Pak file header */
    private final PakHeader header;
    /** A map of path (as strings) to subfile. Flat alternative to {@link PakFile#root} */
    private final Map<String, FileEntry> entryMap;
    /** A {@link DirEntry} representing the root directory in the PakFile */
    private final DirEntry root;
    private final int numFiles;
    /** The RandomAccessFile used to access this PakFile */
    private RandomAccessFile randomAccessFile;

    /**
     * Constructs a PakFile with the given parameters.
     * <p>
     * This constructor is primarily intended for use by {@link PakFileReader#load()}. Please use that instead
     * of manually instantiating this.
     * @param root The DirEntry representing the root directory in the PakFile
     * @param entryMap FileEntries, as a map, mirroring the contents of {@code root}
     * @param header The PakFile header
     * @param path The path to this PakFile (on disk)
     * @param randomAccessFile The RandomAccessFile for accessing the PakFile
     */
    PakFile(DirEntry root, Map<String, FileEntry> entryMap, PakHeader header, Path path, RandomAccessFile randomAccessFile) {
        this.root = root;
        //  NB: entryMap is only made unmodifiable, but by contract with PakFileReader, the backing map is not
        //  changed once this PakFile is constructed, so it is effectively immutable
        this.entryMap = Collections.unmodifiableMap(entryMap);
        this.header = header;
        this.path = path;
        numFiles = entryMap.size();
        this.randomAccessFile = randomAccessFile;
    }

    /**
     * @return The path to the file that this PakFile represents
     */
    public Path getPath() {
        return path;
    }

    /**
     * @return This PakFile's header
     */
    public PakHeader getHeader() {
        return header;
    }

    /**
     * Returns an immutable map view of this PakFile's subfiles.
     * <p>
     * The keys are the full path of the subfile, such as
     * {@code \resources\foo\bar\baz.txt}. The contents mirror that of the {@code root} DirEntry.
     * @return A {@code Map<String, FileEntry>} containing all of this PakFile's subfiles
     * @see PakFile#getRoot()
     */
    public Map<String, FileEntry> getEntryMap() {
        return entryMap;
    }

    /**
     * Returns a tree view of this PakFile's subfiles.
     * <p>
     * The directory tree can be traversed by calling {@link DirEntry#getChildren()} and iterating over the map.
     * Alternatively, you may call {@link DirEntry#get(String)} to get a specific FileEntry by resolving against
     * the current DirEntry (So passing {@code c\d\e} against DirEntry with path {@code \a\b} will resolve to
     * {@code \a\b\c\d\e}.
     * @return The DirEntry that represents the root directory in this PakFile.
     * @see DirEntry#get(String)
     */
    public DirEntry getRoot() {
        return root;
    }

    /**
     * Gets the number of subfiles in this PakFile. This method is preferred to {@code getEntryMap().size()} as
     * the value is cached.
     * @return The number of subfiles in this PakFile
     */
    public int getNumFiles() {
        return numFiles;
    }

    /**
     * Transfers the raw compressed data to the specified target.
     * <p>
     * This method will attempt to transfer {@link FileInfo#diskSize} bytes, starting at byte
     * {@link FileInfo#diskOffset}, to the target. When calling this method, please ensure that there is sufficient
     * capacity in target
     * @param fileInfo The FileInfo specifying which entry to retrieve.
     * @param target A {@code WritableByteChannel} to transfer the data to.
     * @throws IOException If there was an error transferring the data.
     */
    public void transferTo(FileInfo fileInfo, WritableByteChannel target) throws IOException {
        randomAccessFile.getChannel().transferTo(fileInfo.getDiskOffset(), fileInfo.getDiskSize(), target);
    }

    /**
     * Opens the RandomAccessFile used for reading if the current one is not open
     * @throws IOException If there was an error opening the new one
     */
    public void openIfNotOpen() throws IOException {
        if (randomAccessFile == null) {
            randomAccessFile = new RandomAccessFile(path.toFile(), "r");
        }
    }

    /**
     * Re-opens the RandomAccessFile used for reading, closing the current one if it is open.
     * @throws IOException If there was an error closing the previous or opening the new one
     */
    public void reopen() throws IOException {
        if (randomAccessFile != null) {
            close();
        }
        randomAccessFile = new RandomAccessFile(path.toFile(), "r");
    }

    /**
     * Closes this PakFile for reading. The PakFile may be re-opened by calling {@link PakFile#openIfNotOpen()} or
     * {@link PakFile#reopen()}.
     * @throws IOException If there was an error closing the PakFile.
     */
    @Override
    public void close() throws IOException {
        RandomAccessFile randomAccessFile = this.randomAccessFile;
        this.randomAccessFile = null;
        randomAccessFile.close();

    }
}
