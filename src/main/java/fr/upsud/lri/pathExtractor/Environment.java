/*
 * Environment class represents a static environment used for
 * keeping track of variables' binding
 */
package fr.upsud.lri.pathExtractor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class Environment.
 */
public class Environment implements Cloneable {

	/** The mapping between variables and associated paths */
	private Map<String, Paths> env = null;
	
	/** If a variable name is here it was binded in a for-clause,
	 * otherwise in a let-clause. */
	private Set<String> forVar = null;

	/**
	 * Instantiates a new environment.
	 */
	public Environment(){
		super();
		Init();
	}
	
	/**
	 * Instantiates a new environment starting from another one.
	 *
	 * @param env the environment used to fill the new one
	 */
	public Environment(Environment env) {
		super();
		Init();
		this.env.putAll(env.getMapping());
		this.forVar.addAll(env.forVar);
	}

	/**
	 * Gets the mapping between variables and associated paths.
	 *
	 * @return the mapping between variables and associated paths
	 */
	public Map<String, Paths> getMapping() {
		return env;
	}
	
	/**
	 * Initialize the environment.
	 */
	private void Init(){
		this.env = new ConcurrentHashMap<String, Paths>();
		this.forVar = new HashSet<String>();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Environment clone() {
		try {
			Environment e = (Environment) super.clone();
			e.env = new ConcurrentHashMap<String, Paths>();
			for(String key : env.keySet())
				e.env.put(key, env.get(key).clone());
			return e;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets the bindings for a given variable if it exists.
	 *
	 * @param varName the name of the variable
	 * @return the bindings of the variable if exists
	 */
	public Paths getBindings(String varName){
		if(env.containsKey(varName))
			return env.get(varName);
		return null;
	}
	
	/**
	 * Checks if the environment is empty.
	 *
	 * @return true if there are no bindings, false otherwise
	 */
	public boolean isEmpty(){
		return env.isEmpty();
	}
	
	/**
	 * Checks if a given variable is binded.
	 *
	 * @param varName the variable name for which the binding is searched
	 * @return true if the variable is binded, false otherwise
	 */
	public boolean isVariableBinded(String varName){
		return env.containsKey(varName);
	}
	
	/**
	 * Checks if the variable was bounded 
	 * because of a for or let clause.
	 *
	 * @param varName the name of variable under evaluation
	 * @return true if the variable was binded because of a for clause,
	 * false if it was for a let clause
	 */
	public boolean isForVariable(String varName){
		return forVar.contains(varName);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return env.toString();
	}
	
	/**
	 * Adds the binding between a variable and its associated paths.
	 *
	 * @param varName the name of the variable to bind
	 * @param exPaths the paths associated with the variable
	 * @param flworType the flwor type expression that generated 
	 * the binding (are possible only for or let)
	 * @param updateType the type of the update operation in which the binding is included
	 */
	public void addBinding(String varName, ExtractedPaths exPaths/*, boolean pathExpr*/, FLWORType flworType, UpdateOperationType updateType){
		Paths listToAdd = new Paths();
		ExtractedPaths toUseForBinding = exPaths.clone();
		
		for(PathType pt : toUseForBinding.keySet())
			for(Path path : toUseForBinding.get(pt)){
				path.add(new VarItem(varName, flworType));
				//path.setOperationType(updateType);
			}
		
/*		if(pathExpr)
			for(PathType pathType : toUseForBinding.keySet())
				listToAdd.addAll(toUseForBinding.get(pathType));
		else {*/
			listToAdd.addAll(toUseForBinding.get(PathType.NODE_RETURNED));
			listToAdd.addAll(toUseForBinding.get(PathType.STRING_RETURNED));
//		}
		
		if(env.containsKey(varName))
			env.get(varName).addAll(listToAdd);
		else
			env.put(varName, listToAdd);
		
		// at this stage the value can only be FOR or LET 
		if(flworType == FLWORType.FOR)
			forVar.add(varName);
	}

/*	public void addBinding(String varName, Path path, boolean forClause){
		if(env.containsKey(varName))
			if(!env.get(varName).add(path))
				throw new IllegalStateException("Impossible to insert binding between variable " 
						+ varName + " and path " 
						+ path.toString());
		else {
			Paths list = new Paths();
			list.add(path);
			env.put(varName, list);
		}
		
		if(forClause)
			forVar.add(varName);
	}
	*/
/*
	public void addBinding(String varName, Paths paths, boolean forClause){
		if(env.containsKey(varName))
			if(!env.get(varName).addAll(paths))
				throw new IllegalStateException("Impossible to insert binding between variable " 
						+ varName + " and paths " 
						+ paths.toString());
		else {
			Paths list = new Paths();
			list.addAll(paths);
			env.put(varName, list);
		}
		
		if(forClause)
			forVar.add(varName);
	}
	*/
}
