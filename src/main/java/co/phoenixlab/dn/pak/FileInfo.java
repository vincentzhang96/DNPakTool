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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static co.phoenixlab.dn.pak.Util.readNulTerminatedStr;

public class FileInfo {

    /**
     * {@value} bytes, the size of the name field on disk. The name itself may be shorter.
     */
    public static final int NAME_BYTES_SIZE = 256;
    /**
     * {@value} bytes, the size of a FileInfo on disk.
     */
    public static final int FILE_INFO_SIZE = 316;


    /** The full path to this file, starting with root (\) */
    private String fullPath;
    /** The name of this file, which is the path after the last non-trailing backslash (\) */
    private String fileName;
    /** Size of the file on disk (inside the PakFile) */
    private long diskSize;
    /** Size of the file when decompressed */
    private long decompressedSize;
    /** Size of the file when compressed, equal to the size of the file on disk */
    private long compressedSize;
    /** Location within the PakFile that the compressed data resides */
    private long diskOffset;
    /** Unknown field */
    private int unknown;

    /**
     * Loads a FileInfo from the given RandomAccessFile. The RandomAccessFile's position must be at the start
     * of a valid entry for a successful read. This method returns this FileInfo for convenience for daisy-chaining.
     * @param randomAccessFile The RandomAccessFile to read from, already positioned at the desired location
     * @return This FileInfo, after its fields have been filled from the data on disk.
     * @throws IOException If an IOException occured during reading from the RandomAccessFile
     */
    public FileInfo load(RandomAccessFile randomAccessFile) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(FILE_INFO_SIZE);
        FileChannel fileChannel = randomAccessFile.getChannel();
        while ((fileChannel.read(buffer)) > 0);
        buffer.flip();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] nameBytes = new byte[NAME_BYTES_SIZE];
        buffer.get(nameBytes);
        fullPath = readNulTerminatedStr(nameBytes).substring(1);    //  Remove leading \
        String[] tmp = fullPath.split("\\\\");
        fileName = tmp[tmp.length - 1];
        diskSize = Integer.toUnsignedLong(buffer.getInt());
        decompressedSize = Integer.toUnsignedLong(buffer.getInt());
        compressedSize = Integer.toUnsignedLong(buffer.getInt());
        diskSize = Integer.toUnsignedLong(buffer.getInt());
        unknown = buffer.getInt();
        return this;
    }

    @Override
    public String toString() {
        return "FileEntry{" +
                "fullPath='" + fullPath + '\'' +
                ", diskSize=" + diskSize +
                ", decompressedSize=" + decompressedSize +
                ", compressedSize=" + compressedSize +
                ", diskOffset=" + diskOffset +
                ", unknown=" + unknown +
                '}';
    }

    /**
     * Gets the full path to this file, which includes the root directory (\) and the file name at the end
     * @return The full path to this file, starting with root (\)
     */
    public String getFullPath() {
        return fullPath;
    }


    /**
     * Gets the size of the file on disk/inside the PakFile, in bytes
     * @return The size of the file, in bytes
     */
    public long getDiskSize() {
        return diskSize;
    }

    /**
     * Gets the size of the file when decompressed, in bytes
     * @return The size of the decompressed file, in bytes
     */
    public long getDecompressedSize() {
        return decompressedSize;
    }

    /**
     * Gets the size of the file when compressed, in bytes. This is the same as the size of the file on disk.
     * @return The size of the compressed file, in bytes.
     * @see FileInfo#getDiskSize()
     */
    public long getCompressedSize() {
        return compressedSize;
    }

    /**
     * Gets the location of the compressed file within the PakFile, offset in bytes
     * @return The location of the compressed file within the PakFile, in bytes
     */
    public long getDiskOffset() {
        return diskOffset;
    }

    /**
     * Gets the unknown value. Might possibly be some sort of checksum or other validation?
     * @return The unknown value
     */
    public int getUnknown() {
        return unknown;
    }

    /**
     * Gets the name of this file. The filename is determined by taking the portion of the path after the last
     * non-trailing backslash and stripping any trailing backslashes.
     * @return The name of this file, which is the path after the last non-trailing backslash (\)
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * <b>Method only for testing</b>
     * <p>
     * Sets the full path
     * @param fullPath The full path to the file
     */
    void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    /**
     * M<b>Method only for testing</b>
     * <p>
     * Sets the filename
     * @param fileName The name of the file
     */
    void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
