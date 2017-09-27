/*
 * FunctionParameter represents meaningful information 
 * for a function parameter
 */
package fr.upsud.lri.pathExtractor;

/**
 * The Class FunctionParameter.
 */
public class FunctionParameter {
	
	/**
	 * The Enum CardinalityModifier.
	 */
	protected enum CardinalityModifier {
		
		/** The KLENEE-STAR modifier. */
		STAR ("STAR"),
		
		/** The OPTIONAL modifier. */
		OPTIONAL ("OPTIONAL"),
		
		/** No modifiers */
		NONE ("NONE");
		
		/** The name, used for toString methods. */
		private final String name;
		
		/**
		 * Constructor for the enum element.
		 * @param name of the cardinality modifier
		 */
		private CardinalityModifier(String name) {
			this.name = name;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		public String toString(){
			return name;
		}
	};
	
	/** The position of the parameter. */
	private int position;
	
	/** If false the paths extracted from this parameter will
	 * preserve returned type even if a path extraction rules
	 * want to change it, true if the change will take place
	 * under the same circumstances. */
	private boolean turnReturnedIntoUsed;
	
	/** The cardinality modifier. */
	private CardinalityModifier cardinality;
	
	/**
	 * Instantiates a new function parameter.
	 *
	 * @param position the position of the parameter
	 * @param turnUsedIntoReturned says if the used needs to be preserved or turned into returned
	 * @param cardinality the cardinality of the parameter
	 */
	public FunctionParameter(int position, boolean turnUsedIntoReturned, CardinalityModifier cardinality){
		this.position = position;
		this.turnReturnedIntoUsed = turnUsedIntoReturned;
		this.setCardinality(cardinality);
	}
	
	/**
	 * Sets the position of the parameter.
	 *
	 * @param position the new position of the parameter
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * Gets the position of the parameter.
	 *
	 * @return the position of the parameter
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Sets the turn returned into used.
	 *
	 * @param turnUsedIntoReturned the new turn used into returned
	 */
	public void setTurnUsedIntoReturned(boolean turnUsedIntoReturned) {
		this.turnReturnedIntoUsed = turnUsedIntoReturned;
	}

	/**
	 * Checks if returned paths should be preserved or not.
	 *
	 * @return true, returned paths will not turn into used 
	 * even if a path extraction rule says that
	 */
	public boolean isTurnUsedIntoReturned() {
		return turnReturnedIntoUsed;
	}

	/**
	 * Sets the cardinality of the parameter.
	 *
	 * @param cardinality the new cardinality for the parameter
	 */
	public void setCardinality(CardinalityModifier cardinality) {
		this.cardinality = cardinality;
	}

	/**
	 * Gets the cardinality of the parameter.
	 *
	 * @return the cardinality of the parameter
	 */
	public CardinalityModifier getCardinality() {
		return cardinality;
	}
	
}
