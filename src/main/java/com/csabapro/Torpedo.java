package com.csabapro;

public class Torpedo {
    public Vec2 pos;
    public boolean isHit;

    Torpedo(Vec2 pos, boolean isHit) {
        this.pos = pos;
        this.isHit = isHit;
    }
}
