/*
 * The abstract class representing a generic path extractor
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
 * The Class PathExtractor.
 */
public abstract class PathExtractor {

	/**
	 * Instantiates a new path extractor.
	 */
	public PathExtractor(){
		super();
	}
	
	/**
	 * Extract paths.
	 *
	 * @param node the actual node
	 * @param env the static environment
	 * @param flworType the flwor operation type in which the actual node is nested (if any)
	 * @param ctxPath the context path at this stage
	 * @param fixReturnedPaths fix return path category, needed for some function parameter
	 * @param updateType the update operation type in which the current node is
	 * @return the extracted paths
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public abstract ExtractedPaths extractPaths(
			SimpleNode node,
			Environment env,
			FLWORType flworType,
			Path ctxPath,
			boolean fixReturnedPaths,
			UpdateOperationType updateType) throws ParserConfigurationException, SAXException, IOException;
	
	/**
	 * Extract paths.
	 *
	 * @param node the actual node
	 * @param env the static environment
	 * @param sonIndextoStartFrom the index of the son of the actual node we need to analyze at this stage
	 * @param returned says if we are in return statement or not
	 * @param flworType the flwor operation type in which the actual node is nested (if any)
	 * @param axis the axis for the actual step (if any)
	 * @param ctxPath the context path at this stage
	 * @param fixReturnedPaths fix return path category, needed for some function parameter
	 * @param updateType the update operation type in which the current node is
	 * @return the extracted paths
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public abstract ExtractedPaths extractPaths(
			SimpleNode node,
			Environment env,
			int sonIndextoStartFrom, 
			boolean returned, 
			FLWORType flworType,
			String axis,
			Path ctxPath,
			boolean fixReturnedPaths,
			UpdateOperationType updateType) throws ParserConfigurationException, SAXException, IOException;
	
	/**
	 * Search the query body node in a recursive fashion.
	 *
	 * @param nodeList the list containing the nodes still to examine
	 * @return the node associated with query body
	 */
	protected SimpleNode searchQueryBodyNode(List<SimpleNode> nodeList){
		List<SimpleNode> newList = new LinkedList<SimpleNode>();
		
		for (SimpleNode node : nodeList){
			if(node.getId() == XParserTreeConstants.JJTQUERYBODY)
				return node;
			
			for(int c = 0; c < node.jjtGetNumChildren(); c++){
				newList.add(node.getChild(c));
			}
		}
		return searchQueryBodyNode(newList);
	}
}
