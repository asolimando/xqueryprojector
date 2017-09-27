/*
 * Function class represents meaningful information about 
 * functions for path extraction's point of view
 */
package fr.upsud.lri.pathExtractor;

import java.util.LinkedList;

/**
 * The Class Function.
 */
public class Function extends LinkedList<FunctionParameter>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5690793579976415074L;

	/** The function name. */
	private String functionName;
	
	/** The number of parameters. */
	private int numOfParams;
	
	/** The step required by the function for path extraction */
	private StepItem stepToAdd;
	
	/**
	 * Instantiates a new function.
	 */
	public Function(){
		super();
		init();
	}
		
	/**
	 * Instantiates a new function.
	 *
	 * @param functionName the name of the function
	 * @param numOfParams the number of parameters
	 * @param funcParamList the function's parameter list (the signature)
	 * @param stepToAdd the step required by the function for path extraction
	 * @param cond the condition for the step
	 */
	public Function(String functionName, int numOfParams, LinkedList<FunctionParameter> funcParamList, StepItem stepToAdd){
		super();
		this.functionName = functionName;
		this.addAll(funcParamList);
		this.numOfParams = numOfParams;
		this.stepToAdd = stepToAdd;
	}
	
	/**
	 * Initialize the object.
	 */
	private void init(){
		numOfParams = 0;
	}
	
	/**
	 * Sets the function's name.
	 *
	 * @param functionName the new function name
	 */
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	/**
	 * Gets the function's name.
	 *
	 * @return the function's name
	 */
	public String getFunctionName() {
		return functionName;
	}

	/**
	 * Sets the number of parameters.
	 *
	 * @param numOfParams the new number of parameters
	 */
	public void setNumOfParams(int numOfParams) {
		this.numOfParams = numOfParams;
	}

	/**
	 * Gets the number of parameters.
	 *
	 * @return the number of parameters
	 */
	public int getNumOfParams() {
		return numOfParams;
	}

	/**  
	 * @param stepToAdd the step required by the function for path extraction 
	 * */
	public void setStepToAdd(StepItem stepToAdd) {
		this.stepToAdd = stepToAdd;
	}

	/**  
	 * Gets the step to be added to context path because of this function
	 * @return the step required by the function for path extraction 
	 * */
	public StepItem getStepToAdd() {
		return stepToAdd;
	}
	
	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append("(\n");
		
		for (FunctionParameter param : this) {
			str.append("[ Pos: " + param.getPosition());
			str.append(", Cardinality: " + param.getCardinality() + " ]");
		}
		
		str.append("\n)\n");
		return str.toString();
	}
}
