/*
 * Class representing the information of a simple node
 */
package fr.upsud.lri.schemaAsGraph;

/**
 * The Class SimpleTypeNode.
 */
public class SimpleTypeNode extends GraphNode {

	/** The type associated to the node. */
	private String type;
	
	/**
	 * Instantiates a new simple type node.
	 *
	 * @param label the label of the node
	 * @param type the type of the node
	 */
	public SimpleTypeNode(String label, String type) {
		super(label);
		this.type = type;
	}

	/**
	 * Instantiates a new simple type node.
	 *
	 * @param label the label of the node
	 * @param fathers the fathers of the node
	 * @param sons the sons of the node
	 * @param ancestors the ancestors of the node
	 * @param descendants the descendants of the node
	 * @param type the type of the node
	 */
	public SimpleTypeNode(String label, GraphNodes fathers, GraphNodes sons,
			GraphNodes ancestors, GraphNodes descendants, String type) {
		super(label, fathers, sons, ancestors, descendants);
		this.type = type;
	}

	/**
	 * Sets the type of the node.
	 *
	 * @param type the new type of the node
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the type of the node.
	 *
	 * @return the type of the node
	 */
	public String getType() {
		return type;
	}
}
