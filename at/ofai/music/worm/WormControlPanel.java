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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;

import at.ofai.music.util.PSPrinter;


class WormControlPanel extends JPanel
					   implements ActionListener, HierarchyBoundsListener {	

	static final long serialVersionUID = 0;
	protected Worm worm;
	protected JPopupMenu flagMenu;
	protected JRadioButtonMenuItem[] rb;
	protected String[] buttonText = {
	"<<x>>", "<<y>>", "^^^", "<<<", "ML >>", "Smooth", "Flags", "Play","Stop",
	">>x<<", ">>y<<", "vvv", ">>>", "<< ML", "Header", "Load", "Save","Quit"};
	
	public WormControlPanel(Worm w) {
		worm = w;
		setBackground(WormConstants.buttonColor);
		setLayout(new GridLayout(2, buttonText.length / 2));
		for (int i = 0; i < buttonText.length; i++) {
			JButton theButton = new JButton(buttonText[i]);
			theButton.setBackground(WormConstants.buttonColor);
			theButton.setForeground(WormConstants.buttonTextColor);
			theButton.addActionListener(this);
			add(theButton);
			if (buttonText[i].equals("Flags")) {
				flagMenu = new JPopupMenu(buttonText[i]);
				JCheckBoxMenuItem scale = new JCheckBoxMenuItem("AutoScale");
				scale.setBackground(WormConstants.buttonColor);
				scale.setForeground(WormConstants.buttonTextColor);
				scale.setSelected(true);
				scale.addActionListener(this);
				worm.setAutoButton(scale);
				flagMenu.add(scale);
				flagMenu.addSeparator();
				JCheckBoxMenuItem glow = new JCheckBoxMenuItem("Glow Worm");
				glow.setBackground(WormConstants.buttonColor);
				glow.setForeground(WormConstants.buttonTextColor);
				glow.setSelected(false);
				glow.addActionListener(this);
				flagMenu.add(glow);
				flagMenu.addSeparator();
				JCheckBoxMenuItem plots = new JCheckBoxMenuItem("Histograms");
				plots.setBackground(WormConstants.buttonColor);
				plots.setForeground(WormConstants.buttonTextColor);
				plots.setSelected(false);
				plots.addActionListener(this);
				flagMenu.add(plots);
				flagMenu.addSeparator();
				ButtonGroup bg = new ButtonGroup();
				rb = new JRadioButtonMenuItem[Worm.smoothLabels.length];
				for (int j = 0; j < Worm.smoothLabels.length; j++) {
					rb[j] = new JRadioButtonMenuItem(Worm.smoothLabels[j]);
					rb[j].setBackground(WormConstants.buttonColor);
					rb[j].setForeground(WormConstants.buttonTextColor);
					rb[j].addActionListener(this);
					flagMenu.add(rb[j]);
					bg.add(rb[j]);
				}
				rb[w.getSmoothMode()].setSelected(true);
				worm.setSmoothButtons(rb);
				flagMenu.setInvoker(theButton);
				flagMenu.addSeparator();
				JMenuItem printButton = new JMenuItem("Print300");
				printButton.setBackground(WormConstants.buttonColor);
				printButton.setForeground(WormConstants.buttonTextColor);
				printButton.addActionListener(this);
				flagMenu.add(printButton);
				JMenuItem printButton2 = new JMenuItem("Print600");
				printButton2.setBackground(WormConstants.buttonColor);
				printButton2.setForeground(WormConstants.buttonTextColor);
				printButton2.addActionListener(this);
				flagMenu.add(printButton2);
			} else if (buttonText[i].equals("Play"))
				worm.setPlayButton(theButton);
		}
		setSize(w.getWidth(), WormConstants.cpHeight);
		setMaximumSize(new Dimension(w.getWidth(), WormConstants.cpHeight));
		addHierarchyBoundsListener(this);
		worm.setControlPanel(this); // for callback
	} // constructor
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("<<x>>"))
			worm.xZoom(true);
		else if (e.getActionCommand().equals(">>x<<"))
			worm.xZoom(false);
		else if (e.getActionCommand().equals("<<<"))
			worm.xMoveRight(false);
		else if (e.getActionCommand().equals(">>>"))
			worm.xMoveRight(true);
		else if (e.getActionCommand().equals("ML >>"))
			worm.audio.ti.switchLevels(true);
		else if (e.getActionCommand().equals("<< ML"))
			worm.audio.ti.switchLevels(false);
		else if (e.getActionCommand().equals("Play") ||
					e.getActionCommand().equals("Cont"))
			worm.play();
		else if (e.getActionCommand().equals("Pause"))
			worm.pause();
		else if (e.getActionCommand().equals("Stop"))
			worm.stop();
		else if (e.getActionCommand().equals("<<y>>"))
			worm.yZoom(true);
		else if (e.getActionCommand().equals(">>y<<"))
			worm.yZoom(false);
		else if (e.getActionCommand().equals("vvv"))
			worm.yMoveDown(true);
		else if (e.getActionCommand().equals("^^^"))
			worm.yMoveDown(false);
		else if (e.getActionCommand().equals("Header")) {
			worm.editParameters();
		} else if (e.getActionCommand().equals("Smooth"))
			worm.smooth();
		else if (e.getActionCommand().equals("Flags")) {
			flagMenu.setVisible(true);
			flagMenu.setLocation(
						((Component)e.getSource()).getLocationOnScreen());
		} else if (e.getActionCommand().equals("Load")) {
			new WormLoadDialog(worm);
		} else if (e.getActionCommand().equals("Save")) {
			worm.save(new MyFileChooser().browseSave());
		} else if (e.getActionCommand().equals("Quit"))
		    System.exit(0);
		else if (e.getActionCommand().equals("AutoScale"))
			worm.setAutoScaleMode(
							((JCheckBoxMenuItem)e.getSource()).isSelected());
		else if (e.getActionCommand().equals("Glow Worm"))
			worm.setGlow(((JCheckBoxMenuItem)e.getSource()).isSelected());
		else if (e.getActionCommand().equals("Histograms"))
			TempoInducer.plotFlag =
							((JCheckBoxMenuItem)e.getSource()).isSelected();
		else if (e.getActionCommand().equals("Print300"))
			PSPrinter.print(worm, 300);
		else if (e.getActionCommand().equals("Print600"))
			PSPrinter.print(worm, 600);
		else
			for (int i = 0; i < Worm.smoothLabels.length; i++)
				if (e.getActionCommand().equals(Worm.smoothLabels[i]))
					worm.setSmoothMode(i);
	} // actionPerformed

	public void ancestorMoved(HierarchyEvent e) {}

	public void ancestorResized(HierarchyEvent e) {	
		setMaximumSize(new Dimension(worm.getWidth(), WormConstants.cpHeight));
		setSize(worm.getWidth(), WormConstants.cpHeight);
		repaint();
	} // ancestorResized()

} // class WormControlPanel
