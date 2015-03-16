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

import java.io.DataInput;
import java.io.IOException;

public class Util {

    private Util() {
    }

    public static String readNulTerminatedStr(DataInput dataIn) throws IOException {
        StringBuilder builder = new StringBuilder();
        int i;
        while((i = dataIn.readUnsignedByte()) > 0) {
            builder.append((char) i);
        }
        return builder.toString();
    }

    public static String readNulTerminatedStr(byte[] data) {
        StringBuilder builder = new StringBuilder();
        int i;
        int j = 0;
        while((i = data[j]) > 0) {
            builder.append((char) i);
            ++j;
        }
        return builder.toString();
    }

    public static long readUint32(DataInput dataInput) throws IOException {
        return Integer.toUnsignedLong(reverseBytes(dataInput.readInt()));
    }

    public static int reverseBytes(int i) {
        return ((i & 0xFF) << 24) | ((i & 0xFF00) << 8 | ((i & 0xFF0000) >>> 8) | ((i & 0xFF000000) >>> 24));
    }

}
