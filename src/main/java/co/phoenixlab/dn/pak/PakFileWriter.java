/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vincent Zhang/PhoenixLAB
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
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class PakFileWriter {

    private final Path resourceRoot;
    private final Path outputPak;
    private final Deflater deflater;
    private final Map<Path, FileInfo> files;
    private RandomAccessFile raf;

    public PakFileWriter(Path resourceRoot, Path outputPak) {
        this.resourceRoot = resourceRoot;
        this.outputPak = outputPak;
        this.deflater = new Deflater();
        this.files = new HashMap<>();
    }

    public void write() throws IOException {
        raf = new RandomAccessFile(outputPak.toFile(), "rwd");
        //  Autoclosing the channel will also close the RAF
        try (FileChannel channel = raf.getChannel()) {
            //  Skip header
            channel.position(PakHeader.HEADER_SIZE);
            //  Write files
            try (Stream<Path> stream = Files.walk(resourceRoot)) {
                stream.forEach(this::writeFile);
            }
            //  Write header

            //  Write index

        } finally {
            files.clear();
        }
    }

    private void writeFile(Path file) {
        try {
            //  DO NOT AUTOCLOSE - THIS WILL CLOSE THE RANDOM ACCESS FILE
            FileChannel channel = raf.getChannel();
            String fn = file.getFileName().toString();
            String path = relativize(resourceRoot, file);
            long size = Files.size(file);
            long startPos = channel.position();
            deflater.reset();
            byte[] in = Files.readAllBytes(file);
            //  DO NOT CLOSE
            OutputStream out = Channels.newOutputStream(channel);
            //  DO NOT CLOSE
            DeflaterOutputStream dos = new DeflaterOutputStream(out, deflater);
            dos.write(in);
            dos.flush();
            long endPos = channel.position();
            FileInfo fileInfo = new FileInfo();
            long compressedSize = endPos - startPos;
            fileInfo.setCompressedSize(compressedSize);
            fileInfo.setDecompressedSize(size);
            fileInfo.setDiskOffset(startPos);
            fileInfo.setDiskSize(compressedSize);
            fileInfo.setFileName(fn);
            fileInfo.setFullPath(path);
            fileInfo.setUnknown(0);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String relativize(Path base, Path path) {
        return "\\" + base.relativize(path).toString().replace('/', '\\');
    }

}
