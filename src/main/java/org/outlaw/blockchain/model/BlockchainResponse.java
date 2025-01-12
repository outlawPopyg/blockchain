package org.outlaw.blockchain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlockchainResponse {
	private String prevhash;
	private Weights data;
	private String signature;
	private Integer nonce = 0;
	private String ts;
	private String arbitersignature;

	// return as normalized JSON object
	public String toString() {
		return new StringBuilder().append("{")
				.append("\"prevhash\":\"").append(prevhash).append("\",")
				.append("\"data\":").append(data.toString()).append(",")
				.append("\"signature\":\"").append(signature).append("\",")
				.append("\"ts\":\"").append(ts).append("\",")
				.append("\"arbitersignature\":\"").append(arbitersignature).append("\",")
				.append("\"nonce\":\"").append(String.valueOf(nonce)).append("\"}")
				.toString();
	}
}
