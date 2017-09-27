/*
 * The Class representing the information of an XML-Schema schema file
 */
package fr.upsud.lri.schema;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import fr.upsud.lri.schemaAsGraph.AttributeTypeNode;
import fr.upsud.lri.schemaAsGraph.ComplexTypeNode;
import fr.upsud.lri.schemaAsGraph.GraphNode;
import fr.upsud.lri.schemaAsGraph.GraphNodes;
import fr.upsud.lri.schemaAsGraph.SimpleTypeNode;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.parser.XSOMParser;
import com.thaiopensource.relaxng.translate.Driver;

/**
 * The Class XMLSchema.
 */
public class XMLSchema extends Schema {

	/** The url pointing to the file in which the schema is stored. */
	private URL url = null;
	
	/** The uri pointing to the file in which the schema is stored. */
	java.net.URI uri = null;

	/** The schema set associated to the schema. */
	private XSSchemaSet schemaSet = null;
	
	/** The xs schema associated to the schema. */
	private XSSchema xsSchema = null;
	
	/** The parser to parse the schema. */
	private XSOMParser parser = null;

	/**
	 * Instantiates a new XML schema.
	 *
	 * @param filename the filename of the file in which the schema is stored
	 * @throws URISyntaxException the URI syntax exception if the URI construction fails
	 */
	public XMLSchema(String filename) throws URISyntaxException {
		super(filename);
		
		//XSDBuilder builder = null;

		//XSSchemaSet schemaSet = null; /* parse XML Schema */;
		
		if(type == SchemaType.DTD){
			String [] args = {filename, filename.replaceFirst(".dtd", ".xsd")};
			filename = filename.replaceFirst(".dtd", ".xsd");
			new Driver().run(args);
		}
		else if(type != SchemaType.XSD)
			throw new IllegalArgumentException(
					"RelaxNG not supported yet.");
		
		url = createURL(filename);
		uri = url.toURI();
		
		computeRelationships();

		/* we didn't have this information before, we put them now
		 * that we have father/son relationships
		 */
		graph.setRoots(this.getRoot());
		graph.setLeaves(this.getLeaves());
		graph.setGraphNodes(this.getGraphNodes());
		
//		System.out.println(this);
//		for(GraphNode node : graphNodes)
//			System.out.println(node + "\n");
	}
	
	/**
	 * Analyze the schema elements for building the relationships.
	 *
	 * @param complexType the complex type to analyze
	 * @param father the father label of the complex type
	 */
	private void analyzeSchemaElements(XSComplexType complexType, String father){
/* FOR DEBUG!
 		System.out.println("\nComplexType \"" + (complexType.getName() != null 
	    		?  complexType.getName().toUpperCase() 
	    				: "") + "\":");
*/
		XSComplexType xsComplexType = complexType;
		for (XSAttributeUse attributeUse : complexType.getAttributeUses()) {
			XSAttributeDecl attributeDecl = attributeUse.getDecl();
			String attributeName = attributeDecl.getName();
			String attributeType = attributeDecl.getType().getName() != null 
			? attributeDecl.getType().getName()
					: attributeDecl.getType().getPrimitiveType().getName();
			
			GraphNode fatherGraphNode = new ComplexTypeNode(father, false);
			GraphNode sonGraphNode = new AttributeTypeNode(attributeName, attributeType);

			fatherGraphNode = getFatherGraphNode(fatherGraphNode, father, complexType.isMixed());
            
            sonGraphNode = getSonGraphNode(sonGraphNode, false, false, attributeName, attributeType, true);
            
            // we add the computed information
            sons.get(fatherGraphNode).add(sonGraphNode);
            fatherGraphNode.getSons().add(sonGraphNode);
            fathers.get(sonGraphNode).add(fatherGraphNode);
            sonGraphNode.getFathers().add(fatherGraphNode);
			
		}
		
	    XSContentType xsContentType = xsComplexType.getContentType();
	    XSParticle particle = xsContentType.asParticle();
	    if(particle != null){
	        XSTerm term = particle.getTerm();
	        if(term.isModelGroup()){
	            XSModelGroup xsModelGroup = term.asModelGroup();
	            XSParticle[] particles = xsModelGroup.getChildren();
	            for(XSParticle p : particles ){
	                XSTerm pterm = p.getTerm();
	                if(pterm.isElementDecl()){ //xs:element inside complex type
//DEBUG	                    System.out.println(pterm + " type: " 
//	                    		+ pterm.asElementDecl().getType());
	                    String sonName = pterm.asElementDecl().getName();

	                    // computing the variables for the son object
	                    boolean complex = pterm.asElementDecl().getType().isComplexType();
	                    boolean mixedContent =
	                    	complex ? 
	                    			pterm.asElementDecl().getType().asComplexType().isMixed()
	                    				: false;
	                    
	                    GraphNode fatherGraphNode = new ComplexTypeNode(father, false);
	                    GraphNode sonGraphNode = complex ? 
	                    		new ComplexTypeNode(sonName, mixedContent) : 
	                    			new SimpleTypeNode(sonName, 
	                    					pterm.asElementDecl().getType().getName());
	                    
	                    fatherGraphNode = getFatherGraphNode(fatherGraphNode, father, complexType.isMixed());
	                    
	                    sonGraphNode = getSonGraphNode(sonGraphNode, complex, mixedContent, 
	                    		sonName, pterm.asElementDecl().getType().getName(), false);
	                    
	                    // we add the computed information
	                    sons.get(fatherGraphNode).add(sonGraphNode);
	                    fatherGraphNode.getSons().add(sonGraphNode);
	                    fathers.get(sonGraphNode).add(fatherGraphNode);
	                    sonGraphNode.getFathers().add(fatherGraphNode);
	                    
	                    // if the son is complex too we analyze it
	                    if(pterm.asElementDecl().getType().isComplexType()){
	                    	if(!getGraphNodes().contains(new ComplexTypeNode(pterm.asElementDecl().getName(), false)))
	                    		analyzeSchemaElements(pterm.asElementDecl().getType().asComplexType()
	                    			, pterm.asElementDecl().getName());
	                    }
	                }
	                // for debugging purpouse
	                else {
	                	System.out.println("PTERM: " + pterm);
	                }
	            }
	        }
	        else {
	        	System.out.println("Something wrong while evaluating the relationship for the schema.");
	        }
	    }
	}
	
	
	/**
	 * Gets the father graph node.
	 *
	 * @param fatherGraph the father graph
	 * @param father the father but with incomplete informations (only the label)
	 * @param mixed true if it is mixed content
	 * @return the father graph node representing the father
	 */
	private GraphNode getFatherGraphNode(GraphNode fatherGraph, String father, boolean mixed){
		
		GraphNode fatherGraphNode = null;
		
		// if the father node is unknown we initialize it, otherwise we retrieve it
        if(!getGraphNodes().contains(fatherGraph)){
        	// father is necessarily complex (otherwise no sons!)
        	fatherGraphNode = new ComplexTypeNode(father, mixed);
        	
        	initializeGraphNodeInfo(fatherGraphNode);
        }
        else
        	fatherGraphNode = 
        		getGraphNodes().get(getGraphNodes().indexOf(fatherGraph));

        return fatherGraphNode;
	}
	
	
	/**
	 * Gets the son graph node.
	 *
	 * @param sonGraph the son graph but with incomplete informations (only the label)
	 * @param complex true if the node is complex
	 * @param mixedContent true if it is mixed content
	 * @param sonName the son name
	 * @param typeName the type name
	 * @param attribute true if it is an attribute
	 * @return the son graph node representing the son
	 */
	private GraphNode getSonGraphNode(GraphNode sonGraph, 
									  boolean complex, 
									  boolean mixedContent, 
									  String sonName,
									  String typeName, boolean attribute){
		GraphNode sonGraphNode = null;
		
		// if the son node is unknown we initialize it, otherwise we retrieve it
        if(!getGraphNodes().contains(sonGraph)){
        	sonGraphNode = complex ? 
        			new ComplexTypeNode(sonName, mixedContent) :
        				( attribute 
        						? new AttributeTypeNode(sonName, typeName)
        				 			: new SimpleTypeNode(sonName, typeName)
        				);
        			
        	initializeGraphNodeInfo(sonGraphNode);
        }
        else
        	sonGraphNode = 
        		getGraphNodes().get(getGraphNodes().indexOf(sonGraph));

        return sonGraphNode;
	}
	
	
	/**
	 * Initialize graph node info.
	 *
	 * @param graphNode the graph node
	 */
	private void initializeGraphNodeInfo(GraphNode graphNode){
		
        	fathers.put(graphNode, 
        			new GraphNodes());
			sons.put(graphNode, 
					new GraphNodes());
			ancestors.put(graphNode, 
					new GraphNodes());
			descendants.put(graphNode, 
					new GraphNodes());
			
            if(!getGraphNodes().contains(graphNode))
            	getGraphNodes().add(graphNode);
	}
	
	/**
	 * Compute son and father relations.
	 *
	 * @param elDeclarations the element declarations of the schema
	 */
	private void computeSonAndFatherRelations(Map<String, XSElementDecl> elDeclarations){
		for(String key : elDeclarations.keySet()){
			XSElementDecl elDecl = elDeclarations.get(key);
			if(/*elDecl.getType().isAnonymous() &&*/ elDecl.getType().isComplexType())
				analyzeSchemaElements(elDecl.getType().asComplexType(), key);
		}
	}
	
	/**
	 * XSOM library handling.
	 */
	private void XSOMLibraryHandling(){
		XSOMparseInit(new File(uri));

		Map<String, XSElementDecl> elDeclarations = xsSchema.getElementDecls();
		
		computeSonAndFatherRelations(elDeclarations);
		
		// computing root elements (without any father)
		for(GraphNode label : getGraphNodes())
			if(fathers.get(label).isEmpty())
				this.getGraph().getRoots().add(label);
		
		// computing "leaves" (no sons)
		for(GraphNode elem : getGraphNodes())
			if(sons.get(elem).isEmpty() 
					|| fathers.get(elem).containsAll(sons.get(elem)))
				this.getGraph().getLeaves().add(elem);
		
		computeAncestorRelations();
		computeDescendantRelations();
	}
	
	/**
	 * Compute descendant relations.
	 */
	private void computeDescendantRelations(){
		
		GraphNodes analyzedNodes = new GraphNodes();
		
		for(GraphNode root : this.getGraph().getRoots()){
			GraphNodes rootDesc = new GraphNodes();
			for(GraphNode son : sons.get(root)){
				rootDesc.add(son);
				if(analyzedNodes.contains(son)){
					rootDesc.addAll(son.getDescendants());
				}
				else {
					analyzedNodes.add(son);					
					rootDesc.addAll(descendRecursive(son, analyzedNodes));
				}
			}
			descendants.put(root, rootDesc);
			root.descendantsAddAllNoDuplicates(rootDesc);
		}
	}
	
	/**
	 * Recursive method for computing descendant.
	 *
	 * @param elem the element under analysis
	 * @param analyzedNodes the already analyzed nodes
	 * @return the graph nodes descendants of the analyzed one
	 */
	private GraphNodes descendRecursive(GraphNode elem, GraphNodes analyzedNodes){
		
		GraphNodes elemDesc = new GraphNodes();
		for(GraphNode son : sons.get(elem)){
			if(!analyzedNodes.contains(son)){
				analyzedNodes.add(son);
				elemDesc.addAll(descendRecursive(son, analyzedNodes));			
			}
			else {
				elemDesc.addAll(son.getDescendants());
			}
			elemDesc.add(son);
		}
		descendants.put(elem, elemDesc);
		elem.descendantsAddAllNoDuplicates(elemDesc);
		
		return elemDesc;
	}
	
	/**
	 * Recursive method for computing ancestor.
	 *
	 * @param elem element under analysis 
	 * @param analyzedNodes the already analyzed nodes
	 * @return the graph nodes ancestors of the analyzed one
	 */
	private GraphNodes ancestorRecursive(GraphNode elem, GraphNodes analyzedNodes){
		
		GraphNodes elemAnc = new GraphNodes();
		
		for(GraphNode father : fathers.get(elem)){
			if(!analyzedNodes.contains(father)){
				analyzedNodes.add(father);
				elemAnc.addAll(ancestorRecursive(father, analyzedNodes));			
			}
			else {
				elemAnc.addAll(father.getAncestors());
			}
			elemAnc.add(father);
		}
		
		ancestors.put(elem, elemAnc);
		elem.ancestorsAddAllNoDuplicates(elemAnc);
		
		return elemAnc;
	}

	/**
	 * Compute ancestor relations.
	 */
	private void computeAncestorRelations(){
		
		GraphNodes analyzedNodes = new GraphNodes();
		
		for(GraphNode leaf : this.getGraph().getLeaves()){
			GraphNodes leafAnc = new GraphNodes();
			for(GraphNode father : fathers.get(leaf)){
				leafAnc.add(father);
				if(analyzedNodes.contains(father)){
					leafAnc.addAll(father.getAncestors());
				}
				else {
					analyzedNodes.add(father);					
					leafAnc.addAll(ancestorRecursive(father, analyzedNodes));
				}
			}
			ancestors.put(leaf, leafAnc);
			leaf.ancestorsAddAllNoDuplicates(leafAnc);
		}
	}
	
	/**
	 * XSOM parse init.
	 *
	 * @param file the file
	 */
	private void XSOMparseInit(File file){
	    try {
	        parser = new XSOMParser();
	        parser.parse(file);
	        this.schemaSet = parser.getResult();
	        this.xsSchema = this.schemaSet.getSchema(1);
	    }
	    catch (Exception exp) {
	        exp.printStackTrace(System.out);
	    }
	}
	
	/* (non-Javadoc)
	 * @see fr.upsud.lri.schema.Schema#computeRelationships()
	 */
	@Override
	public void computeRelationships() {
//		XSDLibraryHandling();
		XSOMLibraryHandling();
	}

/* OLD LIBRARY
	private void XSDLibraryHandling(){
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		java.util.Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xsd", new XSDResourceFactoryImpl());
		
		// String variable schemaURL is "FindTypesMissingFacets.xsd" or the URL to your schema
		// Create a resource set and load the main schema file into it.
		ResourceSet resourceSet = new ResourceSetImpl();
		XSDResourceImpl xsdSchemaResource = (XSDResourceImpl)resourceSet.getResource(
				 URI.createURI(filename), true);

		// getResources() returns an iterator over all the resources, therefore, the main resource
		// and those that have been included, imported, or redefined.
		for (Iterator<Resource> resources = resourceSet.getResources().iterator(); 
		    resources.hasNext(); )
		{
		    // Return the first schema object found, which is the main schema 
		    //   loaded from the provided schemaURL
		    Resource resource = (Resource)resources.next();
		    if (resource instanceof XSDResourceImpl)
		    {
		        XSDResourceImpl xsdResource = (XSDResourceImpl)resource;
		        // This returns a org.eclipse.xsd.XSDSchema object
		        schemadoc = xsdResource.getSchema();
		    }
		}
		
		cmpList = schemadoc.getElementDeclarations();
		
		for(XSDTypeDefinition elDecl : schemadoc.getTypeDefinitions()){
			System.out.println("");
			System.out.println("1 " + elDecl);
			System.out.println("2 " + elDecl.getSimpleType());
			XSDParticle part = elDecl.getComplexType();
			if(part == null)
				continue;
			XSDParticleImpl content = (XSDParticleImpl) part;
			System.out.println("C " + content);
			System.out.println("CONT " + content.getContent());
		}
		
		
		
		//printStructure(schemadoc);

		computeRelationships();

		//System.out.println(this.toString());
	}
*/

/*	OLD LIBRARY
  	private void printStructure(XSDSchema schema){
		printTreeIterator(schema.eAllContents());
	}*/
	
/* OLD LIBRARY	
	private void printTreeIterator(TreeIterator<EObject> treeIterator){
		while(treeIterator.hasNext()){
			EObject obj = treeIterator.next();
			//System.out.println(obj);
			if(obj instanceof XSDComponent)
				analyzeContentElement((XSDComponent) obj);
			else{
				//System.out.println("Iterator object type error:\n" + obj);
			}
		}
	}
*/

/* OLD LIBRARY
	private void analyzeContentElement(XSDComponent component){
		if(component instanceof XSDTerm){
			if(component instanceof XSDWildcard){
				System.out.println("XSDWildcard = " + component);
			}
			else if(component instanceof XSDModelGroup){
				System.out.println("XSDModelGroup = " + component);
			}
			else if(component instanceof XSDElementDeclaration){
				System.out.println("XSDElementDeclaration = " + component);

				if(((XSDElementDeclaration) component).getAnonymousTypeDefinition() != null){
					System.out.println("ANONTYPE");
				}
				else if(((XSDElementDeclaration) component).getTypeDefinition() != null){
					System.out.println("TYPE");
				}
			}
		}
		else if(component instanceof XSDScope){
			if(component instanceof XSDSchema){
				System.out.println("XSDSchema = " + component);
			}
			else if(component instanceof XSDComplexTypeDefinition){
				System.out.println("XSDComplexTypeDefinition = " + component);
			}
		}
		else if(component instanceof XSDAnnotation){
			//System.out.println("XSDAnnotation = " + component);
		}
		else if(component instanceof XSDAttributeUse){
			//System.out.println("XSDAttributeUse = " + component);
		}
		else if(component instanceof XSDXPathDefinition){
			System.out.println("XSDXPathDefinition = " + component);
		}
		else if(component instanceof XSDNamedComponent){
			if(component instanceof XSDFeature){
				if (component instanceof XSDAttributeDeclaration){
					//System.out.println("XSDAttributeDeclaration = " + component);
				}
				else if(component instanceof XSDElementDeclaration){
					System.out.println("XSDElementDeclaration = " + component);
				}
			}
			else if(component instanceof XSDRedefinableComponent){
				if(component instanceof XSDTypeDefinition){
					if(component instanceof XSDSimpleTypeDefinition){
						//System.out.println("XSDSimpleTypeDefinition = " + component);
					}
					else if(component instanceof XSDComplexTypeDefinition){
						System.out.println("XSDComplexTypeDefinition = " + component);
					}
				}
			}
			else if(component instanceof XSDNotationDeclaration){
				System.out.println("XSDNotationDeclaration = " + component);
			}
			else if(component instanceof XSDIdentityConstraintDefinition){
				System.out.println("XSDIdentityConstraintDefinition = " + component);
			}
		}
		else if(component instanceof XSDComplexTypeContent){
			if(component instanceof XSDParticle){
				System.out.println("XSDParticle = " + component);
			}
			else if(component instanceof XSDSimpleTypeDefinition){
				//System.out.println("XSDSimpleTypeDefinition = " + component);
			}
		}
		else{
			//System.out.println("Unknown content class:\n" + component);
		}
	}
*/

/* OLD LIBRARY
   public void computeRelationships() {
		//schemadoc.isNodeType(XSDTypeConstants.FLOAT);
		XSDElement actualNode = null;//schemadoc;
		XMLSchemaNode schemaNode = schemadoc.getSchemaByTargetNS(
				schemadoc.getSchemaTargetNS());
		XSDNode[] elementSet = schemaNode.getElementSet();
		root = schemadoc;
		
		for(int c = 0; c < elementSet.length; c++){
			HashSet<XSDNode> actualNodeFatherSet = new HashSet<XSDNode>(1);
			fathers.put(elementSet[c], actualNodeFatherSet);
		}
		
		for(int c = 0; c < elementSet.length; c++){
			actualNode = (XSDElement) elementSet[c];
			
			XSDNode [] childElemList = 
				actualNode.getChildElements() == null ? new XSDNode[]{}
						: actualNode.getChildElements();
			HashSet<XSDNode> actualNodeSonsSet = 
				new HashSet<XSDNode>(childElemList.length);

			for(XSDNode node : childElemList){
				fathers.get(node).add(actualNode);
				actualNodeSonsSet.add(node);
			}
			
			sons.put(actualNode, actualNodeSonsSet);
			
		}*/
		/*
		for(int c = 0; c < elementSet.length; c++){
			
			XSDNode actualEl = elementSet[c];
			
			LinkedList<LinkedList<XSDNode>> actualNodeAncestorChains = 
				new LinkedList<LinkedList<XSDNode>>();
			
			LinkedList<XSDNode> actualChain = new LinkedList<XSDNode>();
			actualNodeAncestorChains.add(actualChain);
			
			XSDNode parent = fathers.get(actualEl).;
			
			while(true){
				actualChain.addLast(e);
			}
			
			ancestorChains.put(actualNode, actualNodeAncestorChains);
		}*/
//	}
}
