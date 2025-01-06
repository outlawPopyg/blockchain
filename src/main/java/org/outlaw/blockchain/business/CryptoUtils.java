package org.outlaw.blockchain.business;

import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.digests.GOST3411_2012_256Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.outlaw.blockchain.model.Block;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

public final class CryptoUtils {
	public static final String KEY_ALGORITHM = "RSA";
	public static final String SIGN_ALGORITHM = "SHA256withRSA";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	@SneakyThrows
	public static KeyPair loadKeys() {
		return new KeyPair(getPublicKey(), getPrivateKey());
	}


	@SneakyThrows
	public static PrivateKey getPrivateKey() {
		byte[] keyBytes = Files.readAllBytes(Paths.get("private_key.der"));
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
		return kf.generatePrivate(spec);
	}

	@SneakyThrows
	public static PublicKey getPublicKey() {
		byte[] keyBytes = Files.readAllBytes(Paths.get("public_key.der"));

		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
		return kf.generatePublic(spec);
	}

	public static byte[] getDigest(byte[] message) {
		GOST3411_2012_256Digest digest = new GOST3411_2012_256Digest();
		digest.update(message, 0, message.length);
		byte[] hashMessage = new byte[digest.getDigestSize()];
		digest.doFinal(hashMessage, 0);
		return hashMessage;
	}

	public static byte[] getDigest(Block block) {
		if (block == null) {
			return null;
		}
		Objects.requireNonNull(block.getData(), "Нет данных в блоке");
		Objects.requireNonNull(block.getDataSignature(), "Нет подписи данных");
		return getDigest(addAll(block.getPrevHash(), block.getData().getBytes(StandardCharsets.UTF_8), block.getDataSignature()));
	}

	public static byte[] addAll(byte[] hash, byte[] data, byte[] dataSign) {
		return ArrayUtils.addAll(ArrayUtils.addAll(hash, data), dataSign);
	}

	@SneakyThrows
	public static byte[] generateSignature(PrivateKey privateKey, byte[] input) {
		Signature signature = Signature.getInstance(SIGN_ALGORITHM, "BC");

		signature.initSign(privateKey);

		signature.update(input);

		return signature.sign();
	}

	@SneakyThrows
	public static boolean verifySignature(PublicKey publicKey, byte[] input, byte[] encSignature) {
		Signature signature = Signature.getInstance(SIGN_ALGORITHM, "BC");

		signature.initVerify(publicKey);

		signature.update(input);

		return signature.verify(encSignature);
	}

}
