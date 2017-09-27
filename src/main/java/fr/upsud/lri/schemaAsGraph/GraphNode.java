/*
 * Class representing a node of the graph
 */
package fr.upsud.lri.schemaAsGraph;

/**
 * The Class GraphNode.
 */
public abstract class GraphNode {
	
	/** The fathers of the node. */
	protected GraphNodes fathers;
	
	/** The sons of the node. */
	protected GraphNodes sons;
	
	/** The ancestors of the node. */
	protected GraphNodes ancestors;
	
	/** The descendants of the node. */
	protected GraphNodes descendants;
	
	/** Indicates if the node is of complex type. */
	protected boolean complexType = false;
	
	/** Indicates if the node is an attribute. */
	protected boolean isAttribute = false;
	
	/** The label of the node. */
	protected String label;
	
/*	REMOVED BECAUSE WE MARK DIRECTLY THE GRAPH
 * *//** The first time marked. *//*
	private boolean firstTimeMarked = false;
	
	*//** The second time marked. *//*
	private boolean secondTimeMarked = false;
	*/
	/**
	 * Instantiates a new graph node.
	 *
	 * @param label the label of the node
	 */
	public GraphNode(String label){
		this.label = label;
		fathers = new GraphNodes();
		sons = new GraphNodes();
		ancestors = new GraphNodes();
		descendants = new GraphNodes();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
//		return label;

		StringBuffer str = new StringBuffer();
		
		if(isAttribute())
			str.append("Attribute ");
		
		str.append(label + " = {");
		
		str.append("\n\tFathers -> [ ");
		for(GraphNode father : fathers)
			str.append(father.label + " ");
		str.append("] ");
		
		str.append("\n\tSons -> [ ");
		for(GraphNode son : sons)
			str.append(son.label + " ");
		str.append("] ");
		
		str.append("\n\tAncestors -> [ ");
		for(GraphNode ancestor : ancestors)
			str.append(ancestor.label + " ");
		str.append("] ");
		
		str.append("\n\tDescendants -> [ ");
		for(GraphNode descendant : descendants)
			str.append(descendant.label + " ");
		str.append("] ");
		
		if(this.isComplexType() == false)
			str.append("\n\n\tType: " + ((SimpleTypeNode)this).getType());
		
		str.append("\n}");
		
		return str.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphNode other = (GraphNode) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	/**
	 * Instantiates a new graph node.
	 *
	 * @param label the label of the node
	 * @param fathers the fathers of the node
	 * @param sons the sons of the node
	 * @param ancestors the ancestors of the node
	 * @param descendants the descendants of the node
	 */
	public GraphNode(String label,
			GraphNodes fathers, GraphNodes sons, 
			GraphNodes ancestors, GraphNodes descendants){
		this.label = label;
		this.fathers = fathers;
		this.sons = sons;
		this.ancestors = ancestors;
		this.descendants = descendants;
	}

	/**
	 * Sets the fathers of the node.
	 *
	 * @param fathers the new fathers of the node
	 */
	public void setFathers(GraphNodes fathers) {
		this.fathers = fathers;
	}

	/**
	 * Gets the fathers of the node.
	 *
	 * @return the fathers of the node
	 */
	public GraphNodes getFathers() {
		return fathers;
	}

	/**
	 * Sets the sons of the node.
	 *
	 * @param sons the new sons of the node
	 */
	public void setSons(GraphNodes sons) {
		this.sons = sons;
	}

	/**
	 * Gets the sons of the node.
	 *
	 * @return the sons of the node
	 */
	public GraphNodes getSons() {
		return sons;
	}

	/**
	 * Sets the ancestors of the node.
	 *
	 * @param ancestors the new ancestors of the node
	 */
	public void setAncestors(GraphNodes ancestors) {
		this.ancestors = ancestors;
	}

	/**
	 * Gets the ancestors of the node.
	 *
	 * @return the ancestors of the node
	 */
	public GraphNodes getAncestors() {
		return ancestors;
	}

	/**
	 * Sets the descendants of the node.
	 *
	 * @param descendants the new descendants of the node
	 */
	public void setDescendants(GraphNodes descendants) {
		this.descendants = descendants;
	}

	/**
	 * Gets the descendants of the node.
	 *
	 * @return the descendants of the node
	 */
	public GraphNodes getDescendants() {
		return descendants;
	}
	
	/**
	 * Checks if the node is of complex type.
	 *
	 * @return true, if is the node is of complex type, false otherwise
	 */
	public boolean isComplexType() {
		return complexType;
	}
	
	/**
	 * Sets the label of the node.
	 *
	 * @param label the new label of the node
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the label of the node.
	 *
	 * @return the label of the node
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Add a descendant avoiding duplicates.
	 *
	 * @param graphNode the descendant to add
	 */
	public void descendantsAddNoDuplicates(GraphNode graphNode){
		if(!descendants.contains(graphNode))
			descendants.add(graphNode);
	}
	
	/**
	 * Add a list of descendants avoiding duplicates.
	 *
	 * @param list the list of descendants to add
	 */
	public void descendantsAddAllNoDuplicates(GraphNodes list){
		for (GraphNode graphNode : list) {
			if(!descendants.contains(graphNode))
				descendants.add(graphNode);
		}
	}
	
	/**
	 * Add an ancestor avoiding duplicates.
	 *
	 * @param graphNode the ancestor to add
	 */
	public void ancestorsAddNoDuplicates(GraphNode graphNode){
		if(!ancestors.contains(graphNode))
			ancestors.add(graphNode);
	}
	
	/**
	 * Add a list of ancestors avoiding duplicates.
	 *
	 * @param list the list of ancestors to add
	 */
	public void ancestorsAddAllNoDuplicates(GraphNodes list){
		for (GraphNode graphNode : list) {
			if(!ancestors.contains(graphNode))
				ancestors.add(graphNode);
		}
	}
	
	/**
	 * Add a father avoiding duplicates.
	 *
	 * @param graphNode the father to add
	 */
	public void fathersAddNoDuplicates(GraphNode graphNode){
		if(!fathers.contains(graphNode))
			fathers.add(graphNode);
	}
	
	/**
	 * Add a list of fathers avoiding duplicates.
	 *
	 * @param list the list Sons
	 */
	public void fathersAddAllNoDuplicates(GraphNodes list){
		for (GraphNode graphNode : list) {
			if(!fathers.contains(graphNode))
				fathers.add(graphNode);
		}
	}
	
	/**
	 * Add a son avoiding duplicates.
	 *
	 * @param graphNode the son to add
	 */
	public void sonsAddAllNoDuplicates(GraphNode graphNode){
		if(!sons.contains(graphNode))
			sons.add(graphNode);
	}
	
	/**
	 * Add a list of sons avoiding duplicates.
	 *
	 * @param list the list of sons to add
	 */
	public void sonsAddAllNoDuplicates(GraphNodes list){
		for (GraphNode graphNode : list) {
			if(!sons.contains(graphNode))
				sons.add(graphNode);
		}
	}

/*	*//**
	 * Sets the first time marked.
	 *
	 * @param firstTimeMarked the new first time marked
	 *//*
	public void setFirstTimeMarked(boolean firstTimeMarked) {
		this.firstTimeMarked = firstTimeMarked;
	}

	*//**
	 * Checks if is first time marked.
	 *
	 * @return true, if is first time marked
	 *//*
	public boolean isFirstTimeMarked() {
		return firstTimeMarked;
	}

	*//**
	 * Sets the second time marked.
	 *
	 * @param secondTimeMarked the new second time marked
	 *//*
	public void setSecondTimeMarked(boolean secondTimeMarked) {
		this.secondTimeMarked = secondTimeMarked;
	}

	*//**
	 * Checks if is second time marked.
	 *
	 * @return true, if is second time marked
	 *//*
	public boolean isSecondTimeMarked() {
		return secondTimeMarked;
	}
*/
	/**
	 * Sets if the node is an attribute or not.
	 *
	 * @param isAttribute true if the node is an attribute, false otherwise
	 */
	public void setAttribute(boolean isAttribute) {
		this.isAttribute = isAttribute;
	}

	/**
	 * Checks if the node is an attribute.
	 *
	 * @return true if the node is an attribute, false otherwise
	 */
	public boolean isAttribute() {
		return isAttribute;
	}
}
