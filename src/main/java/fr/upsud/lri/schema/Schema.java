/*
 * Class providing an abstract representation of a schema
 */
package fr.upsud.lri.schema;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import fr.upsud.lri.schemaAsGraph.Graph;
import fr.upsud.lri.schemaAsGraph.GraphNode;
import fr.upsud.lri.schemaAsGraph.GraphNodes;

/**
 * The Class Schema.
 */
public abstract class Schema {
	
	/** The name of the file in which the schema is stored. */
	protected String filename = "";
	
	/** All the types from which the considered one can be reached in one step. */
	protected HashMap<GraphNode, GraphNodes> fathers = 
		new HashMap<GraphNode, GraphNodes>();
	
	/** All the type reachable in one step from the considered type. */
	protected HashMap<GraphNode, GraphNodes> sons = 
		new HashMap<GraphNode, GraphNodes>();
	
	/** It contains all the type chains from root type to the considered one. */
	protected HashMap<GraphNode, GraphNodes> ancestors = 
		new HashMap<GraphNode, GraphNodes>();
	
	/** It contains all the different descending chains starting from the considered type. */
	protected HashMap<GraphNode, GraphNodes> descendants = 
		new HashMap<GraphNode, GraphNodes>();
	
	/** The graph representation of the schema. */
	protected Graph graph = null;
	
	/** The type of the schema (DTD or XSD). */
	protected SchemaType type;
	
	/**
	 * Instantiates a new schema.
	 *
	 * @param filename the filename in which the schema is stored
	 */
	public Schema(String filename) {
		super();
		
		this.graph = new Graph(this);
		this.filename = filename;
		
		if(filename.endsWith(".dtd"))
			type = SchemaType.DTD;
		else if(filename.endsWith(".xsd"))
			type = SchemaType.XSD;
		else if(filename.endsWith(".rng"))
			type = SchemaType.RELAXNG;
	}

	/**
	 * The main method, used for initial testing.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
//		new XMLSchemaRelations("books.xsd");
//		new XMLSchemaRelations("story.xsd");
//		new XMLSchemaRelations("note.dtd");
//		new XMLSchemaRelations("note.xsd");
//		new XMLSchemaRelations("shiporder.xsd");
//		new XMLSchemaRelations("mails.xsd");
		new XMLSchema("dictionary.xsd");
	}
	
	/**
	 * Creates a url representation of a file-system path.
	 *
	 * @param filename the filename of the file in which the schema is stored.
	 * @return the url representation of the file-system path
	 */
	protected URL createURL(String filename){
		URL url = null;
		
		try {
			url = new URL("file:///" + System.getProperty("user.dir") +  File.separator + filename);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return url;
	}
	
	/**
	 * Creates a string representing a url, starting from a file-system path.
	 *
	 * @return the string representing a url, starting from a file-system path
	 */
	protected String createURL(){
		return "file://" + System.getProperty("user.dir") 
		+ File.separator + filename;
	}
	
	/**
	 * Sets the filename in which the schema is stored.
	 *
	 * @param filename the new filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Gets the filename in which the schema is stored.
	 *
	 * @return the filename in which the schema is stored
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * Compute the relationships (fathers, sons, ancestors, descendants).
	 */
	public abstract void computeRelationships();

	/**
	 * Gets the root labels of the schema.
	 *
	 * @return the root labels of the schema
	 */
	public GraphNodes getRoot() {
		return this.graph.getRoots();
	}

	/**
	 * Sets the type of the schema.
	 *
	 * @param type the new type of the schema
	 */
	public void setType(SchemaType type) {
		this.type = type;
	}

	/**
	 * Gets the type of the schema.
	 *
	 * @return the type of the schema
	 */
	public SchemaType getType() {
		return type;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("\nRoots = {");
		for(GraphNode root : this.graph.getRoots())
			strBuffer.append(" " + root.toString() + " ");
		strBuffer.append("}");
		
		
		strBuffer.append("\nLeaves = {");
		for(GraphNode leaf : this.graph.getLeaves())
			strBuffer.append(" " + leaf.toString() + " ");
		strBuffer.append("}");
		
		strBuffer.append("\nFather rel = \n");
		for(GraphNode key : fathers.keySet()){
			strBuffer.append(key + " -> {");
			for(GraphNode node : fathers.get(key))
				strBuffer.append(" " + node + " ");
			strBuffer.append("}\n");
		}
		
		strBuffer.append("\nSons rel = \n");
		for(GraphNode key : sons.keySet()){
			strBuffer.append(key + " -> {");
			for(GraphNode node : sons.get(key))
				strBuffer.append(" " + node + " ");
			strBuffer.append("}\n");
		}
		
		strBuffer.append("\nAncestors chains rel = \n");
		for(GraphNode key : ancestors.keySet()){
			strBuffer.append(key + " -> {");
			for(GraphNode ancestor : ancestors.get(key)){
				strBuffer.append(" " + ancestor + " ");
			}
			strBuffer.append("}\n");
		}
		
		strBuffer.append("\nDescendant chains rel = \n");
		for(GraphNode key : descendants.keySet()){
			strBuffer.append(key + " -> {");
			for(GraphNode descendant : descendants.get(key)){
				strBuffer.append(" " + descendant + " ");
			}
			strBuffer.append("}\n");
		}
		
		return strBuffer.toString();
	}

	/**
	 * Sets the list of the graph nodes.
	 *
	 * @param graphNodes the new graph nodes list
	 */
	public void setGraphNodes(GraphNodes graphNodes) {
		this.graph.setGraphNodes(graphNodes);
	}

	/**
	 * Gets the list of the graph nodes.
	 *
	 * @return the list of the graph nodes
	 */
	public GraphNodes getGraphNodes() {
		return new GraphNodes(sons.keySet());
	}

	/**
	 * Sets the graph representation of the schema.
	 *
	 * @param graph the new graph representation of the schema
	 */
	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Gets the graph representation of the schema.
	 *
	 * @return the graph representation of the schema
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * Sets the map representing the fathers relationship.
	 *
	 * @param fathers the map representing the fathers relationship
	 */
	public void setFathers(HashMap<GraphNode, GraphNodes> fathers) {
		this.fathers = fathers;
	}

	/**
	 * Gets the map representing the fathers relationship.
	 *
	 * @return the map representing the fathers relationship
	 */
	public HashMap<GraphNode, GraphNodes> getFathers() {
		return fathers;
	}

	/**
	 * Sets the map representing the sons relationship.
	 *
	 * @param sons the map representing the sons relationship
	 */
	public void setSons(HashMap<GraphNode, GraphNodes> sons) {
		this.sons = sons;
	}

	/**
	 * Gets the map representing the sons relationship.
	 *
	 * @return the map representing the sons relationship
	 */
	public HashMap<GraphNode, GraphNodes> getSons() {
		return sons;
	}

	/**
	 * Sets the map representing the ancestors relationship.
	 *
	 * @param ancestors the map representing the ancestors relationship
	 */
	public void setAncestors(HashMap<GraphNode, GraphNodes> ancestors) {
		this.ancestors = ancestors;
	}

	/**
	 * Gets the map representing the ancestors relationship.
	 *
	 * @return the map representing the ancestors relationship
	 */
	public HashMap<GraphNode, GraphNodes> getAncestors() {
		return ancestors;
	}

	/**
	 * Sets the map representing the descendants relationship.
	 *
	 * @param descendants the map representing the descendants relationship
	 */
	public void setDescendants(HashMap<GraphNode, GraphNodes> descendants) {
		this.descendants = descendants;
	}

	/**
	 * Gets the map representing the descendants relationship.
	 *
	 * @return the map representing the descendants relationship
	 */
	public HashMap<GraphNode, GraphNodes> getDescendants() {
		return descendants;
	}

	/**
	 * Gets "leaves" of the graph representation of the schema.
	 *
	 * @return the leaves of the graph representation of the schema
	 */
	public GraphNodes getLeaves() {
		return this.graph.getLeaves();
	}
}
