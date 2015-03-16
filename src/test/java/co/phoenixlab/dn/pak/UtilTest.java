package co.phoenixlab.dn.pak;

import org.junit.*;

import java.io.DataInput;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UtilTest {

    @Test
    public void testReadNulTerminatedStr() throws Exception {

    }

    @Test
    public void testReadNulTerminatedStr1() throws Exception {

    }

    @Test
    public void testReadUint32() throws Exception {
        int val = 0x12345678;
        long exp = 0x78563412L;
        DataInput dataInput = mock(DataInput.class);
        when(dataInput.readInt()).thenReturn(val);
        long ret = Util.readUint32(dataInput);
        assertEquals(exp, ret);
    }

    @Test
    public void testReverseBytes() throws Exception {
        final int val = 0x12345678;
        final int exp = 0x78563412;
        assertEquals(exp, Util.reverseBytes(val));
    }
}
