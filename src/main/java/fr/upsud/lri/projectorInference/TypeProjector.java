/*
 * Class representing a Type Projector
 */
package fr.upsud.lri.projectorInference;

import java.util.HashMap;

import fr.upsud.lri.schemaAsGraph.GraphNode;
import fr.upsud.lri.schemaAsGraph.GraphNodes;

/**
 * The Class TypeProjector.
 */
public class TypeProjector extends HashMap<ProjectedTypeCategories, GraphNodes> {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6095942563441374579L;

	/**
	 * Instantiates a new type projector.
	 *
	 * @param queryTypeProjector the type of the projector to instantiate
	 */
	public TypeProjector(TypeProjectorsTypes queryTypeProjector) {
		super();
		type = queryTypeProjector;
		init();
	}

	/** The type of the projector. */
	private TypeProjectorsTypes type = null;
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#toString()
	 */
	public String toString(){
		StringBuffer str = new StringBuffer();
		for(ProjectedTypeCategories key : this.keySet())
			str.append(key + " =\n" + this.get(key) + "\n");
		return str.toString();
	}

	/**
	 * Initialize the categories of the projector, depending on the type.
	 */
	private void init(){
		// queries elements are common to update too!
		this.put(ProjectedTypeCategories.NODE_ONLY, new GraphNodes());
		this.put(ProjectedTypeCategories.ONE_LEVEL_BELOW, new GraphNodes());
		this.put(ProjectedTypeCategories.EVERYTHING_BELOW, new GraphNodes());
		
		if(type == TypeProjectorsTypes.REFINED_TYPE_PROJECTOR){
			this.put(ProjectedTypeCategories.AF, new GraphNodes());
			this.put(ProjectedTypeCategories.AS_FIRST, new GraphNodes());
			this.put(ProjectedTypeCategories.AS_LAST, new GraphNodes());
			this.put(ProjectedTypeCategories.BEF, new GraphNodes());
			this.put(ProjectedTypeCategories.DEL, new GraphNodes());
			this.put(ProjectedTypeCategories.FIRST, new GraphNodes());
			this.put(ProjectedTypeCategories.LAST, new GraphNodes());
			this.put(ProjectedTypeCategories.NEXT, new GraphNodes());
		}
	}

	/**
	 * Sets the type of the Projector.
	 *
	 * @param type the new type of the Projector
	 */
	public void setType(TypeProjectorsTypes type) {
		this.type = type;
	}

	/**
	 * Gets the type of the Projector.
	 *
	 * @return the type of the Projector
	 */
	public TypeProjectorsTypes getType() {
		return type;
	}

	/**
	 * String representation of the projector.
	 *
	 * @param summarize true if the representation should be a summary, false otherwise
	 * @return the string representing the projector
	 */
	public String toString(boolean summarize) {
		if(summarize == false)
			return this.toString();
		
		StringBuffer str = new StringBuffer();
		for(ProjectedTypeCategories key : this.keySet()){
			str.append(key + " = { ");
			for (GraphNode node : this.get(key)) {
				str.append(node.getLabel() + " ");
			} 
			str.append("}\n");
		}
		return str.toString();
	}
	
	/**
	 * Makes the union of several Projectors.
	 *
	 * @param projectors the projectors to be joined
	 * @return the type projector resulting from the union
	 */
	public static TypeProjector projectorUnion(TypeProjector [] projectors){
		
		if(projectors.length == 0)
			return null;
		
		// the new projector will have the same type of the projector to be joined
		TypeProjector typeProjectorUnion = new TypeProjector(projectors[0].getType());
		
		for (TypeProjector typeProjector : projectors) {
			for (ProjectedTypeCategories key : typeProjector.keySet()) {
				if(typeProjector.getType() != typeProjectorUnion.getType())
					throw new IllegalStateException("Cannot make the union " +
							"of inhomogeneous Projectors.");
				
				typeProjectorUnion.get(key).addAllNoDuplicates(typeProjector.get(key));
			}
		}
		
		// force disjointness among the categories
		typeProjectorUnion.forceDisjointness();
		
		return typeProjectorUnion;
	}
	
	/**
	 * Force disjointness of the categories of the projector.
	 */
	private void forceDisjointness(){
		if(this.getType() != TypeProjectorsTypes.THREE_LEVEL_TYPE_PROJECTOR)
			throw new IllegalArgumentException("Unsupported projector type.");
		
		// everything below is ok as it is
		
		/* remove all the common elements between everything below and
		 one level below from the latter */
		this.get(ProjectedTypeCategories.ONE_LEVEL_BELOW).removeAll(
				this.get(ProjectedTypeCategories.EVERYTHING_BELOW));
		
		/* remove from node only category all the elements in common with
		   one level below and everything below */
		this.get(ProjectedTypeCategories.NODE_ONLY).removeAll(
				this.get(ProjectedTypeCategories.EVERYTHING_BELOW));		
		this.get(ProjectedTypeCategories.NODE_ONLY).removeAll(
				this.get(ProjectedTypeCategories.ONE_LEVEL_BELOW));
	}
}
