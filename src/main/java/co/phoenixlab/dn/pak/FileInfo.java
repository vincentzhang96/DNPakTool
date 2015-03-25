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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static co.phoenixlab.dn.pak.Util.*;

public class FileInfo {

    private static final int NUL_PADDING_SIZE = 40;
    public static final int NAME_BYTES_SIZE = 256;
    public static final int FILE_INFO_SIZE = 316;

    private static final byte[] EMPTY = new byte[NAME_BYTES_SIZE];

    private String fullPath;
    private String fileName;
    private long diskSize;
    private long decompressedSize;
    private long compressedSize;
    private long diskOffset;
    private int unknown;

    public FileInfo load(RandomAccessFile randomAccessFile) throws IOException {
        byte[] nameBytes = new byte[NAME_BYTES_SIZE];
        randomAccessFile.readFully(nameBytes);
        fullPath = readNulTerminatedStr(nameBytes).substring(1);    //  Remove leading \
        String[] tmp = fullPath.split("\\\\");
        fileName = tmp[tmp.length - 1];
        diskSize = fromUint32(randomAccessFile.readInt());
        decompressedSize = fromUint32(randomAccessFile.readInt());
        compressedSize = fromUint32(randomAccessFile.readInt());
        diskOffset = fromUint32(randomAccessFile.readInt());
        unknown = reverseBytes(randomAccessFile.readInt());
        randomAccessFile.skipBytes(NUL_PADDING_SIZE);
        return this;
    }

    public void write(ByteBuffer buffer) {
        byte[] str = getFullPath().getBytes(StandardCharsets.UTF_8);
        int pad = NAME_BYTES_SIZE - str.length;
        buffer.put(str);
        buffer.put(EMPTY, 0, pad);
        buffer.putInt((int) diskSize);
        buffer.putInt((int) decompressedSize);
        buffer.putInt((int) compressedSize);
        buffer.putInt((int) diskOffset);
        buffer.putInt(0x27);    //  I have no idea why, but that's what I saw in a mod and it works for them
        buffer.put(EMPTY, 0, NUL_PADDING_SIZE);
        buffer.flip();
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

    public String getFullPath() {
        return fullPath;
    }

    public long getDiskSize() {
        return diskSize;
    }

    public long getDecompressedSize() {
        return decompressedSize;
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    public long getDiskOffset() {
        return diskOffset;
    }

    public int getUnknown() {
        return unknown;
    }

    public String getFileName() {
        return fileName;
    }

    protected void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    protected void setFileName(String fileName) {
        this.fileName = fileName;
    }

    protected void setDiskSize(long diskSize) {
        this.diskSize = diskSize;
    }

    protected void setDecompressedSize(long decompressedSize) {
        this.decompressedSize = decompressedSize;
    }

    protected void setCompressedSize(long compressedSize) {
        this.compressedSize = compressedSize;
    }

    protected void setDiskOffset(long diskOffset) {
        this.diskOffset = diskOffset;
    }

    protected void setUnknown(int unknown) {
        this.unknown = unknown;
    }
}
