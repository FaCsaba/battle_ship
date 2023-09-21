package com.csabapro;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ship {
    public static final int MAX_SHIP_SIZE = 5;
    
    private int size;
    private Orientation orientation;
    private Vec2 startPos;
    private List<Vec2> positions = new ArrayList<Vec2>(MAX_SHIP_SIZE);

    private void calulatePositions() {
        this.positions.clear();

        for (int i = 0; i < size; i++) {
            int x = startPos.x;
            int y = startPos.y;
            
            if (this.orientation == Orientation.Horizontal) x += i;
            else y += i;

            this.positions.add(new Vec2(x, y));
        }
    }

    Ship(Vec2 startPos, int size, Orientation orientation) {
        assert size <= MAX_SHIP_SIZE;

        this.startPos = startPos;
        this.size = size;
        this.orientation = orientation;
        this.positions = new ArrayList<Vec2>(MAX_SHIP_SIZE);

        calulatePositions();
    }

    public List<Vec2> getPositions() {
        return positions;
    }

    public void rotate() {
        orientation = orientation == Orientation.Horizontal ? Orientation.Vertical : Orientation.Horizontal;
        calulatePositions();
    }

    public Vec2 getStartPosition() {
        return startPos;
    }

    public void setStartPos(Vec2 newStartPos) {
        startPos = newStartPos;
        calulatePositions();
    }

    public void setSize(int newSize) {
        size = newSize;
        calulatePositions();
    }

    public boolean isHit(Vec2 torpedoPos) {
        for (Vec2 position : positions) {
            if (torpedoPos.equals(position))
                return true;
        }
        return false;
    }

    public static Ship random() {
        Random r = new Random();
        return new Ship(new Vec2(r.nextInt(Board.WIDTH), r.nextInt(Board.HEIGHT)), r.nextInt(MAX_SHIP_SIZE), r.nextBoolean() ? Orientation.Horizontal : Orientation.Vertical);
    }
}
