package org.outlaw.blockchain.business.neuralnetwork;

public class RandomGenerator {
    public static double randomValue(int min, int max) {
        return min + (max - min) * Math.random();
    }
    public static int random(int min, int max) {
        return (int) (min + (max - min) * Math.random());
    }
}