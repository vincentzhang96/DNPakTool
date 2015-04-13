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

class Segment implements Comparable<Segment> {

    final long start;
    final long length;
    final long end;

    Segment(long start, long length, long end) {
        if (start == -1) {
            this.start = end - length;
            this.length = length;
            this.end = end;
        } else if (length == -1) {
            this.start = start;
            this.length = end - start;
            this.end = end;
        } else if (end == -1) {
            this.start = start;
            this.length = length;
            this.end = start + length;
        } else {
            this.start = start;
            this.length = length;
            this.end = end;
        }
    }

    public Segment merge(Segment other) {
        if (other == null) {
            return null;
        }
        if (start <= other.start && end >= other.start) {
            return new Segment(start, -1, Math.max(end, other.end));
        }
        if (other.start <= start && other.end >= start) {
            return new Segment(other.start, -1, Math.max(end, other.end));
        }
        return null;
    }

    @Override
    public int compareTo(Segment o) {
        return Long.compareUnsigned(start, o.start);
    }

    @Override
    public String toString() {
        return start + "\t" + end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Segment segment = (Segment) o;

        return start == segment.start && length == segment.length && end == segment.end;

    }

    @Override
    public int hashCode() {
        int result = (int) (start ^ (start >>> 32));
        result = 31 * result + (int) (length ^ (length >>> 32));
        result = 31 * result + (int) (end ^ (end >>> 32));
        return result;
    }
}
