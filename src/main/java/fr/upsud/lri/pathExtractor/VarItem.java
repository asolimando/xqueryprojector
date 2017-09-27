/*
 * This class represent a path's component representing the information 
 * of a variable
 */
package fr.upsud.lri.pathExtractor;

/**
 * The Class VarItem.
 */
public class VarItem extends PathItem {
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (forVar ? 1231 : 1237);
		result = prime * result + ((varName == null) ? 0 : varName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VarItem other = (VarItem) obj;
		if (forVar != other.forVar)
			return false;
		if (varName == null) {
			if (other.varName != null)
				return false;
		} else if (!varName.equalsIgnoreCase(other.varName))
			return false;
		return true;
	}

	/** True if the variable was binded by a for clause. */
	private boolean forVar;
	
	/** The variable name. */
	private String varName;
	
	/** The type of the FLWOR expression that binded the variable*/
	private FLWORType type;
	
	/**
	 * Instantiates a new variable item.
	 *
	 * @param varName the variable name
	 * @param flworType the flwor expression type
	 */
	public VarItem(String varName, FLWORType flworType){
		this.forVar = (flworType == FLWORType.FOR);
		this.type = flworType;
		this.varName = varName;
	}

	/**
	 * Sets it to true if the variable was binded by a for clause.
	 *
	 * @param forVar the new boolean value
	 */
	public void setForVar(boolean forVar) {
		this.forVar = forVar;
	}

	/**
	 * Checks if the variable was binded by a for clause.
	 *
	 * @return true, if the variable was binded by a for clause
	 */
	public boolean isForVar() {
		return forVar;
	}

	/**
	 * Sets the variable's name.
	 *
	 * @param varName the new variable's name
	 */
	public void setVarName(String varName) {
		this.varName = varName;
	}

	/**
	 * Gets the variable's name.
	 *
	 * @return the variable's name
	 */
	public String getVarName() {
		return varName;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuffer toRet = new StringBuffer();
		toRet.append("{");
		if(type == FLWORType.FOR)
			toRet.append("FOR");
		else if (type == FLWORType.LET)
			toRet.append(FLWORType.LET);
		else if (type == FLWORType.QUANTIFIED)
			toRet.append(FLWORType.QUANTIFIED);
		toRet.append(" " + varName + "}");
		return toRet.toString();
	}

	/**
	 * Sets the type of the FLWOR expression that binded the variable
	 * @param type of the FLWOR expression
	 */
	public void setType(FLWORType type) {
		this.type = type;
	}

	/**
	 * Returns the type of the FLWOR expression that binded the variable
	 * @return the type of the FLWOR expression that binded the variable
	 */
	public FLWORType getType() {
		return type;
	}
}
