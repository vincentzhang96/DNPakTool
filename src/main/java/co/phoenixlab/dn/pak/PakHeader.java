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

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import static co.phoenixlab.dn.pak.Util.*;

public class PakHeader {

    public static final String MAGIC_WORD = "EyedentityGames Packing File 0.1";
    public static final int UNKNOWN_CONST = 0x0B;

    protected String magic;
    protected int unknown;
    protected long numFiles;
    protected long fileTableOffset;

    public PakHeader read(RandomAccessFile randomAccessFile) throws IOException {
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
            numFiles = Integer.toUnsignedLong(reverseBytes(randomAccessFile.readInt()));
            if (numFiles < 0L || numFiles > 0xFFFFFFFFL) {
                throw new InvalidPakException("Invalid number of files");
            }
            fileTableOffset = Integer.toUnsignedLong(reverseBytes(randomAccessFile.readInt()));
            if (numFiles < 0x400L || numFiles > 0xFFFFFFFFL) {
                throw new InvalidPakException("Invalid file table offset");
            }
        } catch (EOFException eof) {
            throw new InvalidPakException("Unexpected EOF", eof);
        }
        return this;
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
