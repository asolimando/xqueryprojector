/*
Copyright (c) 2005 W3C(r) (http://www.w3.org/) (MIT (http://www.lcs.mit.edu/),
INRIA (http://www.inria.fr/), Keio (http://www.keio.ac.jp/)),
All Rights Reserved.
See http://www.w3.org/Consortium/Legal/ipr-notice-20000612#Copyright.
W3C liability
(http://www.w3.org/Consortium/Legal/ipr-notice-20000612#Legal_Disclaimer),
trademark
(http://www.w3.org/Consortium/Legal/ipr-notice-20000612#W3C_Trademarks),
document use
(http://www.w3.org/Consortium/Legal/copyright-documents-19990405),
and software licensing rules
(http://www.w3.org/Consortium/Legal/copyright-software-19980720)
apply.
 */
package fr.upsud.lri.projectorInference;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import fr.upsud.lri.pathExtractor.Path;
import fr.upsud.lri.pathExtractor.PathType;
import fr.upsud.lri.schema.XMLSchema;
import fr.upsud.lri.schemaAsGraph.Graph;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

// TODO: Auto-generated Javadoc
/**
 * The Class MainPathExtractor.
 */
public class MainTypeInference extends JFrame implements WindowListener, ActionListener {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** Handler for undo/redo on query text area. */
	final UndoManager undo = new UndoManager();

	/** Document element for undo/redo. */
	private Document doc = null;
	
	/** Object encoding the relations of the analyzed schema. */
	private XMLSchema xmlSchemaRelations = null;
	
	/** The file associated with the choosen schema. */
	private File schemaFileSelected = null;

	/** The graph representation of the schema. */
	private mxGraph graph = null;
	
	/** The button that starts the analysis of the inserted query. */
	private JButton jButtonOpen = new JButton("           Open Schema           ");
	
	/** The j button analyze. */
	private JButton jButtonAnalyze = new JButton("              Analyze              ");	
	
	/** Label showing the name of the schema file selected. */
	private JLabel jLabelSchemaFileSelected = new JLabel("Choosen schema: ");
	
	/** The check box discriminating between path extraction for XQuery (false) and XQueryUpdateFacility (true). */
	
	/** The text area for inserting the query. */
	private JTextArea textAreaPath = new JTextArea("", 60, 30);
	
	/** The border layout1. */
	private BorderLayout borderLayout1 = new BorderLayout();

	/** Split pane for the main components (textarea for textual representation and graphical representation of the graph). */
	private JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	
	/** The split pane for graphs. */
	private JSplitPane splitPaneForGraphs = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	
	/** Scroll area for text area representing the textual representation of the graph. */
	private JScrollPane queryAreaScrollPane = new JScrollPane(textAreaPath);
	
	/** Top toolbar with the main buttons. */
	private JToolBar topToolbar = new JToolBar(JToolBar.HORIZONTAL);
	
	/**
	 * Construct the gui.
	 */
	public MainTypeInference() {
		super("Graphical Type Inference");
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments of the main method
	 */
	public static void main(String[] args) {
		MainTypeInference main = new MainTypeInference();
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
		textAreaPath.getActionMap().put("Undo",
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
		textAreaPath.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo" );
		
		// Create a redo action and add it to the text component
		textAreaPath.getActionMap().put("Redo",
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
		textAreaPath.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo" );
	}
	
	/**
	 * Component initialization.
	 */
	private void jbInit() {
		
		this.setPreferredSize(new Dimension(600, 550));
		
		jButtonAnalyze.setFont(new Font("Arial", Font.PLAIN, 17));
		jButtonOpen.setFont(new Font("Arial", Font.PLAIN, 17));
		jLabelSchemaFileSelected.setFont(new Font("Arial", Font.PLAIN, 17));
		
		jButtonOpen.addActionListener(this);
		jButtonAnalyze.addActionListener(this);
		
		JRootPane rootPane = this.getRootPane();
	    rootPane.setDefaultButton(jButtonAnalyze);

	    queryAreaScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//queryAreaScrollPane.setPreferredSize(new Dimension(250, 275));
	    
		textAreaPath.setRows(6);
		textAreaPath.setText("");
		textAreaPath.setFont(new Font("Serif", Font.PLAIN, 18));
		textAreaPath.setLineWrap(true);
		textAreaPath.setWrapStyleWord(true);
		
		// binds the textarea to the document object
		doc = textAreaPath.getDocument();
		// initialize undo/redo action for query text area
		UndoRedoInit();
		
		// toolbar's element insertion
		topToolbar.add(jButtonOpen);
		topToolbar.add(jButtonAnalyze);
		
		
		// layout
		this.getContentPane().setLayout(borderLayout1);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		
		mainSplitPane.add(queryAreaScrollPane);
		mainSplitPane.add(splitPaneForGraphs);
		mainSplitPane.setDividerSize(2);
		
		this.add(topToolbar, BorderLayout.NORTH);
		this.add(mainSplitPane, BorderLayout.CENTER);
		this.add(jLabelSchemaFileSelected, BorderLayout.SOUTH);
		this.doLayout();
		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		this.pack();
		mainSplitPane.setDividerLocation(0.5);
		this.setVisible(true);

		// force the focus to be in the query text area when starting
		textAreaPath.requestFocusInWindow();		
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
	@Override
	public void actionPerformed(ActionEvent arg0) {
		JButton source = (JButton) arg0.getSource();

		if(source.equals(jButtonAnalyze)){
			if(schemaFileSelected == null)
				JOptionPane.showMessageDialog(this, "Please, select a schema first.");
			else {
				String pathText = textAreaPath.getText();
				Path path = new Path(pathText);
				PathType pathType = null;
				mxGraphComponent markedGraphComponent = null;
				
				try {
					xmlSchemaRelations = new XMLSchema(schemaFileSelected.getName());
					
				/*	for(GraphNode node : xmlSchemaRelations.getGraphNodes()){
						//textAreaPath.setText(textAreaPath.getText() + node + "\n");
						System.out.println(node + "\n");
					}*/
					
					graph = new mxGraph();
					graph.getModel().beginUpdate();
					Graph markedGraph = null;
					
					try {
						ProjectorInference proj = 
							new ProjectorInference(xmlSchemaRelations);
						markedGraph = proj.threeLevelAnalyzePath(
								xmlSchemaRelations.getGraph(), path.removeVarItem(), pathType, true, null);
						
						markedGraph.getContext().addAllNoDuplicates(markedGraph.getPermanentlyMarkedNodes());
						
						markedGraph.buildGraphRepresentation(graph);
						mxGraphLayout graphLayout = new mxHierarchicalLayout(graph);
						graphLayout.execute(graph.getDefaultParent());
						markedGraphComponent = new mxGraphComponent(graph);
						
						System.out.println(markedGraph);
						
						splitPaneForGraphs.removeAll();
						splitPaneForGraphs.add(markedGraphComponent);						
						splitPaneForGraphs.validate();
						splitPaneForGraphs.revalidate();
						
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally {
						graph.getModel().endUpdate();
					}
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		else if(source.equals(jButtonOpen)){
			JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
			fileChooser.setFileFilter(new SchemaFileFilter());
			
			int res = fileChooser.showOpenDialog(this);
			switch(res){
				case JFileChooser.CANCEL_OPTION:
					break;
				case JFileChooser.APPROVE_OPTION:
					schemaFileSelected = fileChooser.getSelectedFile();
					jLabelSchemaFileSelected.setText("Choosen schema: " + schemaFileSelected.getName());
					break;
				case JFileChooser.ERROR_OPTION:
					System.out.println("Error while opening the file");
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
}
