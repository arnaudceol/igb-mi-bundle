/* 
 * Copyright 2015 Fondazione Istituto Italiano di Tecnologia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.iit.genomics.cru.igb.bundles.mi.view;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author Arnaud Ceol
 *
 * Generate different shades of a selected color. Used to display the same
 * protein in different chains in Jmol.
 *
 */
public class ColourIterator {

    public enum ColourScheme {
        RED,
        BLUE,
        GREEN
    };

    private final static String[] greenColours = {"#BCED91", "#6A8455", "#397D02",
        "#567E3A", "#A6D785", "#687E5A", "#8AA37B"};

    private final static String[] blueColours = {"#528B8B", "#50A6C2", "#8FD8D8",
        "#70DBDB", "#8DEEEE", "#AEEEEE", "#73B1B7"};

    private final static String[] redColours = {"#FF0000", "#EE6363", "#FA8072", "#CC1100",
        "#A02422", "#EE2C2C", "#F08080"};

    private final static String[] greyColours = {"#AAAAAA", "#BBBBBB", "#CCCCCC", "#DDDDDD",
        "#EEEEEE", "#FFFFFF"};

    private ColourScheme scheme;

    private HashSet<String> colours = new HashSet<>();

    public ColourIterator(ColourScheme scheme) {

        this.scheme = scheme;

        String[] localColours;

        switch (scheme) {
            case RED:
                localColours = redColours;
                break;
            case GREEN:
                localColours = greenColours;
                break;
            case BLUE:
                localColours = blueColours;
                break;
            default:
                localColours = greyColours;
        }

        this.colours.addAll(Arrays.asList(localColours));
        
        colourIterator = this.colours.iterator();
    }

    private final Iterator<String> colourIterator;

    public String next() {
        if (colourIterator.hasNext()) {
            return colourIterator.next();
        }

        Random randomGenerator = new Random();

        int random = 150 + randomGenerator.nextInt(100);

        Color newColor;
        String hexaColor;

        switch (scheme) {
            case RED:
                newColor = new Color(random, 0, 0);
                hexaColor = "#" + Integer.toHexString(newColor.getRGB() & 0x00ffffff);
                break;
            case GREEN:
                newColor = new Color(0, random, 0);
                hexaColor = "#" + Integer.toHexString(newColor.getRGB() & 0x00ffffff);
                break;
            case BLUE:
                newColor = new Color(0, 0, random);
                hexaColor = "#" + Integer.toHexString(newColor.getRGB() & 0x00ffffff);
                break;
            default:
                newColor = new Color(random, random, random);
                hexaColor = "#" + Integer.toHexString(newColor.getRGB() & 0x00ffffff);
        }

        return hexaColor;

    }
}
