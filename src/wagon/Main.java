package wagon;

import wagon.network.expanded.EventActivityNetwork;

public class Main {

	public static void main(String[] args) {
		EventActivityNetwork network = EventActivityNetwork.createTestNetwork();
		System.out.println(network);
	}

}
