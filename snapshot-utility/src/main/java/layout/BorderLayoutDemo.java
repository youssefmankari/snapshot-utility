/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package layout;

/*
 * BorderLayoutDemo.java
 *
 */
import javax.swing.*;
import javax.swing.border.Border;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Color;


public class BorderLayoutDemo {
	public static boolean RIGHT_TO_LEFT = false;

	public static void addComponentsToPane(Container pane) {

		if (!(pane.getLayout() instanceof BorderLayout)) {
			pane.add(new JLabel("Container doesn't use BorderLayout!"));
			return;
		}

		if (RIGHT_TO_LEFT) {
			pane.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
		}

		JLabel titleLabel = new JLabel("Fajr Screenshot Grabber");
		titleLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		titleLabel.setBounds(5, 40, 100, 30);
		JPanel pageStartPanel = createPanel();
		pageStartPanel.add(titleLabel);
		pane.add(pageStartPanel, BorderLayout.PAGE_START);

		JPanel lineStartPanel = createPanel();
		JButton button = new JButton("Button 1 (LINE_START)");
		button.setBounds(10, 10, 100, 30);
		lineStartPanel.add(button);
		pane.add(lineStartPanel, BorderLayout.LINE_START);

		// Make the center component big, since that's the
		// typical usage of BorderLayout.
		JPanel centerPanel = createPanel();
		button = new JButton("Button 2 (CENTER)");
		button.setPreferredSize(new Dimension(200, 100));
		centerPanel.add(button);
		pane.add(centerPanel, BorderLayout.CENTER);

//		button = new JButton("Long-Named Button 4 (PAGE_END)");
//		pane.add(button, BorderLayout.PAGE_END);

		button = new JButton("5 (LINE_END)");
		pane.add(button, BorderLayout.LINE_END);
	}

	private static JPanel createPanel() {
		Border blackline = BorderFactory.createLineBorder(Color.black);
		JPanel jPanel = new JPanel();
		jPanel.setBorder(blackline);
		
		return jPanel;
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event dispatch thread.
	 */
	private static void createAndShowGUI() {

		// Create and set up the window.
		JFrame frame = new JFrame("BorderLayoutDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Set up the content pane.
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();

		GraphicsDevice[] screens = graphicsEnvironment.getScreenDevices();
		Rectangle screenBounds = screens[0].getDefaultConfiguration().getBounds();
		frame.setSize(new Dimension(screenBounds.width / 2, screenBounds.height / 2));
		frame.setPreferredSize(new Dimension(screenBounds.width / 2, screenBounds.height / 2));
		frame.setMaximumSize(new Dimension(screenBounds.width / 2, screenBounds.height / 2));
		frame.setLocationRelativeTo(null); // center it

		addComponentsToPane(frame.getContentPane());
		// Use the content pane's default BorderLayout. No need for
		// setLayout(new BorderLayout());
		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		/* Use an appropriate Look and Feel */
		try {
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		/* Turn off metal's use bold fonts */
		UIManager.put("swing.boldMetal", Boolean.FALSE);

		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
