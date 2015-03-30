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

import java.util.HashMap;
import java.util.Map;

public class DataRow {

    private Map<String, Object> entries;
    private Object[] entryArray;

    protected DataRow(String[] columnNames) {
        entries = new HashMap<>(columnNames.length);
        entryArray = new Object[columnNames.length];
        for (String s : columnNames) {
            entries.put(s, null);
        }
    }

    private <T> T getAs(String columnName, Class<T> tClass) {
        Object o = entries.get(columnName);
        try {
            T t = tClass.cast(o);
            return t;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Column " + columnName + " is not type " + tClass.getSimpleName());
        }
    }






}
