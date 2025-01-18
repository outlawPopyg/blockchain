package org.outlaw.blockchain.business;

import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Hex;
import org.outlaw.blockchain.model.BlockDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockChainService {
	private static final String URL = "http://itislabs.ru/nbc";
	private static final int[] right = {0x00, 0x0F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
			0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
			0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};

	private final RestTemplate restTemplate = new RestTemplate();

	public String getPrevHash() {
		ResponseEntity<List<BlockDTO>> blockchain = restTemplate.exchange(URL + "/chain", HttpMethod.GET, null,
				new ParameterizedTypeReference<>() {
				});

		if (blockchain.hasBody() && !blockchain.getBody().isEmpty()) {
			BlockDTO prevBlock = blockchain.getBody().get(blockchain.getBody().size() - 1);
			byte[] bytes = CryptoUtils.addAll(Hex.decode(prevBlock.getPrevhash()),
					prevBlock.getData().toString().getBytes(),
					Hex.decode(prevBlock.getSignature()),
					intToByteArray(prevBlock.getNonce()));

			return CryptoUtils.getSHA256(bytes);
		}

		throw new IllegalArgumentException("Blockchain response has empty body");
	}

	public void computeNonce(BlockDTO block) {
		int compareResult = 1;
		do {
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");

				byte[] result = digest.digest(CryptoUtils.addAll(Hex.decode(block.getPrevhash()),
						block.getData().toString().getBytes(),
						Hex.decode(block.getSignature()),
						intToByteArray(block.getNonce())));

				compareResult = compare(result, right);
				if (compareResult < 0) {
					break;
				} else {
					block.setNonce(block.getNonce() + 1);
				}
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		} while (compareResult > 0);
	}

	public String sendBlock(BlockDTO block) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<String> entity = new HttpEntity<>(block.toString(), headers);

		ResponseEntity<String> response = restTemplate.exchange(URL + "/newblock", HttpMethod.POST, entity, String.class);
		if (response.hasBody()) {
			return  response.getBody();
		}

		throw new IllegalArgumentException("Response has empty body");
	}

	public String sendAuthor(String authorInfo) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<String> entity = new HttpEntity<>(authorInfo, headers);

		ResponseEntity<String> response = restTemplate.exchange(URL + "/autor", HttpMethod.POST, entity, String.class);
		if (response.hasBody()) {
			return  response.getBody();
		}

		throw new IllegalArgumentException("Response has empty body");
	}

	private byte[] intToByteArray(int x) {
		byte[] r = new byte[4];
		r[0] = (byte) (x >> 24);
		r[1] = (byte) (x >> 16);
		r[2] = (byte) (x >> 8);
		r[3] = (byte) (x);
		return r;
	}

	private int compare(byte[] a, int[] b) {
		int r = 0;
		for (int i = 0; i < 32; ++i) {
			System.out.println((a[i] & 0xFF) + " - " + b[i]);
			r = (a[i] & 0xFF) - b[i];
			if (r != 0) return r;
		}
		return r;
	}
}
