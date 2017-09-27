/*
 * NOT USED CLASS, REPLACED BY MARKED GRAPH
 */
package fr.upsud.lri.projectorInference;

import java.util.LinkedList;

/**
 * The Class Types.
 */
public class Types extends LinkedList<Type> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3578519009412318832L;
	
	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString(){
		StringBuffer str = new StringBuffer("[");
		for(Type type : this)
			str.append(type + " ");
		str.append("]");
		return str.toString();
	}
}
