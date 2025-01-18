package org.outlaw.blockchain;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.outlaw.blockchain.business.BlockChainService;
import org.outlaw.blockchain.business.CryptoUtils;
import org.outlaw.blockchain.business.neuralnetwork.NeuralNetworkService;
import org.outlaw.blockchain.model.BlockDTO;
import org.outlaw.blockchain.model.Weights;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Formatter;

@SpringBootApplication
@Slf4j
public class BlockchainApplication {
	@Bean
	public CommandLineRunner commandLineRunner(NeuralNetworkService neuralNetworkService,
	                                           BlockChainService blockChainService) {

		KeyPair keyPair = CryptoUtils.loadKeys();

		CommandLineRunner sendBlock = args -> {
			Weights weights = neuralNetworkService.getWeights();
			weights.setPublickey(Hex.toHexString(keyPair.getPublic().getEncoded()));

			String prevHash = blockChainService.getPrevHash();
			String sign = new String(Hex.encode(CryptoUtils.generateSignature(keyPair.getPrivate(),
					weights.toString().getBytes(StandardCharsets.UTF_8))));

			BlockDTO block = new BlockDTO();
			block.setPrevhash(prevHash);
			block.setSignature(sign);
			block.setNonce(0);
			block.setData(weights);

			blockChainService.computeNonce(block);

			log.info("Nonce: {}", block.getNonce());

			blockChainService.sendBlock(block);
		};

		CommandLineRunner sendAuthor = args -> {
			String author = "Ахметшин Калим Рустемович, 11-102";

			String sign = new String(Hex.encode(CryptoUtils.generateSignature(keyPair.getPrivate(),
					author.getBytes(StandardCharsets.UTF_8))));

			String authorInfo = new Formatter().format("{\"autor\":\"%s\",\"sign\":\"%s\",\"publickey\":\"%s\"}", author, sign,
					Hex.toHexString(keyPair.getPublic().getEncoded())).toString();

			String response = blockChainService.sendAuthor(authorInfo);
			int i = 0;
		};


		return sendAuthor;
	}


	public static void main(String[] args) {
		SpringApplication.run(BlockchainApplication.class, args);
	}

}
