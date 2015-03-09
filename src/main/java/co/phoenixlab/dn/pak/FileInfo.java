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

import static co.phoenixlab.dn.pak.Util.readNulTerminatedStr;
import static co.phoenixlab.dn.pak.Util.readUint32;
import static co.phoenixlab.dn.pak.Util.reverseBytes;

public class FileInfo {

    private static final int NUL_PADDING_SIZE = 40;

    private String fullPath;
    private String fileName;
    private long diskSize;
    private long decompressedSize;
    private long compressedSize;
    private long diskOffset;
    private int unknown;

    public FileInfo load(RandomAccessFile randomAccessFile) throws IOException {
        byte[] nameBytes = new byte[256];
        randomAccessFile.readFully(nameBytes);
        fullPath = readNulTerminatedStr(nameBytes).substring(1);    //  Remove leading \
        String[] tmp = fullPath.split("\\\\");
        fileName = tmp[tmp.length - 1];
        diskSize = readUint32(randomAccessFile);
        decompressedSize = readUint32(randomAccessFile);
        compressedSize = readUint32(randomAccessFile);
        diskOffset = readUint32(randomAccessFile);
        unknown = reverseBytes(randomAccessFile.readInt());
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
}
