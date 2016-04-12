package wagon.network;

import wagon.network.expanded.EventNode;

public abstract class Edge {
	
	private EventNode source;
	private EventNode target;
	
	public Edge(EventNode source, EventNode target) {
		this.source = source;
		this.target = target;
	}

	public EventNode source() {
		return source;
	}
	
	public EventNode target() {
		return target;
	}
}
