/*
 * This class represents the extracted path from a query/update, 
 * divided in different categories
 */
package fr.upsud.lri.pathExtractor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import fr.upsud.lri.pathExtractor.ExtractedPathsType;

/**
 * The Class ExtractedPaths.
 */
public class ExtractedPaths implements Cloneable {

	/** The map between path categories and paths belonging to them. */
	private Map<PathType, Paths> pathsMap = null;
	
	/** The type of extractedPaths (depends on the rule used for extraction). */
	private ExtractedPathsType type;
	
	/**
	 * Instantiates a new extracted paths.
	 *
	 * @param type the type of the map
	 */
	public ExtractedPaths(ExtractedPathsType type) {
		super();
		this.type= type; 
		initializePathsMap(type);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public ExtractedPaths clone() {
		ExtractedPaths e = new ExtractedPaths(type);
		for(PathType pt : this.keySet())
			e.put(pt, get(pt).clone());
		return e;
	}

	/**
	 * Adds all the paths preserving their category.
	 *
	 * @param exPaths the paths to add
	 * @return true, if successful, false otherwise
	 */
	public boolean addAll(ExtractedPaths exPaths){
		boolean toRet = true;
		
		for(PathType pt : exPaths.keySet())
			toRet = toRet && addPath(pt, exPaths.get(pt));			
		return toRet;
	}
	
	/**
	 * Adds the all turning returned into used but preserving fixed ones.
	 *
	 * @param exPaths the extracted paths to add
	 * @return true, if successful, false otherwise
	 */
	public boolean addAllTurningReturnedIntoUsedButPreservingFixed(ExtractedPaths exPaths){
		boolean toRet = true;
		
		if(this.getType() != exPaths.getType())
			return false;
		
		for(PathType pt : exPaths.keySet()){
			if(pt == PathType.NODE_RETURNED || pt == PathType.STRING_RETURNED){
				
				Paths listUsed = (pt == PathType.STRING_RETURNED 
						? this.get(PathType.STRING_USED) : this.get(PathType.NODE_USED));
				Paths listReturned = (pt == PathType.STRING_RETURNED 
						? this.get(PathType.STRING_RETURNED) : this.get(PathType.NODE_RETURNED));

				
				for(Path path : exPaths.get(pt)){
					if(!path.isReturnPreserving())
						toRet = toRet && listUsed.add(path);
					else
						toRet = toRet && listReturned.add(path);
				}
			}
			else
				toRet = toRet && addPath(pt, exPaths.get(pt).clone());			
		}
		return toRet;
	}
	
	/**
	 * Adds the path.
	 *
	 * @param pathType the path type used to put it the right category
	 * @param path the path to add
	 * @return true, if successful, false otherwise
	 */
	public boolean addPath(PathType pathType, Path path){
		if(!pathsMap.get(pathType).add(path))
			throw new IllegalStateException("Impossible to insert path " 
				+ path + " in the list of " 
				+ pathType.toString() + "'s paths.");
		return true;
	}
	
	/**
	 * Adds all the paths in the list.
	 *
	 * @param pathType the path type used to put the paths in the right category
	 * @param pathList the path list to add
	 * @return true, if successful, false otherwise
	 */
	public boolean addPath(PathType pathType, Paths pathList){
		boolean toRet = true;
		for(Path p : pathList)
			toRet = toRet && addPath(pathType, p);
		
		return toRet;
	}
	
	/**
	 * Initialize paths map.
	 *
	 * @param type the type of the map used to choose the categories to initialize
	 */
	private void initializePathsMap(ExtractedPathsType type){
		
		pathsMap = new ConcurrentHashMap<PathType, Paths>();
		
		switch(type){
			case EP_QUERY:
				pathsMap.put(PathType.EVERYTHING_BELOW_USED, new Paths());
				pathsMap.put(PathType.NODE_RETURNED, new Paths());
				pathsMap.put(PathType.NODE_USED, new Paths());
				pathsMap.put(PathType.STRING_RETURNED, new Paths());
				pathsMap.put(PathType.STRING_USED, new Paths());
				break;
			case EP_CONDITION:
				pathsMap.put(PathType.NODE_ABSOLUTE, new Paths());
				pathsMap.put(PathType.NODE_RELATIVE, new Paths());
				pathsMap.put(PathType.STRING_ABSOLUTE, new Paths());
				pathsMap.put(PathType.STRING_RELATIVE, new Paths());
				pathsMap.put(PathType.EVERYTHING_BELOW_RELATIVE, new Paths());
				pathsMap.put(PathType.EVERYTHING_BELOW_ABSOLUTE, new Paths());				
				break;
			case EP_QUERYPATH:
				pathsMap.put(PathType.NODE_RETURNED_ABSOLUTE, new Paths());
				pathsMap.put(PathType.NODE_RETURNED_RELATIVE, new Paths());
				pathsMap.put(PathType.STRING_RETURNED_ABSOLUTE, new Paths());
				pathsMap.put(PathType.STRING_RETURNED_RELATIVE, new Paths());
				pathsMap.put(PathType.NODE_USED_ABSOLUTE, new Paths());
				pathsMap.put(PathType.NODE_USED_RELATIVE, new Paths());
				pathsMap.put(PathType.STRING_USED_ABSOLUTE, new Paths());
				pathsMap.put(PathType.STRING_USED_RELATIVE, new Paths());
				pathsMap.put(PathType.EVERYTHING_BELOW_USED_ABSOLUTE, new Paths());
				pathsMap.put(PathType.EVERYTHING_BELOW_USED_RELATIVE, new Paths());
				break;
			case EP_UPDATE:
				pathsMap.put(PathType.NODE_ONLY, new Paths());
				pathsMap.put(PathType.ONE_LEVEL_BELOW, new Paths());
				pathsMap.put(PathType.EVERYTHING_BELOW, new Paths());
		}
	}
	
	/**
	 * Returns the keys present in the map.
	 *
	 * @return the keys present in the map
	 */
	public Set<PathType> keySet(){
		return pathsMap.keySet();
	}
	
	/**
	 * Gets the paths belonging to that category.
	 *
	 * @param pathType the path type from which retrieve the paths
	 * @return the paths belonging to the requested category
	 */
	public Paths get(PathType pathType){
		return pathsMap.get(pathType);
	}
	
	/**
	 * Insert the mapping between the type and its content (the paths).
	 *
	 * @param pathType the path type to insert
	 * @param paths the paths associated to the pathType
	 */
	public void put(PathType pathType, Paths paths){
		pathsMap.put(pathType, paths);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		StringBuffer str = new StringBuffer();
		for(PathType key : pathsMap.keySet())
			str.append(key + " =\n" + pathsMap.get(key) + "\n");
		return str.toString();
	}

	/**
	 * Print the extracted paths in a less verbose way
	 * @param summarize, true if the output should be summarized, false otherwise
	 * @return the output, summarized or not depending on the parameter's value
	 */
	public String toString(boolean summarize) {
		if(summarize == false)
			return this.toString();
		
		StringBuffer str = new StringBuffer();
		for(PathType key : pathsMap.keySet()){
			str.append(key + " = {\n");
			for (Path path : pathsMap.get(key)) {
				str.append(path.toString(summarize) + "\n");
			} 
			str.append("}\n");
		}
		return str.toString();
	}
	
	/**
	 * Adds this paths the as suffixes to the paths of the corresponding category.
	 *
	 * @param suffixes the paths to be used as suffixes
	 * @return true, if successful, false otherwise
	 */
	public boolean addAsSuffixes(ExtractedPaths suffixes) {
		boolean toRet = true;
		for(PathType pt : suffixes.keySet()){
			// the suffix-paths are empty, so I use the prefix List directly
			if(get(pt).isEmpty() && !suffixes.get(pt).isEmpty()) {
				remove(pt);
				this.pathsMap.put(pt, suffixes.get(pt));
			}
			else
				for(Path prefixPath : get(pt))
					for(Path p : suffixes.get(pt))
						toRet = toRet && prefixPath.addAll(p);
		}
		return toRet;
	}

	/**
	 * Removes the category and all the paths associated.
	 *
	 * @param pt the path type to remove
	 */
	private void remove(PathType pt) {
		this.pathsMap.remove(pt);
	}

	/**
	 * Adds the prefix to all the paths in the map.
	 *
	 * @param prefix the prefix to append in front of the paths
	 */
	public void addPrefix(StepItem prefix) {
		addSuffixOrPrefix(prefix, true);
	}
	
	/**
	 * Adds the suffix to all the paths in the map.
	 *
	 * @param suffix the suffix to append to all the paths
	 */
	public void addSuffix(StepItem suffix) {
		addSuffixOrPrefix(suffix, false);
	}
	
	/**
	 * Adds the item as a suffix or a prefix to all the paths in the map.
	 *
	 * @param step the step to use as a prefix/suffix
	 * @param prefix, boolean value true for prefix insertion, false for prefix insertion
	 */
	public void addSuffixOrPrefix(StepItem step, boolean prefix){
		for(PathType pt : keySet()){
			if(get(pt).isEmpty())
				continue;
			for(Path actualPath : get(pt)){
				if(prefix)
					actualPath.addFirst(step);
				else
					actualPath.addLast(step);					
			}
		}
	}

	/**
	 * Adds the all the input paths to that particular category.
	 *
	 * @param pt the category into the paths will be inserted
	 * @param paths2 the paths to be inserted
	 * @return true, if successful, false otherwise
	 */
	public boolean addAll(PathType pt, Paths paths2) {
		Paths list = pathsMap.get(pt);
		boolean toRet = true;

		for(Path p : paths2)
			toRet = toRet && list.add(p);
		
		return toRet;
	}
	
	/**
	 * Turns node/string returned into used and clear the node/string returned categories.
	 */
	public void turnReturnedIntoUsed(){
		if(this.getType() != ExtractedPathsType.EP_QUERY)
			return;
		
		this.addAll(PathType.STRING_USED, this.get(PathType.STRING_RETURNED));
		this.addAll(PathType.NODE_USED, this.get(PathType.NODE_RETURNED));
		// we also need to clear string/node returned list once copied
		this.get(PathType.STRING_RETURNED).clear();
		this.get(PathType.NODE_RETURNED).clear();
	}
	
	/**
	 * Turns node/string returned paths into 
	 * everything below used and clears the node/string returned categories.
	 */
	public void turnReturnedIntoEBU(){
		if(this.getType() != ExtractedPathsType.EP_QUERY)
			return;
		
		this.addAll(PathType.EVERYTHING_BELOW_USED, this.get(PathType.STRING_RETURNED));
		this.addAll(PathType.EVERYTHING_BELOW_USED, this.get(PathType.NODE_RETURNED));
		// we also need to clear string/node returned list once copied
		this.get(PathType.STRING_RETURNED).clear();
		this.get(PathType.NODE_RETURNED).clear();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override 
	public boolean equals(Object cmpExPaths) {
		if(this == cmpExPaths)
			return true;
		if(!(cmpExPaths instanceof ExtractedPaths))
			return false;		
		ExtractedPaths cmp = (ExtractedPaths) cmpExPaths;
		
		if(cmp.getType() != this.getType())
			return false;
		
		boolean toRet = true;
		
		for(PathType pt : this.keySet())
			toRet = toRet && this.get(pt).equals(cmp.get(pt));

		return toRet;
	}
	
	/**
	 * Method to add paths to the relative or absolute class of the choosen category.
	 *
	 * @param condPathType category to add to (relative or absolute is the same)
	 * @param paths the paths to add to the choosen category
	 * @return true if the addition was ok, false otherwise
	 */
	public boolean addAbsOrRel(PathType condPathType, Paths paths){
		boolean toRet = true;
		if(condPathType == PathType.EVERYTHING_BELOW_ABSOLUTE 
				||
				condPathType == PathType.EVERYTHING_BELOW_RELATIVE){
			for(Path path : paths){
				if(path.isAbs())
					toRet = toRet && pathsMap.get(
							PathType.EVERYTHING_BELOW_ABSOLUTE).add(path);
				else
					toRet = toRet && pathsMap.get(
							PathType.EVERYTHING_BELOW_RELATIVE).add(path);
			}
		}
		else if(condPathType == PathType.NODE_ABSOLUTE || condPathType == PathType.NODE_RELATIVE){
			for(Path path : paths){
				if(path.isAbs())
					toRet = toRet && pathsMap.get(
							PathType.NODE_ABSOLUTE).add(path);
				else
					toRet = toRet && pathsMap.get(
							PathType.NODE_RELATIVE).add(path);
			}
		}
		else if(condPathType == PathType.STRING_ABSOLUTE ||
		   condPathType == PathType.STRING_RELATIVE){
			for(Path path : paths){
				if(path.isAbs())
					toRet = toRet && pathsMap.get(
							PathType.STRING_ABSOLUTE).add(path);
				else
					toRet = toRet && pathsMap.get(
							PathType.STRING_RELATIVE).add(path);
			}
		}
		else 
			return false;
		return toRet;
	}
	
	/**
	 * Adds all the extracted paths from a query to a condition mapping.
	 *
	 * @param srcPathType the source path type
	 * @param srcPaths the source paths
	 * @return true, if successful, false otherwise
	 */
	public boolean addAllFromQueryToCondition(PathType srcPathType, Paths srcPaths) {
		boolean toRet = true;

		if(srcPathType == PathType.STRING_RETURNED || srcPathType == PathType.STRING_USED){
			for(Path p : srcPaths){
				if(p.isAbs())
					toRet = toRet && pathsMap.get(PathType.STRING_ABSOLUTE).add(p);
				else
					toRet = toRet && pathsMap.get(PathType.STRING_RELATIVE).add(p);
			}
		}		
		
		else if(srcPathType == PathType.NODE_RETURNED || srcPathType == PathType.NODE_USED){
			for(Path p : srcPaths){
				if(p.isAbs())
					toRet = toRet && pathsMap.get(PathType.NODE_ABSOLUTE).add(p);
				else
					toRet = toRet && pathsMap.get(PathType.NODE_RELATIVE).add(p);
			}
		}
		
		else if(srcPathType == PathType.EVERYTHING_BELOW_USED){
			for(Path p : srcPaths){
				if(p.isAbs())
					toRet = toRet && pathsMap.get(PathType.EVERYTHING_BELOW_ABSOLUTE).add(p);
				else
					toRet = toRet && pathsMap.get(PathType.EVERYTHING_BELOW_RELATIVE).add(p);
			}
		}
		
		else 
			return false; // invalid pathtype
		return toRet;
	}
	

	/**
	 * Adds the axis to the first step of all the paths.
	 *
	 * @param axis the axis to add
	 */
	public void addAxis(String axis) {
		for(PathType pt : pathsMap.keySet())
			for(Path p : pathsMap.get(pt))
				p.getFirstStep().setAxis(axis);
	}

	/**
	 * Adds the step as a prefix to all the paths in the map.
	 *
	 * @param stepItem the step item to use as a prefix
	 */
	public void addAsPrefixStep(StepItem stepItem) {
		for(PathType pt : pathsMap.keySet())
			for(Path p : pathsMap.get(pt))
				p.addFirst(stepItem);
	}
	
	/**
	 * Adds the step as a suffix to all the paths in the map.
	 *
	 * @param stepItem the step item to use as a suffix
	 */
	public void addAsSuffixStep(StepItem stepItem){
		for(PathType pt : pathsMap.keySet())
			for(Path p : pathsMap.get(pt))
				p.addLast(stepItem);
	}
	
	/**
	 * Sets the type of the map.
	 *
	 * @param type the new type for the map
	 */
	public void setType(ExtractedPathsType type) {
		this.type = type;
	}

	/**
	 * Gets the type of the map.
	 *
	 * @return the type of the map
	 */
	public ExtractedPathsType getType() {
		return type;
	}

	/**
	 * Returns the extracted paths using update categories.
	 *
	 * @param ctxPath the context path, used if the starting map is of type condition
	 * @return the extracted paths turned into update categories
	 */
	public ExtractedPaths toExtractPathsForUpdate(Path ctxPath){
		ExtractedPathsType type = this.getType();
		
		if(type == ExtractedPathsType.EP_UPDATE)
			return this;
		
		ExtractedPaths updatePaths = new ExtractedPaths(ExtractedPathsType.EP_UPDATE);
		ExtractedPaths actual = this;
		
		if(type == ExtractedPathsType.EP_CONDITION)
			actual = this.toExtractPathsForQuery(ctxPath);
		
		// now actual variable is for sure of type EP_QUERY

		// NODE_ONLY <- NODE_USED
		updatePaths.get(PathType.NODE_ONLY).addAll(actual.get(PathType.NODE_USED));
		// ONE_LEVEL_BELOW <- STRING_USED, STRING_RETURNED
		updatePaths.get(PathType.ONE_LEVEL_BELOW).addAll(actual.get(PathType.STRING_USED));
		updatePaths.get(PathType.ONE_LEVEL_BELOW).addAll(actual.get(PathType.STRING_RETURNED));
		// EVERYTHING_BELOW <- EVERYTHING_BELOW_USED, NODE_RETURNED
		updatePaths.get(PathType.EVERYTHING_BELOW).addAll(actual.get(PathType.EVERYTHING_BELOW_USED));
		updatePaths.get(PathType.EVERYTHING_BELOW).addAll(actual.get(PathType.NODE_RETURNED));
		
		return updatePaths;
	}
	
	/**
	 * Returns the extracted paths using condition categories.
	 *
	 * @return the extracted paths turned into condition categories
	 */
	public ExtractedPaths toExtractPathsForCond(){
		if(this.getType() == ExtractedPathsType.EP_CONDITION)
			return this;
		
		ExtractedPaths exPaths = new ExtractedPaths(ExtractedPathsType.EP_CONDITION);
		
		for(Path path : this.get(PathType.NODE_USED)){
			if(path.isAbs())
				exPaths.addPath(PathType.NODE_ABSOLUTE, path);
			else
				exPaths.addPath(PathType.NODE_RELATIVE, path);
		}
		
		for(Path path : this.get(PathType.NODE_RETURNED)){
			if(path.isAbs())
				exPaths.addPath(PathType.NODE_ABSOLUTE, path);
			else
				exPaths.addPath(PathType.NODE_RELATIVE, path);
		}
		
		for(Path path : this.get(PathType.STRING_USED)){
			if(path.isAbs())
				exPaths.addPath(PathType.STRING_ABSOLUTE, path);
			else
				exPaths.addPath(PathType.STRING_RELATIVE, path);
		}
		
		for(Path path : this.get(PathType.STRING_RETURNED)){
			if(path.isAbs())
				exPaths.addPath(PathType.STRING_ABSOLUTE, path);
			else
				exPaths.addPath(PathType.STRING_RELATIVE, path);
		}
		
		for(Path path : this.get(PathType.EVERYTHING_BELOW_USED)){
			if(path.isAbs())
				exPaths.addPath(PathType.EVERYTHING_BELOW_ABSOLUTE, path);
			else
				exPaths.addPath(PathType.EVERYTHING_BELOW_RELATIVE, path);
		}
		
		return exPaths;
	}
	
	/**
	 * Returns the extracted paths using query categories.
	 *
	 * @param ctxPath the context path
	 * @return the extracted paths turned into query categories
	 */
	public ExtractedPaths toExtractPathsForQuery(Path ctxPath) {
		if(this.getType() == ExtractedPathsType.EP_QUERY)
			return this;
		
		ExtractedPaths exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		
		exPaths.addAll(PathType.NODE_USED, this.get(PathType.NODE_ABSOLUTE));
		exPaths.addAll(PathType.NODE_USED, this.get(PathType.NODE_RELATIVE).addPathAsPrefix(ctxPath, false));

		exPaths.addAll(PathType.STRING_USED, this.get(PathType.STRING_ABSOLUTE));
		exPaths.addAll(PathType.STRING_USED, this.get(PathType.STRING_RELATIVE).addPathAsPrefix(ctxPath, false));

		exPaths.addAll(PathType.EVERYTHING_BELOW_USED, this.get(PathType.EVERYTHING_BELOW_ABSOLUTE));
		exPaths.addAll(PathType.EVERYTHING_BELOW_USED, this.get(PathType.EVERYTHING_BELOW_RELATIVE).addPathAsPrefix(ctxPath, false));
		
		return exPaths;
	}
	
	/**
	 * Adds the parent step (parent::node()) as a suffix to all the paths of the pathtype.
	 *
	 * @param pathType the path type on which Par will be applied
	 */
	public void Par(PathType pathType){
		this.get(pathType).Par();
	}
}
