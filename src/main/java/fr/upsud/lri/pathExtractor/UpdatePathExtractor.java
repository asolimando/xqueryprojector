/*
 * The path extractor for update expressions.
 */
package fr.upsud.lri.pathExtractor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import fr.upsud.lri.xqparser.SimpleNode;
import fr.upsud.lri.xqparser.XParserTreeConstants;
import org.xml.sax.SAXException;

/**
 * The Class UpdatePathExtractor.
 */
public class UpdatePathExtractor extends PathExtractor {
	
	/** The query path extractor. */
	private QueryPathExtractor queryPathExtractor = null;
	
	/**
	 * Instantiates a new update path extractor.
	 *
	 * @param queryPathExtractor the query path extractor
	 */
	public UpdatePathExtractor(QueryPathExtractor queryPathExtractor){
		this.queryPathExtractor = queryPathExtractor;
	}
	
	/* (non-Javadoc)
	 * @see org.w3c.fr.upsud.lri.pathExtractor.PathExtractor#extractPaths(org.w3c.xqparser.SimpleNode, org.w3c.fr.upsud.lri.pathExtractor.Environment, org.w3c.fr.upsud.lri.pathExtractor.FLWORType, org.w3c.fr.upsud.lri.pathExtractor.Path, boolean, org.w3c.fr.upsud.lri.pathExtractor.UpdateOperationType)
	 */
	@Override
	public ExtractedPaths extractPaths(SimpleNode node, Environment env,
			FLWORType flworType, Path ctxPath, boolean fixReturnedPaths, 
			UpdateOperationType updateType) 
	throws ParserConfigurationException, SAXException, IOException {
		
		if(node.getId() == XParserTreeConstants.JJTSTART){
			List<SimpleNode> list = new LinkedList<SimpleNode>();
			list.add(node);
			node = searchQueryBodyNode(list);
		}
		return extractPaths(node, env, 0, false, flworType, "", ctxPath, 
				fixReturnedPaths, updateType);
	}

	/* (non-Javadoc)
	 * @see org.w3c.fr.upsud.lri.pathExtractor.PathExtractor#extractPaths(org.w3c.xqparser.SimpleNode, org.w3c.fr.upsud.lri.pathExtractor.Environment, int, boolean, org.w3c.fr.upsud.lri.pathExtractor.FLWORType, java.lang.String, org.w3c.fr.upsud.lri.pathExtractor.Path, boolean, org.w3c.fr.upsud.lri.pathExtractor.UpdateOperationType)
	 */
	@Override
	public ExtractedPaths extractPaths(SimpleNode node, Environment env,
			int sonIndextoStartFrom, boolean returned, FLWORType flworType, String axis,
			Path ctxPath, boolean fixReturnedPaths, UpdateOperationType updateType) 
	throws ParserConfigurationException, SAXException, IOException {
		
		//ExtractedPaths exPaths = null;
		SimpleNode actualNode = node;
		System.out.print("\n" + XParserTreeConstants.jjtNodeName[node.getId()] 
		                   + " " + node.getValue());
		
		if(sonIndextoStartFrom != 0){
			actualNode = node.getChild(sonIndextoStartFrom);
			System.out.print(" " 
					+ XParserTreeConstants.jjtNodeName[actualNode.getId()] 
			        + " " + actualNode.getValue());
		}		
		switch(actualNode.getId()){
			// InsertExpr ::= "insert" ("node" | "nodes") SourceExpr InsertExprTargetChoice TargetExpr
			case XParserTreeConstants.JJTINSERTEXPR:
				
				UpdateOperationType insertOperation = null;

				if(actualNode.getChild(1).getValue() == null)//.equalsIgnoreCase("into"))
					insertOperation = UpdateOperationType.INSERT_INTO;
				else if(actualNode.getChild(1).getValue().equalsIgnoreCase("first"))
					insertOperation = UpdateOperationType.INSERT_FIRST;
				else if(actualNode.getChild(1).getValue().equalsIgnoreCase("last"))
					insertOperation = UpdateOperationType.INSERT_LAST;
				else if(actualNode.getChild(1).getValue().equalsIgnoreCase("after"))
					insertOperation = UpdateOperationType.INSERT_AFTER;
				else if(actualNode.getChild(1).getValue().equalsIgnoreCase("before"))
					insertOperation = UpdateOperationType.INSERT_BEFORE;

				return extractPathsFromInsertExpr(actualNode, env, returned, 
						flworType, ctxPath, fixReturnedPaths, insertOperation);
/*			case XParserTreeConstants.JJTINSERTEXPRTARGETCHOICE:
				break;
*/			
			// TargetExpr ::= ExprSingle
			case XParserTreeConstants.JJTTARGETEXPR:
			// SourceExpr ::= ExprSingle
			case XParserTreeConstants.JJTSOURCEEXPR:
				return queryPathExtractor.extractPaths(actualNode.getChild(0), env,
						returned, flworType, ctxPath, fixReturnedPaths, updateType);
				
			// DeleteExpr ::= "delete" ("node" | "nodes") TargetExpr
			case XParserTreeConstants.JJTDELETEEXPR:
				return extractPathsFromDeleteExpr(actualNode, env, returned, 
						flworType, ctxPath, fixReturnedPaths, 
						UpdateOperationType.DELETE);
		
			// 	RenameExpr ::= "rename" "node" TargetExpr "as" NewNameExpr
			case XParserTreeConstants.JJTRENAMEEXPR:
				return extractPathsFromRenameExpr(actualNode, env, returned, 
						flworType, ctxPath, fixReturnedPaths, UpdateOperationType.RENAME);
				
			case XParserTreeConstants.JJTREPLACEEXPR:
				return extractPathsFromReplaceExpr(actualNode, env, returned, 
						flworType, ctxPath, fixReturnedPaths, UpdateOperationType.REPLACE);
				
			case XParserTreeConstants.JJTTRANSFORMEXPR:
				throw new UnsupportedOperationException("Node " 
						+ XParserTreeConstants.jjtNodeName[actualNode.getId()] 
						          + " support not yet implemented.");
				
			default:
				return queryPathExtractor.extractPaths(actualNode, env, returned, 
						flworType, ctxPath, fixReturnedPaths, updateType);
		}
		
//		return exPaths;
	}

	/**
	 * Extracts paths from insert expressions.
	 *
	 * @param node the root node of the expression
	 * @param env the environment
	 * @param returned true if the expression is in a return statement
	 * @param flworType the flwor type of the surrounding expression
	 * @param ctxPath the context path
	 * @param fixReturnedPaths true if the return paths should be fixed
	 * @param updateType the update type of the expression
	 * @return the extracted paths
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPathsFromInsertExpr(SimpleNode node, 
			Environment env, boolean returned, FLWORType flworType, Path ctxPath, 
			boolean fixReturnedPaths, UpdateOperationType updateType) 
	throws ParserConfigurationException, SAXException, IOException{

		ExtractedPaths exPathsInsertion = 
			new ExtractedPaths(ExtractedPathsType.EP_UPDATE);
		// q0 and q1 are "swapped" in the rule, we follow their notation
		ExtractedPaths exPathsQ1 = queryPathExtractor.extractPaths(node.getChild(0), 
				env, returned, flworType, ctxPath, fixReturnedPaths, updateType)
				.toExtractPathsForQuery(ctxPath);
		// the second child (index 1) is the "direction" of insertion
		ExtractedPaths exPathsQ0 = queryPathExtractor.extractPaths(node.getChild(2), 
				env, returned, flworType, ctxPath, fixReturnedPaths, updateType)
				.toExtractPathsForQuery(ctxPath);
		
		Paths nodeOnly = exPathsInsertion.get(PathType.NODE_ONLY);
		Paths everythingBelow = exPathsInsertion.get(PathType.EVERYTHING_BELOW);
		
		/* common part for all the insert operations */
		// (ins-no) rule
		nodeOnly.addAll(exPathsQ0.get(PathType.NODE_USED));
		nodeOnly.addAll(exPathsQ1.get(PathType.NODE_USED));
		
		// (ins-eb) rule
		everythingBelow.addAll(exPathsQ0.get(PathType.EVERYTHING_BELOW_USED));
		everythingBelow.addAll(exPathsQ1.get(PathType.EVERYTHING_BELOW_USED));
		everythingBelow.addAll(exPathsQ1.get(PathType.NODE_RETURNED));
		
		// common part between (ins-rep-olb) and (ins-into-olb) rules
		Paths oneLevelBelow = exPathsInsertion.get(PathType.ONE_LEVEL_BELOW);
		oneLevelBelow.addAll(exPathsQ0.get(PathType.STRING_USED));
		oneLevelBelow.addAll(exPathsQ0.get(PathType.STRING_RETURNED));
		oneLevelBelow.addAll(exPathsQ1.get(PathType.STRING_USED));
		oneLevelBelow.addAll(exPathsQ1.get(PathType.STRING_RETURNED));
		
		// one level below extraction, different among insert operations
		if(updateType == UpdateOperationType.INSERT_AFTER
		   || updateType == UpdateOperationType.INSERT_BEFORE){
			// special part for (ins-rep-olb) rule
			oneLevelBelow.addAll(exPathsQ0.get(PathType.NODE_RETURNED));
			oneLevelBelow.Par();			
		}
		else if(updateType == UpdateOperationType.INSERT_INTO
				|| updateType == UpdateOperationType.INSERT_FIRST
				|| updateType == UpdateOperationType.INSERT_LAST){				
			// special part for (ins-into-olb) rule
			oneLevelBelow.Par();
			oneLevelBelow.addAll(exPathsQ0.get(PathType.NODE_RETURNED));
		}
		
		return exPathsInsertion;
	}
	
	/**
	 * Extracts paths from replace expressions.
	 *
	 * @param node the root node of the expression
	 * @param env the environment
	 * @param returned true if the expression is in a return statement
	 * @param flworType the flwor type of the surrounding expression
	 * @param ctxPath the context path
	 * @param fixReturnedPaths true if the return paths should be fixed
	 * @param updateType the update type of the expression
	 * @return the extracted paths
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPathsFromReplaceExpr(SimpleNode node, 
			Environment env, boolean returned, FLWORType flworType, Path ctxPath, 
			boolean fixReturnedPaths, UpdateOperationType updateType) 
	throws ParserConfigurationException, SAXException, IOException{
		
		ExtractedPaths exPathsReplace = 
			new ExtractedPaths(ExtractedPathsType.EP_UPDATE);
		
		ExtractedPaths exPathsQ0 = queryPathExtractor.extractPaths(node.getChild(0), 
				env, returned, flworType, ctxPath, fixReturnedPaths, updateType)
				.toExtractPathsForQuery(ctxPath);
		ExtractedPaths exPathsQ1 = queryPathExtractor.extractPaths(node.getChild(1), 
				env, returned, flworType, ctxPath, fixReturnedPaths, updateType)
				.toExtractPathsForQuery(ctxPath);
		
		Paths nodeOnly = exPathsReplace.get(PathType.NODE_ONLY);
		Paths everythingBelow = exPathsReplace.get(PathType.EVERYTHING_BELOW);
		// (ins-no) rule
		nodeOnly.addAll(exPathsQ0.get(PathType.NODE_USED));
		nodeOnly.addAll(exPathsQ1.get(PathType.NODE_USED));
		
		// (ins-eb) rule
		everythingBelow.addAll(exPathsQ0.get(PathType.EVERYTHING_BELOW_USED));
		everythingBelow.addAll(exPathsQ1.get(PathType.EVERYTHING_BELOW_USED));
		everythingBelow.addAll(exPathsQ1.get(PathType.NODE_RETURNED));
		
		// (ins-rep-olb) rule
		Paths oneLevelBelow = exPathsReplace.get(PathType.ONE_LEVEL_BELOW);
		oneLevelBelow.addAll(exPathsQ0.get(PathType.STRING_USED));
		oneLevelBelow.addAll(exPathsQ0.get(PathType.STRING_RETURNED));
		oneLevelBelow.addAll(exPathsQ1.get(PathType.STRING_USED));
		oneLevelBelow.addAll(exPathsQ1.get(PathType.STRING_RETURNED));
		oneLevelBelow.addAll(exPathsQ0.get(PathType.NODE_RETURNED));
		oneLevelBelow.Par();
		
		return exPathsReplace;
	}
	
	/**
	 * Extracts paths from delete expressions.
	 *
	 * @param node the root node of the expression
	 * @param env the environment
	 * @param returned true if the expression is in a return statement
	 * @param flworType the flwor type of the surrounding expression
	 * @param ctxPath the context path
	 * @param fixReturnedPaths true if the return paths should be fixed
	 * @param updateType the update type of the expression
	 * @return the extracted paths
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPathsFromDeleteExpr(SimpleNode node, 
			Environment env, boolean returned, FLWORType flworType, Path ctxPath, 
			boolean fixReturnedPaths, UpdateOperationType updateType) 
	throws ParserConfigurationException, SAXException, IOException{

		ExtractedPaths exPathsDelete = 
			new ExtractedPaths(ExtractedPathsType.EP_UPDATE);
		
		ExtractedPaths exPathsQ0 = queryPathExtractor.extractPaths(node.getChild(0), 
				env, returned, flworType, ctxPath, fixReturnedPaths, updateType)
				.toExtractPathsForQuery(ctxPath);
		
		Paths nodeOnly = exPathsDelete.get(PathType.NODE_ONLY);
		Paths oneLevelBelow = exPathsDelete.get(PathType.ONE_LEVEL_BELOW);
		Paths everythingBelow = exPathsDelete.get(PathType.EVERYTHING_BELOW);
		
		// node only
		nodeOnly.addAll(exPathsQ0.get(PathType.NODE_USED));
		nodeOnly.addAll(exPathsQ0.get(PathType.NODE_RETURNED));
		
		// one level below
		oneLevelBelow.addAll(exPathsQ0.get(PathType.STRING_USED));
		oneLevelBelow.addAll(exPathsQ0.get(PathType.STRING_RETURNED));
		oneLevelBelow.Par();
		
		// everything below used
		everythingBelow.addAll(exPathsQ0.get(PathType.EVERYTHING_BELOW_USED));
		
		return exPathsDelete;
	}
	
	/**
	 * Extracts paths from rename expressions.
	 *
	 * @param node the root node of the expression
	 * @param env the environment
	 * @param returned true if the expression is in a return statement
	 * @param flworType the flwor type of the surrounding expression
	 * @param ctxPath the context path
	 * @param fixReturnedPaths true if the return paths should be fixed
	 * @param updateType the update type of the expression
	 * @return the extracted paths
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPathsFromRenameExpr(SimpleNode node, 
			Environment env, boolean returned, FLWORType flworType, Path ctxPath, 
			boolean fixReturnedPaths, UpdateOperationType updateType) 
	throws ParserConfigurationException, SAXException, IOException{

		ExtractedPaths exPathsReplace = 
			new ExtractedPaths(ExtractedPathsType.EP_UPDATE);
		// q0 and q1 are "swapped" in the rule, we follow their notation
		ExtractedPaths exPathsQ1 = queryPathExtractor.extractPaths(node.getChild(0), 
				env, returned, flworType, ctxPath, fixReturnedPaths, updateType)
				.toExtractPathsForQuery(ctxPath);
		ExtractedPaths exPathsQ0 = queryPathExtractor.extractPaths(node.getChild(1), 
				env, returned, flworType, ctxPath, fixReturnedPaths, updateType)
				.toExtractPathsForQuery(ctxPath);
		
		Paths nodeOnly = exPathsReplace.get(PathType.NODE_ONLY);
		Paths oneLevelBelow = exPathsReplace.get(PathType.ONE_LEVEL_BELOW);
		Paths everythingBelow = exPathsReplace.get(PathType.EVERYTHING_BELOW);
		
		// node only
		nodeOnly.addAll(exPathsQ0.get(PathType.NODE_USED));
		nodeOnly.addAll(exPathsQ0.get(PathType.NODE_RETURNED));
		nodeOnly.addAll(exPathsQ1.get(PathType.NODE_USED));
		nodeOnly.addAll(exPathsQ1.get(PathType.NODE_RETURNED));
		
		// one level below
		oneLevelBelow.addAll(exPathsQ0.get(PathType.STRING_USED));
		oneLevelBelow.addAll(exPathsQ0.get(PathType.STRING_RETURNED));
		oneLevelBelow.addAll(exPathsQ1.get(PathType.STRING_USED));
		oneLevelBelow.addAll(exPathsQ1.get(PathType.STRING_RETURNED));
		oneLevelBelow.Par();
		
		// everything below used
		everythingBelow.addAll(exPathsQ0.get(PathType.EVERYTHING_BELOW_USED));
		everythingBelow.addAll(exPathsQ1.get(PathType.EVERYTHING_BELOW_USED));
		
		return exPathsReplace;
	}
}
