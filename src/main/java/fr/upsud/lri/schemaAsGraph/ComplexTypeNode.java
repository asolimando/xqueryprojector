/*
 * Class representing the information of a complex node
 */
package fr.upsud.lri.schemaAsGraph;

/**
 * The Class ComplexTypeNode.
 */
public class ComplexTypeNode extends GraphNode {
	
	/** This indicate if the node has a mixed content. */
	private boolean mixedContent;
	
	/**
	 * Instantiates a new complex type node.
	 *
	 * @param label the label of the node
	 * @param mixedContent true if the node is mixed content
	 */
	public ComplexTypeNode(String label, boolean mixedContent) {
		super(label);
		complexType = true;
		this.mixedContent = mixedContent;
	}

	/**
	 * Instantiates a new complex type node.
	 *
	 * @param label the label of the node
	 * @param fathers the fathers of node
	 * @param sons the sons of node
	 * @param ancestors the ancestors of node
	 * @param descendants the descendants of node
	 * @param mixedContent true if the node is mixed content
	 */
	public ComplexTypeNode(String label,
			GraphNodes fathers, GraphNodes sons,
			GraphNodes ancestors, GraphNodes descendants,
			boolean mixedContent) {
		super(label, fathers, sons, ancestors, descendants);
		complexType = true;
		this.mixedContent = mixedContent;
	}

	/**
	 * Sets if the node is mixed content or not.
	 *
	 * @param mixedContent indicates if the node is mixed content or not
	 */
	public void setMixedContent(boolean mixedContent) {
		this.mixedContent = mixedContent;
	}

	/**
	 * Checks if the node is mixed content.
	 *
	 * @return true, if the node is mixed content, false otherwise
	 */
	public boolean isMixedContent() {
		return mixedContent;
	}

}
