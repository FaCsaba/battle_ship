package com.csabapro;

public class Vec2 {
    public int x;
    public int y;

    Vec2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Vec2(Vec2 v) {
        this.x = v.x;
        this.y = v.y;
    }

    @Override
    public boolean equals(Object other) {
        return this.x == ((Vec2)other).x && this.y == ((Vec2)other).y;
    }
}
