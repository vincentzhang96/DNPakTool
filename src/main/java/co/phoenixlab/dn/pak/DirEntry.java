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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a directory in a PakFile. A DirEntry may contain other DirEntries and FileEntries.
 */
public class DirEntry extends Entry implements Comparable<DirEntry> {

    /** This DirEntry's children, can contain DirEntries and FileEntries */
    private final Map<String, Entry> children = new HashMap<>();

    /**
     * Constructs a DirEntry with the given name and parent. If the parent is null, then this is a root entry/node.
     * @param name The name of this DirEntry
     * @param parent The parent that contains this DirEntry, or null if this DirEntry is the root entry
     */
    public DirEntry(String name, Entry parent) {
        super(name, parent);
    }

    /**
     * Returns an unmodifiable map view of this DirEntry's children. After a PakFile is fully loaded and
     * indexed, the children do not change and for all intents and purposes may be considered immutable.
     * @return An unmodifiable map view of this DirEntry's children.
     */
    public Map<String, Entry> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    /**
     * Recursively inserts a FileInfo into a FileEntry in the proper DirEntry.
     * <p>
     * For instance, given the path "\a\b\c", this method will get or create a DirEntry with the name "a" and call
     * {@code a.insert("\b\c", fileInfo)}. Once at "b", "b" will then construct a FileEntry with the given FileInfo
     * and add it as a child, finishing the recursive insert operation.
     * <p>
     * The leading backslash is optional.
     * @param path The path to the desired insertion point, relative to this DirEntry
     * @param fileInfo The FileInfo to insert into a FileEntry once at its proper DirEntry
     * @return The FileEntry that was inserted
     */
    FileEntry insert(String path, FileInfo fileInfo) {
        if (path.startsWith("\\")) {
            path = path.substring(1);
        }
        String[] strs = path.split("\\\\", 2);
        if (strs.length == 1) {
            FileEntry entry =  new FileEntry(fileInfo.getFileName(), parent, fileInfo);
            children.put(strs[0], entry);
            return entry;
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
        return dirEntry.insert(strs[1], fileInfo);
    }

    /**
     * Recursively gets the specified Entry at path.
     * <p>
     * For instance, given the path "\a\b\c", this method will find the child entry named "a". If "a" is a DirEntry,
     * then it will call {@code a.get("\b\c"}. If "a" is not a DirEntry, then the method will fail with an
     * {@code IllegalArgumentException}. Once at "b", "b" will then return whatever "c" is: a DirEntry, a FileEntry, or
     * null (item does not exist).
     * <p>
     * The leading backslash is optional.
     * @param path The path to the entry, relative to this DirEntry
     * @return The Entry at path relative to this DirEntry, or null if no such entry exists
     * @throws IllegalArgumentException If the path requests the child of a file/leaf.
     */
    public Entry get(String path) {
        if (path.startsWith("\\")) {
            path = path.substring(1);
        }
        String[] strs = path.split("\\\\", 2);
        Entry entry = children.get(strs[0]);
        if (strs.length == 1) {
            return entry;
        }
        if (entry instanceof DirEntry) {
            return ((DirEntry) entry).get(strs[1]);
        } else if (entry == null) {
            return null;
        } else {
            throw new IllegalArgumentException("Cannot get a child of a file (leaf) node");
        }
    }

    @Override
    public int compareTo(DirEntry o) {
        return this.name.compareToIgnoreCase(o.name);
    }
}
