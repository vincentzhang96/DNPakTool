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

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

import static co.phoenixlab.dn.pak.Util.*;

public class PakHeader {

    public static final int HEADER_SIZE = 1024;
    public static final String MAGIC_WORD = "EyedentityGames Packing File 0.1";
    public static final byte[] MAGIC_WORD_BYTES = MAGIC_WORD.getBytes(StandardCharsets.UTF_8);
    public static final int UNKNOWN_CONST = 0x0B;
    public static final int NAME_BYTES_SIZE = 256;
    public static final int NUL_PADDING_SIZE = 756;
    private static final byte[] EMPTY = new byte[NUL_PADDING_SIZE];

    protected String magic;
    protected int unknown;
    protected long numFiles;
    protected long fileTableOffset;

    public void read(RandomAccessFile randomAccessFile) throws IOException {
        try {
            randomAccessFile.seek(0L);
            magic = readNulTerminatedStr(randomAccessFile);
            if (!MAGIC_WORD.equals(magic)) {
                throw new InvalidPakException("Magic word does not match");
            }
            randomAccessFile.seek(0x100L);
            unknown = reverseBytes(randomAccessFile.readInt());
            if (unknown != UNKNOWN_CONST) {
                throw new InvalidPakException("Unknown Const does not match");
            }
            numFiles = fromUint32(randomAccessFile.readInt());
            if (numFiles < 0L || numFiles > 0xFFFFFFFFL) {
                throw new InvalidPakException(String.format("Invalid number of files: 0x%016X%n", numFiles));
            }
            fileTableOffset = fromUint32(randomAccessFile.readInt());
            if (fileTableOffset < 0x400L || fileTableOffset > 0xFFFFFFFFL) {
                throw new InvalidPakException(String.format("Invalid file table offset: 0x%016X%n", fileTableOffset));
            }
        } catch (EOFException eof) {
            throw new InvalidPakException("Unexpected EOF", eof);
        }
    }

    public String getMagic() {
        return magic;
    }

    public long getNumFiles() {
        return numFiles;
    }

    public long getFileTableOffset() {
        return fileTableOffset;
    }
}
