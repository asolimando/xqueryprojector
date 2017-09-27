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
package fr.upsud.lri.pathExtractor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.io.UnsupportedEncodingException;
import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.xml.parsers.ParserConfigurationException;

import fr.upsud.lri.xqparser.ParseBAL;
import fr.upsud.lri.xqparser.ParseException;
import fr.upsud.lri.xqparser.PostParseException;
import fr.upsud.lri.xqparser.SimpleNode;
import fr.upsud.lri.xqparser.XParser;
import org.xml.sax.SAXException;

/**
 * The Class MainPathExtractor.
 */
public class MainPathExtractor extends JFrame implements WindowListener/*, ChangeListener*/ {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	// for handling undo/redo on query text area
	final UndoManager undo = new UndoManager();
	private Document doc = null;

	/** The button that starts the analysis of the inserted query. */
	private JButton jButtonAnalyze = new JButton("              Analyze              ");
	
	/** The text area for inserting the query. */
	private JTextArea textAreaQuery = new JTextArea("", 60, 30);
	
	/** The border layout1. */
	private BorderLayout borderLayout1 = new BorderLayout();
	
	/** The text area showing the result of the analysis. */
	private JTextArea textAreaResult = new JTextArea("", 50, 30);

	/** The split pane for the two text areas (one for the input, the other for the output) */
	private JSplitPane splitPaneForTextAreas = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	
	/** Query's text area scrollpane */
	private JScrollPane queryAreaScrollPane = new JScrollPane(textAreaQuery);
	/** Result's text area scrollpane */
	private JScrollPane resultAreaScrollPane = new JScrollPane(textAreaResult);
	
	/** Toolbar on the top of the window */
	private JToolBar topToolbar = new JToolBar(JToolBar.HORIZONTAL);
	
	/**
	 * Construct the gui.
	 */
	public MainPathExtractor() {
		super("Path extractor");
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		MainPathExtractor main = new MainPathExtractor();
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
	 * Initialize the undo/redo handling
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

		// Bind the undo action to ctl-Z, platform independent
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

		// Bind the redo action to ctl-Y, platform independent
		textAreaQuery.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo" );
	}
	
	/**
	 * GUI Components initialization.
	 */
	private void jbInit() {
		
		// sets the preferred size of the window
		this.setPreferredSize(new Dimension(600, 550));
		
		// sets the analyze button parameters
		jButtonAnalyze.setFont(new Font("Arial", Font.PLAIN, 17));
		jButtonAnalyze.addActionListener(new ParseBAL(this));
		JRootPane rootPane = this.getRootPane();
	    // set this button as the default one
		rootPane.setDefaultButton(jButtonAnalyze);

		// set the scrollbar orientation
	    queryAreaScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    
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


		resultAreaScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    // sets the parameters for the result's text area		
		textAreaResult.setFont(new Font("Serif", Font.PLAIN, 18));
		textAreaResult.setLineWrap(true);
		textAreaResult.setWrapStyleWord(true);
		textAreaResult.setText("Output will appear here");
		textAreaResult.setRows(15);
		textAreaResult.setEditable(false);
		// Setting grey background for textarea
		textAreaResult.setBackground(new Color(220, 220, 220));

		
		// toolbar's elements insertion
		topToolbar.add(jButtonAnalyze);
		
		// layout
		this.getContentPane().setLayout(borderLayout1);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);

		splitPaneForTextAreas.add(queryAreaScrollPane);
		splitPaneForTextAreas.add(resultAreaScrollPane);
		splitPaneForTextAreas.setDividerSize(2);
		
		this.add(topToolbar, BorderLayout.NORTH);
		this.add(splitPaneForTextAreas, BorderLayout.CENTER);
		this.doLayout();
		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		this.pack();
		splitPaneForTextAreas.setDividerLocation(0.5);
		this.setVisible(true);

		// force the focus to be in the query text area when starting
		textAreaQuery.requestFocusInWindow();		
	}


	/**
	 * Action event handler for analyze button
	 *
	 * @param evt the action event
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void analyzeButtonAction(ActionEvent evt) 
	throws ParserConfigurationException, SAXException, IOException {
		try {
			String expr = textAreaQuery.getText();
			XParser parser = new XParser(
					new java.io.StringBufferInputStream(expr));
			SimpleNode tree = parser.START();
			if (null == tree)
				textAreaResult.setText("Error, abstract syntax tree is null!");
			else {
				System.out.println("XQuery expression parsing succeded");
/*				ByteArrayOutputStream baos = new ByteArrayOutputStream(
						62 * 1024);
				PrintStream ps = new PrintStream(baos);
				tree.dump("|", ps);
				String s = new String(baos.toByteArray());
				textArea2.setText(s); */
				
				/*
				PathExtractor fr.upsud.lri.pathExtractor = isXQUF
					? new UpdatePathExtractor(new QueryPathExtractor())
					: new QueryPathExtractor();
					*/
				PathExtractor pathExtractor = new QueryPathExtractor();
				
				//textArea2.setText(node.toString());
				ExtractedPaths paths = pathExtractor.extractPaths(
						tree, new Environment(), null, 
						new Path((UpdateOperationType) null), 
						false, null);
				System.out.println(paths.toString());
				textAreaResult.setText(paths.toString());
			}
		} catch (ParseException e) {
			textAreaResult.setText(e.getMessage());
		} catch (Error err) {
			textAreaResult.setText(err.getMessage());
		} catch (PostParseException ppe) {
			textAreaResult.setText(ppe.getMessage());
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
	}
}
