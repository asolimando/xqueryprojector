/*
 * Class representing a collection of graph nodes
 */
package fr.upsud.lri.schemaAsGraph;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The Class GraphNodes.
 */
public class GraphNodes extends LinkedList<GraphNode> {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 620406544858863441L;

	/**
	 * Instantiates a new graph nodes.
	 */
	public GraphNodes() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public GraphNodes clone() {
		GraphNodes e = new GraphNodes();
		e.addAll(this);		
		return e;
	}

	
	/**
	 * Instantiates a new collection of graph nodes.
	 *
	 * @param keySet the key set
	 */
	public GraphNodes(Set<GraphNode> keySet) {
		super(keySet);
	}

	/**
	 * Adds a graph node avoiding duplicates.
	 *
	 * @param graphNode the graph node to add
	 */
	public void addNoDuplicates(GraphNode graphNode){
		if(!this.contains(graphNode))
			this.add(graphNode);
	}
	
	/**
	 * Adds a list of graph nodes avoiding duplicates.
	 *
	 * @param graphNodes the graph nodes to add
	 */
	public void addAllNoDuplicates(List<GraphNode> graphNodes){
		for (GraphNode graphNode : graphNodes)
			addNoDuplicates(graphNode);
	}
	
	/**
	 * Adds a collection of graph nodes avoiding duplicates.
	 *
	 * @param graphNodes the graph nodes to add
	 */
	public void addAllNoDuplicates(GraphNodes graphNodes){
		for (GraphNode graphNode : graphNodes)
			addNoDuplicates(graphNode);
	}
}
