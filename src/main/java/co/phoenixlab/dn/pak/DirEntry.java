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

import java.util.HashMap;
import java.util.Map;

public class DirEntry extends Entry implements Comparable<DirEntry> {

    private final Map<String, Entry> children = new HashMap<>();

    public DirEntry(String name, Entry parent) {
        super(name, parent);
    }

    public Map<String, Entry> getChildren() {
        return children;
    }

    public void insert(String path, FileInfo fileInfo) {
        if (path.startsWith("\\")) {
            path = path.substring(1);
        }
        String[] strs = path.split("\\\\", 2);
        if (strs.length == 1) {
            children.put(strs[0], new FileEntry(fileInfo.getFileName(), parent, fileInfo));
            return;
        }
        Entry newEntry = children.get(strs[0]);
        DirEntry dirEntry;
        if (newEntry instanceof DirEntry) {
            dirEntry = (DirEntry) newEntry;
        } else if (newEntry != null) {
            throw new IllegalArgumentException("Cannot replace an existing file with a directory");
        } else {
            dirEntry = new DirEntry(strs[0], parent);
            children.put(strs[0], dirEntry);
        }
        dirEntry.insert(strs[1], fileInfo);
    }

    public Entry get(String path) {
        String[] strs = path.split("\\\\", 2);
        Entry entry = children.get(strs[0]);
        if (strs.length == 1) {
            return entry;
        }
        if (entry instanceof DirEntry) {
            return ((DirEntry) entry).get(strs[1]);
        } else {
            throw new IllegalArgumentException("Cannot get a child of a file (leaf) node");
        }
    }

    @Override
    public int compareTo(DirEntry o) {
        return this.name.compareToIgnoreCase(o.name);
    }
}
