/*
 * This class represents a sequence of Paths (for position is irrelevant, though)
 */
package fr.upsud.lri.pathExtractor;

import java.util.LinkedList;
import java.util.List;

/**
 * The Class Paths.
 */
public class Paths extends LinkedList<Path> implements Cloneable {
	
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new paths.
	 */
	public Paths(){
		super();
	}	
	
	/**
	 * Instantiates a new paths starting from a list of Path elements.
	 *
	 * @param list the list
	 */
	public Paths(List<Path> list){
		super(list);
	}	
	
	/* (non-Javadoc)
	 * @see java.util.LinkedList#add(java.lang.Object)
	 */
	public boolean add(Path p){
		return super.add(p);
	}
	
	/**
	 * Compare two Paths without considering element order.
	 *
	 * @param cmpPaths the paths element to compare with the actual one
	 * @return true if the two Paths objects are equal, false otherwise
	 */
	@Override public boolean equals(Object cmpPaths) {
		if (this == cmpPaths) 
			return true;

		if (!(cmpPaths instanceof Paths))
			return false;

		// we need to clone so we can safely delete elements later
		Paths cmp = ((Paths) cmpPaths).clone();

		if(cmp.size() != this.size())
			return false;

		boolean toRet = true;
		
		for(int c = 0; c < this.size(); c++){
			Path actualPath = this.get(c);
			if(cmp.isEmpty()){
				toRet = false;
				break;
			}
			
			if(cmp.contains(actualPath))
				cmp.remove(actualPath);
			else{
				toRet = false;
				break;
			}
		}
		return toRet;
	}
	
	/**
	 * Adds the path as prefix to a sequence of Paths.
	 *
	 * @param prefixPath the prefix path
	 * @param cloningRequired if true will clone the starting Paths element,
	 * otherwise will work directly on it, this is needed by the overloaded
	 * function that works with Paths as prefixes
	 * @return the empty path sequence if the starting sequence of path is empty,
	 * the concatenation with the prefix of its element otherwise
	 */
	public Paths addPathAsPrefix(Path prefixPath, boolean cloningRequired){
		if(this.isEmpty())
			return new Paths();
		
		Paths retPaths = cloningRequired ? this.clone() : this;
		
		for(Path actualPath : this)
			actualPath.addAll(0, prefixPath);
		
		return retPaths;
	}
	
	/**
	 * Adds the prefix-paths to a sequence of Paths.
	 *
	 * @param prefixPath the prefix paths
	 * @return the empty path sequence if the starting sequence of path is empty,
	 * the concatenation with the prefixes of its element otherwise
	 */
	public Paths addPathAsPrefix(Paths prefixPaths){
		if(this.isEmpty() || prefixPaths.isEmpty())
			return new Paths();
		
		Paths retPaths = this.clone();
		
		for(Path actualPrefix : prefixPaths)
			retPaths.addPathAsPrefix(actualPrefix, false);
		
		return retPaths;
	}
	
	/**
	 * Removes the path.
	 *
	 * @param path the path to remove
	 * @return true, if successful, false otherwise
	 */
	public boolean remove(Path path){
		return super.remove(path);
	}
	
	/**
	 * Adds all the paths contained in another Paths object.
	 *
	 * @param paths the source sequence of path to insert
	 * @return true, if successful, false otherwise
	 */
	public boolean addAll(Paths paths){
		return super.addAll(paths);
	}
	
	
	/**
	 * Adds all the paths contained in another Paths object leaving out fixReturned one.
	 *
	 * @param paths the source sequence of path to insert
	 * @return true, if successful, false otherwise
	 */
	public boolean addAllFilteringReturnFixed(Paths paths){
		boolean toRet = true;

		for(Path path : paths)
			if(!path.isReturnPreserving())
				toRet = toRet && this.add(path);

		return toRet;
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.LinkedList#clone()
	 */
	public Paths clone() {
/*		Paths e = (Paths) super.clone();
		e.clear();*/
		Paths e = new Paths();
		for(Path path : this)
			e.add(path.clone());
		return e;
	}
	
	/**
	 * Add to the Paths object the path that are absolute or relative, depending on the first parameter
	 * @param absPaths if true filter out relative paths, otherwise will filter absolute ones
	 * @param paths the list of path to add
	 * @return true if the insertion was ok, false otherwise
	 */
	public boolean addAbsOrRel(boolean absPaths, Paths paths){
		boolean toRet = true;
		
		for(Path path : paths){
			if(absPaths){
				if(path.isAbs())
					toRet = toRet && this.add(path);
			}
			else{
				if(!path.isAbs())
					toRet = toRet && this.add(path);
			}
		}
		return toRet;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	@Override
	public String toString(){
		return toString(false);
	}
	
	public String toString(boolean summarize){
		StringBuffer toRet = new StringBuffer();
		for(Path p : this)
			toRet.append(p.toString(summarize) + "\n");
		String res = toRet.toString();
		return res.isEmpty() ? res : res.substring(0, res.length()-1);
	}
	
	/**
	 * Methods that extracts only the paths for which return category is fixed
	 * @return the paths for which return category is fixed
	 */
	public Paths returnOnlyFixedReturnPaths(){
		Paths onlyFixedPaths = this.clone();
		
		for(Path path : this)
			if(!path.isReturnPreserving())
				onlyFixedPaths.remove(path);
		
		return onlyFixedPaths;
	}

	/**
	 * Adds all the paths that are not "return fixed"
	 * @param addPathAsPrefix the paths to add to the object
	 */
	public void addAllFromCondFilteringReturnFixed(Paths addPathAsPrefix) {
		for(Path path : addPathAsPrefix)
			if(!path.isReturnPreserving())
				this.add(path);
	}
	
	/**
	 * This methods apply Par() method to every component of the list
	 */
	public void Par(){
		for(Path path : this)
			path.Par();
	}
}
