/*
 * The MIT License
 *
 * Copyright 2018 Kevin Jiang <kevin51jiang (at) email.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.ThechiveRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Kevin Jiang <kevin51jiang (at) email.com>
 */
public class ThechiveRipperTest extends RippersTest {

    /**
     * Tests general ripping for The Chive
     *
     * @throws IOException
     */
    @Test
    @Tag("flaky")
    public void testTheChiveRip() throws IOException, URISyntaxException {
        ThechiveRipper ripper = new ThechiveRipper(new URI(
                "https://thechive.com/2019/03/16/beautiful-badasses-lookin-good-in-and-out-of-uniform-35-photos/").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testTheChiveGif() throws IOException, URISyntaxException {
        ThechiveRipper ripper = new ThechiveRipper(
                new URI("https://thechive.com/2019/03/14/dont-tease-me-just-squeeze-me-20-gifs/").toURL());
        testRipper(ripper);
    }

    /*
     * "i.thechive.com" test.
     */
    @Test
    @Tag("flaky")
    public void testIDotThechive() throws IOException, URISyntaxException {
        ThechiveRipper ripper = new ThechiveRipper(new URI("https://i.thechive.com/witcheva").toURL());
        testRipper(ripper);
    }

    /*
     * 
     * //If anyone figures out how to get JSOUP Elements mocked up, we can use the
     * following methods to test both jpeg + gif ripping.
     */
    @Test
    @Disabled
    public void testGifRip1() throws IOException {
        String elementInString = "<img width=\"500\" height=\"305\" \n src=\"https://thechive.files.wordpress.com/2018/10/american_mary_crimson_quill-111.jpg?quality=85&amp;strip=info\" \n"
                + "class=\"attachment-gallery-item-full size-gallery-item-full gif-animate\" \n"
                + "alt=\"american mary crimson quill 111 The hottest horror movie villains ever according to science (18 Photos)\" \n"
                + "title=\"\" data-gifsrc=\"https://thechive.files.wordpress.com/2018/10/american_mary_crimson_quill-1.gif?w=500\">";

        // Element el = new Element(new Tag("img"), "", new Attributes());
        // String URL = ThechiveRipper.getImageSource(el);
        // assertTrue(URL.equals("https://thechive.files.wordpress.com/2018/10/american_mary_crimson_quill-1.gif"));
    }

    @Test
    @Disabled
    public void testGifRip2() throws IOException {
        String elementInString = "<img width=\"600\" height=\"409\" src=\"https://thechive.files.wordpress.com/2018/10/the-definitive-list-of-the-hottest-horror-movie-babes-11.jpg?quality=85&amp;strip=info&amp;w=600\" \n"
                + "class=\"attachment-gallery-item-full size-gallery-item-full\" \n"
                + "alt=\"the definitive list of the hottest horror movie babes 11 The hottest horror movie villains ever according to science (18 Photos)\" title=\"\">";

        // Element el = new Element( new Tag("img"), "", new Attributes());
        // String URL = ThechiveRipper.getImageSource(el);
        // assertTrue(URL.equals("https://thechive.files.wordpress.com/2018/10/the-definitive-list-of-the-hottest-horror-movie-babes-11.jpg"));
    }

}
