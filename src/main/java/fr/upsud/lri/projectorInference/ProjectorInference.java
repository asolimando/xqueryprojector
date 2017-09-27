/*
 * The class that provides the engine for the inference of the type projector
 */
package fr.upsud.lri.projectorInference;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import fr.upsud.lri.pathExtractor.*;
import fr.upsud.lri.schema.*;
import fr.upsud.lri.schemaAsGraph.*;
import fr.upsud.lri.xqparser.*;
import org.xml.sax.SAXException;

/**
 * The Class ProjectorInference.
 */
public class ProjectorInference {
	
	/** The schema used for type inference. */
	private Schema schema = null;
	
	/**
	 * Instantiates a new projector inference.
	 *
	 * @param schema the schema used for type inference
	 */
	public ProjectorInference(Schema schema){
		this.schema = schema;
	}
	
	/**
	 * Infers a three level projector.
	 *
	 * @param exPaths the extracted paths to be used for projector inference
	 * @return the type projector related to the extracted paths
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public TypeProjector infer3LevelProjector(ExtractedPaths exPaths) 
	throws CloneNotSupportedException{
		/*if(exPaths.getType() != ExtractedPathsType.EP_QUERY)
			throw new IllegalArgumentException("Query TypeProjector " +
					"can be inferred only from ExtractedPaths for queries.");*/
		
		//ExtractedPaths originalExPaths = exPaths.clone();
		Graph graph = null; //new Graph(schema);
		TypeProjector projector = new TypeProjector(
				TypeProjectorsTypes.THREE_LEVEL_TYPE_PROJECTOR);
		GraphNodes piNodeOnly = 
			projector.get(ProjectedTypeCategories.NODE_ONLY);
		GraphNodes piOneLevelBelow = 
			projector.get(ProjectedTypeCategories.ONE_LEVEL_BELOW);
		GraphNodes piEverythingBelow = 
			projector.get(ProjectedTypeCategories.EVERYTHING_BELOW);
		
		for (PathType key : exPaths.keySet()) {
			for (Path path : exPaths.get(key)) {		
				 graph = threeLevelAnalyzePath(schema.getGraph(), 
						 path.removeVarItem(), key, true, null);
				 
				 // we add to the context the permanent marked nodes
				 // (that are the ones used at some point in a path before
				 // an upward axis that should normally delete the marking)
				 if(!graph.getFrontier().isEmpty())
					 graph.getContext().addAllNoDuplicates(graph.getPermanentlyMarkedNodes());
				 
				 if(exPaths.getType() == ExtractedPathsType.EP_QUERY){
					 switch(key){
					 case EVERYTHING_BELOW_USED:
						 // context -> NO
						 piNodeOnly.addAllNoDuplicates(
								 graph.getContext());
						 // frontier -> EB				 
						 piEverythingBelow.addAllNoDuplicates(
								 graph.getFrontier());
						 break;
					 case STRING_RETURNED:
						 // context -> NO
						 piNodeOnly.addAllNoDuplicates(
								 graph.getContext());
						 // frontier -> EB
						 piEverythingBelow.addAllNoDuplicates(
								 graph.getFrontier());
						 break;
					 case STRING_USED:
						 // context -> NO
						 piNodeOnly.addAllNoDuplicates(
								 graph.getContext());
						 // frontier -> OLB
						 piOneLevelBelow.addAllNoDuplicates(
								 graph.getFrontier());
						 break;
					 case NODE_RETURNED:
						 // context -> NO
						 piNodeOnly.addAllNoDuplicates(
								 graph.getContext());
						 // frontier -> EB
						 piEverythingBelow.addAllNoDuplicates(
								 graph.getFrontier());
						 break;
					 case NODE_USED:
						 // context -> NO
						 piNodeOnly.addAllNoDuplicates(
								 graph.getContext());
						 // frontier -> NO
						 piNodeOnly.addAllNoDuplicates(
								 graph.getFrontier());
						 break;
					 default:
						 throw new IllegalStateException("Path type not " +
						 		"recognized for QUERY extracted paths.");
					 }
				 }
				 else if(exPaths.getType() == ExtractedPathsType.EP_UPDATE){
					 switch(key){
					 case NODE_ONLY:
						 // context -> NO
						 piNodeOnly.addAllNoDuplicates(
								 graph.getContext());
						 // frontier -> NO
						 piNodeOnly.addAllNoDuplicates(
								 graph.getFrontier());
						 break;
					 case EVERYTHING_BELOW:
						 // context -> NO
						 piNodeOnly.addAllNoDuplicates(
								 graph.getContext());
						 // frontier -> EB
						 piEverythingBelow.addAllNoDuplicates(
								 graph.getFrontier());
						 break;
					 case ONE_LEVEL_BELOW:
						 // context -> NO
						 piNodeOnly.addAllNoDuplicates(
								 graph.getContext());
	   					 // frontier -> OLB
						 piOneLevelBelow.addAllNoDuplicates(
								 graph.getFrontier());
						 break;
					 default:
						 throw new IllegalStateException("Path type not recognized " +
						 		"for UPDATE extracted paths."); 
					 }
				 }
				 else
					 throw new IllegalStateException("Extracted paths type not " +
					 		"recognized.");
			}
		}

		// this disjointness enforce is done once at the end
		// EB is ok as it is
		// OLB := OLB - EB
		piOneLevelBelow.removeAll(piEverythingBelow);
		
		// NO := NO - (OLB union EB)
		piNodeOnly.removeAll(piOneLevelBelow);
		piNodeOnly.removeAll(piEverythingBelow);
		
		 /* now we refine the result eliminating the NODE_ONLY types
		  * that have ALL of the parents marked as ONE_LEVEL_BELOW
		  * 
		  * TODO: another possible optimization should be to remove 
		  * OLB and NO for descendant of EB nodes
		  */
		for (GraphNode nodeOnlyType : piNodeOnly.clone())
			if(!nodeOnlyType.getFathers().isEmpty() &&
					piOneLevelBelow.containsAll(nodeOnlyType.getFathers()))
				piNodeOnly.remove(nodeOnlyType);
		
		return projector;
	}
	
	//TODO: public for testing (MainTypeInference), should be private (reflection is such a overkill here!)
	/**
	 * Analyze a single path and infer types for it.
	 *
	 * @param environment the actual environment (marked graph)
	 * @param path the path to be analyzed
	 * @param pathType the type of the path to be analyzed
	 * @param isFirstCall true if it is the first call for the path
	 * @param prevStep the previous step of the path
	 * @return the marked graph according to type inference
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public Graph threeLevelAnalyzePath(Graph environment, Path path, 
			PathType pathType, boolean isFirstCall, StepItem prevStep) 
		throws CloneNotSupportedException{
		
		if(path.isEmpty())
			return environment;
		
		StepItem step = (StepItem) path.removeFirst();
		String axis = step.getAxis();
		String test = step.getTest();
		
		if(axis.equalsIgnoreCase("attribute") 
				&& !(test.equalsIgnoreCase("node()")
					||
					test.equalsIgnoreCase("*")
					||
					test.equalsIgnoreCase("text()"))
		){
			test = "@" + test;
			step.setTest(test);
			axis = "child";
			step.setAxis(axis);
		}
		
		/* the paths with text() test for some step will have
		have also a step "parent::node()" following, it is ok
		to skip them */
		if(test.equalsIgnoreCase("text()")){
			if(!path.isEmpty()){
				StepItem parentNodeStep = (StepItem) path.removeFirst();
				if(parentNodeStep.getAxis().equalsIgnoreCase("parent")
						&& parentNodeStep.getTest().equalsIgnoreCase("node()"))
					return threeLevelAnalyzePath(environment, path, pathType, isFirstCall, step);
				throw new IllegalStateException("text() test not followed by a parent::node()");
			}
			else
				return threeLevelAnalyzePath(environment, path, pathType, isFirstCall, step);
		}
		
		// to handle document node (not explicitly encoded, 
		// so we need special treatment)
		if(step.isDoc() && isFirstCall){
			for (GraphNode root : environment.getRoots()){
				environment.getContext().add(root);
				environment.getFrontier().add(root);
			}
			return threeLevelAnalyzePath(environment, path, pathType, true, step);
		}
		else if(step.isDoc() && !isFirstCall || (isFirstCall && !step.isDoc() 
				  && !environment.isRootLabel(step.getTest())
				  && !axis.equalsIgnoreCase("descendant")
				  && !axis.equalsIgnoreCase("descendant-or-self"))
				)
			throw new IllegalStateException("The first step of a path should be document specification");
		else if(isFirstCall && environment.isRootLabel(step.getTest()))
			return threeLevelAnalyzePath(environment, path, pathType, false, step);
		
		// to handle the root element
		if(step.isSlash()){
			for (GraphNode root : environment.getRoots()){
				environment.getContext().add(root);
				environment.getFrontier().add(root);
			}
			return threeLevelAnalyzePath(environment, path, pathType, false, step);
		}
		
		// HERE START THE PATH REWRITING FOR SOME AXES
		else if(axis.equalsIgnoreCase("descendant-or-self")){
			Path pathSelf = path.clone();
			step.setAxis("descendant");
			pathSelf.addFirst(new StepItem("self", step.getTest()));
			path.addFirst(step);
			
			Graph graphSelf = threeLevelAnalyzePath(environment.clone(), pathSelf, pathType, false, prevStep);
			// here we do not clone because this environment will not be used anymore
			Graph graphDescendant = threeLevelAnalyzePath(environment, path, pathType, false, prevStep);
			
			return graphSelf.union(graphDescendant);
		}
		else if(axis.equalsIgnoreCase("ancestor-or-self")){
			Path pathSelf = path.clone();
			step.setAxis("ancestor");
			pathSelf.addFirst(new StepItem("self", step.getTest()));
			path.addFirst(step);
			
			Graph graphSelf = threeLevelAnalyzePath(environment.clone(), pathSelf, pathType, false, prevStep);
			Graph graphAncestor = threeLevelAnalyzePath(environment, path, pathType, false, prevStep);
			
			return graphSelf.union(graphAncestor);
		}
		else if(axis.equalsIgnoreCase("preceding") || 
				axis.equalsIgnoreCase("following")){
			// they are in reverse order because I'm inserting in FIRST position
			path.addFirst(new StepItem("descendant-or-self", step.getTest()));
			path.addFirst(new StepItem(step.getAxis() + "-sibling", "node()"));
			path.addFirst(new StepItem("ancestor-or-self", "node()"));
			//environment.getSecondMarkNodes().addAllNoDuplicates(environment.getFrontier());

			return threeLevelAnalyzePath(environment, path, pathType, false, prevStep);
		}
		else if(axis.equalsIgnoreCase("preceding-sibling")
				||
				axis.equalsIgnoreCase("following-sibling")){
			// they are in reverse order because I'm inserting in FIRST position
			path.addFirst(new StepItem("child", step.getTest()));
			path.addFirst(new StepItem("parent", "node()"));			
			environment.getPermanentlyMarkedNodes().addAllNoDuplicates(environment.getFrontier());
			
			return threeLevelAnalyzePath(environment, path, pathType, false, prevStep);
		}
		// HERE END THE PATH REWRITING FOR SOME AXES
		
		Graph stepEnv = environment;

		if(test.equalsIgnoreCase("node()")){
		
			GraphNodes forFrontier = 
				stepEnv.singleStepTypingAxis(stepEnv.getFrontier(), axis);

			// Primitive Single step (Rule 1)
			if(axis.equalsIgnoreCase("self") 
					|| axis.equalsIgnoreCase("child") 
					|| axis.equalsIgnoreCase("descendant")){
				
				GraphNodes forContext = 
				stepEnv.singleStepTypingAxis(stepEnv.getFrontier(), axis);
				
				stepEnv.getFrontier().clear();
				/* we want to filter out attributes because they are not nodes
				 * but not if the next step is self::*; in addition, to avoid 
				 * unnecessary marking, we do a lookahead on the next step: 
				 * if it is a self::tag we do not add to the frontier the 
				 * non-matching nodes
				 */
				StepItem nextStep = path.getFirstStep();
				String nextStepTest = nextStep == null ? "" : nextStep.getTest();
				String nextStepAxis = nextStep == null ? "" : nextStep.getAxis();
				
				for (GraphNode frontierNode : forFrontier) {
					if( (!frontierNode.isAttribute() 
							&& ( nextStep == null
								|| (nextStepTest.equalsIgnoreCase(frontierNode.getLabel()) 
									&& nextStep.getAxis().equalsIgnoreCase("self"))
								|| !nextStepAxis.equalsIgnoreCase("self")
								)
						 )
							|| (nextStep != null && nextStepAxis.equalsIgnoreCase("self") 
								&& (nextStepTest.equalsIgnoreCase("*") || 
									nextStepTest.startsWith("@"))
								)
					   )
						stepEnv.getFrontier().addNoDuplicates(frontierNode);
				}
				
				GraphNodes newContext = new GraphNodes();
				
				// to avoid the insertion of useless marked nodes we exclude the ones
				// that are not ancestors of any frontier node <-- this is not present in rule 1!!
				forContext.removeAll(stepEnv.getContext());
				forContext.addAll(stepEnv.getContext());
				
				/* for descendant the new context nodes could not have been before
				 * in the context, but for self and child this should be true,
				 * otherwise testing only ancestorship will not be enough
				 * with recursive schemas
				 */
				for (GraphNode contextCandidate : forContext) {
					for (GraphNode frontierNode : stepEnv.getFrontier()) {
						if(frontierNode.equals(contextCandidate) || (frontierNode.getAncestors().contains(contextCandidate) 
							&& (   axis.equalsIgnoreCase("descendant")
									||
									(
										!axis.equalsIgnoreCase("descendant")
										&&
										stepEnv.getContext().
										contains(contextCandidate)
									)
								)
						   	)
						   ){
							if(!newContext.contains(contextCandidate))
								newContext.add(contextCandidate);
							break;
						}
					}
				}

				stepEnv.getContext().clear();
				stepEnv.getContext().addAll(newContext);
			}
			
			// Primitive Single step (Rule 2)
			// (here attribute handling does not make sense because they have no sons)
			else if(axis.equalsIgnoreCase("parent") 
					|| axis.equalsIgnoreCase("ancestor")){
	
				GraphNodes forContext = 
					stepEnv.singleStepTypingAxis(stepEnv.getContext(), axis);
				
				stepEnv.getPermanentlyMarkedNodes().addAllNoDuplicates(stepEnv.getFrontier());
				
				// intersections
				GraphNodes clonedContext = new GraphNodes();
				// frontier
				clonedContext.addAll(stepEnv.getContext());				
				clonedContext.retainAll(forFrontier);
				stepEnv.setFrontier(clonedContext);
				
				// context
				stepEnv.setContext(forContext);
			}	
		}
		else {
			// Encoded Single Step (Rule 1)
			if(axis.equalsIgnoreCase("self") == false){
				Path newPath = new Path((UpdateOperationType) null);
				newPath.add(new StepItem(axis, "node()"));
				newPath.addLast(new StepItem("self", test));
				stepEnv = threeLevelAnalyzePath(environment, newPath, pathType, false, prevStep);
			}
			// Primitive Single step (Rule 3)
			else {
				// frontier
				GraphNodes oldFrontier = new GraphNodes();
				oldFrontier.removeAll(stepEnv.getFrontier());
				oldFrontier.addAll(stepEnv.getFrontier());
				
				GraphNodes typeComputed 
					= stepEnv.singleStepTypingTest(stepEnv.getFrontier(), test);
				GraphNodes newFrontier = new GraphNodes();
				newFrontier.addAll(typeComputed);
				
				stepEnv.setFrontier(newFrontier);
				
				// context
				GraphNodes newContext 
					= stepEnv.singleStepTypingAxis(typeComputed, "ancestor");

				newContext.retainAll(stepEnv.getContext());
				newContext.addAllNoDuplicates(typeComputed);
				stepEnv.setContext(newContext);
			}
		}

		// the return statement implements rule for Composed Paths
		return threeLevelAnalyzePath(stepEnv, path, pathType, false, step);
	}
	
	
	/**
	 * Infers the refined projector (w.r.t. the 3 level one).
	 *
	 * @param exPaths the extracted paths from which inference should start
	 * @return the refined type projector
	 */
	public TypeProjector inferRefinedProjector(ExtractedPaths exPaths){
		if(exPaths.getType() != ExtractedPathsType.EP_UPDATE)
			throw new IllegalArgumentException("Update TypeProjector " +
					"can be inferred only from ExtractedPaths for updates.");
		//TODO: not yet implemented
		return null;
	}
	
	/**
	 * The main method used for initial testing of the class.
	 *
	 * @param args the arguments
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws CloneNotSupportedException the clone not supported exception
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) 
	throws URISyntaxException, CloneNotSupportedException, ParserConfigurationException, SAXException, IOException {
		try {
			String schemaFilename = "";
			String expr = "";
			XParser parser = new XParser(
					new java.io.StringBufferInputStream(expr));
			SimpleNode tree = parser.START();
			if (null == tree)
				System.out.println("Error, abstract syntax tree is null!");
			else {
				System.out.println("XQuery expression parsing succeded");
				PathExtractor pathExtractor = new QueryPathExtractor();
				ExtractedPaths paths = pathExtractor.extractPaths(
						tree, new Environment(), null, 
						new Path((UpdateOperationType) null), 
						false, null);
				System.out.println(paths.toString());
				
				Schema schema = new XMLSchema(schemaFilename);
				ProjectorInference projInference = new ProjectorInference(schema);
				if(paths.getType() == ExtractedPathsType.EP_QUERY)
					System.out.println(projInference.infer3LevelProjector(paths));
				else if(paths.getType() == ExtractedPathsType.EP_UPDATE)
					System.out.println(projInference.inferRefinedProjector(paths));
				else
					throw new IllegalArgumentException("Illegal ExtractedPaths type for type inference.");
			}
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		} catch (Error err) {
			System.out.println(err.getMessage());
		} catch (PostParseException ppe) {
			System.out.println(ppe.getMessage());
		}
	}
}
