package fr.upsud.lri.pathExtractor;

public enum ExtractedPathsType {
	
	/** Extracted paths for condition expressions */
	EP_CONDITION, 
	/** Extracted paths for generic query expressions */
	EP_QUERY, 
	/** Extracted paths for query path, that is paths 
	 * containing variables and FLWOR expressions 
	 * in arbitrary positions */
	EP_QUERYPATH,
	/**
	 * Extracted paths for updates
	 */
	EP_UPDATE
}
