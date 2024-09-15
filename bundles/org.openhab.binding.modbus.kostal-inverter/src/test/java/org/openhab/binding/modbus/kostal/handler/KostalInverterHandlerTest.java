package org.openhab.binding.modbus.kostal.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class KostalInverterHandlerTest {

    @Test
    void testHardwareVersionToString() {
        int hardwareVersionRaw = 1537;
        String hardwareVersion = String.format("%02X%02X", hardwareVersionRaw & 0xFF, hardwareVersionRaw >> 8 & 0xFF);

        assertEquals("0106", hardwareVersion);
    }
}
