package org.outlaw.blockchain.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.outlaw.blockchain.dao.BlockRepository;
import org.outlaw.blockchain.model.Block;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Objects;

import static org.outlaw.blockchain.business.CryptoUtils.verifySignature;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BlockchainServiceImpl implements BlockchainService {
	private final BlockRepository blockRepository;
	private final KeyPair keyPair = CryptoUtils.loadKeys();

	@Override
	public boolean verify() {
		Block i = blockRepository.findBlockByPrevHashIsNull();
		byte[] prevHash = CryptoUtils.getDigest(i);
		boolean valid = true;
		while ((i = i.getNext()) != null && (valid = verifySignature(keyPair.getPublic(), i.getData().getBytes(StandardCharsets.UTF_8), i.getDataSignature()) &&
				Objects.deepEquals(i.getPrevHash(), prevHash) &&
				verifySignature(keyPair.getPublic(), CryptoUtils.getDigest(i), i.getHashSignature()))) {
			prevHash = CryptoUtils.getDigest(i);
		}

		return valid;
	}

	@Override
	public boolean verify(Long id) {
		return blockRepository.findById(id).filter(block ->
				verifySignature(keyPair.getPublic(), block.getData().getBytes(StandardCharsets.UTF_8), block.getDataSignature()) &&
						Objects.deepEquals(block.getPrevHash(), CryptoUtils.getDigest(blockRepository.findBlockByNextId(id))) &&
						verifySignature(keyPair.getPublic(), CryptoUtils.getDigest(block), block.getHashSignature())).isPresent();
	}

	@Override
	public void add(String data) {
		Block lastBlock = blockRepository.findBlockByNextIsNull();

		Block block = new Block();
		block.setData(data);
		block.setDataSignature(CryptoUtils.generateSignature(keyPair.getPrivate(), block.getData().getBytes(StandardCharsets.UTF_8)));
		block.setPrevHash(CryptoUtils.getDigest(lastBlock));
		block.setHashSignature(CryptoUtils.generateSignature(keyPair.getPrivate(), CryptoUtils.getDigest(block)));

		if (lastBlock != null) {
			lastBlock.setNext(block);
		}

		blockRepository.save(block);
	}

	@Override
	public void print() {
		blockRepository.findAll(Sort.by("id")).forEach(block -> {
			System.out.println("\n");
			log.info("Блок {}", block.getId());
			log.info("Данные блока: {}", block.getData());
			log.info("Хэш блока: {}", Hex.toHexString(CryptoUtils.getDigest(block)));
			log.info("Предыдущй хэш: {}", block.getPrevHash() != null ? Hex.toHexString(block.getPrevHash()) : "...");
			log.info("Подписанные данные: {}", Hex.toHexString(block.getDataSignature()));
			log.info("Подписанный хэш: {}", Hex.toHexString(block.getHashSignature()));
			System.out.println("\n");
		});
	}
}
