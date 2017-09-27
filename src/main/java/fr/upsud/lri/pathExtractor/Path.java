/*
 * This class represents a XPath's paths, made of a sequence of StepItem
 * or VarItem elements
 */
package fr.upsud.lri.pathExtractor;

import java.util.LinkedList;
import java.util.List;

import fr.upsud.lri.pathExtractor.PathType;

/**
 * The Class Path.
 */
public class Path extends LinkedList<PathItem> implements Cloneable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The path type. */
	private PathType pathType;
	
	/** The operation type (the update operation 
	 * that made this path extracted). */
	private UpdateOperationType updateType;

	/** The XML document name, set usually through doc() function-calls. */
	private String documentName;
	
	/** True if the category of this returned path should remain
	 * the same even if another rule want to change it to used.
	 * This is useful for path extracted from some functions
	 * and nodeConstructor */
	private boolean returnPreserving = false;
	
	/**
	 * Instantiates a new path.
	 *
	 * @param path the list representing the path we want to build
	 */
	public Path(List<PathItem> path, UpdateOperationType updateType){
		super();
		this.updateType = updateType;
		this.addAll(path);	
	}
	
	/**
	 * Instantiates a new path.
	 *
	 * @param pathItem the only element of our new path
	 * @param returnPreserving sets if the path should preserve its category in case it is returned
	 */
	public Path(PathItem pathItem, boolean returnPreserving, UpdateOperationType updateType){
		super();
		this.add(pathItem);
		this.updateType = updateType;
		this.returnPreserving = returnPreserving;
	}

	/**
	 * Instantiates a new path.
	 *
	 * @param pathType the path type
	 * @param pathItem the only element of our new path
	 * @param returnPreserving sets if the path should preserve its category in case it is returned
	 */
	public Path(PathType pathType, PathItem pathItem, 
			boolean returnPreserving, UpdateOperationType updateType) {
		super();
		this.updateType = updateType;
		this.pathType = pathType;
		this.add(pathItem);
		this.returnPreserving = returnPreserving;
	}
	
	/**
	 * Instantiates a new path.
	 *
	 * @param pathType the path type
	 * @param path the list representing the path we want to build
	 * @param returnPreserving sets if the path should preserve its category in case it is returned
	 */
	public Path(PathType pathType, List<PathItem> path, 
			boolean returnPreserving, UpdateOperationType updateType) {
		super();
		this.updateType = updateType;
		this.pathType = pathType;
		this.addAll(path);
		this.returnPreserving = returnPreserving;
	}
	
	/**
	 * Instantiates a new path.
	 *
	 * @param pathType the path type
	 * @param operationType the operation type
	 * @param path the list representing the path we want to build
	 * @param returnPreserving sets if the path should preserve its category in case it is returned
	 */
	public Path(PathType pathType, UpdateOperationType updateType, List<PathItem> path, boolean returnPreserving) {
		super();
		this.pathType = pathType;
		this.updateType = updateType;
		this.addAll(path);		
		this.returnPreserving = returnPreserving;
	}

	/**
	 * Instantiates a new path.
	 *
	 * @param prefix the prefix of the new path
	 * @param suffix the suffix of the new path
	 * @param pathType the path type of the new path
	 */
	public Path(Path prefix, Path suffix, PathType pathType){
		super();
		this.pathType = suffix.getPathType();
		this.addAll(prefix);
		this.addAll(suffix);
		/*TODO: check if these are right but it should be so
		 beacause the "ground" path is aware of it while
		 the prefix is usually the contextPath that is not */
		this.returnPreserving = suffix.returnPreserving;
		this.updateType = suffix.updateType;
	}
	
	/**
	 * Instantiates a new path.
	 *
	 * @param prefix the prefix of the new path
	 * @param suffix the suffix of the new path
	 * @param pathType the path type
	 * @param operationType the operation type (the update 
	 * operation that made this path extracted)
	 */
	public Path(Path prefix, Path suffix, PathType pathType, 
			UpdateOperationType operationType){
		super();
		this.pathType = suffix.getPathType();
		this.addAll(prefix);
		this.addAll(suffix);
		this.updateType = operationType;
		/*TODO: check if these are right but it should be so
		 beacause the "ground" path is aware of it while
		 the prefix is usually the contextPath that is not */
		this.returnPreserving = suffix.returnPreserving;
		this.updateType = suffix.updateType;
	}

	/**
	 * Instantiates a new path.
	 * @param updateType the type of the operation in which this path appears
	 */
	public Path(UpdateOperationType updateType) {
		super();
		this.updateType = updateType;
	}

	/**
	 * Instantiates a new path starting from it's toString representation 
	 * (for testing).
	 *@param stringRepr the textual representation of the path
	 */
	public Path(String stringRepr) {
		super();
		
		if(stringRepr.isEmpty())
			return;
		
		stringRepr = stringRepr.trim();
		String [] strArray = stringRepr.split("/");
		
		for(String str : strArray){
			str = str.trim();
			if(str.startsWith("doc(\"") && str.endsWith("\")"))
				this.add(new StepItem(str, ""));
			else if(str.startsWith("{LET ") && str.endsWith("}"))
				this.add(new VarItem(str.substring(5, str.length()-1), FLWORType.LET));
			else if(str.startsWith("{FOR ") && str.endsWith("}"))
				this.add(new VarItem(str.substring(5, str.length()-1), FLWORType.FOR));
			else if(str.startsWith("{QUANTIFIED ") && str.endsWith("}"))
				this.add(new VarItem(str.substring(12, str.length()-1), FLWORType.QUANTIFIED));
			else {
				String [] axis_step = str.split("::");
				this.add(new StepItem(axis_step[0], axis_step[1]));
			}				
		}
		if(((StepItem)this.getFirst()).isDoc() == false){
			this.addFirst(new StepItem("/", ""));
		}
	}
	
	// shallow copy of PathItem should be enough (they are not modified, only read)
	/* (non-Javadoc)
	 * @see java.util.LinkedList#clone()
	 */
	public Path clone(){
		Path e = (Path) super.clone();
		e.clear();
		for(PathItem p : this)
			e.add(p.clone());
		return e;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#equals(java.lang.Object)
	 */
	@Override public boolean equals(Object cmpPath) {
		if(this == cmpPath)
			return true;

		if (!(cmpPath instanceof Path))
			return false;
		
		Path cmp = (Path) cmpPath;

		if(this.getOperationType() != cmp.getOperationType())
			return false;
		
		if(this.size() != cmp.size())
			return false;
		
		boolean toRet = true;
		
		for(int c = 0; c < this.size(); c++)
			toRet = toRet && this.get(c).equals(cmp.get(c));

		return toRet;
	}


	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString(){
		return toString(false);
	}
	
	/**
	 * To string method.
	 *
	 * @param hideVar true if you want to hide varItem elements, 
	 * false otherwise
	 * @return the string representing the path
	 */
	public String toString(boolean hideVar){
		StringBuffer toRet = new StringBuffer("Path: ");		
		//boolean skipNext = false;
		
		for(PathItem item : this){
			/*if(skipNext){
				skipNext = false;
				continue;
			}	*/			
			if(item instanceof StepItem)
				if(((StepItem) item).isSlash() || ((StepItem) item).isSlashSlash())
					toRet.append(((StepItem) item).toString() + " ");
				else
					toRet.append(((StepItem) item).toString() + " / ");
			else if(item instanceof VarItem){
				if(!hideVar)
					toRet.append(((VarItem) item).toString() + " / ");
				/*else { 
					toRet.deleteCharAt(toRet.lastIndexOf("/"));
					skipNext = true;
				}*/
			}
		}
		toRet.deleteCharAt(toRet.lastIndexOf("/"));
		toRet = new StringBuffer(toRet.toString().trim());
		if(!hideVar)
			toRet.append(", OperationPath: " + updateType + ", PathType: " + pathType + ", RetFixed: " + isReturnPreserving());
		return toRet.toString();
	}
	
	/**
	 * Checks if a path is query path.
	 *
	 * @return true if the path is a query path,
	 * false otherwise (update path)
	 */
	public boolean isQueryPath() {
		return getPathType().isQueryPathType();
	}
	
	/**
	 * This method returns the last step for a Path (same as getLastStep,
	 * for presentation purpose).
	 *
	 * @return last stepItem element from a path
	 */
	public StepItem Ter() {
		return getLastStep();
	}
	
	/**
	 * This method returns the last step for a Path.
	 *
	 * @return last stepItem element from a path
	 */
	public StepItem getLastStep(){
		PathItem actualItem = null;
		
		for(int c = this.size() - 1; c >= 0; c--){
			actualItem = this.get(c);
		
			if(actualItem instanceof StepItem)
				return (StepItem) actualItem;
		}
		return null;		
	}

	/**
	 * Checks if the path represents a text element.
	 *
	 * @return true if the Path represents a text node, false otherwise
	 */
	public boolean isText() {
		String test = Ter().getTest();
		return 
		(	test.compareToIgnoreCase("text()") == 0)
			||
			(test.compareToIgnoreCase("comment()") == 0)
			||
			(test.compareToIgnoreCase("node()") == 0 && Type("String")
		) && 
		// TODO: is it always node or it can also be string? Should we use Type()?
		(test.compareToIgnoreCase("*") != 0);
	}
	
	/**
	 * Returns the type of the path
	 * TODO: stub, to be implemented using type inference rules.
	 *
	 * @param type the type to test
	 * @return true if String is in Type(Path), false otherwise
	 */
	public boolean Type(String type){
		return false;
	}

	/**
	 * Sets the update operation type that required the 
	 * extraction of this path.
	 *
	 * @param operationType the (update) operation type
	 */
	public void setOperationType(UpdateOperationType operationType) {
		this.updateType = operationType;
	}

	/**
	 * Gets the update operation type that required the 
	 * extraction of this path.
	 *
	 * @return the (update) operation type
	 */
	public UpdateOperationType getOperationType() {
		return updateType;
	}

	/**
	 * Sets the path type.
	 *
	 * @param pathType the new path type
	 */
	public void setPathType(PathType pathType) {
		this.pathType = pathType;
	}

	/**
	 * Gets the path type.
	 *
	 * @return the path type
	 */
	public PathType getPathType() {
		return pathType;
	}

	/**
	 * Sets the XML document name.
	 *
	 * @param documentName the new XML document name to 
	 * which the path is related to
	 */
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	/**
	 * Gets the XML document name.
	 *
	 * @return the XML document name
	 */
	public String getDocumentName() {
		return documentName;
	}

	/**
	 * Gets the first element of type StepItem.
	 *
	 * @return the first element of type StepItem
	 */
	public StepItem getFirstStep() {
		PathItem actualItem = null;
		
		for(int c = 0; c < this.size(); c++){
			actualItem = this.get(c);
		
			if(actualItem instanceof StepItem)
				return (StepItem) actualItem;
		}
		return null;
	}

	/**
	 * Sets if the return path should preserv its category.
	 *
	 * @param returnPreserving true if return category 
	 * will be preserver, false otherwise
	 */
	public void setReturnPreserving(boolean returnPreserving) {
		this.returnPreserving = returnPreserving;
	}

	/**
	 * Checks if the return path preservs its category.
	 *
	 * @return true, if is return preserving, false otherwise
	 */
	public boolean isReturnPreserving() {
		return returnPreserving;
	}

	/**
	 * Checks if the path is absolute or relative.
	 *
	 * @return true if the starting element is a slash, false otherwise
	 */	
	public boolean isAbs() {
		StepItem firstStep = (StepItem) getFirst(); 
		return firstStep.isSlash() || firstStep.isDoc();
	}

	/**
	 * Adds to this path another one as its prefix
	 * @param prefixPath the path to be added as a prefix
	 */
	public void addAsPrefix(Path prefixPath) {
		for(PathItem item : prefixPath)
			this.addFirst(item);
	}

	/**
	 * Adds "parent::node()" as last step of the path
	 */
	public void Par() {
		this.addLast(new StepItem("parent", "node()"));
	}
	
	/**
	 * These method return the same path without the variable information
	 * (encoded in VarItem elements)
	 * @return the same path without variable information
	 */
	public Path removeVarItem(){
		Path newPath = this.clone();
		for (PathItem item : this) {
			if(item.isVarItem())
				newPath.remove(item);
		}
		return newPath;
	}
}
