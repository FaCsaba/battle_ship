package com.csabapro;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Board {
    public static final int WIDTH = 10;
    public static final int HEIGHT = 10;
    public static final int MAX_NUMBER_OF_SHIPS = 5;

    // Spaces where the user selects where torpedos are sent to
    private BoardSpace[][] startegySpaces = new BoardSpace[HEIGHT][WIDTH];
    // Spaces where the ships are located
    private BoardSpace[][] spaces = new BoardSpace[HEIGHT][WIDTH];

    private List<Ship> ships = new ArrayList<Ship>(MAX_NUMBER_OF_SHIPS);
    private List<Torpedo> sentTorpedos = new ArrayList<Torpedo>();
    private List<Torpedo> receivedTorpedos = new ArrayList<Torpedo>();

    private boolean isPlacingTorpedo = false;
    private boolean isPlacingShip = false;
    private Ship shipToPlace;
    private Vec2 torpedoToPlace;

    Board() {
        recalculateSpaces();
    }

    private List<Vec2> getShipPositions() {
        return ships.stream().flatMap((s) -> s.getPositions().stream()).collect(Collectors.toList());
    }

    private boolean checkIfPositionsDontCollide(List<Vec2> positions) {
        List<Vec2> onBoardPositions = getShipPositions();
        for (Vec2 position : positions) {
            // Check if position collides with a ship position that is on the board
            for (Vec2 onBoardPosition : onBoardPositions) {
                if (position.equals(onBoardPosition))
                    return false;
            }
        }
        return true;
    }

    private boolean checkIfPositionsInBounds(List<Vec2> positions) {
        for (Vec2 position : positions) {
            if (position.x >= WIDTH || position.y >= HEIGHT || position.x < 0 || position.y < 0)
                return false;
        }
        return true;
    }

    public int getNumberOfShips() {
        return ships.size();
    }

    private void recalculateSpaces() {
        for (int h = 0; h < HEIGHT; h++) {
            Arrays.fill(startegySpaces[h], BoardSpace.Nothing);
            Arrays.fill(spaces[h], BoardSpace.Nothing);
        }

        getShipPositions().forEach((shipPos) -> spaces[shipPos.y][shipPos.x] = BoardSpace.Ship);

        receivedTorpedos.forEach((receivedTorpedo) -> {
            spaces[receivedTorpedo.pos.y][receivedTorpedo.pos.x] = receivedTorpedo.isHit ? BoardSpace.Hit
                    : BoardSpace.Miss;
        });

        sentTorpedos.forEach((sentTorpedos) -> {
            startegySpaces[sentTorpedos.pos.y][sentTorpedos.pos.x] = sentTorpedos.isHit ? BoardSpace.Hit
                    : BoardSpace.Miss;
        });
    }

    public boolean isHit(Vec2 pos) {
        // Check if pos collides with any ship positions
        for (Vec2 shipPosition : getShipPositions()) {
            if (pos.equals(shipPosition))
                return true;
        }
        return false;
    }

    public void startPlacingShip(int size) {
        isPlacingShip = true;
        shipToPlace = new Ship(new Vec2(0, 0), size, Orientation.Horizontal);
    }

    public void startPlacingTorpedo() {
        isPlacingTorpedo = true;
        torpedoToPlace = new Vec2(0, 0);
    }

    private Vec2 handleMovement(Vec2 pos, Direction direction) {
        switch (direction) {
            case Down:
                pos.y += pos.y < (HEIGHT - 1) ? 1 : 0;
                break;
            case Left:
                pos.x -= pos.x > 0 ? 1 : 0;
                break;
            case Right:
                pos.x += pos.x < (WIDTH - 1) ? 1 : 0;
                break;
            case Up:
                pos.y -= pos.y > 0 ? 1 : 0;
                break;
        }
        return pos;
    }

    public void moveToPlaceShip(Direction direction) {
        Vec2 pos = shipToPlace.getStartPosition();
        Vec2 newPos = new Vec2(pos);
        handleMovement(newPos, direction);
        shipToPlace.setStartPos(newPos);
        // if the movement results in out of bounds we don't allow that
        if(!checkIfPositionsInBounds(shipToPlace.getPositions()))
            shipToPlace.setStartPos(pos);
    }

    public void moveToPlaceTorpedo(Direction direction) {
        torpedoToPlace = handleMovement(torpedoToPlace, direction);
    }

    public void rotateToPlaceShip() {
        shipToPlace.rotate();
        if (!checkIfPositionsInBounds(shipToPlace.getPositions())) {
            shipToPlace.rotate();
        }
    }

    public boolean finishPlacingShip() {
        if (!checkIfPositionsDontCollide(shipToPlace.getPositions()))
            return false;
        ships.add(shipToPlace);
        recalculateSpaces();
        isPlacingShip = false;
        return true;
    }

    public boolean placeShip(Ship ship) {
        if (!checkIfPositionsDontCollide(ship.getPositions()) || !checkIfPositionsInBounds(ship.getPositions()))
            return false;
        ships.add(ship);
        recalculateSpaces();
        return true;
    }

    public Torpedo finishPlacingTorpedo() {
        // if the torpedo has already been sent we report that as incorrect
        if (sentTorpedos.stream().map((t) -> t.pos).toList().contains(torpedoToPlace))
            return null;
        Torpedo t = new Torpedo(torpedoToPlace, false);
        sentTorpedos.add(t);
        isPlacingTorpedo = false;
        return t;
    }

    public Torpedo sendTorpedo(Vec2 pos) {
        if (sentTorpedos.stream().map((t) -> t.pos).toList().contains(pos))
            return null;
        Torpedo t = new Torpedo(pos, false);
        sentTorpedos.add(t);
        isPlacingTorpedo = false;
        return t;
    }

    public void receiveTorpedo(Torpedo t) {
        t.isHit = isHit(t.pos);
        receivedTorpedos.add(t);
        recalculateSpaces();
    }

    public boolean isWon() {
        int amountOfHits = (int) sentTorpedos.stream().filter((t) -> t.isHit).count();
        return getShipPositions().size() <= amountOfHits;
    }

    public void render(PrintWriter writer) {
        List<Vec2> shipPositions = getShipPositions();
        StringBuilder boardString = new StringBuilder("");
        for (int h = 0; h < HEIGHT; h++) {
            for (int w = 0; w < WIDTH; w++) {
                if (isPlacingTorpedo && torpedoToPlace.equals(new Vec2(w, h))) {
                    if (startegySpaces[h][w] != BoardSpace.Nothing) {
                        boardString.append("\033[41m🚀\033[0m");
                        continue;
                    }
                    boardString.append("🚀");
                    continue;
                }

                switch (startegySpaces[h][w]) {
                    case Hit:
                        boardString.append("🔥");
                        break;
                    case Miss:
                        boardString.append(". ");
                        break;
                    case Nothing:
                        boardString.append("~ ");
                        break;
                    case Ship:
                        // We assume that on the strategy board there can never be a ship for now
                        // TODO: if the user loses reveal all the ships that the enemy had
                        assert false;
                        break;
                }
            }
            boardString.append("\n");
        }

        boardString.append("\n");

        for (int h = 0; h < HEIGHT; h++) {
            for (int w = 0; w < WIDTH; w++) {
                if (isPlacingShip && shipToPlace.getPositions().contains(new Vec2(w, h))) {
                    if (shipPositions.contains(new Vec2(w, h))) {
                        boardString.append("\033[31m■\033[0m ");
                        continue;
                    }
                    boardString.append("\033[94m■\033[0m ");
                    continue;
                }

                switch (spaces[h][w]) {
                    case Hit:
                        boardString.append("🔥");
                        break;
                    case Miss:
                        boardString.append(". ");
                        break;
                    case Nothing:
                        boardString.append("~ ");
                        break;
                    case Ship:
                        boardString.append("■ ");
                        break;
                }
            }

            boardString.append("\n");
        }

        writer.print(boardString);
    }
}
