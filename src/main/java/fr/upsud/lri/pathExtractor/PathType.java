/*
 * Paths' categories
 */
package fr.upsud.lri.pathExtractor;

/**
 * The Enum PathType.
 */
public enum PathType {
	
	/** The NODE RETURNED path type (for query). */
	NODE_RETURNED (false, "NODE_RETURNED", ExtractedPathsType.EP_QUERY),
	
	/** The STRING RETURNED path type (for query). */
	STRING_RETURNED (false, "STRING_RETURNED", ExtractedPathsType.EP_QUERY),
	
	/** The NODE USED path type (for query). */
	NODE_USED (false, "NODE_USED", ExtractedPathsType.EP_QUERY),
	
	/** The STRING USED path type (for query). */
	STRING_USED (false, "STRING_USED", ExtractedPathsType.EP_QUERY),
	
	/** The EVERYTHING BELOW USED path type (for query). */
	EVERYTHING_BELOW_USED (false, "EVERYTHING_BELOW_USED", ExtractedPathsType.EP_QUERY),
	
	
	/** The NODE ABSOLUTE path type (for conditions). */
	NODE_ABSOLUTE(false, "NODE_ABSOLUTE", ExtractedPathsType.EP_CONDITION),
	
	/** The NODE RELATIVE path type (for conditions). */
	NODE_RELATIVE(false, "NODE_RELATIVE", ExtractedPathsType.EP_CONDITION),
	
	/** The STRING ABSOLUTE path type (for conditions). */
	STRING_ABSOLUTE(false, "STRING_ABSOLUTE", ExtractedPathsType.EP_CONDITION),
	
	/** The STRING RELATIVE path type (for conditions). */
	STRING_RELATIVE(false, "STRING_RELATIVE", ExtractedPathsType.EP_CONDITION),
	
	/** The EVERYTHING BELOW USED ABSOLUTE path type (for conditions). */
	EVERYTHING_BELOW_ABSOLUTE(false, "EVERYTHING_BELOW_ABSOLUTE", ExtractedPathsType.EP_CONDITION),
	
	/** The EVERYTHING BELOEW RELATIVE path type (for conditions). */
	EVERYTHING_BELOW_RELATIVE(false, "EVERYTHING_BELOW_RELATIVE", ExtractedPathsType.EP_CONDITION),

	
	
	/** The NODE RETURNED ABSOLUTE path type (for query paths). */
	NODE_RETURNED_ABSOLUTE (false, "NODE_RETURNED_ABSOLUTE", ExtractedPathsType.EP_QUERYPATH),
	
	/** The NODE RETURNED RELATIVE path type (for query paths). */
	NODE_RETURNED_RELATIVE (false, "NODE_RETURNED_RELATIVE", ExtractedPathsType.EP_QUERYPATH),
	
	/** The STRING RETURNED ABSOLUTE path type (for query paths). */
	STRING_RETURNED_ABSOLUTE (false, "STRING_RETURNED_ABSOLUTE", ExtractedPathsType.EP_QUERYPATH),
	
	/** The STRING RETURNED RELATIVE path type (for query paths). */
	STRING_RETURNED_RELATIVE (false, "STRING_RETURNED_RELATIVE", ExtractedPathsType.EP_QUERYPATH),
	
	/** The NODE USED ABSOLUTE path type (for query paths). */
	NODE_USED_ABSOLUTE (false, "NODE_USED_ABSOLUTE", ExtractedPathsType.EP_QUERYPATH),
	
	/** The NODE USED RELATIVE path type (for query paths). */
	NODE_USED_RELATIVE (false, "NODE_USED_RELATIVE", ExtractedPathsType.EP_QUERYPATH),
	
	/** The STRING USED ABSOLUTE path type (for query paths). */
	STRING_USED_ABSOLUTE (false, "STRING_USED_ABSOLUTE", ExtractedPathsType.EP_QUERYPATH),
	
	/** The STRING USED RELATIVE path type (for query paths). */
	STRING_USED_RELATIVE (false, "STRING_USED_RELATIVE", ExtractedPathsType.EP_QUERYPATH),
	
	/** The EVERYTHING BELOW USED ABSOLUTE path type (for query paths). */
	EVERYTHING_BELOW_USED_ABSOLUTE (false, "EVERYTHING_BELOW_USED_ABSOLUTE", ExtractedPathsType.EP_QUERYPATH),
	
	/** The EVERYTHING BELOW USED RELATIVE path type (for query paths). */
	EVERYTHING_BELOW_USED_RELATIVE (false, "EVERYTHING_BELOW_USED_RELATIVE", ExtractedPathsType.EP_QUERYPATH),
	
	
	
	/**
	 * The NODE_ONLY path type (for update paths)
	 */
	NODE_ONLY (true, "NODE_ONLY", ExtractedPathsType.EP_UPDATE), 
	/**
	 * The ONE_LEVEL_BELOW path type (for update paths)
	 */
	ONE_LEVEL_BELOW (true, "ONE_LEVEL_BELOW", ExtractedPathsType.EP_UPDATE), 
	/**
	 * The EVERYTHING_BELOW path type (for update paths)
	 */
	EVERYTHING_BELOW (true, "EVERYTHING_BELOW", ExtractedPathsType.EP_UPDATE);
	
	
	
	/** Variable to discriminate between update and query path type. */
	private final boolean isQueryPathType;
	
	/** The name, used for toString methods. */
	private final String name;
	
	/** The extracted path type legal for the constant. */
	private final ExtractedPathsType extractedPathType;
	
	/**
	 * Instantiates a new path type.
	 *
	 * @param isQueryPathType the is query path type
	 * @param name the name
	 * @param epType type of the extractedPath legal for this constant
	 */
	private PathType(boolean isQueryPathType, String name, ExtractedPathsType epType){
		this.isQueryPathType = isQueryPathType;
		this.name = name;
		this.extractedPathType = epType;
	}
	
	/**
	 * Checks if is query path type.
	 *
	 * @return true, if is query path type
	 */
	public boolean isQueryPathType(){
		return isQueryPathType;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	public String toString(){
		return name;
	}

	/**
	 * Gets the extracted path type legal for this constants.
	 *
	 * @return the extracted path type
	 */
	public ExtractedPathsType getExtractedPathType() {
		return extractedPathType;
	}
}
