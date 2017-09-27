/*
 * Class providing a graph representation of a schema
 */
package fr.upsud.lri.schemaAsGraph;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import fr.upsud.lri.schema.Schema;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

/**
 * The Class Graph.
 */
public class Graph implements Cloneable {

	/** The roots nodes (nodes without fathers). */
	private GraphNodes roots = new GraphNodes();
	
	/** The leaves nodes (nodes without sons). */
	private GraphNodes leaves = new GraphNodes();
	
	/** The nodes reached by type inference in actual moment. */
	private GraphNodes frontier = new GraphNodes();
	
	/** All the graph nodes. */
	private GraphNodes graphNodes = new GraphNodes();
	 
	/** Nodes marked with first marker. */
	private GraphNodes context = new GraphNodes();

	/** Nodes permanently marked for the context. */
	private GraphNodes permanentlyMarkedNodes = new GraphNodes();
	
	/** The schema from which the graph has been generated. */
	private Schema schema;
	
	/**
	 * Instantiates a new graph.
	 *
	 * @param schema the schema from which the graph has been generated
	 */
	public Graph(Schema schema) {
		super();
		this.schema = schema;
		
		roots.addAll(computeRoots(schema.getGraphNodes()));
		leaves.addAll(computeLeaves(schema.getGraphNodes()));
		graphNodes.addAll(schema.getGraphNodes());
	}
	
	/**
	 * Compute "leaves" for the graph.
	 *
	 * @param nodes the nodes of the graph
	 * @return the list of the leaves graph nodes
	 */
	private GraphNodes computeLeaves(GraphNodes nodes){
		GraphNodes leaves = new GraphNodes();
		
		for (GraphNode node : nodes)
			if(node.sons.isEmpty())
				leaves.add(node);
		
		return leaves;
	}
	
	/**
	 * Compute roots for the graph.
	 *
	 * @param nodes the nodes of the graph
	 * @return the list of the roots graph nodes
	 */
	private GraphNodes computeRoots(GraphNodes nodes){
		GraphNodes roots = new GraphNodes();
		
		for (GraphNode node : nodes)
			if(node.fathers.isEmpty())
				roots.add(node);
		
		return roots;
	}
	
	public void cleanContext(){
		GraphNodes contextClone = (GraphNodes) context.clone();
		for (GraphNode contextNode : contextClone) {
			boolean isDirty = true;
			for (GraphNode frontierNode : context) {
				if(frontierNode.getAncestors().contains(contextNode))
					isDirty = false;
			}
			if(isDirty)
				context.remove(contextNode);
		}
	}
	
	/**
	 * Builds the graph visual representation.
	 *
	 * @param graph the graph to visualize
	 * @return the object representing the graph visualization
	 */
	public Object buildGraphRepresentation(mxGraph graph){
		Object parent = graph.getDefaultParent();
		Map<GraphNode, Object> builtNodes = new HashMap<GraphNode, Object>();
		Object rootRepr = null;
		
		for(GraphNode root : this.getRoots()){
			rootRepr = graph.insertVertex(parent, null, root.getLabel(), 0, 0, 80, 30);
			String rootStyle = "fillColor=" + mxUtils.getHexColorString(
					context.contains(root) 
					? Color.yellow 
							: frontier.contains(root) 
							? Color.green 
									: Color.cyan);
			
			
			rootStyle = mxUtils.setStyle(rootStyle, mxConstants.STYLE_SHAPE, root.isAttribute() 
					? mxConstants.SHAPE_ELLIPSE : mxConstants.SHAPE_RECTANGLE);
			
			if(context.contains(root) && frontier.contains(root))
				rootStyle = mxUtils.setStyle(rootStyle, 
						mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(Color.red));
			
			((mxCell)rootRepr).setStyle(rootStyle);
			
			builtNodes.put(root, rootRepr);
			buildSons(graph, rootRepr, root, builtNodes);
		}
		return rootRepr;
	}
	
	/**
	 * Builds the sons visual representation.
	 *
	 * @param graph the graph to which the son belongs
	 * @param fatherRepr the father visual representation
	 * @param father the father graph node object
	 * @param builtNodes the already built nodes
	 */
	protected void buildSons(mxGraph graph, Object fatherRepr, GraphNode father, Map<GraphNode, Object> builtNodes){
		Object parent = graph.getDefaultParent();
		for(GraphNode child : father.getSons()){
			if(builtNodes.containsKey(child) == false){
				Object childRepr = graph.insertVertex(parent, null, child.getLabel(), 0, 0, 80, 30);
				String rootStyle = "fillColor=" + mxUtils.getHexColorString(
						context.contains(child) 
						? Color.yellow 
								: frontier.contains(child) 
								? Color.green 
										: Color.cyan);
				
				rootStyle = mxUtils.setStyle(rootStyle, mxConstants.STYLE_SHAPE, 
						child.isAttribute() ? mxConstants.SHAPE_ELLIPSE : mxConstants.SHAPE_RECTANGLE);
				
				if(context.contains(child) && frontier.contains(child))
					rootStyle = mxUtils.setStyle(rootStyle, 
							mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(Color.red));
				
				((mxCell)childRepr).setStyle(rootStyle);
				
				graph.insertEdge(fatherRepr, null, "", fatherRepr, childRepr);
				builtNodes.put(child, childRepr);
				buildSons(graph, childRepr, child, builtNodes);
			}
			else {
				graph.insertEdge(fatherRepr, null, "", fatherRepr, builtNodes.get(child));
			}
		}
	}
	
	/**
	 * Single step typing axis.
	 *
	 * @param listNodes the list of input nodes
	 * @param axis the axis used to test
	 * @return the list of computed types for that axis, starting from an initial list of nodes
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public GraphNodes singleStepTypingAxis(GraphNodes listNodes, String axis) 
		throws CloneNotSupportedException{

		GraphNodes result = new GraphNodes();

		// A_E(tau, self)
		if(axis.equalsIgnoreCase("self")){
			/*result.removeAll(this.frontier);
			result.addAll(this.frontier);*/
			result.addAllNoDuplicates(this.frontier);
			return result;
		}
		
		for (GraphNode graphNode : listNodes) {
			GraphNodes list = new GraphNodes();
			
			// A_E(tau, ancestor)
			if(axis.equalsIgnoreCase("ancestor")){
/*				list.removeAll(this.getSchema().getAncestors().get(graphNode));
				list.addAll(this.getSchema().getAncestors().get(graphNode));*/
				list.addAllNoDuplicates(this.getSchema().getAncestors().get(graphNode));
				list.retainAll(this.context);
			}
			// A_E(tau, child)
			else if(axis.equalsIgnoreCase("child")){
/*				list.removeAll(this.getSchema().getSons().get(graphNode));
				list.addAll(this.getSchema().getSons().get(graphNode));*/
				list.addAllNoDuplicates(this.getSchema().getSons().get(graphNode));
			}
			// A_E(tau, parent)
			else if(axis.equalsIgnoreCase("parent")){
/*				list.removeAll(this.getSchema().getFathers().get(graphNode));
				list.addAll(this.getSchema().getFathers().get(graphNode));*/
				list.addAllNoDuplicates(this.getSchema().getFathers().get(graphNode));
				list.retainAll(this.context);
			}
			// A_E(tau, descendant)
			else if(axis.equalsIgnoreCase("descendant")){
/*				list.removeAll(this.getSchema().getDescendants().get(graphNode));
				list.addAll(this.getSchema().getDescendants().get(graphNode));*/
				list.addAllNoDuplicates(this.getSchema().getDescendants().get(graphNode));
			}
			else if(axis.equalsIgnoreCase("attribute")){
				list.removeAll(this.getSchema().getSons().get(graphNode));
				for (GraphNode son : this.getSchema().getSons().get(graphNode))
					if(son.isAttribute)
						list.add(son);
			}
/*			result.removeAll(list);
			result.addAll(list);*/
			result.addAllNoDuplicates(list);
		}
				
		return result;
	}
	
	/**
	 * Single step typing test.
	 *
	 * @param listNodes the list of input nodes
	 * @param test the test used
	 * @return the list of computed types for that test, starting from an initial list of nodes
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public GraphNodes singleStepTypingTest(GraphNodes listNodes, 
			String test) throws CloneNotSupportedException{
		
		GraphNodes result = new GraphNodes();
		
		// T_E(tau, node()) nothing to do, returns the actual frontier
		if(test.equalsIgnoreCase("node()")){
			for (GraphNode graphNode : listNodes)
				if(!graphNode.isAttribute())
					result.add(graphNode);
			return result;
		}
		
		// no filtering, with wildcard we keep every node
		if(test.equalsIgnoreCase("*"))
			return this.frontier;
			
		// T_E(tau, text())
		if(test.equalsIgnoreCase("text()"))
			test = "string";
		
		// T_E(tau, tag)		
		for (GraphNode graphNode : listNodes)
			if(graphNode.label.equalsIgnoreCase(test))
				result.add(graphNode);
		
		return result;
	}
	
	/**
	 * Methods that builds the union graph of the two input graphs.
	 *
	 * @param other the second operand/graph
	 * @return the graph resulting from the union of the two starting graphs
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public Graph union(Graph other) throws CloneNotSupportedException{
		
		// if one graph is a superset of the other returns it
		if(this.context.containsAll(other.context) 
				&&
			this.frontier.containsAll(other.frontier))
			return this.clone();
		else if(other.context.containsAll(this.context) 
				&&
				other.frontier.containsAll(this.frontier))
			return other.clone();
		
		// otherwise builds the union
		Graph unionGraph = new Graph(schema);
		
		// we add each frontier node of the first graph. Preservation of
		// the reachability from the root is guaranteed by a later step
		// that "imports" also marked nodes (of course we suppose well 
		// formed starting graphs!)
		for (GraphNode node : frontier) {
			// no duplicates
			if(!unionGraph.frontier.contains(node)){
				unionGraph.frontier.add(node);
			}
		}
		// same thing for the second graph
		for (GraphNode node : other.frontier) {
			// no duplicates
			if(!unionGraph.frontier.contains(node)){
				unionGraph.frontier.add(node);
			}
		}
		
		// we add to the marked nodes the ones that were so in the first graph
		for (GraphNode node : context) {
			// no duplicates
			if(!unionGraph.context.contains(node)){
				unionGraph.context.add(node);
			}
		}
		// same thing for the second graph
		for (GraphNode node : other.context) {
			// no duplicates
			if(!unionGraph.context.contains(node)){
				unionGraph.context.add(node);
			}
		}
		
		return unionGraph;
	}
	
	/**
	 * Methods that builds the intersection graph of the two input graphs.
	 *
	 * @param other the second operand/graph
	 * @return the graph resulting from the intersection of the two starting graphs
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public Graph intersection(Graph other) throws CloneNotSupportedException{
		
		// if one graph is a superset of the other returns the smaller one
		if(this.context.containsAll(other.context) 
				&&
			this.frontier.containsAll(other.frontier))
			return other.clone();
		else if(other.context.containsAll(this.context) 
				&&
				other.frontier.containsAll(this.frontier))
			return this.clone();
		
		Graph intersectionGraph = new Graph(schema);
		
		// we check only for one graph because if a node is not
		// in its frontier we are sure that will not be in the
		// intersection
		for (GraphNode node : frontier) {
			if(!intersectionGraph.frontier.contains(node) 
					&& other.frontier.contains(node))
				intersectionGraph.frontier.add(node);
		}
		
		// a marked node will be marked in the intersection graph
		// iff: 
		// 1) it is marked in both the starting graphs
		// 2) at least one of its son is marked/frontier in both graphs too
		for (GraphNode node : context) {
			// condition 1)
			if(!intersectionGraph.context.contains(node) 
					&& other.context.contains(node)){
				// condition 2)
				for(GraphNode son : node.sons){
					if((frontier.contains(son) 
							&& other.frontier.contains(son))
						||
						(context.contains(son) 
								&& other.context.contains(son)))
						intersectionGraph.context.add(node);
				}
			}
		}
		
		return intersectionGraph;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Graph clone() throws CloneNotSupportedException {
		Graph e = new Graph(schema);
		
		e.frontier.addAll(this.frontier);
		e.context.addAll(this.context);
		e.permanentlyMarkedNodes.addAll(this.permanentlyMarkedNodes);
		
		return e;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		
		str.append("Roots = { ");
		for (GraphNode elem : roots) {
			str.append(elem.getLabel() + " ");
		}
		str.append(" }\n");
	
		str.append("Leaves = { ");
		for (GraphNode elem : leaves) {
			str.append(elem.getLabel() + " ");
		}
		str.append(" }\n");

		str.append("Frontier = { ");
		for (GraphNode elem : frontier) {
			str.append(elem.getLabel() + " ");
		}
		str.append(" }\n");
		
		str.append("Context = { ");
		for (GraphNode elem : context) {
			str.append(elem.getLabel() + " ");
		}
		str.append(" }\n");

		str.append("PermanentlyMarkedNodes = { ");
		for (GraphNode elem : permanentlyMarkedNodes) {
			str.append(elem.getLabel() + " ");
		}
		str.append(" }\n");
		
		str.append("GraphNodes = { ");
		for (GraphNode elem : graphNodes) {
			str.append(elem.getLabel() + " ");
		}
		str.append(" }\n");
		
		return str.toString();
	}

	/**
	 * Sets the "roots" for the graph.
	 *
	 * @param roots the new roots
	 */
	public void setRoots(GraphNodes roots) {
		this.roots = roots;
	}


	/**
	 * Gets the "roots" of the graph.
	 *
	 * @return the roots
	 */
	public GraphNodes getRoots() {
		return roots;
	}


	/**
	 * Sets the "leaves" of the graph.
	 *
	 * @param leaves the new leaves of the graph
	 */
	public void setLeaves(GraphNodes leaves) {
		this.leaves = leaves;
	}


	/**
	 * Gets the leaves of the graph.
	 *
	 * @return the leaves of the graph
	 */
	public GraphNodes getLeaves() {
		return leaves;
	}


	/**
	 * Sets the frontier for the graph.
	 *
	 * @param frontier the new frontier for the graph
	 */
	public void setFrontier(GraphNodes frontier) {
		this.frontier = frontier;
	}


	/**
	 * Gets the frontier of the graph.
	 *
	 * @return the frontier of the graph
	 */
	public GraphNodes getFrontier() {
		return frontier;
	}


	/**
	 * Sets the nodes of the graph.
	 *
	 * @param graphNodes the new graph nodes
	 */
	public void setGraphNodes(GraphNodes graphNodes) {
		this.graphNodes = graphNodes;
	}

	/**
	 * Gets the nodes of the graph.
	 *
	 * @return the graph nodes
	 */
	public GraphNodes getGraphNodes() {
		return graphNodes;
	}

	/**
	 * Sets the context of the graph.
	 *
	 * @param context the new context
	 */
	public void setContext(GraphNodes context) {
		this.context = context;
	}

	/**
	 * Gets the context of the graph.
	 *
	 * @return the context
	 */
	public GraphNodes getContext() {
		return context;
	}

	/**
	 * Sets the second mark nodes.
	 *
	 * @param permanentlyMarkedNodes the new second mark nodes
	 */
	public void setPermanentlyMarkedNodes(GraphNodes permanentlyMarkedNodes) {
		this.permanentlyMarkedNodes = permanentlyMarkedNodes;
	}

	/**
	 * Gets the permanently marked nodes.
	 *
	 * @return the permanently marked nodes
	 */
	public GraphNodes getPermanentlyMarkedNodes() {
		return permanentlyMarkedNodes;
	}

	/**
	 * Sets the schema represented by this graph.
	 *
	 * @param schema the schema represented by this graph
	 */
	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	/**
	 * Gets the schema represented by this graph.
	 *
	 * @return the schema represented by this graph
	 */
	public Schema getSchema() {
		return schema;
	}
	
	/**
	 * Checks if the label is associated to a root node.
	 *
	 * @param label the label to check
	 * @return true, if is the label of a root node, false otherwise
	 */
	public boolean isRootLabel(String label){
		for (GraphNode node : roots)
			if(node.label.equalsIgnoreCase(label))
				return true;
		return false;
	}
}
