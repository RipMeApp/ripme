package com.rarchives.ripme.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransferRateTest {

    @Test
    void formatHumanTransferRate() {
        //TransferRate transferRate = new TransferRate();
        TransferRate transferRate = Mockito.spy();

        Mockito.when(transferRate.calculateBytesPerSecond()).thenReturn(999.);
        Assertions.assertEquals("999.00 B/s", transferRate.formatHumanTransferRate());

        Mockito.when(transferRate.calculateBytesPerSecond()).thenReturn(1013.);
        Assertions.assertEquals("0.99 KiB/s", transferRate.formatHumanTransferRate());

        Mockito.when(transferRate.calculateBytesPerSecond()).thenReturn(1024.0 * 1024);
        Assertions.assertEquals("1.00 MiB/s", transferRate.formatHumanTransferRate());

        Mockito.when(transferRate.calculateBytesPerSecond()).thenReturn(1024.0 * 1013);
        Assertions.assertEquals("0.99 MiB/s", transferRate.formatHumanTransferRate());

        Mockito.when(transferRate.calculateBytesPerSecond()).thenReturn(1024.0 * 1024 * 1024);
        Assertions.assertEquals("1.00 GiB/s", transferRate.formatHumanTransferRate());

        Mockito.when(transferRate.calculateBytesPerSecond()).thenReturn(1024.0 * 1024 * 1013);
        Assertions.assertEquals("0.99 GiB/s", transferRate.formatHumanTransferRate());

        Mockito.when(transferRate.calculateBytesPerSecond()).thenReturn(1024.0 * 1024 * 1024 * 1013);
        Assertions.assertEquals("1013.00 GiB/s", transferRate.formatHumanTransferRate());
    }
}
