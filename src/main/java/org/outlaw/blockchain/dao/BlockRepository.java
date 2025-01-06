package org.outlaw.blockchain.dao;

import org.outlaw.blockchain.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long> {
	Block findBlockByNextIsNull();
	Block findBlockByPrevHashIsNull();
	Block findBlockByNextId(Long id);
}
