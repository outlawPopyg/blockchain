package org.outlaw.blockchain.neuralnetwork;

public interface IActivationFunction {
    double output(double x);

    double outputDerivative(double x);
}