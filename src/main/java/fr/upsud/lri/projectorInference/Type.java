/*
 * NOT USED CLASS, REPLACED BY MARKED GRAPH
 */
package fr.upsud.lri.projectorInference;

import java.util.LinkedList;

/**
 * The Class Type.
 */
public class Type extends LinkedList<String> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9158193830451628592L;
	
	/** The type category. */
	private ProjectedTypeCategories typeCategory = null;
	
	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString(){
		StringBuffer str = new StringBuffer("(");
		for(String type : this)
			str.append(type + " ");
		str.append(")");
		return str.toString();
	}

	/**
	 * Sets the type.
	 *
	 * @param typeCategory the new type
	 */
	public void setType(ProjectedTypeCategories typeCategory) {
		this.typeCategory = typeCategory;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public ProjectedTypeCategories getType() {
		return typeCategory;
	}
}
