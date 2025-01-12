package org.outlaw.blockchain.business.neuralnetwork;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


public class ConnectionHolder {
	@Getter
	private static final List<Connection> connections = new ArrayList<>();

	public static void add(Connection connection) {
		connections.add(connection);
	}
}
