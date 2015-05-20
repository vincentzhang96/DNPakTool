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

public class FileEntryTest {

    @Test
    public void testCompareTo() throws Exception {
        FileEntry a = new FileEntry("a", null, null);
        FileEntry b = new FileEntry("b", null, null);
        Assert.assertTrue(a.compareTo(b) < 0);
        Assert.assertTrue(b.compareTo(a) > 0);
    }

    @Test
    public void testCompareToSelf() throws Exception {
        FileEntry fileEntry = new FileEntry("test", null, null);
        Assert.assertEquals(0, fileEntry.compareTo(fileEntry));
    }

    @Test
    public void testCompareToCaps() throws Exception {
        FileEntry a = new FileEntry("A", null, null);
        FileEntry b = new FileEntry("b", null, null);
        Assert.assertTrue(a.compareTo(b) < 0);
        Assert.assertTrue(b.compareTo(a) > 0);
    }

    @Test(expected = NullPointerException.class)
    public void testCompareToNull() throws Exception {
        FileEntry fileEntry = new FileEntry("test", null, null);
        fileEntry.compareTo(null);
    }
}
