package com.chinczyk.logic;

import java.util.Random;

/**
 * Kostka do gry. Zawiera metodę losującą wartość 1-6.
 */
public class Dice {
    private Random random;
    private int lastValue;

    public Dice() {
        this.random = new Random();
        this.lastValue = 1;
    }

    public int roll() {
        lastValue = random.nextInt(6) + 1;
        return lastValue;
    }

    public int getLastValue() {
        return lastValue;
    }
}
