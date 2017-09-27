/*
 * FunctionInfo class represents a lookup table for retrieving
 * function information starting from the name
 */
package fr.upsud.lri.pathExtractor;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.upsud.lri.pathExtractor.FunctionParameter.CardinalityModifier;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.*;

/**
 * The Class FunctionInfo.
 */
public class FunctionInfo extends ConcurrentHashMap<String, Function> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	private static final String functionConfigurationFile = "resources/configs/functions.xml";

	private static final String functionConfigurationSchema = "resources/configs/functions.xsd";
	
	/**
	 * Instantiates a new function info.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public FunctionInfo() 
	throws ParserConfigurationException, SAXException, IOException {
		super();
		Init();
	}

	/**
	 * Instantiates a new function info.
	 *
	 * @param initialCapacity the initial capacity
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public FunctionInfo(int initialCapacity) 
	throws ParserConfigurationException, SAXException, IOException {
		super(initialCapacity);
		Init();
	}

	/**
	 * Instantiates a new function info.
	 *
	 * @param map the map used to fill the new object
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public FunctionInfo(Map<? extends String, ? extends Function> map) 
	throws ParserConfigurationException, SAXException, IOException {
		super(map);
		Init();
	}

	/**
	 * Instantiates a new function info.
	 *
	 * @param initialCapacity the initial capacity
	 * @param loadFactor the load factor
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public FunctionInfo(int initialCapacity, float loadFactor) throws ParserConfigurationException, SAXException, IOException {
		super(initialCapacity, loadFactor);
		Init();
	}

	/**
	 * Instantiates a new function info.
	 *
	 * @param initialCapacity the initial capacity
	 * @param loadFactor the load factor
	 * @param concurrencyLevel the concurrency level
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public FunctionInfo(int initialCapacity, float loadFactor, int concurrencyLevel) 
	throws ParserConfigurationException, SAXException, IOException {
		super(initialCapacity, loadFactor, concurrencyLevel);
		Init();
	}
	
	/**
	 * Initialize the object.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private void Init() 
	throws ParserConfigurationException, SAXException, IOException{
		/*		StepItem selfStep = new StepItem("self", "node");
		StepItem descselfStep = new StepItem("descendant-or-self", "node");
		
		FunctionParameter funcParam = new FunctionParameter(1, false, CardinalityModifier.NONE);
		LinkedList<FunctionParameter> funcParamList = new LinkedList<FunctionParameter>();
		funcParamList.add(funcParam);		
		Function func = new Function("doc", 1, funcParamList, selfStep);
		put(func.getFunctionName(), func);
		
		funcParam = new FunctionParameter(1, false, CardinalityModifier.NONE);
		funcParamList = new LinkedList<FunctionParameter>();		
		funcParamList.add(funcParam);
		func = new Function("count", 1, funcParamList, selfStep);
		put(func.getFunctionName(), func);
		
		funcParam = new FunctionParameter(1, false, CardinalityModifier.NONE);
		funcParamList = new LinkedList<FunctionParameter>();		
		funcParamList.add(funcParam);
		func = new Function("position", 1, funcParamList, null);
		put(func.getFunctionName(), func);

		
		funcParam = new FunctionParameter(1, false, CardinalityModifier.NONE);
		funcParamList = new LinkedList<FunctionParameter>();		
		funcParamList.add(funcParam);
		func = new Function("string", 1, funcParamList, descselfStep);
		put(func.getFunctionName(), func);

		funcParam = new FunctionParameter(1, false, CardinalityModifier.NONE);
		funcParamList = new LinkedList<FunctionParameter>();		
		funcParamList.add(funcParam);
		func = new Function("number", 1, funcParamList, descselfStep);
		put(func.getFunctionName(), func);

		funcParam = new FunctionParameter(1, true, CardinalityModifier.NONE);
		funcParamList = new LinkedList<FunctionParameter>();		
		funcParamList.add(funcParam);
		func = new Function("dummy", 1, funcParamList, null);
		put(func.getFunctionName(), func);
*/

		// this method loads function info from an xml file
		loadInfoFromConfigurationFile();
	}
	
	/**
	 * Load info from configuration file.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private void loadInfoFromConfigurationFile() 
	throws ParserConfigurationException, SAXException, IOException{
		
		Document xmlDOM = getDocument(FunctionInfo.functionConfigurationFile);
		NodeList functions = xmlDOM.getElementsByTagName("function");
		xmlDOM.getDocumentElement().normalize();
		
		FunctionParameter funcParam = null;
		LinkedList<FunctionParameter> funcParamList = 
			new LinkedList<FunctionParameter>();
		Function func = null;
		Node functionNode = null;
		NodeList parametersNodes = null;
		NamedNodeMap functionAttributes = null;
		String functionName = null;
		int functionNumParams = 0;
		CardinalityModifier paramModifier = null;
		String paramModifierString = null;
		Node parameterNode = null;
		
		for(int c = 0; c < functions.getLength(); c++){
			functionNode = functions.item(c);
			
			if(functionNode.getNodeType() == Node.ELEMENT_NODE){
			
				functionAttributes = functionNode.getAttributes();			
				functionName = functionAttributes.getNamedItem("name").getNodeValue();
				functionNumParams = Integer.valueOf(
						functionAttributes.getNamedItem("num_param").getNodeValue()
						).intValue();
	
				funcParamList.clear();
	
				parametersNodes = functionNode.getChildNodes();
				for(int d = 0; d < parametersNodes.getLength(); d++){
					
					parameterNode = parametersNodes.item(d);
					
					if(parameterNode.getNodeType() == Node.ELEMENT_NODE){
						paramModifierString = parameterNode.getTextContent().trim();
						if(paramModifierString.equalsIgnoreCase("NONE"))
							paramModifier = CardinalityModifier.NONE;
						else if(paramModifierString.equalsIgnoreCase("STAR"))
							paramModifier = CardinalityModifier.STAR;
						else if(paramModifierString.equalsIgnoreCase("OPTIONAL"))
							paramModifier = CardinalityModifier.OPTIONAL;
						else
							throw new IllegalArgumentException("Invalid cardinality " +
									"modifier for function attribute: " 
									+ paramModifierString);
		
						funcParam = new FunctionParameter(d, 
								Boolean.getBoolean(
										parameterNode.getAttributes().
										getNamedItem("fixed_return").
										getNodeValue()), 
								paramModifier);
						
						funcParamList.add(funcParam);
					}
				}
			}

			func = new Function(functionName, functionNumParams, funcParamList, null);
			put(functionName, func);
		}
		
		System.out.println(this);
	}
	
	/**
	 * Method returning DOM from an XML file encoding functions
	 * informations after its validation against functions.xsd schema
	 * @param file storing functions information 
	 * @return the DOM document representing the XML file
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws Exception
	 */
	public static Document getDocument(String file) 
	throws ParserConfigurationException, SAXException, IOException {
		
	    // Step 1: create a DocumentBuilderFactory
	     DocumentBuilderFactory dbf = 
	    	 DocumentBuilderFactory.newInstance();

	    // Step 2: create a DocumentBuilder
	     DocumentBuilder db = 
	    	 dbf.newDocumentBuilder();

	    // Step 3: parse the input file to get a Document object
	    Document document = db.parse(new File(file));
	    
		// Validation of the XML file against the schema
	    
	    // create a SchemaFactory capable of understanding WXS schemas
	    SchemaFactory factory = SchemaFactory.newInstance(
	    		XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    
	    // load a WXS schema, represented by a Schema instance
	    Source schemaFile = new StreamSource(new File(
	    		FunctionInfo.functionConfigurationSchema));
	    Schema schema = factory.newSchema(schemaFile);
	    
	    // create a Validator instance, which can be used to validate an instance document
	    Validator validator = schema.newValidator();
	    
	    // validate the DOM tree
	    validator.validate(new DOMSource(document));
	    
	    // if the document is invalid the method above will throw an exception!
	    return document;
	}      
}