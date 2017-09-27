package fr.upsud.lri.projectorInference;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.xml.parsers.ParserConfigurationException;
import fr.upsud.lri.pathExtractor.Environment;
import fr.upsud.lri.pathExtractor.ExtractedPaths;
import fr.upsud.lri.pathExtractor.Path;
import fr.upsud.lri.pathExtractor.PathExtractor;
import fr.upsud.lri.pathExtractor.QueryPathExtractor;
import fr.upsud.lri.pathExtractor.UpdateOperationType;
import fr.upsud.lri.schema.XMLSchema;
import fr.upsud.lri.xqparser.ParseException;
import fr.upsud.lri.xqparser.SimpleNode;
import fr.upsud.lri.xqparser.XParser;
import org.xml.sax.SAXException;

/**
 * The Class MainPathExtractor.
 */
public class MainProjectorInference extends JFrame implements WindowListener, ActionListener {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** File chooser for loading schemas */
	private JFileChooser schemaFileChooser = 
		new JFileChooser(System.getProperty("user.dir"));

	/** File chooser for loading/saving queries in text files */
	private JFileChooser queryFileChooser = 
		new JFileChooser(System.getProperty("user.dir"));
	
	/** Handler for undo/redo on query text area. */
	final UndoManager undo = new UndoManager();

	/** Document element for undo/redo. */
	private Document doc = null;
	
	/** Object encoding the relations of the analyzed schema. */
	private XMLSchema xmlSchemaRelations = null;
	
	/** The file associated with the choosen schema. */
	private File schemaFileSelected = null;

	/** The query list. */
	private List<String> queryList = new LinkedList<String>();
	
	/** The selected query index. */
	private int selectedQueryIndex = 0;
	
	/** The graph representation of the schema. *//*
	private mxGraph graph = null;
	*/
	/** The button that starts the analysis of the inserted query. */
	private JButton jButtonOpen = new JButton("Open Schema");
	
	/** The j button analyze. */
	private JButton jButtonAnalyze = new JButton("Analyze");	
	
	/** The j button add query. */
	private JButton jButtonAddQuery = new JButton("Add query");

	/** The j button add query. */
	private JButton jButtonRemoveQuery = new JButton("Remove query");
	
	/** The j button add query. */
	private JButton jButtonLoadQueries = new JButton("Load queries");

	/** The j button add query. */
	private JButton jButtonSaveQueries = new JButton("Save queries");
	
	/** Label showing the name of the schema file selected. */
	private JLabel jLabelSchemaFileSelected = new JLabel("Choosen schema: ");

	/** The text area for inserting the query. */
	private JTextArea textAreaQuery = new JTextArea("", 60, 30);
	/** The text area for the result. */
	private JTextArea textAreaResult = new JTextArea("", 60, 30);
	/** The text area for the extracted paths. */
	private JTextArea textAreaExPaths = new JTextArea("", 60, 30);
	
	/** The split pane for input and output. */
	private JSplitPane splitPaneForTextAreas = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	
	/** The split pane for the two text areas for the result. */
	private JSplitPane splitPaneForResults = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	
/*	*//** The split pane for the central part and the list on the side. *//*
	private JSplitPane splitPaneForMainPart = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);*/

	/** The list of the query to analyze. */
	private JList queryJList = getJList();
	
	/** Query's list scrollpane. */
	private JScrollPane listScrollPane = new JScrollPane(queryJList);

	/** Query's text area scrollpane. */
	private JScrollPane queryAreaScrollPane = new JScrollPane(textAreaQuery);
	
	/** Result's text area scrollpane. */
	private JScrollPane resultAreaScrollPane = new JScrollPane(textAreaResult);
	
	/** Extracted paths's text area scrollpane. */
	private JScrollPane resultExPathsAreaScrollPane = new JScrollPane(textAreaExPaths);
		
	/** The border layout1. */
	private BorderLayout borderLayout1 = new BorderLayout();

	/** Top toolbar with the main buttons. */
	private JToolBar topToolbar = new JToolBar(JToolBar.HORIZONTAL);
	
	/** Checkbox for swtiching between verbose output or summarized one. */
	private JCheckBox checkBoxDetailedOutput = new JCheckBox("Detailed output");
	
	/** Grey color for non-editable text areas (used for output) */
	private Color greyColor = new Color(220, 220, 220);
	
	/** Construct the gui. */
	public MainProjectorInference() {
		super("Projector Inference");
	}

	/** The main method.
	 * @param args the arguments of the main method
	 */
	public static void main(String[] args) {
		MainProjectorInference main = new MainProjectorInference();
		main.init();
	}

	/**
	 * Initialize the gui.
	 */
	public void init() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the components handling Undo/Redo elements.
	 */
	private void UndoRedoInit(){
		// Listen for undo and redo events
		doc.addUndoableEditListener(new UndoableEditListener() {
		    public void undoableEditHappened(UndoableEditEvent evt) {
		        undo.addEdit(evt.getEdit());
		    }
		});

		// Create an undo action and add it to the text component
		textAreaQuery.getActionMap().put("Undo",
		    new AbstractAction("Undo") {
		        /**
				 * 
				 */
				private static final long serialVersionUID = -6245413583602475079L;

				public void actionPerformed(ActionEvent evt) {
		            try {
		                if (undo.canUndo()) {
		                    undo.undo();
		                }
		            } catch (CannotUndoException e) {
		            }
		        }
		   });

		// Bind the undo action to ctrl-Z, platform independent
		textAreaQuery.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo" );
		
		// Create a redo action and add it to the text component
		textAreaQuery.getActionMap().put("Redo",
		    new AbstractAction("Redo") {
		        /**
				 * 
				 */
				private static final long serialVersionUID = 1628723302535804706L;

				public void actionPerformed(ActionEvent evt) {
		            try {
		                if (undo.canRedo()) {
		                    undo.redo();
		                }
		            } catch (CannotRedoException e) {
		            }
		        }
		    });

		// Bind the redo action to ctrl-Y, platform independent
		textAreaQuery.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo" );
	}
	
	private void buttonsInit(){
		jButtonAnalyze.setFont(new Font("Arial", Font.PLAIN, 17));
		jButtonOpen.setFont(new Font("Arial", Font.PLAIN, 17));
		jButtonAddQuery.setFont(new Font("Arial", Font.PLAIN, 17));
		jButtonRemoveQuery.setFont(new Font("Arial", Font.PLAIN, 17));
		jLabelSchemaFileSelected.setFont(new Font("Arial", Font.PLAIN, 17));
		jButtonLoadQueries.setFont(new Font("Arial", Font.PLAIN, 17));
		jButtonSaveQueries.setFont(new Font("Arial", Font.PLAIN, 17));	
		
		jButtonOpen.addActionListener(this);
		jButtonAnalyze.addActionListener(this);
		jButtonAddQuery.addActionListener(this);
		jButtonRemoveQuery.addActionListener(this);
		jButtonLoadQueries.addActionListener(this);
		jButtonSaveQueries.addActionListener(this);
		
		JRootPane rootPane = this.getRootPane();
	    rootPane.setDefaultButton(jButtonAnalyze);
	}
	
	private void scrollbarsInit(){
		// set the scrollbar orientation
	    queryAreaScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    resultAreaScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    resultExPathsAreaScrollPane.setVerticalScrollBarPolicy(
	    		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    listScrollPane.setVerticalScrollBarPolicy(
	    		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	}
	
	private void textAreasInit(){
		// sets the parameters for the query's text area
		textAreaQuery.setRows(6);
		textAreaQuery.setText("");
		textAreaQuery.setFont(new Font("Serif", Font.PLAIN, 18));
		textAreaQuery.setLineWrap(true);
		textAreaQuery.setWrapStyleWord(true);
		
		// binds the textarea to the document object
		doc = textAreaQuery.getDocument();
		// initialize undo/redo action for query text area
		UndoRedoInit();

	    // sets the parameters for the result's text area		
		textAreaResult.setFont(new Font("Serif", Font.PLAIN, 18));
		textAreaResult.setLineWrap(true);
		textAreaResult.setWrapStyleWord(true);
		textAreaResult.setText("Type projector inferred will appear here");
		textAreaResult.setRows(5);
		textAreaResult.setEditable(false);
		// Setting grey background for textarea
		textAreaResult.setBackground(greyColor);

	    // sets the parameters for the extracted paths' text area		
		textAreaExPaths.setFont(new Font("Serif", Font.PLAIN, 18));
		textAreaExPaths.setLineWrap(true);
		textAreaExPaths.setWrapStyleWord(true);
		textAreaExPaths.setText("Extracted Paths will appear here");
		textAreaExPaths.setRows(5);
		textAreaExPaths.setEditable(false);
		// Setting grey background for textarea
		textAreaExPaths.setBackground(greyColor);
	}
	
	private void splitPanesInit(){
		splitPaneForResults.add(resultAreaScrollPane);
		splitPaneForResults.add(resultExPathsAreaScrollPane);
		splitPaneForResults.setDividerSize(2);
		
		splitPaneForTextAreas.add(queryAreaScrollPane);
		splitPaneForTextAreas.add(splitPaneForResults);
		splitPaneForTextAreas.setDividerSize(6);
		
/*		splitPaneForMainPart.add(splitPaneForTextAreas);
		splitPaneForMainPart.add(listScrollPane);
		splitPaneForTextAreas.setDividerSize(6);*/
	}
	
	private void checkBoxesInit(){
		// checkbox init
		checkBoxDetailedOutput.setFont(new Font("Serif", Font.PLAIN, 18));
	}
	
	private void toolbarInit(){
		// toolbar's element insertion
		topToolbar.add(jButtonOpen);
		topToolbar.add(jButtonAnalyze);
		topToolbar.add(jButtonAddQuery);
		topToolbar.add(jButtonRemoveQuery);
		topToolbar.add(checkBoxDetailedOutput);
		topToolbar.add(jButtonLoadQueries);
		topToolbar.add(jButtonSaveQueries);
	}
	
	/**
	 * Component initialization.
	 */
	private void jbInit() {
		
		this.setPreferredSize(new Dimension(600, 550));

		queryFileChooser.setFileFilter(new QueryFileFilter());

		buttonsInit();
		
		scrollbarsInit();

		textAreasInit();
	    
		checkBoxesInit();
		
		toolbarInit();
		
		// layout
		this.getContentPane().setLayout(borderLayout1);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		
		splitPanesInit();
		
		this.add(topToolbar, BorderLayout.NORTH);
		this.add(splitPaneForTextAreas, BorderLayout.CENTER);
		this.add(listScrollPane, BorderLayout.EAST);
		//this.add(getJList(), BorderLayout.EAST);
		this.add(jLabelSchemaFileSelected, BorderLayout.SOUTH);
		this.doLayout();
		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		this.pack();
		splitPaneForTextAreas.setDividerLocation(0.5);
		splitPaneForResults.setDividerLocation(0.5);
//		splitPaneForTextAreas.setDividerLocation(0.1);
		this.setVisible(true);

		// force the focus to be in the query text area when starting
		textAreaQuery.requestFocusInWindow();		
	}

	/**
	 * Gets the jlist associated to the query list.
	 *
	 * @return the jlist associated to the query list
	 */
	private JList getJList(){
		if (queryJList == null) {
			queryJList = new JList();
			queryJList.setCellRenderer(new QueryCellRenderer());
			queryJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			queryJList.setToolTipText("List of the queries to analyze");
			queryJList.setVisibleRowCount(500);
			queryJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
					JList source = (JList) e.getSource();
					String selectedQuery = ((String) source.getSelectedValue());
					int newSelectedIndex = source.getSelectedIndex();
					
					if(newSelectedIndex == -1)
						return;
					
					// save the actual query before loading the new one
					if(newSelectedIndex != selectedQueryIndex)
						queryList.set(selectedQueryIndex, textAreaQuery.getText());
					// loads the new query
					if(selectedQuery == null){
						selectedQuery = "";
						queryList.add(selectedQueryIndex, selectedQuery);
					}
					textAreaQuery.setText(selectedQuery);
					//textAreaQuery.setText(selectedQuery != null ? selectedQuery : "");
					selectedQueryIndex = newSelectedIndex;
					rebuildList();
					queryJList.ensureIndexIsVisible(selectedQueryIndex);										
				}
			});
			queryList.add(textAreaQuery.getText());
			rebuildList();
			queryJList.setSelectedIndex(0);
		}
		return queryJList;
	}
	
	/**
	 * Rebuild list.
	 */
	private void rebuildList(){
		if(queryList != null){
			DefaultListModel listModel = new DefaultListModel();
			for (String query : queryList) {
				listModel.addElement(query);
			}
			// adds the new visualization to the list
			queryJList.setModel(listModel);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowActivated(WindowEvent arg0) {	
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent arg0) {
		String message = "Do you really want to exit?";
		int answer = JOptionPane.showConfirmDialog(
				this, message,"Exit",JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION)
			System.exit(0);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeiconified(WindowEvent arg0) {		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowOpened(WindowEvent arg0) {
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);  
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void actionPerformed(ActionEvent arg0) {
		JButton source = (JButton) arg0.getSource();

		if(source.equals(jButtonAnalyze)){
			if(schemaFileSelected == null)
				JOptionPane.showMessageDialog(this, 
						"Please, select a schema first.");
			else {
				// we need to save the current query because could have been modified
				queryList.set(selectedQueryIndex, textAreaQuery.getText());
				
				try {
					// computing the relations of the selected schema
					xmlSchemaRelations = 
						new XMLSchema(schemaFileSelected.getName());
					
					// array of type projectors, one for each query/update evaluated
					TypeProjector [] typeProjectors = 
						new TypeProjector[queryList.size()];
					
					try {
						// type projector related to the selected schema
						ProjectorInference proj = 
							new ProjectorInference(xmlSchemaRelations);
						
						// instance of the path extractor
						PathExtractor pathExtractor = 
							new QueryPathExtractor();
						
						XParser parser = null;
						SimpleNode tree = null;
						ExtractedPaths exPaths = null;
						
						textAreaExPaths.setText("");
						
						// parsing all the queries, extracting the paths and the type projector
						// for each of them
						for(int index = 0; index < queryList.size(); index++){
						
							parser = new XParser(
									new java.io.StringBufferInputStream(
											queryList.get(index)));
							
							tree = parser.START();
							
							if(null == tree){								
								JOptionPane.showMessageDialog(
										this, "Error, abstract syntax " +
										"tree is null for query Q" + index 
										+ "!","Parsing error",
										JOptionPane.OK_OPTION);
								return;
							}
							else {
								System.out.println("XQuery expression Q" + index 
										+ " parsing succeded");
								
								// paths extraction from the parse tree of the query
								exPaths = pathExtractor.extractPaths(
										tree, new Environment(), null, 
										new Path((UpdateOperationType) null), 
										false, null);
								
								// computation of the projector starting from the extracted paths
								typeProjectors[index] = proj.infer3LevelProjector(exPaths);
								
								textAreaExPaths.setText(textAreaExPaths.getText() 
										+ "\nQuery " + index + ":\n");
								
								// outputs the type projector
								if(checkBoxDetailedOutput.isSelected()){
									textAreaExPaths.setText(textAreaExPaths.getText() 
											+ exPaths.toString());
								}
								else {
									textAreaExPaths.setText(textAreaExPaths.getText() 
											+ exPaths.toString(true));
								}
							}
						}
						
						// makes the union of all the projectors
						TypeProjector typeProjector = 
							TypeProjector.projectorUnion(typeProjectors);
						
						// outputs detailed information for the type projector 
						if(checkBoxDetailedOutput.isSelected()){
							textAreaResult.setText("Type projector =\n" 
									+ typeProjector);
						}
						// outputs summarized information for the type projector
						else {
							textAreaResult.setText("Type projector =\n" 
									+ typeProjector.toString(true));							
						}
						
					} catch (ParseException e) {
						System.out.println("Parsing error: " + e.getMessage());
						JOptionPane.showMessageDialog(this, 
								"Parsing error: " + e.getMessage());
						//textAreaResult.setText("Parsing error: " + e.getMessage());
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		else if(source.equals(jButtonOpen)){
			schemaFileChooser.setFileFilter(new SchemaFileFilter());
			
			int res = schemaFileChooser.showOpenDialog(this);
			switch(res){
				case JFileChooser.CANCEL_OPTION:
					break;
				case JFileChooser.APPROVE_OPTION:
					schemaFileSelected = schemaFileChooser.getSelectedFile();
					jLabelSchemaFileSelected.setText("Choosen schema: " 
							+ schemaFileSelected.getName());
					break;
				case JFileChooser.ERROR_OPTION:
					JOptionPane.showMessageDialog(this, 
							"Error while opening the file.");
					break;
				default:
					System.exit(1);
			}
		}
		else if(source.equals(jButtonAddQuery)){
			queryList.add("");
			queryList.set(selectedQueryIndex, textAreaQuery.getText());
			rebuildList();
			queryJList.setSelectedIndex(queryList.size()-1);
		}
		else if(source.equals(jButtonRemoveQuery)){
			if(queryList.size() <= 1){
				JOptionPane.showMessageDialog(this, 
						"Cannot remove the last query.");
				return;
			}
			queryList.remove(selectedQueryIndex);
			rebuildList();
			selectedQueryIndex = selectedQueryIndex == 0 ? 
					selectedQueryIndex : --selectedQueryIndex;
			queryJList.setSelectedIndex(selectedQueryIndex);
		}
		else if(source.equals(jButtonLoadQueries) || source.equals(jButtonSaveQueries)){

			boolean saving = source.equals(jButtonSaveQueries);
			int res = 0;
			
			if(saving){
				queryFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				queryFileChooser.setMultiSelectionEnabled(false);
				res = queryFileChooser.showSaveDialog(this);
			}
			else {
				queryFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				queryFileChooser.setMultiSelectionEnabled(true);
				res = queryFileChooser.showOpenDialog(this);
			}
			
			switch(res){
				case JFileChooser.CANCEL_OPTION:
					break;
				case JFileChooser.APPROVE_OPTION:
					if(saving){
						File dir = queryFileChooser.getSelectedFile();
						GregorianCalendar data = new GregorianCalendar();
						Date date = data.getTime();
						SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss");
						PrintWriter outFile = null;
						int idx = 0;
						
						for (String query : queryList) {
							try {
								outFile = new PrintWriter(new FileWriter(new File(dir.getAbsolutePath() + "/q" 
										+ idx++ + "_" 
										+ simpleDate.format(date) + ".txt")));
								outFile.write(query);
								outFile.close();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					else {
						File [] files = queryFileChooser.getSelectedFiles();
						
						// save the current query before loading the new ones
						String actualQuery = textAreaQuery.getText();
						if(!actualQuery.isEmpty())
							queryList.set(selectedQueryIndex, textAreaQuery.getText());
						else {
							queryList.remove(selectedQueryIndex);
						}
						
						// load the new queries (we suppose one query per file)
						for (File file : files) {
							StringBuffer str = new StringBuffer();
							try {
								for(Scanner s = new Scanner(file).useDelimiter("\\n"); 
									s.hasNext(); str.append(s.next() + "\n"));
							} catch (FileNotFoundException e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(this, 
								"File " + file.toString() + " not found.");
							}
							queryList.add(str.toString());
						}
						textAreaQuery.setText(queryList.get(selectedQueryIndex));
						rebuildList();
					}
					break;
				case JFileChooser.ERROR_OPTION:
					JOptionPane.showMessageDialog(this, 
							"Error while handling the file(s).");
					break;
				default:
					System.exit(1);
			}
		}
	}
	
	/**
	 * Class representing a filter based on file's extension.
	 */
	class SchemaFileFilter extends FileFilter {

		 /**
 		 * Methods that accept or reject a file by the use of a filter.
 		 *
 		 * @param file the file to accept or reject
 		 * @return true if the file is acceptable for this filter, false otherwise
 		 */
		  public boolean accept(File file) {
		    if (file.isDirectory()) return true;
		    String fname = file.getName().toLowerCase();
		    return fname.endsWith(".xsd") || fname.endsWith(".dtd");
		  }

		  /**
  		 * Methods that return a string representation of the filter.
  		 *
  		 * @return the description of the filter
  		 */
		  public String getDescription() {
		    return "Schema file in DTD or XSD format";
		  }
		}
	
	/**
	 * Class representing a filter based on file's extension.
	 */
	class QueryFileFilter extends FileFilter {

		 /**
 		 * Methods that accept or reject a file by the use of a filter.
 		 *
 		 * @param file the file to accept or reject
 		 * @return true if the file is acceptable for this filter, false otherwise
 		 */
		  public boolean accept(File file) {
		    if (file.isDirectory()) return true;
		    String fname = file.getName().toLowerCase();
		    return fname.endsWith(".txt");
		  }

		  /**
  		 * Methods that return a string representation of the filter.
  		 *
  		 * @return the description of the filter
  		 */
		  public String getDescription() {
		    return "Query file in textual format (.txt)";
		  }
		}
}

/**
 * Renderer for JList used for queries
 */
class QueryCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 2989274212999540358L;
	
    @Override
    public Component getListCellRendererComponent(JList list, 
            Object value, 
            int index,
            boolean isSelected, 
            boolean cellHasFocus) {
        
         JLabel renderer = (JLabel) super.getListCellRendererComponent(list, 
               value, 
               index,
               isSelected, 
               cellHasFocus);
         
         renderer.setText("Query " + (index >= 0 && index < 10 ? "0" : "") + index);
         
         return renderer;
    }
}
