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

/**
 * Represents a generic entry in the PakFile file structure.
 */
@SuppressWarnings("WeakerAccess")
public abstract class Entry {

    /** The name of this Entry. This is NOT the full path */
    public final String name;
    /** The Entry that "owns" or contains this Entry */
    public final Entry parent;

    /**
     * Constructs an Entry with given name and parent. The parent cannot be a FileEntry.
     * @param name The name for this Entry
     * @param parent The parent for this Entry, cannot be a FileEntry
     * @throws IllegalArgumentException If the parent is a FileEntry
     */
    public Entry(String name, Entry parent) {
        this.name = name;
        this.parent = parent;
        if (parent instanceof FileEntry) {
            throw new IllegalArgumentException("FileEntry cannot be a parent");
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Entry)) {
            return false;
        }

        Entry entry = (Entry) o;

        return name.equals(entry.name);
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }
}
