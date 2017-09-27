/*
 * Enumeration for the different FLWOR expressions
 */
package fr.upsud.lri.pathExtractor;

/**
 * The Enum FLWORType.
 */
public enum FLWORType {
	
	/** The FOR clause. */
	FOR, 
	
	/** The LET clause. */
	LET,
	
	/** The QUANTIFIED clause. */
	QUANTIFIED,
	
	/** The WHERE clause. */
	WHERE,
	
	/** The ORDERBY clause. */
	ORDERBY, 
	
	/** The RETURN clause. */
	RETURN;
}
