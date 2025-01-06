package org.outlaw.blockchain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

@Entity
@Table(name = "block")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Block {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "data")
	private String data;

	@Column(name = "prev_hash")
	private byte[] prevHash;

	@Column(name = "data_signature")
	private byte[] dataSignature; // подписанные данные

	@Column(name = "hash_signature")
	private byte[] hashSignature; // хэш подписи

	@ManyToOne
	@JoinColumn(name = "next_id")
	private Block next;
}
