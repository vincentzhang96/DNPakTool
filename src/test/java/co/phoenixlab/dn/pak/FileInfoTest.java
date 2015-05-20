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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class FileInfoTest {

    private ByteBuffer data;
    private final String filePath = "\\resource\\sound\\littledemon\\littledemon_attack1_hit_b.wav";
    private final String strippedFilePath = filePath.substring(1);
    private final String fileName = "littledemon_attack1_hit_b.wav";
    private final int diskSize = 0x712F;
    private final int realSize = 0xA980;
    private final int compressedSize = diskSize;
    private final int diskOffset = 0x1FFFDDD3;
    private final int unknown = 0x18;


    @Before
    public void setUp() throws Exception {
        data = ByteBuffer.allocate(512);
        data.order(ByteOrder.LITTLE_ENDIAN);
        byte[] nameBytes = new byte[256];
        byte[] str = filePath.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(str, 0, nameBytes, 0, str.length);
        data.put(nameBytes);
        data.putInt(diskSize);
        data.putInt(realSize);
        data.putInt(compressedSize);
        data.putInt(diskOffset);
        data.putInt(unknown);
        data.put(new byte[40]);
        data.flip();
    }

    @Test
    public void testLoad() throws Exception {
        FileInfo fileInfo = new FileInfo();
        fileInfo.load(data);
        assertEquals(strippedFilePath, fileInfo.getFullPath());
        assertEquals(fileName, fileInfo.getFileName());
        assertEquals(diskSize, fileInfo.getDiskSize());
        assertEquals(realSize, fileInfo.getDecompressedSize());
        assertEquals(compressedSize, fileInfo.getCompressedSize());
        assertEquals(diskOffset,  fileInfo.getDiskOffset());
        assertEquals(unknown, fileInfo.getUnknown());
    }
}
