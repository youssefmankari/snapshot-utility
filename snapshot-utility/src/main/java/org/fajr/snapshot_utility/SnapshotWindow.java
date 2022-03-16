package org.fajr.snapshot_utility;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.fajr.snapshot_utility.event.ScreenshotEvent;
import org.fajr.snapshot_utility.event.listener.ScreenshotEventListener;

public class SnapshotWindow implements ActionListener, TreeSelectionListener, ScreenshotEventListener, MouseListener {

	JDesktopPane desktop;
	private JFrame mainFrame;
	protected SettingsInternalFrame settingsInternalFrame;
	private DefaultMutableTreeNode screenshotsNode;
	private DefaultMutableTreeNode scheduledJobsMasterNode;
	private JTree tree;
	private JTextField statusTextField;

	private final Icon[] busyIcons = new Icon[15];
	private javax.swing.Timer busyIconTimer = null;
	private JLabel iconStatusLabel;
	private DefaultMutableTreeNode top;
	private JPanel centerPanel;
	private ScheduledJobsList scheduledJobsList = new ScheduledJobsList();
	private List<ScreenshotGrabberTimerTask> screenshotGrabberTimerTaskList = new ArrayList<ScreenshotGrabberTimerTask>();

	private Settings settings;

	public SnapshotWindow(Settings settings) {
		setSettings(settings);
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public Settings getSettings() {
		return settings;
	}

	public void createAndShowWindow() {

		initBusyIcons();

		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Stack");

		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();

		GraphicsDevice[] screens = graphicsEnvironment.getScreenDevices();
		Rectangle screenBounds = screens[0].getDefaultConfiguration().getBounds();

		mainFrame = new JFrame("Snapshot Window");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		desktop = new JDesktopPane(); // a specialized layered pane

		// Use the content pane's default BorderLayout. No need for
		desktop.setLayout(new BorderLayout());

//		JLabel titleLabel = new JLabel("Fajr Screenshot Grabber");
//		titleLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
//		titleLabel.setBounds(5, 40, 100, 30);
//		JPanel pageStartPanel = createLeftPanel();
//		pageStartPanel.add(titleLabel);

		JPanel lineStartPanel = createLeftPanel();
		// Create the nodes.
		top = new DefaultMutableTreeNode("Library");
		createNodes(top);

		// Create a tree that allows one selection at a time.
		tree = new JTree(top);
		tree.putClientProperty("JTree.lineStyle", "None");
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(this);

		tree.putClientProperty("JTree.lineStyle", "Horizontal");

		lineStartPanel.add(tree);
		desktop.add(lineStartPanel, BorderLayout.LINE_START);

//		JPanel centerPanel = createCenterPanel1();
//		desktop.add(centerPanel, BorderLayout.CENTER);

		desktop.setBackground(Color.lightGray);
		mainFrame.getContentPane().add(desktop);

		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;

		// Create the menu bar.
		menuBar = new JMenuBar();

		// Build the first menu.
		menu = new JMenu("Screenshots");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("Screenshot helper");
		menuBar.add(menu);

		// a group of JMenuItems
		menuItem = new JMenuItem("New screenschot", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("New screenschot session");
		menuItem.setActionCommand("new-screenshot");
		menuItem.addActionListener(this);

		menu.add(menuItem);

		menuItem = new JMenuItem("Settings", new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_B);
		menu.add(menuItem);
		menuItem.setActionCommand("settings");
		menuItem.addActionListener(this);

		iconStatusLabel = new JLabel();

		mainFrame.setJMenuBar(menuBar);

		// Display the window.
		mainFrame.pack();
		mainFrame.setSize(new Dimension(screenBounds.width / 2, screenBounds.height / 2));
		mainFrame.setLocationRelativeTo(null); // center it
		mainFrame.setVisible(true);

		// start screengrab schedules if we have any
		startSchedules();

	}

	private void startSchedules() {
		for (Iterator<ScheduledJob> iterator = scheduledJobsList.iterator(); iterator.hasNext();) {
			ScheduledJob scheduledJob = (ScheduledJob) iterator.next();
			if (scheduledJob.getStatus() == ScheduledJobStatus.SCHEDULED)
				scheduleScreenGrab(scheduledJob);

		}

	}

	public void initBusyIcons() {
		// ResourceBundle fajrResourceBundle =
		// ResourceBundle.getBundle("tv/bug/brain/ui/resources/BrainUI");
		for (int i = 0; i < busyIcons.length; i++) {
			ClassLoader classLoader = getClass().getClassLoader();
//			URL resource = classLoader.getResource(fileName);
			URL resource = classLoader.getResource("busy-icons/busy-icon" + i + ".png");
			// System.out.println("resource = " + resource);
			busyIcons[i] = new ImageIcon(resource);
		}
	}

	private void createNodes(DefaultMutableTreeNode top) {

		screenshotsNode = new DefaultMutableTreeNode("Screenshots");
		top.add(screenshotsNode);
		scheduledJobsMasterNode = new DefaultMutableTreeNode("Scheduled Jobs");

		List<ScheduledJob> schedules = settings.getSchedules();
		if (schedules != null && schedules.size() != 0) {
			addSchedulesToTreeView(schedules);
		}

		// for test
//		DefaultMutableTreeNode scheduledJobNode = new DefaultMutableTreeNode(new ScheduledJob(0, new Date(), new Date(), "00:05"));
//		scheduledJobsMasterNode.add(scheduledJobNode);

		top.add(scheduledJobsMasterNode);

	}

	private void addSchedulesToTreeView(List<ScheduledJob> schedules) {
		for (Iterator<ScheduledJob> iterator = schedules.iterator(); iterator.hasNext();) {
			ScheduledJob scheduledJob = iterator.next();
			DefaultMutableTreeNode scheduledJobNode = new DefaultMutableTreeNode(scheduledJob.getId());
			scheduledJobsMasterNode.add(scheduledJobNode);
			scheduledJobsList.add(scheduledJob);
		}
	}

	private static JPanel createLeftPanel() {
		Border blackline = BorderFactory.createLineBorder(Color.white);
		JPanel jPanel = new JPanel();
		jPanel.setBorder(blackline);
		jPanel.setVisible(true);
		return jPanel;
	}

	private JPanel createMiddlePanel(ScheduledJob scheduledJob) {

		Border blackline = BorderFactory.createLineBorder(Color.white);
		JPanel jPanel = new JPanel();
		jPanel.setLayout(null);
		jPanel.setBorder(blackline);

		JLabel statusLabel = new JLabel("status");
//		Border redline = BorderFactory.createLineBorder(Color.red);
//		statusLabel.setBorder(redline);
		statusLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		statusLabel.setBounds(1, 1, 100, 30);
		jPanel.add(statusLabel);

		statusTextField = new JTextField();
		statusTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		statusTextField.setBounds(135, 1, 228, 30);
		jPanel.add(statusTextField);
		statusTextField.setColumns(10);
		statusTextField.setText(scheduledJob.getStatus().toString());
//		statusTextField.setText("Screenshot Job Scheduled");
		statusTextField.setEditable(false);

		Image image = getStatusIcon(scheduledJob);
		if (image != null)
			iconStatusLabel.setIcon(new ImageIcon(image));
		iconStatusLabel.setBounds(365, 1, 25, 25);
//		iconStatusLabel.setBorder(redline);
		jPanel.add(iconStatusLabel);

		////////
		JLabel startAtLabel = new JLabel("Start Time");
//		startAtLabel.setBorder(redline);
		startAtLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		startAtLabel.setBounds(1, 30, 100, 30);
		jPanel.add(startAtLabel);

		JTextField startAtTextField = new JTextField();
		startAtTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		startAtTextField.setBounds(135, 30, 228, 30);
		startAtTextField.setColumns(10);
		startAtTextField.setText(scheduledJob.getStartAt().toString());
		startAtTextField.setEditable(false);
		jPanel.add(startAtTextField);

		////////
		JLabel endAtLabel = new JLabel("End Time");
//		endAtLabel.setBorder(redline);
		endAtLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		endAtLabel.setBounds(1, 60, 100, 30);
		jPanel.add(endAtLabel);

		JTextField endAtTextField = new JTextField();
		endAtTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		endAtTextField.setBounds(135, 60, 228, 30);
		jPanel.add(endAtTextField);
		endAtTextField.setColumns(10);
		endAtTextField.setText(scheduledJob.getEndAt().toString());
		endAtTextField.setEditable(false);

		////////
		JLabel periodicityLabel = new JLabel("Take screenshot every:");
//		periodicityLabel.setBorder(redline);
		periodicityLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		periodicityLabel.setBounds(1, 90, 135, 30);
		jPanel.add(periodicityLabel);

		JTextField periodicityTextField = new JTextField();
		periodicityTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		periodicityTextField.setBounds(135, 90, 228, 30);
		jPanel.add(periodicityTextField);
		periodicityTextField.setColumns(10);
		periodicityTextField.setText(scheduledJob.getPeriodicity());
		periodicityTextField.setEditable(false);
		///////////////
		jPanel.setBorder(blackline);
		jPanel.setVisible(true);
		return jPanel;
	}

	private Image getStatusIcon(ScheduledJob scheduledJob) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
//		Border redline = BorderFactory.createLineBorder(Color.red);
		ClassLoader classLoader = getClass().getClassLoader();
		Image image = null;
		URL resource;
		System.out.println("getIconStatus methof entred :scheduledJob.getStatus() = " + scheduledJob.getStatus());
		switch (scheduledJob.getStatus()) {
		case SCHEDULED:
			stopBusyTimer();
			resource = classLoader.getResource("rt_g_25.png");
			image = toolkit.getImage(resource);
			break;

		case RUNNING:
			// return nothing (null). we show busy icoons
			showBusyIcons();
			break;

		case ENDED:
			stopBusyTimer();
			resource = classLoader.getResource("check.png");
			image = toolkit.getImage(resource);
			break;

		default:
			break;
		}

		return image;

	}

	// React to menu selections.
	public void actionPerformed(ActionEvent e) {
		if ("settings".equals(e.getActionCommand())) { // new
		} else if ("new-screenshot".equals(e.getActionCommand())) { // new
			createAndShowSettingsWindow();
		} else { // quit
			quit();
		}
	}

//	private void startNewScrrenshotSession() {
//		//
//		String fileName = "telescop.png";
//		Toolkit toolkit = Toolkit.getDefaultToolkit();
//		ClassLoader classLoader = getClass().getClassLoader();
//		URL resource = classLoader.getResource(fileName);
//		Image image = toolkit.getImage(resource);
//		Cursor cursor = toolkit.createCustomCursor(image, new Point(0, 0), "png");
//		FullScreenGrabberJFrame fullScreenGrabberInternalFrame = new FullScreenGrabberJFrame(this, null);
//		fullScreenGrabberInternalFrame.setCursor(cursor);
//	}

	public JDesktopPane getDesktop() {
		return desktop;
	}

	// Create a new internal frame.
	protected void createAndShowSettingsWindow() {

		if (settingsInternalFrame != null)
			return;
		settingsInternalFrame = new SettingsInternalFrame(desktop, settings, new Callback() {

			@Override
			public void taskTerminated(ScheduledJob scheduledJob, Settings settings) {
				System.out.println("taskTerminated with callback---> scheduledJob = " + scheduledJob);
				setSettings(settings);
				scheduleScreenGrab(scheduledJob);
				scheduledJobsList.add(scheduledJob);
				updateScheduledJobTreeView(scheduledJob);
			}

			@Override
			public void taskTerminated(boolean success, String message, Settings settings) {

			}
		});
		settingsInternalFrame.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				settingsInternalFrame = null;
				//SettingsInternalFrame settingsInternalFrame = (SettingsInternalFrame) e.getSource();
				//ScheduledJob scheduledJob = settingsInternalFrame.getScheduledJob();
			}
		});
		desktop.add(settingsInternalFrame);
		try {
			settingsInternalFrame.setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
		}
	}

	// Quit the application.
	protected void quit() {
		System.exit(0);
	}

	/** Creates BufferedImage with Transparency.TRANSLUCENT */
	static final java.awt.image.BufferedImage createBufferedImage(File imageSrc, int width, int height) {

		BufferedImage img = null;
		try {
			img = ImageIO.read(imageSrc);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Constructs a BufferedImage of one of the predefined image types.
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// Create a graphics which can be used to draw into the buffered image
		Graphics2D g2d = bufferedImage.createGraphics();

		g2d.drawImage(img, 0, 0, null);

		// Disposes of this graphics context and releases any system resources that it
		// is using.
		g2d.dispose();

		// Save as PNG
		File file = new File("/Users/youssef/git/snapshot-utility/snapshot-utility/src/resources/test.png");
		try {
			ImageIO.write(bufferedImage, "png", file);
			// Save as JPEG
			file = new File("/Users/youssef/git/snapshot-utility/snapshot-utility/src/resources/test.jpg");
			ImageIO.write(bufferedImage, "jpg", file);
			return bufferedImage;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {

		}
	}

	public class CapturePane extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Rectangle selectionBounds;
		private Point clickPoint;

		public CapturePane() {
			setOpaque(false);

			MouseAdapter mouseHandler = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
						System.exit(0);
					}
				}

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

			addMouseListener(mouseHandler);
			addMouseMotionListener(mouseHandler);
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

	private void scheduleScreenGrab(ScheduledJob scheduledJob) {

		SelectedRectangle selectedRectangle = scheduledJob.getSelectedRectangle();
		int id = scheduledJob.getId();

		if (selectedRectangle.getWidth() <= 0 || selectedRectangle.getHeight() <= 0) {
			JOptionPane.showConfirmDialog(desktop, "rect width = " + selectedRectangle.getWidth()
					+ " . or rect height = " + selectedRectangle.getHeight());
			return;
		}
		String startTimeString = scheduledJob.getStartAt();
		String pattern = "E MMM dd HH:mm:ss zzz yyyy";
		DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
				// case insensitive to parse JAN and FEB
				.parseCaseInsensitive()
				// add pattern
				.appendPattern(pattern)
				// create formatter (use English Locale to parse month names)
				.toFormatter(Locale.ENGLISH);
		LocalDateTime startDateTime = LocalDateTime.parse(startTimeString, dateTimeFormatter);
		System.out.println("startDateTime = " + startDateTime);
		Date startAt = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
		String endTimeString = scheduledJob.getEndAt();
		LocalDateTime endDateTime = LocalDateTime.parse(endTimeString, dateTimeFormatter);
		Date endAt = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
		String periodicity = "00:" + scheduledJob.getPeriodicity();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.CANADA);
		LocalTime period = LocalTime.parse(periodicity, dtf);
		int secondOfDay = period.toSecondOfDay();
		int millis = secondOfDay * 1000;
		Timer timer = new Timer("Timer");
		ScreenshotGrabberTimerTask task = new ScreenshotGrabberTimerTask(id, getSettings(), selectedRectangle, timer,
				startAt, endAt, millis);
		screenshotGrabberTimerTaskList.add(task);
		task.setScreenshotEventListener(this);
//		task.setTimer(timer);

		System.out.println(
				"startAt = " + startAt + " . endAt = " + endAt + " . period = " + period + " . millis = " + millis);
//		timer.schedule(task, startAt, 24 * 60 * 60 * 1000L);
		timer.schedule(task, startAt, 120 * 1000L);

		// create new scheduled job and add to tree view .
		scheduledJob.setStatus(ScheduledJobStatus.SCHEDULED);
//		scheduledJobsList.add(scheduledJob);
//		updateScheduledJobTreeView(scheduledJob);
	}

	private void updateScheduledJobTreeView(ScheduledJob scheduledJob) {
		DefaultMutableTreeNode scheduledJobTreeNode = new DefaultMutableTreeNode(scheduledJob.getId());
		scheduledJobsMasterNode.add(scheduledJobTreeNode);
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		model.reload(top);
		tree.expandPath(tree.getSelectionPath());
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node == null)
			return;
		Object nodeInfo = node.getUserObject();
		if (centerPanel != null) {
			desktop.remove(centerPanel);
			centerPanel.setVisible(false);
		}
		if (node.isLeaf() && (nodeInfo instanceof Integer)) {
			int scheduledJobId = (Integer) nodeInfo;
			ScheduledJob scheduledJob = getScheduledJobById(scheduledJobId);
			centerPanel = createMiddlePanel(scheduledJob);
			centerPanel.setVisible(true);
			desktop.add(centerPanel, BorderLayout.CENTER);
		}
		desktop.revalidate();
//		SwingUtilities.updateComponentTreeUI(mainFrame);
//		mainFrame.invalidate();
//		mainFrame.validate();
		mainFrame.repaint();
	}

	private ScheduledJob getScheduledJobById(int scheduledJobId) {
		for (ScheduledJob scheduledJob : scheduledJobsList) {
			if (scheduledJobId == scheduledJob.getId()) {
				return scheduledJob;
			}
		}
		return null;
	}

	@Override
	public void didTakeScreenshot(ScreenshotEvent screenshotEvent) {
		System.out.println("SnapshotWindow class --> didTakeScreenshot method entred");

	}

	private void showBusyIcons() {

		if (busyIconTimer == null) {
			ActionListener actionListener = new ActionListener() {
				private int busyIconIndex;

				@Override
				public void actionPerformed(ActionEvent e) {
					busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
					if (iconStatusLabel != null)
						iconStatusLabel.setIcon(busyIcons[busyIconIndex]);
				}
			};
			busyIconTimer = new javax.swing.Timer(100, actionListener);
			busyIconTimer.setRepeats(true);
		}
		busyIconTimer.start();
	}

	private void stopBusyTimer() {
		if (busyIconTimer != null && busyIconTimer.isRunning()) {
			busyIconTimer.stop();
		}
	}

	@Override
	public void screenshotGrabberStarted(int id) {
		updateStatus(id, ScheduledJobStatus.RUNNING);
		// persist status to file
		Utilities.updateStatusForSettingId(id, ScheduledJobStatus.RUNNING);
	}

	@Override
	public void screenshotGrabberEnded(int id) {
		updateStatus(id, ScheduledJobStatus.ENDED);
		// persist status to file
		Utilities.updateStatusForSettingId(id, ScheduledJobStatus.ENDED);
	}

	private void updateStatus(int id, ScheduledJobStatus newStatus) {
		ScheduledJob scheduledJob = getScheduledJobById(id);
		if (scheduledJob == null)
			return;
		scheduledJob.setStatus(newStatus);
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (selectedNode == null)
			return;

		Object userObject = selectedNode.getUserObject();
		if (userObject instanceof String)
			return;

		Integer selectedId = (Integer) userObject;
		if (selectedId == id) {
			statusTextField.setText(newStatus.toString());

			Image image = getStatusIcon(scheduledJob);
			if (image != null) {
				busyIconTimer.stop();
				iconStatusLabel.setIcon(new ImageIcon(image));
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		doMouseClicked(e);

	}

	private void doMouseClicked(MouseEvent mouseEvent) {
		TreePath tp = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());

		if (SwingUtilities.isRightMouseButton(mouseEvent)) {
			DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) tp.getLastPathComponent();

			Object userObject = lastPathComponent.getUserObject();
			if (userObject instanceof Integer) {
				int id = (Integer) userObject;
				showPopupMenu(mouseEvent, id);
			}
		}
	}

	private void showPopupMenu(MouseEvent e, int id) {
		final JPopupMenu popupmenu = new JPopupMenu("Edit");
		JMenuItem removeJItemMenu = new JMenuItem("Remove");
		removeJItemMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ScreenshotGrabberTimerTask screenshotGrabberTimerTask = getScreenshotTaskById(id);
				if (screenshotGrabberTimerTask != null) {
					screenshotGrabberTimerTask.getScreenshotTimer().cancel();
					screenshotGrabberTimerTask.cancel();
					screenshotGrabberTimerTaskList.remove(screenshotGrabberTimerTask);
				}
				removeScheduledJobById(id);
				removeFromJTree(id);

			}

		});
		popupmenu.add(removeJItemMenu);
		popupmenu.show(mainFrame, e.getX(), e.getY() + 30);

	}

	protected ScreenshotGrabberTimerTask getScreenshotTaskById(int id) {
		for (Iterator<ScreenshotGrabberTimerTask> iterator = screenshotGrabberTimerTaskList.iterator(); iterator
				.hasNext();) {
			ScreenshotGrabberTimerTask screenshotGrabberTimerTask = iterator.next();
			if (screenshotGrabberTimerTask.getId() == id)
				return screenshotGrabberTimerTask;
		}
		return null;
	}

	protected void removeFromJTree(int id) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		TreePath[] paths = tree.getSelectionPaths();
		if (paths != null) {
			for (TreePath path : paths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (node.getParent() != null) {
					model.removeNodeFromParent(node);
				}
			}
		}
		model.reload(top);
		tree.expandPath(tree.getSelectionPath());
	}

	protected void removeScheduledJobById(int id) {
		int position = -1;
		for (Iterator<ScheduledJob> iterator = scheduledJobsList.iterator(); iterator.hasNext();) {
			ScheduledJob scheduledJob = (ScheduledJob) iterator.next();
			position++;
			if (scheduledJob.getId() == id) {
				// iterator.remove();
				break;
			}
		}
		scheduledJobsList.remove(position);
		settings.getSchedules().remove(position);
		// update setting file
		Utilities.persistSettings(settings);

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}
