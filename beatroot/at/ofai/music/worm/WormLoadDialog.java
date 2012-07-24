/*  Performance Worm: Visualisation of Expressive Musical Performance
	Copyright (C) 2001, 2006 by Simon Dixon

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License along
	with this program (the file gpl.txt); if not, download it from
	http://www.gnu.org/licenses/gpl.txt or write to the
	Free Software Foundation, Inc.,
	51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package at.ofai.music.worm;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import at.ofai.music.util.FrameMargins;

class WormLoadDialog extends JDialog {
	
	static final long serialVersionUID = 0;
	
	class MyButton extends JButton {
		
		static final long serialVersionUID = 0;
		public MyButton(String text, ActionListener al) {
			super(text);
			setBackground(WormConstants.buttonColor);
			setForeground(WormConstants.buttonTextColor);
			addActionListener(al);
		}
	} // inner class MyButton

	class MyTextField extends TextField {

		static final long serialVersionUID = 0;
		public MyTextField(String text, TextListener tl) {
			super(text);
			setBackground(WormConstants.buttonColor);
			setForeground(WormConstants.buttonTextColor);
			addTextListener(tl);
		}
	
	} // inner class MyTextField

	class MyLabel extends JLabel {
		
		static final long serialVersionUID = 0;
		public MyLabel(String text) {
			super(text);
			setForeground(WormConstants.buttonTextColor);
		}

	} // inner class MyLabel

	Worm worm;
	TextField inputFileField, matchFileField, wormFileField, timingOffsetField,
			synchronisationField;
	JButton inputFileButton, matchFileButton, wormFileButton, cancelButton,
			okButton;
	MyFileChooser chooser;
	
	public WormLoadDialog(Worm w) {
		super(w.theFrame, "Input Data", true);
		worm = w;
		worm.stop();
		chooser = new MyFileChooser();
		inputFileField = new MyTextField(worm.getInputFile(),new TextListener(){
			public void textValueChanged(TextEvent e) {
				worm.setInputFile(inputFileField.getText());
				worm.clearWormFile();
			}
		});
		matchFileField = new MyTextField(worm.getMatchFile(),new TextListener(){
			public void textValueChanged(TextEvent e) {
				worm.setMatchFile(matchFileField.getText());
			}
		});
		wormFileField = new MyTextField(worm.getWormFileName(),
			new TextListener() {
				public void textValueChanged(TextEvent e) {
					worm.setWormFile(wormFileField.getText());
				}
			}
		);
		timingOffsetField = new MyTextField(worm.getTimingOffsetString(),
			new TextListener() {
				public void textValueChanged(TextEvent e) {
					worm.setTimingOffsetString(timingOffsetField.getText());
				}
			}
		);
		synchronisationField = new MyTextField(worm.getFileDelayString(),
			new TextListener() {
				public void textValueChanged(TextEvent e) {
					worm.setFileDelayString(synchronisationField.getText());
				}
			}
		);
		inputFileButton = new MyButton("Browse", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				worm.setInputFile(chooser.browseOpen(inputFileField,
													MyFileFilter.waveFilter));
			}
		});
		matchFileButton = new MyButton("Browse", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				worm.setMatchFile(chooser.browseOpen(matchFileField,
													MyFileFilter.matchFilter));
			}
		});
		wormFileButton = new MyButton("Browse", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				worm.setWormFile(chooser.browseOpen(wormFileField,
													MyFileFilter.wormFilter));
			}
		});
		cancelButton = new MyButton("OK", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		okButton = new MyButton("OK", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		Container cp = getContentPane();
		cp.setLayout(null);
		JPanel p1 = new JPanel(new GridLayout(5,1));
		JPanel p2 = new JPanel(new GridLayout(5,1));
		JPanel p3 = new JPanel(new GridLayout(5,1));
		JLabel inputLabel = new MyLabel("Input file: ");
		JLabel matchLabel = new MyLabel("Match file: ");
		JLabel wormLabel = new MyLabel("Worm file: ");
		JLabel timingOffsetLabel = new MyLabel("Match/Audio Offset: ");
		JLabel synchronisationLabel = new MyLabel("Synchronisation: ");
		cp.setBackground(WormConstants.buttonColor);
		p1.setBackground(WormConstants.buttonColor);
		p1.add(inputLabel);
		p2.add(inputFileField);
		p3.add(inputFileButton);
		p1.add(matchLabel);
		p2.add(matchFileField);
		p3.add(matchFileButton);
		p1.add(wormLabel);
		p2.add(wormFileField);
		p3.add(wormFileButton);
		p1.add(timingOffsetLabel);
		p2.add(timingOffsetField);
		p3.add(cancelButton);
		p1.add(synchronisationLabel);
		p2.add(synchronisationField);
		p3.add(okButton);
		p1.setBounds(10,5,130,150);
		p2.setBounds(150,5,500,150);
		p3.setBounds(660,5,100,150);
		cp.add(p1);
		cp.add(p2);
		cp.add(p3);
		Dimension d = FrameMargins.get(false);
		setSize(770 + d.width, 160 + d.height);
		int x = w.getLocationOnScreen().x + (w.getWidth() - getWidth()) / 2;
		int y = w.getLocationOnScreen().y + (w.getHeight() - getHeight()) / 2;
		setLocation(x, y);
		setVisible(true);
	} // constructor
	
} // class WormLoadDialog
