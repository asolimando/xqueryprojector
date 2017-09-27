/*
 * Enum representing the different categories inside a type projector
 */
package fr.upsud.lri.projectorInference;

/**
 * The Enum ProjectedTypeCategories.
 */
public enum ProjectedTypeCategories {
	
	/** The NODE ONLY category. */
	NODE_ONLY ("NODE_ONLY", TypeProjectorsTypes.THREE_LEVEL_TYPE_PROJECTOR),
	
	/** The EVERYTHING BELOW category. */
	EVERYTHING_BELOW ("EVERYTHING_BELOW", TypeProjectorsTypes.THREE_LEVEL_TYPE_PROJECTOR),
	
	/** The ONE LEVEL BELOW category. */
	ONE_LEVEL_BELOW ("ONE_LEVEL_BELOW", TypeProjectorsTypes.THREE_LEVEL_TYPE_PROJECTOR),
	
	/** The AS LAST category. */
	AS_LAST ("AS_LAST", TypeProjectorsTypes.REFINED_TYPE_PROJECTOR),
	
	/** The AS FIRST category. */
	AS_FIRST ("AS_FIRST", TypeProjectorsTypes.REFINED_TYPE_PROJECTOR),
	
	/** The BEFORE category. */
	BEF ("BEF", TypeProjectorsTypes.REFINED_TYPE_PROJECTOR),
	
	/** The AFTER category. */
	AF ("AF", TypeProjectorsTypes.REFINED_TYPE_PROJECTOR),
	//REP ("REP", TypeProjectorsTypes.UPDATE_TYPE_PROJECTOR),
	/** The NEXT category. */
	NEXT ("NEXT", TypeProjectorsTypes.REFINED_TYPE_PROJECTOR),
	
	/** The DELETE category. */
	DEL ("DEL", TypeProjectorsTypes.REFINED_TYPE_PROJECTOR),
	
	/** The FIRST category. */
	FIRST ("FIRST", TypeProjectorsTypes.REFINED_TYPE_PROJECTOR),
	
	/** The LAST category. */
	LAST ("LAST", TypeProjectorsTypes.REFINED_TYPE_PROJECTOR)	
	;
	
	/** The textual representation of the category. */
	private final String name;
	
	/** The type of the projectors for which the category is valid. */
	private final TypeProjectorsTypes typeProjectorsType;
	

	/**
	 * Instantiates a new projected type categories.
	 *
	 * @param name the name of the category
	 * @param tpType the type projector's type
	 */
	private ProjectedTypeCategories(String name, TypeProjectorsTypes tpType){
		this.name = name;
		this.typeProjectorsType = tpType;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	public String toString(){
		return name;
	}

	/**
	 * Gets the type projector type.
	 *
	 * @return the type of the type projector
	 */
	public TypeProjectorsTypes getTypeProjectorType() {
		return typeProjectorsType;
	}
}
