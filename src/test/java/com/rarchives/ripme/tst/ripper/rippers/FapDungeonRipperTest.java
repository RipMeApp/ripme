package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.FapDungeonRipper;
import org.junit.jupiter.api.Test;

public class FapDungeonRipperTest extends RippersTest {
    @Test
    public void testFapDungeon1() throws IOException, URISyntaxException {
        FapDungeonRipper ripper = new FapDungeonRipper(new URI("https://fapdungeon.com/white/thegorillagrip-busty-cutie-onlyfans-nudes/").toURL());
        testRipper(ripper);
    }

    @Test
    public void testFapDungeon2() throws IOException, URISyntaxException {
        FapDungeonRipper ripper = new FapDungeonRipper(new URI("https://fapdungeon.com/asian/joythailia-sexy-asian-petite-onlyfans-nudes/").toURL());
        testRipper(ripper);
    }

    @Test
    public void testFapDungeon3() throws IOException, URISyntaxException {
        FapDungeonRipper ripper = new FapDungeonRipper(new URI("https://fapdungeon.com/black/jaaden-kyrelle-busty-ebony-onlyfans-sextapes-nudes/").toURL());
        testRipper(ripper);
    }
}
