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

import java.util.Map;

import static org.junit.Assert.*;

public class DirEntryTest {

    DirEntry dirEntry;
    String path;
    FileInfo fileInfo;
    String path1;
    FileInfo fileInfo1;

    @Before
    public void setUp() throws Exception {
        dirEntry = new DirEntry("\\", null);
        fileInfo = new FileInfo();
        path = "\\test\\foo\\bar\\bay.baz\\qq.dds";
        fileInfo.setFileName("qq.dds");
        fileInfo.setFullPath(path);
        dirEntry.insert(path, fileInfo);
        fileInfo1 = new FileInfo();
        path1 = "\\moo.bar";
        fileInfo1.setFullPath(path1);
        fileInfo1.setFileName("moo.bar");
        dirEntry.insert(path1, fileInfo1);
    }

    @Test
    public void testGetChildren() throws Exception {
        Map<String, Entry> map = dirEntry.getChildren();
        assertTrue(map.get("test") instanceof DirEntry);
        assertTrue(map.get("moo.bar") instanceof FileEntry);
        assertSame(fileInfo1, ((FileEntry) map.get("moo.bar")).getFileInfo());
        assertNull(map.get("potato"));
    }

    @Test
    public void testInsertAndGet() throws Exception {
        //  This test passes if insert worked in setUp() and if get() works
        assertSame(fileInfo, ((FileEntry) dirEntry.get(path)).getFileInfo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertThrow() {
        dirEntry.insert(path1 + "\\bar", fileInfo1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetThrow() throws Exception {
        dirEntry.get(path1 + "\\bar");
    }

    @Test
    public void testGetNull() throws Exception {
        assertNull(dirEntry.get("\\potato\\tuber"));
    }

    @Test
    public void testCompareTo() throws Exception {
        DirEntry a = new DirEntry("a", null);
        DirEntry aa = new DirEntry("a", null);
        DirEntry b = new DirEntry("b", null);
        DirEntry bb = new DirEntry("B", null);
        assertTrue(a.compareTo(a) == 0);
        assertTrue(a.compareTo(aa) == 0);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertTrue(a.compareTo(bb) < 0);
        assertTrue(bb.compareTo(a) > 0);
        assertTrue(b.compareTo(bb) == 0);
        assertTrue(bb.compareTo(b) == 0);
    }
}
