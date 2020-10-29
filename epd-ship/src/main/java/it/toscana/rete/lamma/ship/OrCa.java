package it.toscana.rete.lamma.ship;

import dk.dma.epd.ship.EPDShip;

import java.io.IOException;

public final class OrCa extends EPDShip {

    /**
     * Constructor
     *
     * @param path the home path to use
     */
    public OrCa(String path) throws IOException {
        super(path);
    }

    public static void main(String[] args) throws IOException {
        // Check if the home path has been specified via the command line
        String homePath = (args.length > 0) ? args[0] : null;
        new OrCa(homePath);
    }
}
