package org.outlaw.blockchain.business.neuralnetwork;

public class TanH implements IActivationFunction {
    @Override
    public double output(double x) {
        return Math.tanh(x);
    }

    @Override
    public double outputDerivative(double x) {
        return 1 - Math.pow(Math.tanh(x), 2);
    }
}