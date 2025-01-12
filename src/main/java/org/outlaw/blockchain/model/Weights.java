package org.outlaw.blockchain.model;

import lombok.*;
import org.apache.commons.math3.util.Precision;
import org.outlaw.blockchain.business.neuralnetwork.Connection;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Weights {
	private String w11;
	private String w12;
	private String w21;
	private String w22;
	private String v11;
	private String v12;
	private String v13;
	private String v21;
	private String v22;
	private String v23;
	private String w1;
	private String w2;
	private String w3;
	private String e;
	private String publickey;

	@SneakyThrows
	public static Weights fillFromConnections(List<Connection> connections) {
		Weights result = new Weights();
		Map<String, String> weights = new HashMap<>();
		weights.put("x1-h11", "w11");
		weights.put("x1-h12", "w12");

		weights.put("x2-h12", "w22");
		weights.put("x2-h11", "w21");

		weights.put("h11-h21", "v11");
		weights.put("h11-h22", "v12");
		weights.put("h11-h23", "v13");

		weights.put("h12-h21", "v21");
		weights.put("h12-h22", "v22");
		weights.put("h12-h23", "v23");

		weights.put("h21-y", "w1");
		weights.put("h22-y", "w2");
		weights.put("h23-y", "w3");

		for (Connection connection : connections) {
			String weightName = weights.get(String.format("%s-%s", connection.getFrom().getName(), connection.getTo().getName()));
			Field field = result.getClass().getDeclaredField(weightName);
			field.setAccessible(true);
			field.set(result, String.valueOf(Precision.round(connection.getSynapticWeight(), 12)));
		}

		return result;
	}

	// return as normalized JSON object
	public String toString() {
		return new StringBuilder().append("{")
				.append("\"w11\":\"").append(w11).append("\",")
				.append("\"w12\":\"").append(w12).append("\",")
				.append("\"w21\":\"").append(w21).append("\",")
				.append("\"w22\":\"").append(w22).append("\",")
				.append("\"v11\":\"").append(v11).append("\",")
				.append("\"v12\":\"").append(v12).append("\",")
				.append("\"v13\":\"").append(v13).append("\",")
				.append("\"v21\":\"").append(v21).append("\",")
				.append("\"v22\":\"").append(v22).append("\",")
				.append("\"v23\":\"").append(v23).append("\",")
				.append("\"w1\":\"").append(w1).append("\",")
				.append("\"w2\":\"").append(w2).append("\",")
				.append("\"w3\":\"").append(w3).append("\",")
				.append("\"e\":\"").append(e).append("\",")
				.append("\"publickey\":\"").append(publickey).append("\"}")
				.toString();
	}
}
