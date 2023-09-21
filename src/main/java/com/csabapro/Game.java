package com.csabapro;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

public class Game {
    /**
     * Indicates whos turn it is to play
     */
    private PlayerTurn playerTurn = PlayerTurn.Human;
    /**
     * Indicates which phase the game's in
     */
    private Phase phase = Phase.ShipPlacement;

    private Board humanBoard = new Board();
    private int placedHumanShips = 0;
    private boolean isPlacing = false;

    private Board cpuBoard = new Board();
    private CPU cpu = new CPU(cpuBoard);
    private int placedCpuShips = 0;
    private boolean isThinking = false;

    private static final Integer[] shipSizes = {1, 2, 3, 4, 5};

    private NonBlockingReader inputReader;
    private PrintWriter outputWriter;

    Game() {
        try {
            Terminal terminal = TerminalBuilder.terminal();
            terminal.enterRawMode();
            inputReader = terminal.reader();
            outputWriter = terminal.writer();
        } catch (IOException e) {
            System.err.println("Could not use terminal!");
            System.exit(1);
        }
    }

    /**
     * Represents who won the game. If null it means that the game is yet to be
     * completed.
     */
    private PlayerTurn won = null;

    private void flipTurn() {
        playerTurn = playerTurn == PlayerTurn.Human ? PlayerTurn.CPU : PlayerTurn.Human;
    }

    private InputType getInput() {
        int input;
        while (true) {
            try {
                input = inputReader.read();
            } catch (IOException e) {
                continue;
            }
            switch (input) {
                case 'w':
                    return InputType.KeyW;
                case 's':
                    return InputType.KeyS;
                case 'a':
                    return InputType.KeyA;
                case 'd':
                    return InputType.KeyD;
                case 'r':
                    return InputType.KeyR;
                case 13:
                    return InputType.KeyEnter;
            }
        }
    }

    private void update() {
        if (won != null) {
            if (won == PlayerTurn.Human) {
                outputWriter.println("🎉Congratulations you beat that pesky computer🎉");
            } else {
                outputWriter.println("Oops. You've lost. You'll get 'em next time.");
            }
            System.exit(0);
        }

        if (playerTurn == PlayerTurn.CPU) {
            if (isThinking) {try {
                Thread.sleep(new Random().nextLong());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isThinking = false;
        }
            if (isPlacing) {flipTurn(); return;}
            switch (phase) {
                case ShipPlacement:
                    if (placedCpuShips >= shipSizes.length) break;
                    cpu.placeShip(shipSizes[placedCpuShips]);
                    placedCpuShips++;
                    break;
                case Torpedo:
                    Torpedo t = null; 
                    do {
                        t = cpuBoard.sendTorpedo(cpu.getNextTorpedoPlacement());
                        if (t != null) {
                            humanBoard.receiveTorpedo(t);
                        }
                    } while (t == null);
                    if (cpuBoard.isWon()) {
                        won = PlayerTurn.CPU;
                        return;
                    }
                    break;
            }
            flipTurn();
            return;
        }

        switch (phase) {
            case ShipPlacement:
                if (isPlacing) {
                    InputType in = getInput();
                    switch (in) {
                        case KeyW:
                            humanBoard.moveToPlaceShip(Direction.Up);
                            break;
                        case KeyS:
                            humanBoard.moveToPlaceShip(Direction.Down);
                            break;
                        case KeyA:
                            humanBoard.moveToPlaceShip(Direction.Left);
                            break;
                        case KeyD:
                            humanBoard.moveToPlaceShip(Direction.Right);
                            break;
                        case KeyR:
                            humanBoard.rotateToPlaceShip();
                            break;
                        case KeyEnter:
                            // If the ships placement was correct we are no longer placing
                            if (humanBoard.finishPlacingShip()) {
                                isPlacing = false;
                                placedHumanShips++;
                            }
                            break;
                    }
                } else if (shipSizes.length > placedHumanShips) {
                    humanBoard.startPlacingShip(shipSizes[placedHumanShips]);
                    isPlacing = true;
                } else {
                    phase = Phase.Torpedo;
                }
                break;
            case Torpedo:
                if (isPlacing) {
                    InputType in = getInput();
                    switch (in) {
                        case KeyW:
                            humanBoard.moveToPlaceTorpedo(Direction.Up);
                            break;
                        case KeyS:
                            humanBoard.moveToPlaceTorpedo(Direction.Down);
                            break;
                        case KeyA:
                            humanBoard.moveToPlaceTorpedo(Direction.Left);
                            break;
                        case KeyD:
                            humanBoard.moveToPlaceTorpedo(Direction.Right);
                            break;
                        case KeyEnter:
                            // If the torpedo placement was correct we are no longer placing
                            Torpedo t = humanBoard.finishPlacingTorpedo();
                            if (t != null) {
                                isPlacing = false;
                                cpuBoard.receiveTorpedo(t);
                            }
                            break;
                        default:
                    }
                } else {
                    humanBoard.startPlacingTorpedo();
                    isPlacing = true;
                    if (humanBoard.isWon()) {
                        won = PlayerTurn.Human;
                        return;
                    }
                }
                break;
        }
        flipTurn();
    }

    private void render() {
        // Clear screen; Goto 1;1 (top-left corner)
        outputWriter.print("\033[2J\033[1;1H");
        outputWriter.printf("It's %s turn\n", playerTurn == PlayerTurn.Human ? "your" : "the CPUs");

        switch (phase) {
            case ShipPlacement:
                outputWriter.println("Select where to place your ship! (WASD to move; r to rotate)");
                break;
            case Torpedo:
                outputWriter.println("Select where to place the torpedo! (WASD to move)");
                break;
        }

        humanBoard.render(outputWriter);
    }

    public void run() {
        render();
        while (true) {
            update();
            render();
            // for debug purposes
            // outputWriter.println("\nCPU Board:");
            // cpuBoard.render(outputWriter);
        }
    }
}

enum PlayerTurn {
    Human,
    CPU
}

enum Phase {
    ShipPlacement,
    Torpedo,
}