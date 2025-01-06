package org.outlaw.blockchain;

import lombok.extern.slf4j.Slf4j;
import org.outlaw.blockchain.business.BlockchainService;
import org.outlaw.blockchain.business.CryptoUtils;
import org.outlaw.blockchain.dao.BlockRepository;
import org.outlaw.blockchain.model.Block;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Objects;
import java.util.Scanner;

import static org.outlaw.blockchain.business.CryptoUtils.verifySignature;

@SpringBootApplication
@Slf4j
public class BlockchainApplication {

	@Bean
	public CommandLineRunner commandLineRunner(BlockchainService blockchainService) {
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.println("""
					1. Напечатать
					2. Добавить блок
					3. Верифицировать всю цепочку
					4. Верифицировать блок
					0. Выход""");
			String value = scanner.nextLine();
			if (value.equals("0")) {
				break;
			}
			switch (value) {
				case "1" -> blockchainService.print();
				case "2" -> {
					System.out.println("Введите данные: ");
					String data = scanner.nextLine();
					blockchainService.add(data);
				}
				case "3" -> System.out.println(blockchainService.verify() ? "Ошибок не обнуружено" : "Обнаружены ошибки");
				case "4" -> {
					System.out.println("Введите номер блока: ");
					System.out.println(blockchainService.verify(Long.valueOf(scanner.nextLine())) ? "Ошибок не обнуружено" : "Обнаружены ошибки");
				}

				default -> throw new IllegalStateException("Unexpected value");
			}
			System.out.println('\n');
		}

//		Hex.toHexString(hashMessage).toUpperCase()
		return args -> {
		};
	}


	public static void main(String[] args) {
		SpringApplication.run(BlockchainApplication.class, args);
	}

}
