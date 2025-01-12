package org.outlaw.blockchain.business.neuralnetwork;

public interface IActivationFunction {
    double output(double x);

    double outputDerivative(double x);
}