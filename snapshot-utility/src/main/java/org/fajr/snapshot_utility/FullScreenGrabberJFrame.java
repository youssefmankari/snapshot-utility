package org.fajr.snapshot_utility;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class FullScreenGrabberJFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private ScreenshotSessionInternalFrame settingsInternalFrame;
	static Logger log = Logger.getLogger(FullScreenGrabberJFrame.class.getName());


	public FullScreenGrabberJFrame(ScreenshotSessionInternalFrame settingsInternalFrame, Settings fajrAppSettings) {
		this.settingsInternalFrame = settingsInternalFrame;
		setUndecorated(true);
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = graphicsEnvironment.getScreenDevices();
		Rectangle screenBounds = screens[0].getDefaultConfiguration().getBounds();
		setSize((int) screenBounds.getWidth(), (int) screenBounds.getHeight());
		add(new CapturePane());
		setOpacity((float) 0.5);
		setAlwaysOnTop(true);
		setVisible(true);
	}

	public class CapturePane extends JPanel {

		private static final long serialVersionUID = 1L;
		private Rectangle selectionBounds;
		private Point clickPoint;

		public CapturePane() {
			this.setFocusable(true);
			this.requestFocus();
			setOpaque(false);

			MouseAdapter mouseHandler = new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					clickPoint = e.getPoint();
					selectionBounds = null;
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					clickPoint = null;
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					Point dragPoint = e.getPoint();
					int x = Math.min(clickPoint.x, dragPoint.x);
					int y = Math.min(clickPoint.y, dragPoint.y);
					int width = Math.max(clickPoint.x - dragPoint.x, dragPoint.x - clickPoint.x);
					int height = Math.max(clickPoint.y - dragPoint.y, dragPoint.y - clickPoint.y);
					selectionBounds = new Rectangle(x, y, width, height);
					repaint();
				}
			};

			KeyAdapter keyAdapter = new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {

					if (e.getKeyCode() == KeyEvent.VK_ENTER) {// code key for "enter" key
						
						FullScreenGrabberJFrame.this.dispose();
						if (selectionBounds == null) {
							JOptionPane.showMessageDialog(settingsInternalFrame, "Drag your mouse to select screen rectangle to capture and then hit <ENTER>");
							return;
						}

						scheduleScreenshotGrab(selectionBounds);
					}
				}
			};
			addMouseListener(mouseHandler);
			addMouseMotionListener(mouseHandler);
			addKeyListener(keyAdapter);
		}

		protected void scheduleScreenshotGrab(Rectangle selectionBounds2) {
			log.info("scheduling...selectionBounds2 = " + selectionBounds2.toString());
			SelectedRectangle selectedRectangle = new SelectedRectangle((int) selectionBounds2.getX(),
					(int) selectionBounds2.getY(), (int) selectionBounds2.getWidth(),
					(int) selectionBounds2.getHeight());
			settingsInternalFrame.screenRectangleSelected(selectedRectangle, null);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setColor(new Color(255, 255, 255, 128));

			Area fill = new Area(new Rectangle(new Point(0, 0), getSize()));
			if (selectionBounds != null) {
				fill.subtract(new Area(selectionBounds));
			}
			g2d.fill(fill);
			if (selectionBounds != null) {
				g2d.setColor(Color.BLACK);
				g2d.draw(selectionBounds);
			}
			g2d.dispose();
		}
	}

}
