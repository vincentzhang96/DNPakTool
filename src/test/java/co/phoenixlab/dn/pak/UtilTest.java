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

import java.io.DataInput;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UtilTest {

    @Test
    public void testReadNulTerminatedStr() throws Exception {

    }

    @Test
    public void testReadNulTerminatedStr1() throws Exception {

    }

    @Test
    public void testReadUint32() throws Exception {
        int val = 0x12345678;
        long exp = 0x78563412L;
        DataInput dataInput = mock(DataInput.class);
        when(dataInput.readInt()).thenReturn(val);
        long ret = Util.fromUint32(dataInput.readInt());
        assertEquals(exp, ret);
    }

    @Test
    public void testReverseBytes() throws Exception {
        final int val = 0x12345678;
        final int exp = 0x78563412;
        assertEquals(exp, Util.reverseBytes(val));
    }
}
