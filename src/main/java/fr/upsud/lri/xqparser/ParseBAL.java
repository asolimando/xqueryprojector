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

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import fr.upsud.lri.pathExtractor.MainPathExtractor;
import org.xml.sax.SAXException;

public class ParseBAL implements java.awt.event.ActionListener {
	MainParser main = null;
	MainPathExtractor main2 = null;
	
	public ParseBAL(MainParser main) {
		this.main = main;
	}
	
	public ParseBAL(MainPathExtractor main) {
		this.main2 = main;
	}

	public void actionPerformed(ActionEvent e) {
		if(main == null)
			try {
				main2.analyzeButtonAction(e);
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
			} catch (SAXException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		else
			main.button1_action(e); 
	}
}