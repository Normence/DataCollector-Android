package com.github.pires.obd.exceptions;

import com.github.pires.obd.commands.SpeedCommand;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.powermock.api.easymock.PowerMock.*;

/**
 * Test results with echo on and off.
 */
@PrepareForTest(InputStream.class)
public class BusInitExceptionTest {

    private SpeedCommand command;
    private InputStream mockIn;

    /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        command = new SpeedCommand();
    }

    /**
     * Test for valid InputStream read with echo
     *
     * @throws java.io.IOException, java.lang.InterruptedException
     */
    @Test(expectedExceptions = BusInitException.class)
    public void testValidSpeedMetricWithMessage() throws IOException, InterruptedException {
        // mock InputStream read
        mockIn = createMock(InputStream.class);
        mockIn.read();
        expectLastCall().andReturn((byte) 'B');
        expectLastCall().andReturn((byte) 'U');
        expectLastCall().andReturn((byte) 'S');
        expectLastCall().andReturn((byte) ' ');
        expectLastCall().andReturn((byte) 'I');
        expectLastCall().andReturn((byte) 'N');
        expectLastCall().andReturn((byte) 'I');
        expectLastCall().andReturn((byte) 'T');
        expectLastCall().andReturn((byte) '.');
        expectLastCall().andReturn((byte) '.');
        expectLastCall().andReturn((byte) '.');
        expectLastCall().andReturn((byte) ' ');
        expectLastCall().andReturn((byte) 'E');
        expectLastCall().andReturn((byte) 'R');
        expectLastCall().andReturn((byte) 'R');
        expectLastCall().andReturn((byte) 'O');
        expectLastCall().andReturn((byte) 'R');
        expectLastCall().andReturn((byte) '>');

        replayAll();

        // call the method to test
        command.run(mockIn, new ByteArrayOutputStream());

        verifyAll();
    }

}
