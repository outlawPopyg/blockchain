package org.outlaw.blockchain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.encoders.Hex;
import org.outlaw.blockchain.business.CryptoUtils;
import org.outlaw.blockchain.business.neuralnetwork.NeuralNetworkService;
import org.outlaw.blockchain.model.BlockchainResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootApplication
@Slf4j
public class BlockchainApplication {
	@Bean
	public CommandLineRunner commandLineRunner(NeuralNetworkService neuralNetworkService) {
		return args -> {
			ObjectMapper objectMapper = new ObjectMapper();
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<List<BlockchainResponse>> forObject = restTemplate.exchange("http://itislabs.ru/nbc/chain?hash=0005e9c3274d5b5bafbddde40eb8fe646173e7ba4130a67fa33940c8d68b5aed",
					HttpMethod.GET, null, new ParameterizedTypeReference<>() {
					});

			BlockchainResponse target = forObject.getBody().get(0);
			String prevhash = forObject.getBody().get(1).getPrevhash();

			byte[] bytes = CryptoUtils.addAll(Hex.decode(target.getPrevhash()),
					target.getData().toString().getBytes(),
					Hex.decode(target.getSignature()),
					intToByteArray(target.getNonce()));
			String sha256 = CryptoUtils.getSHA256(bytes);
			int i = 0;
		};
	}

	public static byte[] intToByteArray(int x) {
		byte[] r = new byte[4];
		r[0] = (byte) (x >> 24);
		r[1] = (byte) (x >> 16);
		r[2] = (byte) (x >> 8);
		r[3] = (byte) (x);
		return r;
	}


	public static void main(String[] args) {
		SpringApplication.run(BlockchainApplication.class, args);
	}

}
