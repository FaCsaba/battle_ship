package com.csabapro;

import java.util.Random;

/**
 * Handles the moves that the CPU character makes
 */
public class CPU {
    private Board board;

    CPU(Board board) {
        this.board = board;
    }

    /** Places ships randomly on the board */
    public void placeShip(int shipSize) {
        Ship ship = null;
        do {
            ship = Ship.random();
            ship.setSize(shipSize);
        }
        while (!board.placeShip(ship));
    }

    public Vec2 getNextTorpedoPlacement() {
        Random r = new Random();
        return new Vec2(r.nextInt(Board.WIDTH), r.nextInt(Board.HEIGHT));
    }
}
