/*
 * Class that implements a path extractor for XQuery queries
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
 * The Class QueryPathExtractor.
 */
public class QueryPathExtractor extends PathExtractor {
	
	/** The update path extractor. */
	private UpdatePathExtractor updatePathExtractor = new UpdatePathExtractor(this);
	
	/** */
	private FunctionInfo funcInfo = null;
	
	/**
	 * Instantiates a new query path extractor.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public QueryPathExtractor() 
	throws ParserConfigurationException, SAXException, IOException {
		super();
		funcInfo = new FunctionInfo();
	}
	
	/* (non-Javadoc)
	 * @see org.w3c.fr.upsud.lri.pathExtractor.PathExtractor#extractPaths(org.w3c.xqparser.SimpleNode, org.w3c.fr.upsud.lri.pathExtractor.Environment, org.w3c.fr.upsud.lri.pathExtractor.FLWORType)
	 */
	@Override
	public ExtractedPaths extractPaths(
			SimpleNode node, 
			Environment env, 
			FLWORType flworType, 
			Path ctxPath, 
			boolean fixReturnedPaths,
			UpdateOperationType updateType) throws ParserConfigurationException, SAXException, IOException{
		
		if(node.getId() == XParserTreeConstants.JJTSTART){
			List<SimpleNode> list = new LinkedList<SimpleNode>();
			list.add(node);
			node = searchQueryBodyNode(list);
		}
		return extractPaths(node, env, 0, false, flworType, "", ctxPath, 
				fixReturnedPaths, updateType);
	}
	
	
	/**
	 * Extract paths from an expression represented by an abstract syntax tree rooted in node.
	 *
	 * @param node the root of the abstract syntax tree
	 * @param env the static environment
	 * @param returned say if we are in a return statement or not
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param ctxPath the ctx path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from this subtree
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public ExtractedPaths extractPaths(SimpleNode node, 
			Environment env, 
			boolean returned, 
			FLWORType flworType, 
			Path ctxPath, 
			boolean fixReturnedPaths,
			UpdateOperationType updateType) throws ParserConfigurationException, SAXException, IOException{
		
		return extractPaths(node, env, 0, returned, flworType, 
				"", ctxPath, fixReturnedPaths, updateType);
	}
	
	/* (non-Javadoc)
	 * @see org.w3c.fr.upsud.lri.pathExtractor.PathExtractor#extractPaths(org.w3c.xqparser.SimpleNode, org.w3c.fr.upsud.lri.pathExtractor.Environment, int, boolean, org.w3c.fr.upsud.lri.pathExtractor.FLWORType, java.lang.String)
	 */
	@Override
	public ExtractedPaths extractPaths(
			SimpleNode node, 
			Environment env,
			int sonIndextoStartFrom, 
			boolean returned, 
			FLWORType flworType,
			String axis, 
			Path ctxPath, 
			boolean fixReturnedPaths,
			UpdateOperationType updateType
	) throws ParserConfigurationException, SAXException, IOException{
		ExtractedPaths exPaths = null;
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
				
			// QUERYBODY = EXPR
			case XParserTreeConstants.JJTQUERYBODY:
				exPaths = extractPaths(actualNode.getChild(0), env, flworType, 
						ctxPath, fixReturnedPaths, updateType);
				return exPaths;
	
			// EXPR = SINGLEEXPR ("," SINGLEEXPR)*
			case XParserTreeConstants.JJTEXPR:
				exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
				ExtractedPathsType lastPathType = null;
				ExtractedPaths exPathsActualSon = null;
				for(int c = 0; c < actualNode.jjtGetNumChildren(); c++){
					exPathsActualSon = extractPaths(actualNode.getChild(c), env, 
							flworType, ctxPath.clone(), fixReturnedPaths, 
							updateType);
					if(c == 0){
						lastPathType = exPathsActualSon.getType();
						exPaths = new ExtractedPaths(lastPathType == ExtractedPathsType.EP_CONDITION 
								? ExtractedPathsType.EP_QUERY : lastPathType);
					}
					else if(exPathsActualSon.getType() != lastPathType)
						throw new IllegalStateException("All the concatenated expressione " +
						"should be of the same type (query or update)."); 
					
					if(exPathsActualSon.getType() != ExtractedPathsType.EP_UPDATE)
						exPathsActualSon = exPathsActualSon.toExtractPathsForQuery(ctxPath);

					exPaths.addAll(exPathsActualSon);

				}
				return exPaths;
				
				
			// FLOWREXPR10 = (FORCLAUSE | LETCLAUSE)+ WhereClause? OrderByClause? "return" SINGLEEXPR
			case XParserTreeConstants.JJTFLWOREXPR10:
				/* we clone the env to handle variable local scoping so after method
				   call we recover the previous bindings for the actual scope */
				Environment newEnv = env;//.clone();
				exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
				int numChildren = actualNode.jjtGetNumChildren();
				
				FLWORType forOrLet = null;
				if(actualNode.getChild(0).getId() == XParserTreeConstants.JJTFORCLAUSE)
					forOrLet = FLWORType.FOR;
				else if(actualNode.getChild(0).getId() == XParserTreeConstants.JJTLETCLAUSE)
					forOrLet = FLWORType.LET;
				
				// -1 because the return expression is handled in for/let handler
				for(int c = 0; c < numChildren - 1; c++)
					exPaths.addAll(extractPaths(actualNode.getChild(c), newEnv, 
							returned, forOrLet, ctxPath.clone(), fixReturnedPaths, 
							updateType).toExtractPathsForQuery(ctxPath));
				
				// FLWORType? how can I choose if there are more than one?
				/* We are handling separately the parts of the rules that differs 
				 * from "let" to "for", handling once the return statement path extraction
				 * that is commont to all the clauses
				 */
				ExtractedPaths exPathsQ1 = extractPaths(actualNode.getChild(actualNode.jjtGetNumChildren()-1),
						env, true, forOrLet, ctxPath.clone(), fixReturnedPaths, updateType);

				if(exPathsQ1.getType() == ExtractedPathsType.EP_UPDATE){
					ExtractedPaths updateExPaths = new ExtractedPaths(ExtractedPathsType.EP_UPDATE);
					Paths nodeOnly = updateExPaths.get(PathType.NODE_ONLY);
					Paths oneLevelBelow = updateExPaths.get(PathType.ONE_LEVEL_BELOW);
					Paths everythingBelow = updateExPaths.get(PathType.EVERYTHING_BELOW);
					
					// (let-no) and common part with (for-no)
					nodeOnly.addAll(exPaths.get(PathType.NODE_USED));
					if(forOrLet == FLWORType.FOR)
						nodeOnly.addAll(exPaths.get(PathType.NODE_RETURNED));
					nodeOnly.addAll(exPathsQ1.get(PathType.NODE_ONLY));
					
					// (let-olb) and common part with (for-olb)
					oneLevelBelow.addAll(exPaths.get(PathType.STRING_USED));
					if(forOrLet == FLWORType.FOR)
						oneLevelBelow.addAll(exPaths.get(PathType.STRING_RETURNED));
					oneLevelBelow.Par();
					oneLevelBelow.addAll(exPathsQ1.get(PathType.ONE_LEVEL_BELOW));
					
					// (let-eb), (for-eb)
					everythingBelow.addAll(exPaths.get(PathType.EVERYTHING_BELOW_USED));
					everythingBelow.addAll(exPathsQ1.get(PathType.EVERYTHING_BELOW));
					
					return updateExPaths;
				}
				
				exPathsQ1 = exPathsQ1.toExtractPathsForQuery(ctxPath);
				
				exPaths.addAll(exPathsQ1);
/*				exPaths.get(PathType.STRING_RETURNED).addAll(exPathsQ1.get(PathType.STRING_RETURNED));
				exPaths.get(PathType.NODE_RETURNED).addAll(exPathsQ1.get(PathType.NODE_RETURNED));
				exPaths.get(PathType.STRING_USED).addAll(exPathsQ1.get(PathType.STRING_USED));
				exPaths.get(PathType.NODE_USED).addAll(exPathsQ1.get(PathType.NODE_USED));
				exPaths.get(PathType.EVERYTHING_BELOW_USED).addAll(exPathsQ1.get(PathType.EVERYTHING_BELOW_USED));
	*/			
				return exPaths;
			
			// QuantifiedExpr ::= ("some" | "every") "$" VarName TypeDeclaration? "in" ExprSingle ("," "$" VarName TypeDeclaration? "in" ExprSingle)* "satisfies" ExprSingle
			case XParserTreeConstants.JJTQUANTIFIEDEXPR:
				SimpleNode presentNode = null;
				ExtractedPaths exPathsQuantifiedExpr = new ExtractedPaths(
						ExtractedPathsType.EP_QUERY);
				ExtractedPaths presentExPaths = null;
				// local binding, we don't want to propagate in caller context
				Environment localEnv = env.clone();
				
				// last one will be handled separately
				for(int c = 0; c < actualNode.jjtGetNumChildren() - 1; c++){
					presentNode = actualNode.getChild(c);
					if(presentNode.getId() == XParserTreeConstants.JJTVARNAME){
						presentExPaths = extractPaths(actualNode.getChild(c+1), 
								env, FLWORType.QUANTIFIED, ctxPath, 
								fixReturnedPaths, updateType);
						localEnv.addBinding(presentNode.getChild(0).getValue(), 
								presentExPaths, FLWORType.QUANTIFIED, updateType);
						presentExPaths.turnReturnedIntoUsed();
						exPathsQuantifiedExpr.addAll(presentExPaths);
					}
				}
				
				// the conversion from EP_CONDITION -> EP_QUERY implements (quantified-x) rules
				exPathsQuantifiedExpr.addAll(extractPaths(
						actualNode.getChild(actualNode.jjtGetNumChildren()-1),
						localEnv, FLWORType.QUANTIFIED, 
						ctxPath, fixReturnedPaths, updateType
						).toExtractPathsForQuery(ctxPath));
				
				// this will take care of (quantified-x) rules!
				exPathsQuantifiedExpr.turnReturnedIntoEBU();
				
				return exPathsQuantifiedExpr;
				
			// WHERECLAUSE ::= "where" SINGLEEXPR
			case XParserTreeConstants.JJTWHERECLAUSE:
				ExtractedPaths whereExPaths = extractPaths(actualNode.getChild(0), 
						env, flworType, ctxPath, fixReturnedPaths, updateType);
	
				// this handles all the (where-x) rules
				whereExPaths.toExtractPathsForQuery(ctxPath);
				
/*				// (where-nu)
				whereExPaths.get(PathType.NODE_USED).addAll(exprExPaths.get(PathType.NODE_ABSOLUTE));
				whereExPaths.get(PathType.NODE_USED).addAll(exprExPaths.get(PathType.NODE_RELATIVE).addPathAsPrefix(ctxPath, false));
				// (where-su)
				whereExPaths.get(PathType.STRING_USED).addAll(exprExPaths.get(PathType.STRING_ABSOLUTE));
				whereExPaths.get(PathType.STRING_USED).addAll(exprExPaths.get(PathType.STRING_RELATIVE).addPathAsPrefix(ctxPath, false));
				// (where-ebu)
				whereExPaths.get(PathType.EVERYTHING_BELOW_USED).addAll(exprExPaths.get(PathType.EVERYTHING_BELOW_ABSOLUTE));
				whereExPaths.get(PathType.EVERYTHING_BELOW_USED).addAll(exprExPaths.get(PathType.EVERYTHING_BELOW_RELATIVE).addPathAsPrefix(ctxPath, false));
*/
				// (where-ret), nothing to do, where returns boolean value
				
				return whereExPaths;
//				return extractPaths(actualNode.getChild(0), env, flworType, ctxPath);
			
			// OrderByClause ::= (("order" "by") | ("stable" "order" "by")) OrderSpecList
			// OrderSpecList ::= OrderSpecList ::= OrderSpec ("," OrderSpec)*
			// OrderSpec ::= ExprSingle OrderModifier
			// OrderModifier ::= ("ascending" | "descending")? ("empty" ("greatest" | "least"))? ("collation" URILiteral)?
			case XParserTreeConstants.JJTORDERBYCLAUSE:
				SimpleNode orderSpecListNode = actualNode.getChild(0);
				ExtractedPaths orderExPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
				
				for(int c = 0; c < orderSpecListNode.jjtGetNumChildren(); c++){
					// here we extract only from the first child of OrderSpec that is SingleExpr
					orderExPaths.addAll(
							extractPaths(orderSpecListNode.getChild(c).getChild(0), 
									env, flworType, ctxPath.clone(), 
									fixReturnedPaths, updateType));
				}
				
				/*// returned strings/nodes for order by are used for the flower expr
				// we do this here because orderclause can appear only in flower expr
				// and we will never need it's classification outside
				orderExPaths.addAll(PathType.STRING_USED, orderExPaths.get(PathType.STRING_RETURNED));
				orderExPaths.addAll(PathType.NODE_USED, orderExPaths.get(PathType.NODE_RETURNED));
				// we also need to clear string/node returned list once copied
				orderExPaths.get(PathType.STRING_RETURNED).clear();
				orderExPaths.get(PathType.NODE_RETURNED).clear();*/
				
				// this implements rules (ob-x)
				orderExPaths.turnReturnedIntoEBU();
				
				return orderExPaths;
				
			case XParserTreeConstants.JJTFORCLAUSE:
				return extractPathsFromForClause(actualNode, env, returned, 
						flworType, ctxPath.clone(), fixReturnedPaths, updateType);
			
			// LetClause ::= "let" "$" VarName ":=" SINGLEEXPR ("," "$" VarName TypeDeclaration? ":=" ExprSingle)*
			case XParserTreeConstants.JJTLETCLAUSE:
				return extractPathsFromLetClause(actualNode, env, returned, 
						flworType, ctxPath.clone(), fixReturnedPaths, updateType);
			
			case XParserTreeConstants.JJTIFEXPR:
				return extractPathsFromIfExpr(actualNode, env, flworType, 
						ctxPath.clone(), fixReturnedPaths, updateType);
			case XParserTreeConstants.JJTPATHEXPR:
				return extractPathsFromPathExpr(actualNode, env, returned, 
						flworType, ctxPath.clone(), fixReturnedPaths, updateType);				
			case XParserTreeConstants.JJTVARNAME:
				return extractPathsFromVariable(actualNode, env, returned, 
						ctxPath, fixReturnedPaths, updateType);
			case XParserTreeConstants.JJTSTEPEXPR:
				return extractPathsFromStepExpr(actualNode, env, 0, returned, 
						flworType, axis, ctxPath, fixReturnedPaths, updateType);
			case XParserTreeConstants.JJTABBREVFORWARDSTEP:
				return extractPathsFromAbbreviatedForwardStep(actualNode, env, 
						returned, flworType, axis, ctxPath, fixReturnedPaths, 
						updateType);

			/* 
			 * NOT IMPLEMENTED YET 
			 * */
			// SCHEMAATTRIBUTETEST := "schema-attribute" "(" AttributeDeclaration ")"
			case XParserTreeConstants.JJTSCHEMAATTRIBUTETEST:
			// SCHEMAELEMENTTEST := "schema-element" "(" ElementDeclaration ")"
			case XParserTreeConstants.JJTSCHEMAELEMENTTEST:
			// ELEMENTTEST := "element" "(" (ElementNameOrWildcard ("," TypeName "?"?)?)? ")"
			case XParserTreeConstants.JJTELEMENTTEST:
			// ATTRIBUTETEST := "attribute" "(" (AttribNameOrWildcard ("," TypeName)?)? ")"
			case XParserTreeConstants.JJTATTRIBUTETEST:
			// PITEST := "processing-instruction" "(" (NCName | StringLiteral)? ")"
			case XParserTreeConstants.JJTPITEST:
			// DOCUMENTTEST := "document-node" "(" (ElementTest | SchemaElementTest)? ")"
			case XParserTreeConstants.JJTDOCUMENTTEST:
				
			case XParserTreeConstants.JJTFORWARDAXIS:
				throw new UnsupportedOperationException("Node " 
						+ XParserTreeConstants.jjtNodeName[actualNode.getId()] 
						              + " support not yet implemented.");
			
				

			// COMMENTTEST := "comment" "(" ")"
			case XParserTreeConstants.JJTCOMMENTTEST:
				return extractPathsFromTests(axis, actualNode, ctxPath, 
						fixReturnedPaths, updateType);
			// WILDCARD := "*" | (NCName ":" "*") | ("*" ":" NCName)
			case XParserTreeConstants.JJTWILDCARD:
				return extractPathsFromTests(axis, actualNode, ctxPath, 
						fixReturnedPaths, updateType);
			// Leaf node of the AbstractSyntaxTree
			case XParserTreeConstants.JJTQNAME:
				return extractPathsFromTests(axis, actualNode, ctxPath, 
						fixReturnedPaths, updateType);
			// TEXTTEST := "text" "(" ")"
			case XParserTreeConstants.JJTTEXTTEST:
				return extractPathsFromTests(axis, actualNode, ctxPath, 
						fixReturnedPaths, updateType);
			// ANYKINDTEST := "node" "(" ")"
			case XParserTreeConstants.JJTANYKINDTEST:
				return extractPathsFromTests(axis, actualNode, ctxPath, 
						fixReturnedPaths, updateType);
			// NodeTest := KindTest() | NameTest()
			case XParserTreeConstants.JJTNODETEST:
				return extractPaths(actualNode.getChild(0), env, flworType, 
						axis, ctxPath, fixReturnedPaths, updateType);
			// NAMETEST := QName | WildCard
			case XParserTreeConstants.JJTNAMETEST:
				return extractPaths(actualNode.getChild(0), env, flworType, 
						axis, ctxPath, fixReturnedPaths, updateType);
				
			// ParenthesizedExpr := "(" EXPR? ("," EXPR)* ")"
			case XParserTreeConstants.JJTPARENTHESIZEDEXPR:
				ExtractedPaths pathAcc = null;
				ExtractedPaths actPath = null;
				ExtractedPathsType pathSeqType = null;
				
				for(int c = 0; c < actualNode.jjtGetNumChildren(); c++){
					actPath = extractPaths(actualNode.getChild(c), env, flworType, 
							axis, ctxPath.clone(), fixReturnedPaths, 
							updateType);
					
					if(c == 0){
						pathSeqType = actPath.getType();
						pathAcc = new ExtractedPaths(pathSeqType);
					}
					else if(actPath.getType() != pathSeqType)
						throw new IllegalStateException("All the concatenated expressione " +
						"should be of the same type (query or update)."); 
					
					if(actPath.getType() != ExtractedPathsType.EP_UPDATE)
						actPath = actPath.toExtractPathsForQuery(ctxPath);

					pathAcc.addAll(actPath);
				}
				
				return pathAcc;
				
			// ORDEREDEXPR := "ordered" "{" EXPR "}"
			case XParserTreeConstants.JJTORDEREDEXPR:
				return extractPaths(actualNode.getChild(0), env, flworType, 
						axis, ctxPath.clone(), fixReturnedPaths, updateType);
			// UNORDEREDEXPR := "unordered" "{" EXPR "}"
			case XParserTreeConstants.JJTUNORDEREDEXPR:
				return extractPaths(actualNode.getChild(0), env, flworType, 
						axis, ctxPath.clone(), fixReturnedPaths, updateType);
			// JJTCONTEXTITEMEXPR := ".."
			case XParserTreeConstants.JJTABBREVREVERSESTEP:
				exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
				Path newPath = new Path(new StepItem("parent", "*"), 
						fixReturnedPaths, updateType);
				//TODO always returned, I suppose
				ctxPath.addAll(ctxPath.size(), newPath);
				exPaths.addPath(returned ? PathType.NODE_RETURNED 
						: PathType.NODE_USED, newPath);
			// JJTCONTEXTITEMEXPR := "."
			case XParserTreeConstants.JJTCONTEXTITEMEXPR:
				exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
				//TODO to handle binding of the leading expression?
				exPaths.addPath(
						returned ? PathType.NODE_RETURNED 
								: PathType.NODE_USED, 
						new Path(new StepItem("self", "*"), 
								fixReturnedPaths, updateType));
				return exPaths;
			
			// (Value) rule TODO: check if it is complete
			case XParserTreeConstants.JJTINTEGERLITERAL:
			case XParserTreeConstants.JJTDOUBLELITERAL:
			case XParserTreeConstants.JJTSTRINGLITERAL:
			case XParserTreeConstants.JJTDECIMALLITERAL:
				return new ExtractedPaths(ExtractedPathsType.EP_QUERY);
				
			// ComparisonExpr ::= RangeExpr ( (ValueComp | GeneralComp | NodeComp) RangeExpr )?
			case XParserTreeConstants.JJTCOMPARISONEXPR:
				return extractPathsFromComparisons(actualNode, env, returned, 
						flworType, ctxPath.clone(), fixReturnedPaths, updateType);
			
			/* 
			 * (ElemConst) rule handling starts here
			 */
			// Constructor ::= DirectConstructor | ComputedConstructor
			case XParserTreeConstants.JJTCONSTRUCTOR:
				return extractPaths(actualNode.getChild(0), env, returned, 
						flworType, ctxPath, fixReturnedPaths, updateType);
			
			// DirectConstructor ::= DirElemConstructor | DirCommentConstructor | DirPIConstructor
			case XParserTreeConstants.JJTDIRECTCONSTRUCTOR:
				return extractPaths(actualNode.getChild(0), env, returned, 
						flworType, ctxPath.clone(), fixReturnedPaths, updateType);
			
			// DirElemConstructor ::= "<" QName DirAttributeList ("/>" | (">" DirElemContent* "</" QName S? ">"))
			case XParserTreeConstants.JJTDIRELEMCONSTRUCTOR:
				int j = 0;
				SimpleNode son = actualNode.getChild(j++);
				ExtractedPaths exP = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
				
				while(j < actualNode.jjtGetNumChildren()){
					if(son.getId() == XParserTreeConstants.JJTDIRELEMCONTENT ||
					   son.getId() == XParserTreeConstants.JJTDIRATTRIBUTELIST){
						exP.addAll(
								extractPaths(son, env, returned, 
										flworType, ctxPath.clone(), 
										fixReturnedPaths, updateType));
					}
					son = actualNode.getChild(j++);
				} 
				return exP;
			
			// DirAttributeList ::= (S (QName S? "=" S? DirAttributeValue)?)*
			case XParserTreeConstants.JJTDIRATTRIBUTELIST:
				int k = 0;
				ExtractedPaths exPDirAttr = new ExtractedPaths(
						ExtractedPathsType.EP_QUERY);
				if(actualNode.jjtGetNumChildren() == 0)
					return exPDirAttr;
				
				SimpleNode sonDirAttr = null;
				
				while(k < actualNode.jjtGetNumChildren()){
					sonDirAttr = actualNode.getChild(k++);
					if(sonDirAttr.getId() == 
						XParserTreeConstants.JJTDIRATTRIBUTEVALUE){
						exPDirAttr.addAll(
								extractPaths(sonDirAttr, env, returned, 
										flworType, ctxPath.clone(), 
										fixReturnedPaths, updateType));
					}
				}
				
				return exPDirAttr;
			
			/* DirAttributeValue ::= ('"' (EscapeQuot | QuotAttrValueContent)* '"') 
			 * | ("'" (EscapeApos | AposAttrValueContent)* "'")
			*/
			// we skip the rest and analyze only CommonContent element (the rest is meaningless for us)
			case XParserTreeConstants.JJTDIRATTRIBUTEVALUE:
				return extractPaths(actualNode.getChild(1).getChild(0), env, 
						flworType, ctxPath.clone(), fixReturnedPaths, updateType);
				
			// DirElemContent ::= DirectConstructor | CDataSection | CommonContent | ElementContentChar
			case XParserTreeConstants.JJTDIRELEMCONTENT:
				SimpleNode child = actualNode.getChild(0);
				if(child.getId() == XParserTreeConstants.JJTCOMMONCONTENT
						||
						child.getId() == XParserTreeConstants.JJTDIRECTCONSTRUCTOR)
					return extractPaths(child, env, returned, flworType, 
							ctxPath.clone(), fixReturnedPaths, updateType);
				// the other options are JJTELEMENTCONTENTCHAR and
				// JJTCDATASECTION but they are useless for us, raw text
				return new ExtractedPaths(ExtractedPathsType.EP_QUERY);
			
			// CommonContent ::= PredefinedEntityRef | CharRef | "{{" | "}}" | EnclosedExpr
			case XParserTreeConstants.JJTCOMMONCONTENT:
				SimpleNode firstSon = actualNode.getChild(0);
				if(firstSon.getId() == XParserTreeConstants.JJTENCLOSEDEXPR)
					return extractPaths(firstSon, env, returned, flworType, 
							ctxPath.clone(), fixReturnedPaths, updateType);
				/* the other options are JJTLCurlyBraceEscape, RCurlyBraceEscape,
				   JJTCHARREF, JJTPREDEFINEDENTITYREF, useless for us */
				return new ExtractedPaths(ExtractedPathsType.EP_QUERY);
			/* 
			 * (ElemConst) rule handling ends here
			 */
				
			// EnclosedExpr ::= "{" Expr "}", so we analyze child with index 1
			case XParserTreeConstants.JJTENCLOSEDEXPR:
				return extractPaths(actualNode.getChild(1), env, returned, 
						flworType, ctxPath.clone(), fixReturnedPaths, updateType);
			
			case XParserTreeConstants.JJTFUNCTIONCALL:
				return extractPathsFromFunctionCall(actualNode, env, 
						returned, flworType, ctxPath.clone(), 
						fixReturnedPaths, updateType);
			
			case XParserTreeConstants.JJTADDITIVEEXPR:
			case XParserTreeConstants.JJTMULTIPLICATIVEEXPR:
			// OrExpr ::= AndExpr ( "or" AndExpr )*
			case XParserTreeConstants.JJTOREXPR:
			/* case XParserTreeConstants.JJTANDEXPR:
				exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
				exPaths.addAll(extractPaths(actualNode.getChild(0), env, returned, flworType, ctxPath.clone()));
				exPaths.addAll(extractPaths(actualNode.getChild(1), env, returned, flworType, ctxPath.clone()));
				return exPaths; 
			*/
			// AndExpr ::= ComparisonExpr ( "and" ComparisonExpr )*
			case XParserTreeConstants.JJTANDEXPR:
				return extractPathsFromCondition(actualNode, env, 
						flworType, ctxPath, fixReturnedPaths, updateType);

			// nothing to do
			case XParserTreeConstants.JJTS:
			case XParserTreeConstants.JJTTAGQNAME: // tag name when creating one, useless
			case XParserTreeConstants.JJTVALUEINDICATOR: // "=" symbol for element construction, useless
				return new ExtractedPaths(ExtractedPathsType.EP_QUERY);
				
			// CALLING TO UPDATEPATHEXTRACTOR:	
			case XParserTreeConstants.JJTINSERTEXPR:
			case XParserTreeConstants.JJTSOURCEEXPR:
			case XParserTreeConstants.JJTINSERTEXPRTARGETCHOICE:
			case XParserTreeConstants.JJTTARGETEXPR:
			case XParserTreeConstants.JJTDELETEEXPR:
			case XParserTreeConstants.JJTRENAMEEXPR:
			case XParserTreeConstants.JJTREPLACEEXPR:
			case XParserTreeConstants.JJTTRANSFORMEXPR:
				return updatePathExtractor.extractPaths(actualNode, env, 
						flworType, ctxPath, fixReturnedPaths, updateType);
			
			// This is used in Rename statement
			// NewNameExpr ::= ExprSingle
			case XParserTreeConstants.JJTNEWNAMEEXPR:
				return extractPaths(actualNode.getChild(0), env, 
						flworType, ctxPath.clone(), fixReturnedPaths, 
						updateType);
			
			case XParserTreeConstants.JJTUNIONEXPR:
				exPaths = extractPaths(actualNode.getChild(0), env,
						flworType, ctxPath.clone(), fixReturnedPaths,
						updateType);
				exPaths.addAll(extractPaths(actualNode.getChild(1), env,
						flworType, ctxPath.clone(), fixReturnedPaths,
						updateType));
				return exPaths;
				
			default:
				throw new IllegalArgumentException("Node id not recognized: " 
						+ XParserTreeConstants.jjtNodeName[actualNode.getId()] 
						                                   + ".");
		}
	}
	
	/**
	 * Extract paths from an expression represented by an abstract syntax tree rooted in node.
	 *
	 * @param node the root of the abstract syntax tree
	 * @param env the static environment
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param axis the axis read at the preceding step of the analysis
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from this subtree
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPaths(SimpleNode node, Environment env,
			FLWORType flworType, String axis, Path ctxPath, 
			boolean fixReturnedPaths, UpdateOperationType updateType) throws ParserConfigurationException, SAXException, IOException {
		return extractPaths(node, env, 0, false, flworType, axis, ctxPath, 
				fixReturnedPaths, updateType);
	}

	/**
	 * Extract paths from a function call.
	 *
	 * @param actualNode the root of the abstract syntax tree
	 * @param env the static environment
	 * @param returned say if we are in a return statement or not
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from this subtree
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPathsFromFunctionCall(
			SimpleNode actualNode,
			Environment env, 
			boolean returned, 
			FLWORType flworType, 
			Path ctxPath, 
			boolean fixReturnedPaths,
			UpdateOperationType updateType) throws ParserConfigurationException, SAXException, IOException {
		
		if(actualNode.getId() != XParserTreeConstants.JJTFUNCTIONCALL)
			throw new IllegalArgumentException("This method can be applied only to JJTFUNCTIONCALL nodes, found " 
					+ XParserTreeConstants.jjtNodeName[actualNode.getId()] + ".");
		
		ExtractedPaths exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		
		// FunctionCall ::= QName(FUNCTIONQNAME) "(" (ExprSingle ("," ExprSingle)*)? ")"
		String functionName = actualNode.getChild(0).getValue();
		System.out.println("\n" + functionName + "\n");
		
		if(functionName.equalsIgnoreCase("doc")){
//			ctxPath.setDocumentName(actualNode.getChild(1).getValue());
			exPaths.addPath(PathType.NODE_RETURNED, new Path(
					new StepItem(functionName + "(" 
							+ actualNode.getChild(1).getValue() 
							+ ")", ""), fixReturnedPaths, updateType));
		}
		else{
			ExtractedPaths argExPaths;
			
			// first child is function name, nothing to extract from it
			for(int c = 1; c < actualNode.jjtGetNumChildren(); c++){
				boolean fixRet = fixReturnedPaths;
				if(funcInfo.containsKey(functionName))
					fixRet = fixRet || funcInfo.get(functionName).get(c-1).isTurnUsedIntoReturned();
				
				argExPaths = extractPaths(actualNode.getChild(c), env, 
						returned, flworType, ctxPath.clone(), fixRet, updateType);
				exPaths.addAllTurningReturnedIntoUsedButPreservingFixed(
						argExPaths.toExtractPathsForQuery(ctxPath));

				/*TODO: this should work also with self::node(); we need to extract all the children of
				 * context in order to be able to evaluate the condition, see VLDB '06 paper for details
				 */
				if(functionName.equalsIgnoreCase("not")){
					Path p = null;
					if(!ctxPath.isEmpty()){
						p = new Path(new StepItem("child", "node"), fixRet, updateType);
						p.addAsPrefix(ctxPath);
						exPaths.get(PathType.NODE_USED).add(p);
					}
					// we have no context, so we discard the last step and add child::node() as last step
					else {
						for (PathType key : argExPaths.keySet()) {
							for (Path pathNot : argExPaths.get(key)) {
								Path clonedPath = pathNot.clone();
								while(clonedPath.getLast().isVarItem())
									clonedPath.removeLast();
								clonedPath.removeLast();
								
								clonedPath.addLast(new StepItem("child", "node"));
								exPaths.get(PathType.NODE_USED).add(clonedPath);
							}
						}
					}
				}
			}
		}
		
		return exPaths;
	}

	/**
	 * Extract paths from a path expression.
	 *
	 * @param actualNode the root of the abstract syntax tree
	 * @param env the static environment
	 * @param returned say if we are in a return statement or not
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from this subtree
	 * @throws IllegalStateException the illegal state exception
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPathsFromPathExpr(SimpleNode actualNode,
			Environment env, boolean returned, FLWORType flworType, 
			Path ctxPath, boolean fixReturnedPaths, 
			UpdateOperationType updateType) 
	throws IllegalStateException, ParserConfigurationException, SAXException, IOException {
		
		ExtractedPaths exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		String axis = "";
		// we don't want to alter contextPath for the calling context
		Path contextPathForPath = ctxPath.clone();
		if(fixReturnedPaths)
			contextPathForPath.setReturnPreserving(fixReturnedPaths);
		Paths intermediateStringPathUsed = new Paths();
		
		for(int c = 0; c < actualNode.jjtGetNumChildren(); c++){
			SimpleNode actualChild = actualNode.getChild(c);
			System.out.println(" PathExpr index c = " + c + " " 
			  + XParserTreeConstants.jjtNodeName[actualChild.getId()]
		    );
			
			if(actualChild.getId() == XParserTreeConstants.JJTSLASH){
				if(c == 0)
					contextPathForPath.add(new StepItem("/", ""));
				else
					axis = "child";					
				continue;
			}
			if(actualChild.getId() == XParserTreeConstants.JJTSLASHSLASH){
				if(c == 0)
					contextPathForPath.add(new StepItem("//", ""));
				else
					axis = "descendant";
				continue;
			}
			
			// STEP/P-USE 3, first part
			StepItem lastStepOfContextPath = contextPathForPath.getLastStep();
			if(lastStepOfContextPath != null 
			   && lastStepOfContextPath.getTest() != null
			   && contextPathForPath.isText()){
				Path intermediateStringPath = contextPathForPath.clone();
				intermediateStringPath.setOperationType(updateType);
				intermediateStringPathUsed.add(intermediateStringPath);
			}
				
			
			/* we do not clone the contextPath because it needs to be shared 
			   among successive evaluations of steps */
			ExtractedPaths newExPaths = null;
			
			if(actualChild.getId() == XParserTreeConstants.JJTVARNAME){
				newExPaths = extractPathsFromVariable(actualChild, env, 
						returned, contextPathForPath, fixReturnedPaths, updateType);
				// variable binding always overwrite, we can have at most on path
				// binded to the variable, so it's correct to get the first
				// (and only) one
				if(!newExPaths.get(PathType.NODE_RETURNED).isEmpty())
					contextPathForPath.addAll(0, 
							newExPaths.get(PathType.NODE_RETURNED).getFirst());
				else
					contextPathForPath.addAll(0, 
							newExPaths.get(PathType.STRING_RETURNED).getFirst());
			}
			else if(actualChild.getId() == XParserTreeConstants.JJTSTEPEXPR)
				newExPaths = extractPathsFromStepExpr(actualChild, env, 0, returned, 
					flworType, axis, contextPathForPath, fixReturnedPaths, updateType);
			else if(actualChild.getId() == XParserTreeConstants.JJTFUNCTIONCALL){
				newExPaths = extractPathsFromFunctionCall(actualChild, env, returned, 
						flworType, contextPathForPath, fixReturnedPaths, updateType);
				if(actualChild.getChild(0).getValue().equalsIgnoreCase("id")
						&& actualChild.getParent().jjtGetNumChildren() > c - 1){
					contextPathForPath.add(new StepItem("descendant", "node()"));
				}
			}			
			else {
				throw new IllegalStateException("Node type " 
						+ XParserTreeConstants.jjtNodeName[actualChild.getId()] 
						     + " is not valid in extractPathsFromPathExpr method.");
			}
			// STEP/P-USE 1 and 2
			exPaths.get(PathType.NODE_USED).addAll(
					newExPaths.get(PathType.NODE_USED));
			exPaths.get(PathType.STRING_USED).addAll(
					newExPaths.get(PathType.STRING_USED));
			exPaths.get(PathType.EVERYTHING_BELOW_USED).addAll(
					newExPaths.get(PathType.EVERYTHING_BELOW_USED));
			// STEP/P-RET
			exPaths.put(PathType.NODE_RETURNED, newExPaths.get(PathType.NODE_RETURNED));
			exPaths.put(PathType.STRING_RETURNED, newExPaths.get(PathType.STRING_RETURNED));
			
			// here we handle step[cond] if the last son is a PredicateList alias cond
			SimpleNode lastChildOfActualChild = actualChild.getChild(
					actualChild.jjtGetNumChildren() - 1);			
			if(lastChildOfActualChild.getId() == XParserTreeConstants.JJTPREDICATELIST)				
				exPaths.addAll(extractPathsFromStepCond(lastChildOfActualChild, env, 
						flworType, exPaths, contextPathForPath, fixReturnedPaths, updateType)); // contextPathForPath.clone()?
			axis = "";
		}
		// STEP/P-USE 3, second part
		exPaths.get(PathType.STRING_USED).addAll(intermediateStringPathUsed);
		return exPaths;
	}
	
	
	/**
	 * Extract paths for step with predicates/conditions.
	 *
	 * @param actualNode the root of the abstract syntax tree
	 * @param env the static environment
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths for step[cond]
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IllegalStateException 
	 */
	private ExtractedPaths extractPathsFromStepCond(
			SimpleNode actualNode, 
			Environment env, 
			FLWORType flworType, 
			ExtractedPaths stepPaths, 
			Path ctxPath, 
			boolean fixReturnedPaths, 
			UpdateOperationType updateType) throws IllegalStateException, ParserConfigurationException, SAXException, IOException{
		
		ExtractedPaths condExPaths = extractPathsFromConditions(actualNode, env, 
				flworType, ctxPath, fixReturnedPaths, updateType);
		
		Paths condNodeAbsolute = condExPaths.get(PathType.NODE_ABSOLUTE);
		Paths condNodeRelative = condExPaths.get(PathType.NODE_RELATIVE);
	
		Paths condStringAbsolute = condExPaths.get(PathType.STRING_ABSOLUTE);
		Paths condStringRelative = condExPaths.get(PathType.STRING_RELATIVE);
		
		Paths condEverythingBelowAbsolute = condExPaths.get(PathType.EVERYTHING_BELOW_ABSOLUTE);
		Paths condEverythingBelowRelative = condExPaths.get(PathType.EVERYTHING_BELOW_RELATIVE);
		
		
		ExtractedPaths stepCondExPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		
		Paths actualPaths = new Paths();
		
		// (STEP-nu)
		actualPaths.addAllFilteringReturnFixed(condNodeAbsolute);
		//actualPaths.addAll(condNodeAbsolute);
		actualPaths.addAll(condNodeRelative.addPathAsPrefix(
				stepPaths.get(PathType.NODE_RETURNED)));
		actualPaths.addAll(condNodeRelative.addPathAsPrefix(
				stepPaths.get(PathType.STRING_RETURNED)));
		
		stepCondExPaths.put(PathType.NODE_USED, actualPaths);

		
		// (STEP-su)
		actualPaths = new Paths();
		actualPaths.addAllFilteringReturnFixed(condStringAbsolute);
		actualPaths.addAll(condStringRelative.addPathAsPrefix(
				stepPaths.get(PathType.NODE_RETURNED)));
		actualPaths.addAll(condStringRelative.addPathAsPrefix(
				stepPaths.get(PathType.STRING_RETURNED)));
		
		stepCondExPaths.addAll(PathType.STRING_USED, actualPaths);
		
		// (STEP-ebu)
		//TODO: like for node used/returned or not necessary to preserv them (never "reclassified"?)
		actualPaths = new Paths();
		//actualPaths.addAllFilteringReturnFixed(condEverythingBelowAbsolute);
		actualPaths.addAll(condEverythingBelowAbsolute);
		actualPaths.addAll(condEverythingBelowRelative.addPathAsPrefix(
				stepPaths.get(PathType.NODE_RETURNED)));
		actualPaths.addAll(condEverythingBelowRelative.addPathAsPrefix(
				stepPaths.get(PathType.STRING_RETURNED)));
		
		stepCondExPaths.addAll(PathType.EVERYTHING_BELOW_USED, actualPaths);
		
		// (STEP-RET)
		//stepCondExPaths.put(PathType.NODE_RETURNED, stepCondExPaths.get(PathType.NODE_RETURNED));
		//stepCondExPaths.put(PathType.STRING_RETURNED, stepCondExPaths.get(PathType.STRING_RETURNED));		
		stepCondExPaths.get(PathType.NODE_RETURNED).addAll(
				stepCondExPaths.get(PathType.NODE_RETURNED));
		stepCondExPaths.get(PathType.STRING_RETURNED).addAll(
				stepCondExPaths.get(PathType.STRING_RETURNED));
		stepCondExPaths.put(PathType.NODE_RETURNED, condNodeAbsolute.returnOnlyFixedReturnPaths());
		stepCondExPaths.put(PathType.STRING_RETURNED, condStringAbsolute.returnOnlyFixedReturnPaths());
		
		return stepCondExPaths;
	}
	
	/**
	 * Extract paths for predicates/conditions.
	 *
	 * @param actualNode the actual node
	 * @param env the static environment
	 * @param flworType the flwor type expression in which the present is nested, if any
	 * @param ctxPath the ctx path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths for conditions
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IllegalStateException 
	 */
	private ExtractedPaths extractPathsFromConditions(SimpleNode actualNode,
			Environment env, FLWORType flworType, Path ctxPath, 
			boolean fixReturnedPaths, UpdateOperationType updateType) throws IllegalStateException, ParserConfigurationException, SAXException, IOException {
		
		ExtractedPaths condExPaths = new ExtractedPaths(ExtractedPathsType.EP_CONDITION);
		
		// PredicateList := Predicate*
		// Predicate := "[" Exp "]" <- we want to call the method on Exp, so the son of the son
		for(int c = 0; c < actualNode.jjtGetNumChildren(); c++){
			SimpleNode actualChild = actualNode.getChild(c);
			condExPaths.addAll(extractPathsFromCondition(actualChild.getChild(0).getChild(0), 
					env, flworType, ctxPath.clone(), fixReturnedPaths, updateType));
		}
		
		return condExPaths;
	}

	/**
	 * Extract paths for a single predicate/condition.
	 *
	 * @param actualNode the actual node
	 * @param env the static environment
	 * @param flworType the flwor type expression in which the present is nested, if any
	 * @param ctxPath the ctx path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths for cond
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IllegalStateException 
	 */
	private ExtractedPaths extractPathsFromCondition(SimpleNode actualNode,
			Environment env, FLWORType flworType, Path ctxPath, 
			boolean fixReturnedPaths, UpdateOperationType updateType) throws IllegalStateException, ParserConfigurationException, SAXException, IOException {
		ExtractedPaths condExPaths = new ExtractedPaths(ExtractedPathsType.EP_CONDITION);
		// Predicate := "[" Expr "]", we need receive node Expr from the caller
		int actualNodeId = actualNode.getId();
		
		// the different path categories for this condition
		Paths nodeAbs = condExPaths.get(PathType.NODE_ABSOLUTE);
		Paths nodeRel = condExPaths.get(PathType.NODE_RELATIVE);
		Paths ebAbs = condExPaths.get(PathType.EVERYTHING_BELOW_ABSOLUTE);
		Paths ebRel = condExPaths.get(PathType.EVERYTHING_BELOW_RELATIVE);
		Paths stringAbs = condExPaths.get(PathType.STRING_ABSOLUTE);
		Paths stringRel = condExPaths.get(PathType.STRING_RELATIVE);
		
		// (Cond) rule
		if(actualNodeId == XParserTreeConstants.JJTANDEXPR 
				|| actualNodeId == XParserTreeConstants.JJTOREXPR){
			condExPaths.addAll(extractPathsFromCondition(
					actualNode.getChild(0), env, flworType, ctxPath.clone(), 
					fixReturnedPaths, updateType));
			condExPaths.addAll(extractPathsFromCondition(
					actualNode.getChild(1), env, flworType, ctxPath.clone(), 
					fixReturnedPaths, updateType));
		}
		// (Exp) Expression rules and (CExp) Comparison Expression Rules
		else if(actualNodeId == XParserTreeConstants.JJTCOMPARISONEXPR){
			
			String cmpSymbol = actualNode.getValue();
			ComparisonType cmpType;
			
			// VComp, from FULL-XQuery grammar definition
			if(cmpSymbol.equals("=")||
			   cmpSymbol.equals("!=")||
			   cmpSymbol.equals("<")||
			   cmpSymbol.equals("<=")||
			   cmpSymbol.equals(">")||
			   cmpSymbol.equals(">=")||
			   cmpSymbol.equalsIgnoreCase("eq") ||
			   cmpSymbol.equalsIgnoreCase("ne") ||
			   cmpSymbol.equalsIgnoreCase("lt") ||
			   cmpSymbol.equalsIgnoreCase("le") ||
			   cmpSymbol.equalsIgnoreCase("gt") ||
			   cmpSymbol.equalsIgnoreCase("ge")
			){
				cmpType = ComparisonType.VALUECOMP;
			}
			// NComp, from FULL-XQuery grammar definition
			else if(cmpSymbol.equalsIgnoreCase("is") ||
					cmpSymbol.equals("<<") ||
					cmpSymbol.equals(">>")){
				cmpType = ComparisonType.NODECOMP;
			}
			else
				throw new IllegalArgumentException("Symbol " + cmpSymbol 
						+ " is not valid as a comparison symbol according " +
								"to our FULL-XQuery grammar.");
			
			SimpleNode exp1 = actualNode.getChild(0);
			SimpleNode exp2 = actualNode.getChild(1);
			ExtractedPaths exPathExp1QUERY = extractPaths(exp1, env, flworType, 
					ctxPath, fixReturnedPaths, updateType);
			ExtractedPaths exPathExp2QUERY = extractPaths(exp2, env, flworType, 
					ctxPath, fixReturnedPaths, updateType);
			ExtractedPaths exPathExp1 = exPathExp1QUERY.toExtractPathsForCond();
			ExtractedPaths exPathExp2 = exPathExp2QUERY.toExtractPathsForCond();
				
			
			Paths everythingBelowAbsolute = new Paths();
			Paths everythingBelowRelative = new Paths();
			
			// not in the rule but I think is needed
			
			everythingBelowAbsolute.addAll(exPathExp1.get(PathType.EVERYTHING_BELOW_ABSOLUTE));
			everythingBelowRelative.addAll(exPathExp1.get(PathType.EVERYTHING_BELOW_RELATIVE));
			everythingBelowAbsolute.addAll(exPathExp2.get(PathType.EVERYTHING_BELOW_ABSOLUTE));
			everythingBelowRelative.addAll(exPathExp2.get(PathType.EVERYTHING_BELOW_RELATIVE));
			condExPaths.get(PathType.STRING_ABSOLUTE).addAll(exPathExp1.get(PathType.STRING_ABSOLUTE));
			condExPaths.get(PathType.STRING_RELATIVE).addAll(exPathExp2.get(PathType.STRING_RELATIVE));
			condExPaths.get(PathType.STRING_ABSOLUTE).addAll(exPathExp2.get(PathType.STRING_ABSOLUTE));
			condExPaths.get(PathType.STRING_RELATIVE).addAll(exPathExp1.get(PathType.STRING_RELATIVE));
			
			
			/* alternative version far from the rules but equivalent, testing if 
			 * one of the "operands" is a QP it's the same as saying it not to be
			 * an Arithmetic or Comparison expression, that here equals to test
			 * if the ExtractedPaths type is different from EP_QUERY
			 */
			if(exPathExp1QUERY.getType() == ExtractedPathsType.EP_QUERY
			   && exPathExp2QUERY.getType() == ExtractedPathsType.EP_QUERY
			   && cmpType == ComparisonType.VALUECOMP){
				
				// (CExp-eb)
				everythingBelowAbsolute.addAbsOrRel(true, 
						exPathExp1QUERY.get(PathType.NODE_RETURNED));
				everythingBelowRelative.addAbsOrRel(false, 
						exPathExp1QUERY.get(PathType.NODE_RETURNED));
				// (CExp-node)
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(true, 
						exPathExp1QUERY.get(PathType.NODE_USED));
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(false, 
						exPathExp1QUERY.get(PathType.NODE_USED));

				// (CExp-eb)
				everythingBelowAbsolute.addAbsOrRel(true, 
						exPathExp2QUERY.get(PathType.NODE_RETURNED));
				everythingBelowRelative.addAbsOrRel(false, 
						exPathExp2QUERY.get(PathType.NODE_RETURNED));
				
				// (CExp-node)
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(true, 
						exPathExp2QUERY.get(PathType.NODE_USED));
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(false, 
						exPathExp2QUERY.get(PathType.NODE_USED));
			}
			else if(exPathExp1QUERY.getType() != ExtractedPathsType.EP_QUERY
					&& exPathExp2QUERY.getType() == ExtractedPathsType.EP_QUERY
					&& cmpType == ComparisonType.VALUECOMP){
				// (CExp-eb)
				everythingBelowAbsolute.addAbsOrRel(true, 
						exPathExp2QUERY.get(PathType.NODE_RETURNED));
				everythingBelowRelative.addAbsOrRel(false, 
						exPathExp2QUERY.get(PathType.NODE_RETURNED));
				// (CExp-node)
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(true, 
						exPathExp2QUERY.get(PathType.NODE_USED));
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(false, 
						exPathExp2QUERY.get(PathType.NODE_USED));				
			}
			else if(exPathExp1QUERY.getType() == ExtractedPathsType.EP_QUERY
					&& exPathExp2QUERY.getType() != ExtractedPathsType.EP_QUERY
					&& cmpType == ComparisonType.VALUECOMP){
				// (CExp-eb)
				everythingBelowAbsolute.addAbsOrRel(true, 
						exPathExp1QUERY.get(PathType.NODE_RETURNED));
				everythingBelowRelative.addAbsOrRel(false, 
						exPathExp1QUERY.get(PathType.NODE_RETURNED));
				// (CExp-node)
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(true, 
						exPathExp1QUERY.get(PathType.NODE_USED));
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(false, 
						exPathExp1QUERY.get(PathType.NODE_USED));				
			}
			else {
				// common to all rules
				condExPaths.addAll(exPathExp1);
				condExPaths.addAll(exPathExp2);
			}
			/* both different from EP_QUERY or wrong comparison type, 
			 * nothing to do because this case is handled by general rule
			 */

			condExPaths.get(PathType.EVERYTHING_BELOW_ABSOLUTE).addAll(
					everythingBelowAbsolute);
			condExPaths.get(PathType.EVERYTHING_BELOW_RELATIVE).addAll(
					everythingBelowRelative);
		}
		
		// (Arit-Gen) rule
		else if(actualNodeId == XParserTreeConstants.JJTADDITIVEEXPR
				|| actualNodeId == XParserTreeConstants.JJTMULTIPLICATIVEEXPR){
			SimpleNode arit1 = actualNode.getChild(0);
			SimpleNode arit2 = actualNode.getChild(1);
			ExtractedPaths exPathArit1QUERY = extractPaths(arit1, env, flworType, 
					ctxPath, fixReturnedPaths, updateType);
			ExtractedPaths exPathArit2QUERY = extractPaths(arit2, env, flworType, 
					ctxPath, fixReturnedPaths, updateType);
			ExtractedPaths exPathArit1 = exPathArit1QUERY.toExtractPathsForCond();
			ExtractedPaths exPathArit2 = exPathArit2QUERY.toExtractPathsForCond();

			
			Paths everythingBelowAbsolute = new Paths();
			Paths everythingBelowRelative = new Paths();
			
			// not explicitly in the rule but needed imho
			condExPaths.get(PathType.STRING_ABSOLUTE).addAll(
					exPathArit1.get(PathType.STRING_ABSOLUTE));
			condExPaths.get(PathType.STRING_ABSOLUTE).addAll(
					exPathArit2.get(PathType.STRING_ABSOLUTE));
			condExPaths.get(PathType.STRING_RELATIVE).addAll(
					exPathArit1.get(PathType.STRING_RELATIVE));
			condExPaths.get(PathType.STRING_RELATIVE).addAll(
					exPathArit2.get(PathType.STRING_RELATIVE));
			
			/* alternative version far from the rules but equivalent, testing if 
			 * one of the "operands" is a QP it's the same as saying it not to be
			 * an Arithmetic or Comparison expression, that here equals to test
			 * if the ExtractedPaths type is different from EP_QUERY
			 */
			if(exPathArit1QUERY.getType() == ExtractedPathsType.EP_QUERY
			   && exPathArit2QUERY.getType() == ExtractedPathsType.EP_QUERY){
				
				// (AExp-eb)
				everythingBelowAbsolute.addAbsOrRel(true, 
						exPathArit1QUERY.get(PathType.NODE_RETURNED));
				everythingBelowRelative.addAbsOrRel(false, 
						exPathArit1QUERY.get(PathType.NODE_RETURNED));
				// (AExp-node)
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(true, 
						exPathArit1QUERY.get(PathType.NODE_USED));
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(false, 
						exPathArit1QUERY.get(PathType.NODE_USED));
				
				// (AExp-eb)
				everythingBelowAbsolute.addAbsOrRel(true, 
						exPathArit2QUERY.get(PathType.NODE_RETURNED));
				everythingBelowRelative.addAbsOrRel(false, 
						exPathArit2QUERY.get(PathType.NODE_RETURNED));
				// (AExp-node)
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(true, 
						exPathArit2QUERY.get(PathType.NODE_USED));
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(false, 
						exPathArit2QUERY.get(PathType.NODE_USED));
			}
			else if(exPathArit1QUERY.getType() != ExtractedPathsType.EP_QUERY
					&& exPathArit2QUERY.getType() == ExtractedPathsType.EP_QUERY){
				
				// (AExp-eb)
				everythingBelowAbsolute.addAbsOrRel(true, 
						exPathArit2QUERY.get(PathType.NODE_RETURNED));
				everythingBelowRelative.addAbsOrRel(false, 
						exPathArit2QUERY.get(PathType.NODE_RETURNED));
				// (AExp-node)
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(true, 
						exPathArit2QUERY.get(PathType.NODE_USED));
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(false, 
						exPathArit2QUERY.get(PathType.NODE_USED));
				
				condExPaths.get(PathType.NODE_ABSOLUTE).addAll(
						exPathArit1QUERY.get(PathType.NODE_ABSOLUTE));
				condExPaths.get(PathType.NODE_RELATIVE).addAll(
						exPathArit1QUERY.get(PathType.NODE_RELATIVE));

			}
			else if(exPathArit1QUERY.getType() == ExtractedPathsType.EP_QUERY
					&& exPathArit2QUERY.getType() != ExtractedPathsType.EP_QUERY){

				// (AExp-eb)
				everythingBelowAbsolute.addAbsOrRel(true, 
						exPathArit1QUERY.get(PathType.NODE_RETURNED));
				everythingBelowRelative.addAbsOrRel(false, 
						exPathArit1QUERY.get(PathType.NODE_RETURNED));
				// (AExp-node)
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(true, 
						exPathArit1QUERY.get(PathType.NODE_USED));
				condExPaths.get(PathType.NODE_ABSOLUTE).addAbsOrRel(false, 
						exPathArit1QUERY.get(PathType.NODE_USED));
				
				condExPaths.get(PathType.NODE_ABSOLUTE).addAll(
						exPathArit2QUERY.get(PathType.NODE_ABSOLUTE));
				condExPaths.get(PathType.NODE_RELATIVE).addAll(
						exPathArit2QUERY.get(PathType.NODE_RELATIVE));

			}
			// both different from EP_QUERY, nothing to do because this case is handled by general rule
			else {
				// common to all rules, everythingBelow can change, though
				// we are not explicitly handling rules (arit-node 1 and 2)
				// because if they will apply the node category will be empty,
				// so preventing its addition is useless
				condExPaths.addAll(exPathArit1);
				condExPaths.addAll(exPathArit2);
			}
			condExPaths.get(PathType.EVERYTHING_BELOW_ABSOLUTE).addAll(
					everythingBelowAbsolute);
			condExPaths.get(PathType.EVERYTHING_BELOW_RELATIVE).addAll(
					everythingBelowRelative);
			
		}
		// (path-abs), (path-rel) and (path-eb) rules
		else if(actualNodeId == XParserTreeConstants.JJTPATHEXPR){
			/* TODO: here we do not take into account other than simple path
			 * that is without FLWOR expression inside or variable occurrences:
			 * by this assumption the rules expect Node/String returned to be
			 * a SINGLETON and will not work in other cases or can have unexpected
			 * behavious.
			 * */
			ExtractedPaths pathExPaths = extractPathsFromPathExpr(actualNode, env, 
					false, flworType, new Path(updateType), fixReturnedPaths, updateType);
			Path path = null;
			
			// node case
			if(pathExPaths.get(PathType.NODE_RETURNED).isEmpty() == false){
				path = pathExPaths.get(PathType.NODE_RETURNED).getFirst();
				// (path-abs): node case
				if(path.isAbs()){
					nodeAbs.add(path);
					//nodeRel will be empty
				}
				// (path-rel): node case
				else {
					//nodeAbs will be empty
					nodeRel.add(pathExPaths.get(PathType.NODE_RETURNED).getFirst());
				}
			}
			// string case
			else if(pathExPaths.get(PathType.STRING_RETURNED).isEmpty() == false){
				path = pathExPaths.get(PathType.STRING_RETURNED).getFirst();
				// (path-abs): string case
				if(path.isAbs()){
					stringAbs.add(path);
					//stringRel will be empty
				}
				else {
					//stringAbs will be empty
					stringRel.add(path);
				}
			}
			
			//(path-eb), nothing to do
		}
		
		// (function) rule
		else if(actualNodeId == XParserTreeConstants.JJTFUNCTIONCALL){

			ExtractedPaths expExPaths = extractPaths(actualNode, env, flworType, 
					ctxPath, fixReturnedPaths, updateType).toExtractPathsForCond();
			nodeAbs.addAll(expExPaths.get(PathType.NODE_ABSOLUTE));
			nodeRel.addAll(expExPaths.get(PathType.NODE_RELATIVE));
			stringAbs.addAll(expExPaths.get(PathType.STRING_ABSOLUTE));
			stringRel.addAll(expExPaths.get(PathType.STRING_RELATIVE));
			ebAbs.addAll(expExPaths.get(PathType.EVERYTHING_BELOW_ABSOLUTE));
			ebAbs.addAll(expExPaths.get(PathType.EVERYTHING_BELOW_RELATIVE));
			
			/*
			// skipping functionQName, the first element
			for(int c = 1; c < actualNode.jjtGetNumChildren(); c++){
				expExPaths = extractPaths(actualNode.getChild(c), env, flworType, ctxPath, fixReturnedPaths).toExtractPathsForCond();
				nodeAbs.addAll(expExPaths.get(PathType.NODE_ABSOLUTE));
				nodeRel.addAll(expExPaths.get(PathType.NODE_RELATIVE));
			}
			//TODO fix return boolean value?
			nodeRel.add(new Path(new StepItem("self", "node"), fixReturnedPaths));
			*/
		}
		else if(actualNodeId == XParserTreeConstants.JJTSTEPEXPR){
			ExtractedPaths pathExPaths = extractPathsFromStepExpr(actualNode, env, 
					0, false, flworType, "", ctxPath.clone(), fixReturnedPaths, 
					updateType);
			// we assume simplePaths, all the categories are lists of one element
			Path retPath = !pathExPaths.get(PathType.NODE_RETURNED).isEmpty() 
			? pathExPaths.get(PathType.NODE_RETURNED).getFirst()
					: pathExPaths.get(PathType.STRING_RETURNED).getFirst();
			
			boolean isTextPath = retPath.isText();
			
			if(retPath.isAbs()){
				if(isTextPath)
					stringAbs.add(retPath);
				else
					nodeAbs.add(retPath);
			}
			else {
				if(isTextPath)
					stringRel.add(retPath);
				else
					nodeRel.add(retPath);
			}
		}
		else if(actualNodeId == XParserTreeConstants.JJTEXPR){
			return extractPaths(actualNode.getChild(0), env, flworType, 
					ctxPath, fixReturnedPaths, updateType);
		}
		// (valeur) rule, we should test all possible value node
		else {
			return extractPaths(actualNode, env, flworType, ctxPath, 
					fixReturnedPaths, updateType).toExtractPathsForCond();
		}
		
		return condExPaths;
	}
	
	/**
	 * Extract paths from comparisons.
	 *
	 * @param actualNode the actual node
	 * @param env the static environment
	 * @param returned the returned
	 * @param flworType the flwor type expression in which the present is nested, if any
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths for the comparison
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IllegalStateException 
	 */
	private ExtractedPaths extractPathsFromComparisons(
			SimpleNode actualNode, 
			Environment env, 
			boolean returned, 
			FLWORType flworType, 
			Path ctxPath, 
			boolean fixReturnedPaths,
			UpdateOperationType updateType) throws IllegalStateException, ParserConfigurationException, SAXException, IOException{
		
		return extractPathsFromCondition(actualNode, env, flworType, ctxPath, 
				fixReturnedPaths, updateType);//.toExtractPathsForQuery(ctxPath);
	}
	
	/**
	 * Extract paths for tests.
	 *
	 * @param axis the axis read at the preceding step of the analysis
	 * @param actualNode the root of the abstract syntax tree
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from this subtree
	 */
	private ExtractedPaths extractPathsFromTests(String axis, 
			SimpleNode actualNode, Path ctxPath, 
			boolean fixReturnedPaths, 
			UpdateOperationType updateType){
		ExtractedPaths exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		String test = "";
		
		//TODO: check if it is complete or not
		switch(actualNode.getId()){
		case XParserTreeConstants.JJTTEXTTEST:
			test = "text()";
			break;
		case XParserTreeConstants.JJTANYKINDTEST:
			test = "node()";
			break;
		case XParserTreeConstants.JJTCOMMENTTEST:
			test = "comment()";
			break;
/*		case XParserTreeConstants.JJTNODETEST:
			test = actualNode.getChild(0).getValue();
			break;
		case XParserTreeConstants.JJTNAMETEST:
			test = actualNode.getChild(0).getValue();
			break;*/
		case XParserTreeConstants.JJTWILDCARD:
			test = "*";
			break;
		case XParserTreeConstants.JJTQNAME:
			if(actualNode.getParent().getId() == XParserTreeConstants.JJTNAMETEST)
				if(actualNode.getParent().getParent().getId() 
						== XParserTreeConstants.JJTNODETEST)
					if(actualNode.getParent().getParent().getParent().getId() 
						== XParserTreeConstants.JJTABBREVFORWARDSTEP 
						&& actualNode.getParent().getParent().getParent().getValue() != null
						&& actualNode.getParent().getParent().getParent().getValue().equals("@")){
						//test = "@";
						axis = "attribute";
					}
			//test = test + actualNode.getValue();
			test = actualNode.getValue();
			break;
		default:
			throw new IllegalArgumentException("Node id not recognized as valid for test node: " 
						+ XParserTreeConstants.jjtNodeName[actualNode.getId()] + ".");
		}
		//TODO: to check...
/*		if(ctxPath.isEmpty()){
			throw new IllegalStateException("Relative path without context path.");
		}*/
		
		ctxPath.add(ctxPath.size(), new StepItem(axis, test));
		PathType pathType = ctxPath.isText() ? PathType.STRING_RETURNED 
				: PathType.NODE_RETURNED;
		ctxPath.setPathType(pathType);
		ctxPath.setOperationType(updateType);
		if(!exPaths.addPath(pathType, ctxPath))
			throw new IllegalStateException("Impossible to add Path extracted from " 
					+ XParserTreeConstants.jjtNodeName[actualNode.getId()] + " node.");
		return exPaths;
	}
	
	/**
	 * Extract pahts from abbreviated forward step.
	 *
	 * @param actualNode the root of the abstract syntax tree
	 * @param env the static environment
	 * @param returned say if we are in a return statement or not
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param axis the axis read at the preceding step in the analysis
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from this subtree
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPathsFromAbbreviatedForwardStep(
			SimpleNode actualNode, Environment env, boolean returned, 
			FLWORType flworType, String axis, Path ctxPath, 
			boolean fixReturnedPaths,
			UpdateOperationType updateType) 
	throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException {
		
		ExtractedPaths exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		if(actualNode.getId() != XParserTreeConstants.JJTABBREVFORWARDSTEP)
			throw new IllegalArgumentException("This method can be applied only to JJTABBREVFORWARDSTEP nodes, found " 
					+ XParserTreeConstants.jjtNodeName[actualNode.getId()] + ".");
		/* ABBREVFORWARDSTEP := @? NodeTest()
		   NodeTest := KindTest() | NameTest(), 
		   that's why I call recursive method one level below the actual node,
		   there is no need to analyze NodeTest node, we need only to analyze
		   its child node
		*/
		exPaths.addAll(extractPaths(actualNode.getChild(0).getChild(0), 
				env, returned, flworType, axis, ctxPath, fixReturnedPaths, 
				updateType));
		return exPaths;
	}

	/**
	 * Extract paths from the expression rooted in node.
	 *
	 * @param node the root of the abstract syntax tree
	 * @param env the static environment
	 * @param returned say if we are in a return statement or not
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param axis the axis read at the preceding step in the analysis
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from this subtree
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPaths(SimpleNode node, Environment env,
			boolean returned, FLWORType flworType, String axis, 
			Path ctxPath, boolean fixReturnedPaths, UpdateOperationType updateType) throws ParserConfigurationException, SAXException, IOException {
		return extractPaths(node, env, 0, returned, flworType, axis, ctxPath, 
				fixReturnedPaths, updateType);
	}

	/**
	 * Extract paths from step expression.
	 *
	 * @param node the root of the abstract syntax tree
	 * @param env the static environment
	 * @param actualSonIndex the index of the son of the current node from which 
	 * the analysis have to start
	 * @param returned say if we are in a return statement or not
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param axis the axis read at the preceding step in the analysis
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from this subtree
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPathsFromStepExpr(SimpleNode node,
			Environment env, int actualSonIndex, boolean returned, 
			FLWORType flworType, String axis, Path ctxPath, 
			boolean fixReturnedPaths, 
			UpdateOperationType updateType) 
	throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException {

		SimpleNode actualNode = actualSonIndex != 0 
		? node.getChild(actualSonIndex) : node;
		
		switch(actualNode.getId()){
			// VARNAME := QNAME
			case XParserTreeConstants.JJTVARNAME:
//				SimpleNode parent = actualNode.getParent();
	//			if(parent.getChild(parent.jjtGetNumChildren()-1) == actualNode)
					return extractPathsFromVariable(actualNode, env, returned, ctxPath, fixReturnedPaths, updateType);
	//			return extractPathsForVariableWithStep(actualNode.getParent(), env, returned, flworType, ctxPath.clone());
			// TODO: is not sufficient for step[cond], we should test also predicate if there is (actualNode.getChild(1))
			// STEPEXPR := FilterExpr | AxisStep
			case XParserTreeConstants.JJTSTEPEXPR:
				SimpleNode firstSonNode = actualNode.getChild(0);
				String actualAxis = "";
				//StepItem step = null;
				ExtractedPaths exPaths = null;
				if(firstSonNode.getId() == XParserTreeConstants.JJTFORWARDAXIS
				   ||
				   firstSonNode.getId() == XParserTreeConstants.JJTREVERSEAXIS){
					actualAxis = firstSonNode.getValue();
//					step = new StepItem(actualAxis, actualNode.getChild(1).getChild(0).getChild(0).getValue());
					exPaths = extractPaths(actualNode.getChild(1), env, flworType, 
							actualAxis, ctxPath, fixReturnedPaths, updateType);
//					Path path = new Path(step);
					
					// RULE step-nr, step-sr, step-used (for the last one, nothing to do)
			/*		if(path.isText())
						exPaths.get(PathType.STRING_RETURNED).add(path);
					else
						exPaths.get(PathType.NODE_RETURNED).add(path);*/
				}
				else if(firstSonNode.getId() == XParserTreeConstants.JJTABBREVFORWARDSTEP)
					exPaths = extractPaths(firstSonNode, env, 0, returned, 
							flworType, axis, ctxPath, fixReturnedPaths, updateType);
		
				// THIS IS WRONG
				//if(axis.compareToIgnoreCase("descendant") == 0)
				//	exPaths.addAsPrefixStep(new StepItem(axis, "*"));
				
				else if(firstSonNode.getId() == XParserTreeConstants.JJTFUNCTIONCALL){
					exPaths = extractPathsFromFunctionCall(firstSonNode, env, returned, 
							flworType, ctxPath, fixReturnedPaths, updateType);
					if(firstSonNode.getChild(0).getValue().equalsIgnoreCase("id")
							&& (firstSonNode.getParent().getChild(firstSonNode.getParent().
									jjtGetNumChildren() - 1) != firstSonNode
								)){
						ctxPath.add(new StepItem("descendant", "node()"));
					}
				}			
				
				return exPaths;
				
			case XParserTreeConstants.JJTFUNCTIONCALL:
				return extractPaths(actualNode.getChild(0), env, returned, 
						flworType, ctxPath.clone(), fixReturnedPaths, updateType);
			default:
				throw new IllegalArgumentException("Node id not recognized while analyzing sons of PATHEXPR: " 
						+ XParserTreeConstants.jjtNodeName[actualNode.getId()] + ".");
		}
	}
	
	/**
	 * Extract paths from if expression.
	 *
	 * @param node the root of the abstract syntax tree
	 * @param env the static environment
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from this subtree
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IllegalStateException 
	 */
	//TODO string relative in cond makes sense in if statement?
	private ExtractedPaths extractPathsFromIfExpr(SimpleNode node,
			Environment env, FLWORType flworType, Path ctxPath, 
			boolean fixReturnedPaths, UpdateOperationType updateType) 
	throws IllegalArgumentException, IllegalStateException, ParserConfigurationException, SAXException, IOException {
		
		if(node.getId() != XParserTreeConstants.JJTIFEXPR)
			throw new IllegalArgumentException(
					"Extraction rules for if-clause can be applied only to JJTIFEXPR nodes, found " 
					+ XParserTreeConstants.jjtNodeName[node.getId()] + ".");
		// IFEXPR = "if" "(" EXPR ")" "then" SINGLEEXPR "else" SINGLEEXPR
		ExtractedPaths exPathsQ0 = extractPathsFromCondition(node.getChild(0), env, 
				flworType, ctxPath.clone(), fixReturnedPaths, updateType);
		ExtractedPaths exPathsQ1 = extractPaths(node.getChild(1), env, flworType, 
				ctxPath.clone(), fixReturnedPaths, updateType);
		ExtractedPaths exPathsQ2 = extractPaths(node.getChild(2), env, flworType, 
				ctxPath.clone(), fixReturnedPaths, updateType);
		
		ExtractedPathsType q1PathType = exPathsQ1.getType();
		ExtractedPathsType q2PathType = exPathsQ2.getType();
		
		if(q1PathType != q2PathType)	
			throw new IllegalStateException("The \"then\" and \"else\" branches should be " +
					"of the same type (both updates of both queries):\nthen = " 
					+ (q1PathType == ExtractedPathsType.EP_QUERY 
							? "QUERY" : "UPDATE") 
					+ " \nelse = " + (q2PathType == ExtractedPathsType.EP_QUERY 
							? "QUERY" : "UPDATE"));
		
		// USELESS?
		 
		   if(q1PathType != ExtractedPathsType.EP_QUERY){
			ExtractedPaths exPathIfUpdates = new ExtractedPaths(ExtractedPathsType.EP_UPDATE);
			
			Paths nodeOnly = exPathIfUpdates.get(PathType.NODE_ONLY);
			Paths oneLevelBelow = exPathIfUpdates.get(PathType.ONE_LEVEL_BELOW);
			Paths everythingBelow = exPathIfUpdates.get(PathType.EVERYTHING_BELOW);
			
			// (if-no)
				nodeOnly.addAll(exPathsQ0.get(PathType.NODE_RELATIVE)
						.addPathAsPrefix(ctxPath, false));
				nodeOnly.addAll(exPathsQ0.get(PathType.NODE_ABSOLUTE));
				
				if(q1PathType == ExtractedPathsType.EP_UPDATE){
					nodeOnly.addAll(exPathsQ1.get(PathType.NODE_ONLY));
					nodeOnly.addAll(exPathsQ2.get(PathType.NODE_ONLY));
				}
				else if(q1PathType == ExtractedPathsType.EP_QUERY){
					nodeOnly.addAll(exPathsQ1.get(PathType.NODE_USED));
					nodeOnly.addAll(exPathsQ1.get(PathType.NODE_RETURNED));
					nodeOnly.addAll(exPathsQ2.get(PathType.NODE_USED));
					nodeOnly.addAll(exPathsQ2.get(PathType.NODE_RETURNED));
				}
					
			// (if-olb)
				oneLevelBelow.addAll(exPathsQ0.get(PathType.STRING_RELATIVE)
						.addPathAsPrefix(ctxPath, false));
				oneLevelBelow.addAll(exPathsQ0.get(PathType.STRING_ABSOLUTE));
				oneLevelBelow.Par();
				if(q1PathType == ExtractedPathsType.EP_UPDATE){
					oneLevelBelow.addAll(exPathsQ1.get(PathType.ONE_LEVEL_BELOW));
					oneLevelBelow.addAll(exPathsQ2.get(PathType.ONE_LEVEL_BELOW));
				}
				else if(q1PathType == ExtractedPathsType.EP_QUERY){
					oneLevelBelow.addAll(exPathsQ1.get(PathType.STRING_USED));
					oneLevelBelow.addAll(exPathsQ1.get(PathType.STRING_RETURNED));
					oneLevelBelow.addAll(exPathsQ2.get(PathType.STRING_USED));
					oneLevelBelow.addAll(exPathsQ2.get(PathType.STRING_RETURNED));
				}
			// (if-eb)
				everythingBelow.addAll(exPathsQ0.get(PathType.EVERYTHING_BELOW_RELATIVE)
						.addPathAsPrefix(ctxPath, false));
				everythingBelow.addAll(exPathsQ0.get(PathType.EVERYTHING_BELOW_ABSOLUTE));
				if(q1PathType == ExtractedPathsType.EP_UPDATE){
					everythingBelow.addAll(exPathsQ1.get(PathType.EVERYTHING_BELOW));
					everythingBelow.addAll(exPathsQ2.get(PathType.EVERYTHING_BELOW));
				}
				else if(q1PathType == ExtractedPathsType.EP_QUERY){
					everythingBelow.addAll(exPathsQ1.get(PathType.EVERYTHING_BELOW_USED));
					everythingBelow.addAll(exPathsQ2.get(PathType.EVERYTHING_BELOW_USED));
				}
			return exPathIfUpdates;
		}
		
		ExtractedPaths exPathIfExpr = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		PathType actualPt;
		// rule 7.1 IF-SU = Q0-SU UNION Q0-SR UNION Q1-SU UNION Q2-SU
		actualPt = PathType.STRING_USED;
		exPathIfExpr.get(actualPt).addAllFromCondFilteringReturnFixed(
				exPathsQ0.get(PathType.STRING_ABSOLUTE));
		exPathIfExpr.get(actualPt).addAllFromCondFilteringReturnFixed(
				exPathsQ0.get(PathType.STRING_RELATIVE).addPathAsPrefix(ctxPath, false));
		
		exPathIfExpr.addPath(actualPt, exPathsQ1.get(PathType.STRING_USED));
		exPathIfExpr.addPath(actualPt, exPathsQ2.get(PathType.STRING_USED));
		exPathIfExpr.get(PathType.STRING_RETURNED).addAll(
				exPathsQ0.get(PathType.STRING_ABSOLUTE).returnOnlyFixedReturnPaths());
		exPathIfExpr.get(PathType.STRING_RETURNED).addAll(
				exPathsQ0.get(PathType.STRING_RELATIVE).returnOnlyFixedReturnPaths());
		
		// rule 7.2 IF-NU = Q0-NU UNION Q0-NR UNION Q1-NU UNION Q2-NU
		actualPt = PathType.NODE_USED;
		exPathIfExpr.get(actualPt).addAllFromCondFilteringReturnFixed(
				exPathsQ0.get(PathType.NODE_ABSOLUTE));
		exPathIfExpr.get(actualPt).addAllFromCondFilteringReturnFixed(
				exPathsQ0.get(PathType.NODE_RELATIVE).addPathAsPrefix(ctxPath, false));
		
		exPathIfExpr.addPath(actualPt, exPathsQ1.get(PathType.NODE_USED));
		exPathIfExpr.addPath(actualPt, exPathsQ2.get(PathType.NODE_USED));
		
		exPathIfExpr.get(PathType.NODE_RETURNED).addAll(
				exPathsQ0.get(PathType.NODE_ABSOLUTE).returnOnlyFixedReturnPaths());
		exPathIfExpr.get(PathType.NODE_RETURNED).addAll(
				exPathsQ0.get(PathType.NODE_RELATIVE).returnOnlyFixedReturnPaths());
		
		// rule 7.3 IF-SR = Q1-SR UNION Q2-SR, IF-NR = Q1-NR UNION Q2-NR
		actualPt = PathType.STRING_RETURNED;
		exPathIfExpr.addPath(actualPt, exPathsQ1.get(PathType.STRING_RETURNED));
		exPathIfExpr.addPath(actualPt, exPathsQ2.get(PathType.STRING_RETURNED));
		
		actualPt = PathType.NODE_RETURNED;
		exPathIfExpr.addPath(actualPt, exPathsQ1.get(PathType.NODE_RETURNED));
		exPathIfExpr.addPath(actualPt, exPathsQ2.get(PathType.NODE_RETURNED));		

		// rule 7.4 IF-EBU = Q0-EBU UNION Q1-EBU UNION Q2-EBU
		actualPt = PathType.EVERYTHING_BELOW_USED;
//		exPathIfExpr.addPath(actualPt, exPathsQ0.get(PathType.EVERYTHING_BELOW_USED));
		exPathIfExpr.get(actualPt).addAll(
				exPathsQ0.get(PathType.EVERYTHING_BELOW_ABSOLUTE));
		exPathIfExpr.get(actualPt).addAll(
				exPathsQ0.get(PathType.EVERYTHING_BELOW_RELATIVE).addPathAsPrefix(ctxPath, false));
		
		exPathIfExpr.addPath(actualPt, exPathsQ1.get(PathType.EVERYTHING_BELOW_USED));
		exPathIfExpr.addPath(actualPt, exPathsQ2.get(PathType.EVERYTHING_BELOW_USED));
	
		return exPathIfExpr;
	}

	/**
	 * Extract paths from let clause.
	 *
	 * @param node the root of the abstract syntax tree
	 * @param env the static environment
	 * @param returned say if we are in a return statement or not
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from this subtree
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPathsFromLetClause(
			SimpleNode node, 
			Environment env, boolean returned, 
			FLWORType flworType, Path ctxPath, 
			boolean fixReturnedPaths,
			UpdateOperationType updateType) throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException {
		
		if(node.getId() != XParserTreeConstants.JJTLETCLAUSE)
			throw new IllegalArgumentException(
					"This extraction rule can be applied only to JJTLETCLAUSE nodes, found " 
					+ XParserTreeConstants.jjtNodeName[node.getId()] + ".");
		// LETCLAUSE = VarName(QName) ":=" SIMPLEExpr ("," VarName(QName) ":=" SIMPLEExpr)*
		ExtractedPaths exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		ExtractedPaths exPathsQ0 = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
//		ExtractedPaths exPathsQ1 = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		
		commonFORLETClauses(node, env, exPathsQ0, returned, FLWORType.LET, 
				ctxPath.clone(), fixReturnedPaths, updateType);

		
//		SimpleNode parent = node.getParent();
		// return statement that corresponds to q1 is the last son of the parent
//		SimpleNode q1 = node.getParent().getChild(parent.jjtGetNumChildren()-1);
//		exPathsQ1 = extractPaths(q1, env, true, FLWORType.LET, ctxPath.clone()).toExtractPathsForQuery(ctxPath);
		
		// (FLWR-returned)
/*		exPaths.get(PathType.STRING_RETURNED).addAll(
				exPathsQ1.get(PathType.STRING_RETURNED));
		exPaths.get(PathType.NODE_RETURNED).addAll(
				exPathsQ1.get(PathType.NODE_RETURNED));*/
		
		// (FLWR-ebu)
		//TODO: discarding ebu of Q0!?
/*		exPaths.get(PathType.EVERYTHING_BELOW_USED).addAll(
				exPathsQ1.get(PathType.EVERYTHING_BELOW_USED));*/
		exPaths.get(PathType.EVERYTHING_BELOW_USED).addAll(
				exPathsQ0.get(PathType.EVERYTHING_BELOW_USED));
		
		
		// (let-used)
		exPaths.get(PathType.STRING_USED).addAll(
				exPathsQ0.get(PathType.STRING_USED));
		exPaths.get(PathType.NODE_USED).addAll(
				exPathsQ0.get(PathType.NODE_USED));
/*		exPaths.get(PathType.STRING_USED).addAll(
				exPathsQ1.get(PathType.STRING_USED));
		exPaths.get(PathType.NODE_USED).addAll(
				exPathsQ1.get(PathType.NODE_USED));*/
		
		// rules for fixedReturnedPath handling, not in the formal rules
		exPaths.get(PathType.NODE_RETURNED).addAll(
				exPathsQ0.get(PathType.NODE_RETURNED
						).returnOnlyFixedReturnPaths());
		exPaths.get(PathType.STRING_RETURNED).addAll(
				exPathsQ0.get(PathType.STRING_RETURNED
				).returnOnlyFixedReturnPaths());
		
		return exPaths;
	}
	
	/**
	 * Extract paths from for clause.
	 *
	 * @param node the root of the abstract syntax tree
	 * @param env the static environment
	 * @param returned say if we are in a return statement or not
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from this subtree
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private ExtractedPaths extractPathsFromForClause(
			SimpleNode node, 
			Environment env, boolean returned, 
			FLWORType flworType, Path ctxPath, 
			boolean fixReturnedPaths, 
			UpdateOperationType updateType) throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException {
		
		if(node.getId() != XParserTreeConstants.JJTFORCLAUSE)
			throw new IllegalArgumentException(
					"This extraction rule can be applied only to JJTFORCLAUSE nodes, found " 
					+ XParserTreeConstants.jjtNodeName[node.getId()] + ".");
		// FORCLAUSE = VarName(QName) SIMPLEExpr ("," VarName(QName) SIMPLEExpr)*
		ExtractedPaths exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		ExtractedPaths exPathsQ0 = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
//		ExtractedPaths exPathsQ1 = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		
		commonFORLETClauses(node, env, exPathsQ0, returned, FLWORType.FOR, 
				ctxPath.clone(), fixReturnedPaths, updateType);
		//SimpleNode parent = node.getParent();
		// return statement that corresponds to q1 is the last son of the parent
//		SimpleNode q1 = node.getParent().getChild(parent.jjtGetNumChildren()-1);
		
		// (FLWR-returned)
//		exPathsQ1 = extractPaths(q1, env, true, FLWORType.FOR, ctxPath.clone());
		
//		exPathsQ1 = exPathsQ1.toExtractPathsForQuery(ctxPath);
		
//		exPaths.get(PathType.STRING_RETURNED).addAll(exPathsQ1.get(PathType.STRING_RETURNED));
//		exPaths.get(PathType.NODE_RETURNED).addAll(exPathsQ1.get(PathType.NODE_RETURNED));
		
		// (FLWR-ebu)
		exPaths.get(PathType.EVERYTHING_BELOW_USED).addAll(
				exPathsQ0.get(PathType.EVERYTHING_BELOW_USED));
		/*		exPaths.get(PathType.EVERYTHING_BELOW_USED).addAll(
		exPathsQ1.get(PathType.EVERYTHING_BELOW_USED));*/
		
		// (FOR-su)
		exPaths.get(PathType.STRING_USED).addAll(
				exPathsQ0.get(PathType.STRING_USED));
		exPaths.get(PathType.STRING_USED).addAllFilteringReturnFixed(
				exPathsQ0.get(PathType.STRING_RETURNED));
		exPaths.get(PathType.STRING_RETURNED).addAll(
				exPathsQ0.get(PathType.STRING_RETURNED).returnOnlyFixedReturnPaths());
//		exPaths.get(PathType.STRING_USED).addAll(exPathsQ1.get(PathType.STRING_USED));
		
		// (FOR-nu)
		exPaths.get(PathType.NODE_USED).addAll(
				exPathsQ0.get(PathType.NODE_USED));
		exPaths.get(PathType.NODE_USED).addAllFilteringReturnFixed(
				exPathsQ0.get(PathType.NODE_RETURNED));
		exPaths.get(PathType.NODE_RETURNED).addAll(
				exPathsQ0.get(PathType.NODE_RETURNED).returnOnlyFixedReturnPaths());		
//		exPaths.get(PathType.NODE_USED).addAll(exPathsQ1.get(PathType.NODE_USED));
		
		return exPaths;
	}
	
	/*
	 * Commont part between FORCLAUSE and LETCLAUSE handling
	 */
	/**
	 * Common part for FOR and LET clauses.
	 *
	 * @param node the root of the abstract syntax tree
	 * @param env the static environment
	 * @param exPathsQ0 the extracted paths from the left hand side expression (Q0 in the rules)
	 * @param returned say if we are in a return statement or not
	 * @param flworType the type of flwor operation the actual node is nested into
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private void commonFORLETClauses(SimpleNode node, 
			Environment env, ExtractedPaths exPathsQ0, 
			boolean returned, FLWORType flworType, 
			Path ctxPath, boolean fixReturnedPaths, 
			UpdateOperationType updateType) throws ParserConfigurationException, SAXException, IOException{
		int j = 0;
		
		while(j < node.jjtGetNumChildren()){
			SimpleNode varNameNode = node.getChild(j);
			SimpleNode qNameNode = varNameNode.getChild(0);
			if(varNameNode.getId() != XParserTreeConstants.JJTVARNAME
					|| qNameNode.getId() != XParserTreeConstants.JJTQNAME)
				throw new IllegalArgumentException(
						"Expected JJTVARNAME node with JJTQNAME node as first son, found " 
						+ XParserTreeConstants.jjtNodeName[varNameNode.getId()] + " and "
						+ XParserTreeConstants.jjtNodeName[qNameNode.getId()] + ".");
			
			String varName = qNameNode.getValue();
			
			// binding the variable to the paths of q0
			ExtractedPaths actualExPaths = extractPaths(node.getChild(j+1),
					env, returned, flworType, ctxPath, fixReturnedPaths, updateType);
			exPathsQ0.addAll(actualExPaths);
			env.addBinding(varName, actualExPaths/*, node.getChild(j+1).getId() == XParserTreeConstants.JJTPATHEXPR*/
					, flworType, updateType);
			
			j+=2;
		}
	}
	
	/*
	 * Method handling rules "Primitive queries: x"
	 */
	/**
	 * Extract paths for variable.
	 *
	 * @param node the root of the abstract syntax tree
	 * @param env the static environment
	 * @param returned say if we are in a return statement or not
	 * @param ctxPath the context path
	 * @param fixReturnedPaths the fix returned paths
	 * @param updateType the update type
	 * @return the extracted paths from the current subtree
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private ExtractedPaths extractPathsFromVariable (SimpleNode node, 
			Environment env, boolean returned, 
			Path ctxPath, boolean fixReturnedPaths,
			UpdateOperationType updateType) 
	throws IllegalArgumentException {
		
		if(node.getId() != XParserTreeConstants.JJTVARNAME)
			throw new IllegalArgumentException(
					"Extraction rules for variables can be applied only to " +
					"JJTVARNAME nodes, found " 
					+ XParserTreeConstants.jjtNodeName[node.getId()] + ".");

		String varName = (node.getChild(0)).getValue();
		ExtractedPaths exPaths = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		
		if(env.isEmpty() || !env.isVariableBinded(varName))
			return exPaths;
		// (Var-used), (Var-nr), (Var-sr) rules
		else if(env.isVariableBinded(varName)){
			// (Var-nr), (Var-sr) rules
			for(Path p : env.getBindings(varName)){
				PathType pt = p.isText() ? PathType.STRING_RETURNED : PathType.NODE_RETURNED;
				/*	? (returned ? PathType.STRING_RETURNED : PathType.STRING_USED)
					: (returned ? PathType.NODE_RETURNED : PathType.NODE_USED);*/
				p.setOperationType(updateType);
				exPaths.addPath(pt, p);
			}
			//(Var-used) rule, nothing to do
		}
		
		return exPaths;
	}
}
