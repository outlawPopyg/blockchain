package org.outlaw.blockchain.business;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.encoders.Hex;
import org.outlaw.blockchain.dao.BlockRepository;
import org.outlaw.blockchain.model.Block;
import org.outlaw.blockchain.model.TimeStampResp;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

import static org.outlaw.blockchain.business.CryptoUtils.KEY_ALGORITHM;
import static org.outlaw.blockchain.business.CryptoUtils.verifySignature;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BlockchainServiceImpl implements BlockchainService {
	private static final String URL = "http://itislabs.ru/ts";

	private final BlockRepository blockRepository;
	private final KeyPair keyPair = CryptoUtils.loadKeys();
	private final RestTemplate restTemplate = new RestTemplate();
	private PublicKey arbiterPublicKey;

	@PostConstruct
	@SneakyThrows
	public void init() {
		String publicKeyHex = restTemplate.getForObject(URL + "/public", String.class);
		Assert.notNull(publicKeyHex, "Получен пустой ключ арбитра");

		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Hex.decode(publicKeyHex));
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		arbiterPublicKey = keyFactory.generatePublic(publicKeySpec);

	}

	@Override
	public boolean verify() {
		Block i = blockRepository.findBlockByPrevHashIsNull();
		byte[] prevHash = CryptoUtils.getDigest(i);
		boolean valid = true;
		while (i != null && (i = i.getNext()) != null && (valid = verifySignature(keyPair.getPublic(), i.getData().getBytes(StandardCharsets.UTF_8), i.getDataSignature()) &&
				Objects.deepEquals(i.getPrevHash(), prevHash) &&
				verifySignature(arbiterPublicKey, ArrayUtils.addAll(i.getTimestamp().getBytes(),
						CryptoUtils.getDigest(i)), Hex.decode(i.getArbiterSignature())))) {
			prevHash = CryptoUtils.getDigest(i);
		}

		return valid;
	}

	@Override
	public boolean verify(Long id) {
		return blockRepository.findById(id).filter(block ->
						verifySignature(keyPair.getPublic(), block.getData().getBytes(StandardCharsets.UTF_8), block.getDataSignature()) &&
								Objects.deepEquals(block.getPrevHash(), CryptoUtils.getDigest(blockRepository.findBlockByNextId(id))) &&
								verifySignature(arbiterPublicKey, ArrayUtils.addAll(block.getTimestamp().getBytes(),
										CryptoUtils.getDigest(block)), Hex.decode(block.getArbiterSignature())))
				.isPresent();
	}

	@Override
	public void add(String data) {
		Block lastBlock = blockRepository.findBlockByNextIsNull();

		Block block = new Block();
		block.setData(data);
		block.setDataSignature(CryptoUtils.generateSignature(keyPair.getPrivate(), block.getData().getBytes(StandardCharsets.UTF_8)));
		block.setPrevHash(CryptoUtils.getDigest(lastBlock));

		byte[] digest = CryptoUtils.getDigest(block);
		TimeStampResp timestamp = getTimestamp(Hex.toHexString(digest));

		block.setTimestamp(timestamp.getTimeStampToken().getTs());
		block.setArbiterSignature(timestamp.getTimeStampToken().getSignature());

		Block savedBlock = blockRepository.save(block);

		if (lastBlock != null) {
			lastBlock.setNext(savedBlock);
			if (!verify(savedBlock.getId())) {
				throw new IllegalArgumentException("Проверка не пройдена");
			}
		}

	}

	@Override
	public void print() {
		blockRepository.findAll(Sort.by("id")).forEach(block -> {
			System.out.println("\n");
			System.out.println("Блок " + block.getId());
			System.out.println("Данные блока: " + block.getData());
			System.out.println("Хэш блока: " + Hex.toHexString(CryptoUtils.getDigest(block)));
			System.out.println("Предыдущй хэш: " + (block.getPrevHash() != null ? Hex.toHexString(block.getPrevHash()) : "..."));
			System.out.println("Подписанные данные: " + Hex.toHexString(block.getDataSignature()));
			System.out.println("Временная метка: " + block.getTimestamp());
			System.out.println("Подпись арбитра: " + block.getArbiterSignature());

			System.out.println("\n");
		});
	}

	private TimeStampResp getTimestamp(String digest) {
		return restTemplate.getForObject(URL + "?digest=" + digest, TimeStampResp.class);
	}
}
