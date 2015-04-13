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

import org.junit.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class UtilTest {

    @Test
    public void testReadNulTerminatedStr() throws Exception {
        String string = "This is a short string.";
        byte[] bytes = new byte[256];
        byte[] working;
        String ret;

        Arrays.fill(bytes, (byte) 0);
        working = string.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(working, 0, bytes, 0, working.length);
        ret = Util.readNulTerminatedStr(bytes);
        Assert.assertEquals(string, ret);
    }

    @Test
    public void testReadNulTerminatedStrWithEmptyStr() throws Exception {
        byte[] bytes = new byte[256];
        String ret;

        Arrays.fill(bytes, (byte) 0);
        ret = Util.readNulTerminatedStr(bytes);
        Assert.assertTrue(ret.isEmpty());
    }

    @Test
    public void testReadNulTerminatedStrWithStrWithNull() throws Exception {
        String string = "This is a short string.";
        String nulString = string + "\0\0\0" + "moo";
        byte[] bytes = new byte[256];
        byte[] working;
        String ret;

        Arrays.fill(bytes, (byte) 0);
        working = nulString.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(working, 0, bytes, 0, working.length);
        ret = Util.readNulTerminatedStr(bytes);
        Assert.assertEquals(string, ret);
    }

    @Test
    public void testReadNulTerminatedStrEndOfArray() throws Exception {
        byte[] bytes = new byte[256];
        byte[] working;
        String ret;

        Arrays.fill(bytes, (byte) 'a');
        ret = Util.readNulTerminatedStr(bytes);
        working = ret.getBytes(StandardCharsets.UTF_8);
        Assert.assertArrayEquals(bytes, working);
    }
}
