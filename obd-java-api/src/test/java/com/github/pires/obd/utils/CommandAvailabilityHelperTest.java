package com.github.pires.obd.utils;

import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CommandAvailabilityHelperTest {

    @Test
    public void testDigestAvailabilityString() throws Exception {
        int[] expected = new int[]{Integer.parseInt("10111110", 2), Integer.parseInt("00011111", 2),
                Integer.parseInt("10101000", 2), Integer.parseInt("00010011", 2)};
        int[] result = CommandAvailabilityHelper.digestAvailabilityString("BE1FA813");
        Assert.assertEquals(expected, result);

        //Now with 16 characters
        expected = new int[]{Integer.parseInt("10111110", 2), Integer.parseInt("00011111", 2),
                Integer.parseInt("10101000", 2), Integer.parseInt("00010011", 2),
                Integer.parseInt("10111110", 2), Integer.parseInt("00011111", 2),
                Integer.parseInt("10101000", 2), Integer.parseInt("00010011", 2)};

        result = CommandAvailabilityHelper.digestAvailabilityString("BE1FA813BE1FA813");
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testIsAvailable() throws Exception {
        Assert.assertEquals(CommandAvailabilityHelper.isAvailable("02", "BE1FA813"), false);
        Assert.assertEquals(CommandAvailabilityHelper.isAvailable("07", "BE1FA813"), true);
        Assert.assertEquals(CommandAvailabilityHelper.isAvailable(new ThrottlePositionCommand().getCommandPID() /*11*/, "BE1FA813"), true);
        Assert.assertEquals(CommandAvailabilityHelper.isAvailable("1A", "BE1FA813"), false);
        Assert.assertEquals(CommandAvailabilityHelper.isAvailable("1D", "BE1FA813"), false);
        Assert.assertEquals(CommandAvailabilityHelper.isAvailable("1F", "BE1FA813"), true);
        Assert.assertEquals(CommandAvailabilityHelper.isAvailable("22", "BE1FA813BE1FA813"), false);
        Assert.assertEquals(CommandAvailabilityHelper.isAvailable("27", "BE1FA813BE1FA813"), true);
        Assert.assertEquals(CommandAvailabilityHelper.isAvailable("3A", "BE1FA813BE1FA813"), false);
        Assert.assertEquals(CommandAvailabilityHelper.isAvailable("3D", "BE1FA813BE1FA813"), false);
        Assert.assertEquals(CommandAvailabilityHelper.isAvailable("3F", "BE1FA813BE1FA813"), true);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFail() throws Exception {
        CommandAvailabilityHelper.digestAvailabilityString("AAA");
        CommandAvailabilityHelper.digestAvailabilityString("AAAAAAAR");
        CommandAvailabilityHelper.isAvailable("2F", "BE1FA813");
    }

}
