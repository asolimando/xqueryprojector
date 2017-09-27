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
package fr.upsud.lri.xqparser;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
//import java.io.UnsupportedEncodingException;
import javax.swing.*;

public class MainParser extends JFrame {
	boolean isStandalone = false;

	Button button1 = new Button();

	TextArea textArea1 = new TextArea("", 100, 30,
			TextArea.SCROLLBARS_VERTICAL_ONLY);

	BorderLayout borderLayout1 = new BorderLayout();
	BorderLayout borderLayout2 = new BorderLayout();

	Panel buttonPanel = new Panel();

	TextArea textArea2 = new TextArea("", 100, 30,
			TextArea.SCROLLBARS_VERTICAL_ONLY);

	/** Construct the applet */
	public MainParser() {
	}

	public static void main(String[] args) {
		MainParser main = new MainParser();
		main.init();
	}

	/** Initialize the applet */
	public void init() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Component initialization */
	private void jbInit() throws Exception {
		button1.setLabel("              Parse              ");

		button1.addActionListener(new ParseBAL(this));
		textArea1.setRows(6);
		textArea1.setText("");
		this.getContentPane().setLayout(borderLayout1);
		textArea2.setText("Output will appear here");
		textArea2.setRows(15);

		buttonPanel.setLayout(borderLayout2);
		buttonPanel.add(button1, BorderLayout.WEST);
		this.add(buttonPanel, BorderLayout.NORTH);
		this.add(textArea1, BorderLayout.CENTER);
		this.add(textArea2, BorderLayout.SOUTH);
		this.doLayout();
		this.pack();
		this.setVisible(true);
	}

	/** Get parameter info */
	public String[][] getParameterInfo() {
		return null;
	}

	void button1_action(ActionEvent evt) {
		try {
			String expr = textArea1.getText();
			XParser parser = new XParser(new java.io.StringBufferInputStream(
					expr));
			SimpleNode tree = parser.START();
			if (null == tree)
				textArea2.setText("Error!");
			else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream(
						62 * 1024);
				PrintStream ps = new PrintStream(baos);
				tree.dump("|", ps);
				String s = new String(baos.toByteArray());
				textArea2.setText(s);
			}
		} catch (ParseException e) {
			textArea2.setText(e.getMessage());
		} catch (Error err) {
			textArea2.setText(err.getMessage());
		} catch (PostParseException ppe) {
			textArea2.setText(ppe.getMessage());
		} catch (Exception genericException) {
			textArea2.setText(genericException.getMessage());
		}
	}
}
