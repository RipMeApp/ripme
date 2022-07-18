package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.ElitebabesRipper;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Test;

public class ElitebabesRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        final ElitebabesRipper ripper = new ElitebabesRipper(
                new URL("https://www.elitebabes.com/top-class-babe-genesys-rests-her-amazing-body-by-the-pool-as-she-takes-off-her-bikini-67484/"));
        testRipper(ripper);
    }
}
