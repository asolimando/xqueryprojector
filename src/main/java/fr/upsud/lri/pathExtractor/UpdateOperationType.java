/*
 * The Update Operation enumeration
 */
package fr.upsud.lri.pathExtractor;

/**
 * The Enum OperationType.
 */
public enum UpdateOperationType {
	
	/** The INSERT LAST XQuery Update Facility primitive. */
	INSERT_LAST,
	
	/** The INSERT FIRST XQuery Update Facility primitive. */
	INSERT_FIRST,
	
	/** The INSERT INTO XQuery Update Facility primitive. */
	INSERT_INTO,
	
	/** The INSERT BEFORE XQuery Update Facility primitive. */
	INSERT_BEFORE,
	
	/** The INSERT AFTER XQuery Update Facility primitive. */
	INSERT_AFTER,
	
	/** The DELETE XQuery Update Facility primitive. */
	DELETE, 
	
	/** The REPLACE XQuery Update Facility primitive. */
	REPLACE, 
	
	/** The RENAME XQuery Update Facility primitive. */
	RENAME
}
