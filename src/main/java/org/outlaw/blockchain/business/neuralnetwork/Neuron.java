package org.outlaw.blockchain.business.neuralnetwork;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Neuron {

    private String name;
    private List<Connection> incomingConnections;
    private List<Connection> outgoingConnections;
    private double bias;
    private double gradient;
    private double output;
    private double outputBeforeActivation;
    private IActivationFunction activationFunction;

    public Neuron(String name) {
        this.name = name;
        this.incomingConnections = new ArrayList<>();
        this.outgoingConnections = new ArrayList<>();
        this.bias = 0;
    }

    public Neuron(String name, List<Neuron> neurons, IActivationFunction activationFunction) {
        this(name);
        this.activationFunction = activationFunction;
        for (Neuron neuron : neurons) {
            Connection connection = new Connection(neuron, this);
            ConnectionHolder.add(connection);
            neuron.getOutgoingConnections().add(connection);
            this.incomingConnections.add(connection);
        }
    }

    public void calculateOutput() {
        this.outputBeforeActivation = 0.0;
        for (Connection connection : incomingConnections) {
            this.outputBeforeActivation += connection.getSynapticWeight() * connection.getFrom().getOutput();
        }
        this.output = activationFunction.output(this.outputBeforeActivation + bias);
    }

    public double error(double target) {
        return (target - output);
    }

    public void calculateGradient(double target) {
        this.gradient = error(target) * activationFunction.outputDerivative(output);
    }

    public void calculateGradient() {
        this.gradient = outgoingConnections.stream().mapToDouble(connection -> connection.getTo().getGradient() * connection.getSynapticWeight()).sum()
                * activationFunction.outputDerivative(output);
    }

    public void updateConnections(double lr, double mu) {
        for (Connection connection : incomingConnections) {
            double prevDelta = connection.getSynapticWeightDelta();
            connection.setSynapticWeightDelta(lr * gradient * connection.getFrom().getOutput());
            connection.updateSynapticWeight(connection.getSynapticWeightDelta() + mu * prevDelta);
        }
    }

}