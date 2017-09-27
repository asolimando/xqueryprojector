/*
 * PathItem class represents a generic element of a path
 */
package fr.upsud.lri.pathExtractor;

/**
 * The Class PathItem.
 */
public abstract class PathItem implements Cloneable {
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public PathItem clone(){
		try {
			return (PathItem) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Methods to check if the item represents a step or a variable
	 * @return true if the item is a step, false if it represents a variable
	 */
	public boolean isStepItem(){
		return this instanceof StepItem;
	}
	
	/**
	 * Methods to check if the item represents a step or a variable
	 * @return true if the item represents a variable, false if it is a step
	 */
	public boolean isVarItem(){
		return this instanceof VarItem;
	}
}
