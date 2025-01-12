package org.outlaw.blockchain.neuralnetwork;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public class NeuralNetwork {

	private static final Logger logger = LogManager.getLogger(NeuralNetwork.class);

	private final int inputSize;
	private final int firstHiddenSize;
	private final int secondHiddenSize;
	private final int outputSize;

	private final List<Neuron> inputLayer;
	private final List<Neuron> firstHiddenLayer;
	private final List<Neuron> secondHiddenLayer;
	private final List<Neuron> outputLayer;

	@Setter
	private double learningRate = 0.01;
	@Setter
	private double momentum = 0.5;
	private IActivationFunction activationFunction = new Sigmoid(); // default activation function
	private boolean initialized = false;


	public NeuralNetwork(int inputSize, int firstHiddenSize, int secondHiddenSize, int outputSize) {
		this.inputSize = inputSize;
		this.firstHiddenSize = firstHiddenSize;
		this.secondHiddenSize = secondHiddenSize;
		this.outputSize = outputSize;
		this.inputLayer = new ArrayList<>();
		this.firstHiddenLayer = new ArrayList<>();
		this.secondHiddenLayer = new ArrayList<>();
		this.outputLayer = new ArrayList<>();
	}

	public void setActivationFunction(ActivationFunction activationFunction) {
		switch (activationFunction) {
			case LEAKY_RELU:
				this.activationFunction = new LeakyReLu();
				break;
			case TANH:
				this.activationFunction = new TanH();
				break;
			case SIGMOID:
				this.activationFunction = new Sigmoid();
				break;
			case SWISH:
				this.activationFunction = new Swish();
				break;
		}
	}

	public void init() {
		for (int i = 0; i < inputSize; i++) {
			this.inputLayer.add(new Neuron(String.format("x%d", i + 1)));
		}
		for (int i = 0; i < firstHiddenSize; i++) {
			this.firstHiddenLayer.add(new Neuron(String.format("h1%d", i + 1), this.inputLayer, activationFunction));
		}
		for (int i = 0; i < secondHiddenSize; i++) {
			this.secondHiddenLayer.add(new Neuron(String.format("h2%d", i + 1), this.firstHiddenLayer, activationFunction));
		}
		for (int i = 0; i < outputSize; i++) {
			this.outputLayer.add(new Neuron("y", this.secondHiddenLayer, activationFunction));
		}
		this.initialized = true;
		logger.info("Network Initialized.");
	}


	public void train(MLDataSet set, int epoch) {
		if (!initialized) {
			this.init();
		}
		logger.info("Training Started");
		for (int i = 0; i < epoch; i++) {
			Collections.shuffle(set.getData());

			for (MLData datum : set.getData()) {
				forward(datum.getInputs());
				backward(datum.getTargets());
			}
		}
		logger.info("Training Finished.");
	}

	private void backward(double[] targets) {
		int i = 0;
		for (Neuron neuron : outputLayer) {
			neuron.calculateGradient(targets[i++]);
		}
		for (Neuron neuron : secondHiddenLayer) {
			neuron.calculateGradient();
		}
		for (Neuron neuron : firstHiddenLayer) {
			neuron.calculateGradient();
		}

		for (Neuron neuron : firstHiddenLayer) {
			neuron.updateConnections(learningRate, momentum);
		}
		for (Neuron neuron : secondHiddenLayer) {
			neuron.updateConnections(learningRate, momentum);
		}
		for (Neuron neuron : outputLayer) {
			neuron.updateConnections(learningRate, momentum);
		}
	}

	private void forward(double[] inputs) {
		int i = 0;
		for (Neuron neuron : inputLayer) {
			neuron.setOutput(inputs[i++]);
		}
		for (Neuron neuron : firstHiddenLayer) {
			neuron.calculateOutput();
		}
		for (Neuron neuron : secondHiddenLayer) {
			neuron.calculateOutput();
		}
		for (Neuron neuron : outputLayer) {
			neuron.calculateOutput();
		}
	}

	public double[] predict(double... inputs) {
		forward(inputs);
		double[] output = new double[outputLayer.size()];
		for (int i = 0; i < output.length; i++) {
			output[i] = outputLayer.get(i).getOutput();
		}
		logger.info("Input : " + Arrays.toString(inputs) + " Predicted : " + Arrays.toString(output));
		return output;
	}


}