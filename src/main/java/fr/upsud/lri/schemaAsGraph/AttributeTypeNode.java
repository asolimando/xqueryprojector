/*
 * Class representing the information of an attribute node
 */
package fr.upsud.lri.schemaAsGraph;

/**
 * The Class AttributeTypeNode.
 */
public class AttributeTypeNode extends SimpleTypeNode {

	/**
	 * Instantiates a new node attribute type.
	 *
	 * @param label the label of the node
	 * @param type the type of the node
	 */
	public AttributeTypeNode(String label, String type) {
		super("@" + label, type);
		this.isAttribute = true;
	}
	
	/**
	 * Instantiates a new attribute type node.
	 *
	 * @param label the label of the node
	 * @param fathers the fathers of the node
	 * @param sons the sons of the node
	 * @param ancestors the ancestors of the node
	 * @param descendants the descendants of the node
	 * @param type the type of the node
	 */
	public AttributeTypeNode(String label, GraphNodes fathers,
			GraphNodes sons, GraphNodes ancestors,
			GraphNodes descendants, String type) {
		super("@" + label, fathers, sons, ancestors, descendants, type);
		this.isAttribute = true;
	}
}
