/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2015 Vincent Zhang/PhoenixLAB
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package co.phoenixlab.dn.dnt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DataRow {

    class Pair {
        final Object object;
        final Class<?> type;

        public Pair(Object object, Class<?> type) {
            this.object = object;
            this.type = type;
        }
    }

    private Map<String, Pair> entries;
    private Pair[] entryArray;

    protected DataRow(String[] columnNames) {
        entries = new HashMap<>(columnNames.length);
        entryArray = new Pair[columnNames.length];
        for (String s : columnNames) {
            entries.put(s, null);
        }
    }

    public Map<String, Pair> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    public Pair[] getEntryArray() {
        return entryArray;
    }

    public long getUint32(String columnName) {
        return getAs(columnName, Long.class);
    }

    public String getString(String columnName) {
        return getAs(columnName, String.class);
    }

    public boolean getBool(String columnName) {
        return getAs(columnName, Boolean.class);
    }

    public int getInt(String columnName) {
        return getAs(columnName, Integer.class);
    }

    public float getFloat(String columnName) {
        return getAs(columnName, Float.class);
    }
    
    public double getDouble(String columnName) {
        return getAs(columnName, Double.class);
    }

    public long getUint32(int columnNum) {
        return getAs(columnNum, Long.class);
    }

    public String getString(int columnNum) {
        return getAs(columnNum, String.class);
    }

    public boolean getBool(int columnNum) {
        return getAs(columnNum, Boolean.class);
    }

    public int getInt(int columnNum) {
        return getAs(columnNum, Integer.class);
    }

    public float getFloat(int columnNum) {
        return getAs(columnNum, Float.class);
    }

    public double getDouble(int columnNum) {
        return getAs(columnNum, Double.class);
    }

    public Class<?> getType(String columnName) {
        Pair pair = entries.get(columnName);
        if (pair == null) {
            throw new NoSuchElementException(columnName);
        }
        return pair.object.getClass();
    }

    public Class<?> getType(int columnNum) {
        Pair pair = entryArray[columnNum];
        if (pair == null) {
            throw new IllegalStateException("Column " + columnNum + " not initialized yet!");
        }
        return pair.object.getClass();
    }

    private <T> T getAs(String columnName, Class<T> tClass) {
        Pair pair = entries.get(columnName);
        if (pair == null) {
            throw new NoSuchElementException(columnName);
        }
        try {
            return tClass.cast(pair.object);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Column " + columnName + " is not type " + tClass.getSimpleName());
        }
    }

    private <T> T getAs(int columnNum, Class<T> tClass) {
        Pair pair = entryArray[columnNum];
        if (pair == null) {
            throw new IllegalStateException("Column " + columnNum + " not initialized yet!");
        }
        try {
            return tClass.cast(pair.object);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Column " + columnNum + " is not type " + tClass.getSimpleName());
        }
    }

    protected void put(String columnName, int columnNum, Object o) {
        Pair pair = new Pair(o, o.getClass());
        entries.put(columnName, pair);
        entryArray[columnNum] = pair;
    }





}
