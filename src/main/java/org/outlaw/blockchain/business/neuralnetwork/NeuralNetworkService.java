package org.outlaw.blockchain.business.neuralnetwork;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.math3.util.Precision;
import org.bouncycastle.util.encoders.Hex;
import org.outlaw.blockchain.business.CryptoUtils;
import org.outlaw.blockchain.model.Weights;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.*;

@Service
public class NeuralNetworkService {
	@SneakyThrows
	public Weights getWeights() {
		double[][] inputs;
		double[][] outputs;
		ObjectMapper objectMapper = new ObjectMapper();

		try (BufferedReader reader = Files.newBufferedReader(Paths.get("test_data_100.csv"))) {
			List<List<String>> records = reader.lines()
					.map(line -> Arrays.asList(line.split(";")))
					.toList();
			inputs = new double[records.size()][2];
			outputs = new double[100][1];
			for (int i = 0; i < records.size(); i++) {
				inputs[i][0] = Double.parseDouble(records.get(i).get(0));
				inputs[i][1] = Double.parseDouble(records.get(i).get(1));
				outputs[i][0] = Double.parseDouble(records.get(i).get(2));
			}

			int i = 0;
		}

		NeuralNetwork neuralNetwork = new NeuralNetwork(2, 2, 3, 1);
		neuralNetwork.init();
		neuralNetwork.setLearningRate(0.01);
		neuralNetwork.setMomentum(0.5);
		neuralNetwork.setActivationFunction(ActivationFunction.SIGMOID);

		MLDataSet dataSet = new MLDataSet(inputs, outputs);
		neuralNetwork.train(dataSet, 100_000);

		double error = 0;
		for (int i = 0; i < inputs.length; i++) {
			double predicted = neuralNetwork.predict(inputs[i])[0];
			double ideal = outputs[i][0];
			error += ((ideal - predicted) * (ideal - predicted));
		}

		Weights weights = Weights.fillFromConnections(ConnectionHolder.getConnections());
		weights.setE(BigDecimal.valueOf(Precision.round(error, 12)).toString());


		return weights;
	}

}
