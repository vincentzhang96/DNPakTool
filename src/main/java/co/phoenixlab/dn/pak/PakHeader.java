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
import static java.lang.Integer.*;

public class PakHeader {

    /** {@value} bytes, the size of the entire header. */
    public static final int HEADER_SIZE = 1024;
    /** The magic word "{@value}" at the beginning of all PakFiles */
    public static final String MAGIC_WORD = "EyedentityGames Packing File 0.1";
    /** {@value}, The unknown constant in the header */
    public static final int UNKNOWN_CONST = 0x0B;
    /** {@value} bytes, the size of the magic word text field (including nulls)*/
    public static final int MAGIC_WORD_SIZE = 256;

    /**
     * The magic word
     * @see #MAGIC_WORD
     */
    protected String magic;
    /**
     * The unknown field
     * @see #UNKNOWN_CONST
     */
    protected int unknown;
    /**
     * The number of files in this PakFile
     */
    protected long numFiles;
    /**
     * The byte position of the file table
     */
    protected long fileTableOffset;

    /**
     * Reads the header from a PakFile through the given RandomAccessFile. The RandomAccessFile must be positioned at
     * the start of the logical file.
     * @param randomAccessFile The RandomAccessFile to read from, positioned at offset 0.
     * @throws IOException If there was an error reading the header
     * @throws InvalidPakException If the header is invalid
     */
    public void read(RandomAccessFile randomAccessFile) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        FileChannel fileChannel = randomAccessFile.getChannel();
        int read;
        //noinspection StatementWithEmptyBody
        while ((read = fileChannel.read(buffer)) > 0) ;
        if (read == -1) {
            throw new InvalidPakException("Unexpected EOF");
        }
        buffer.flip();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] magicBytes = new byte[MAGIC_WORD_SIZE];
        buffer.get(magicBytes);
        magic = readNulTerminatedStr(magicBytes);
//        if (!MAGIC_WORD.equals(magic)) {
//            throw new InvalidPakException("Magic word does not match");
//        }
        unknown = buffer.getInt();
        if (unknown != UNKNOWN_CONST) {
            throw new InvalidPakException("Unknown Const does not match");
        }
        numFiles = toUnsignedLong(buffer.getInt());
        if (numFiles < 0L || numFiles > 0xFFFFFFFFL) {
            throw new InvalidPakException(String.format("Invalid number of files: 0x%016X%n", numFiles));
        }
        fileTableOffset = toUnsignedLong(buffer.getInt());
        if (fileTableOffset < 0x400L || fileTableOffset > 0xFFFFFFFFL) {
            throw new InvalidPakException(String.format("Invalid file table offset: 0x%016X%n", fileTableOffset));
        }
    }

    /**
     * Gets the magic word. A valid PakFile will always return {@link #MAGIC_WORD}, since an invalid one
     * will fail at {@link PakHeader#read(RandomAccessFile)} with an {@link InvalidPakException}.
     * @return The magic word {@value #MAGIC_WORD}
     */
    public String getMagic() {
        return magic;
    }

    /**
     * Gets the number of files in this PakFile
     * @return The number of files in this PakFile
     */
    public long getNumFiles() {
        return numFiles;
    }

    /**
     * Gets the offset to this PakFile's file table
     * @return The location of the file table
     */
    public long getFileTableOffset() {
        return fileTableOffset;
    }
}
