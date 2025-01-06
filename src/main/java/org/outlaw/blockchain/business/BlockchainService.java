package org.outlaw.blockchain.business;

public interface BlockchainService {
	boolean verify();
	boolean verify(Long id);
	void add(String data);
	void print();
}
