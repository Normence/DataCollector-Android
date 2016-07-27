package com.github.pires.obd.commands;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.powermock.api.easymock.PowerMock.*;
import static org.testng.Assert.assertEquals;

/**
 * Tests for ObdSpeedCommand class.
 */
@PrepareForTest(InputStream.class)
public class SpeedCommandTest {

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
     * Test for valid InputStream read, 64km/h
     *
     * @throws IOException
     */
    @Test
    public void testValidSpeedMetric() throws IOException {
        // mock InputStream read
        mockIn = createMock(InputStream.class);
        mockIn.read();
        expectLastCall().andReturn((byte) '4');
        expectLastCall().andReturn((byte) '1');
        expectLastCall().andReturn((byte) ' ');
        expectLastCall().andReturn((byte) '0');
        expectLastCall().andReturn((byte) 'D');
        expectLastCall().andReturn((byte) ' ');
        expectLastCall().andReturn((byte) '4');
        expectLastCall().andReturn((byte) '0');
        expectLastCall().andReturn((byte) '>');

        replayAll();

        // call the method to test
        command.readResult(mockIn);
        command.getFormattedResult();
        assertEquals(command.getMetricSpeed(), 64);

        verifyAll();
    }

    /**
     * Test for valid InputStream read, 42.87mph
     *
     * @throws IOException
     */
    @Test
    public void testValidSpeedImperial() throws IOException {
        // mock InputStream read
        mockIn = createMock(InputStream.class);
        mockIn.read();
        expectLastCall().andReturn((byte) '4');
        expectLastCall().andReturn((byte) '1');
        expectLastCall().andReturn((byte) ' ');
        expectLastCall().andReturn((byte) '0');
        expectLastCall().andReturn((byte) 'D');
        expectLastCall().andReturn((byte) ' ');
        expectLastCall().andReturn((byte) '4');
        expectLastCall().andReturn((byte) '5');
        expectLastCall().andReturn((byte) '>');

        replayAll();

        // call the method to test
        command.readResult(mockIn);
        command.useImperialUnits = true;
        command.getFormattedResult();
        assertEquals(command.getImperialSpeed(), 42.87461f);

        verifyAll();
    }

    /**
     * Test for valid InputStream read, 0km/h
     *
     * @throws IOException
     */
    @Test
    public void testZeroSpeedMetric() throws IOException {
        // mock InputStream read
        mockIn = createMock(InputStream.class);
        mockIn.read();
        expectLastCall().andReturn((byte) '4');
        expectLastCall().andReturn((byte) '1');
        expectLastCall().andReturn((byte) ' ');
        expectLastCall().andReturn((byte) '0');
        expectLastCall().andReturn((byte) 'D');
        expectLastCall().andReturn((byte) ' ');
        expectLastCall().andReturn((byte) '0');
        expectLastCall().andReturn((byte) '0');
        expectLastCall().andReturn((byte) '>');

        replayAll();

        // call the method to test
        command.readResult(mockIn);
        command.getFormattedResult();
        assertEquals(command.getMetricSpeed(), 0);

        verifyAll();
    }

    /**
     * Clear resources.
     */
    @AfterClass
    public void tearDown() {
        command = null;
        mockIn = null;
    }

}
