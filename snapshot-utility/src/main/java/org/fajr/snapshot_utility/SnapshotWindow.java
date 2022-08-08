package org.fajr.snapshot_utility;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.fajr.snapshot_utility.event.ScreenshotEvent;
import org.fajr.snapshot_utility.event.listener.ScreenshotEventListener;
import org.opencv.core.Core;

public class SnapshotWindow
		implements ActionListener, TreeSelectionListener, ScreenshotEventListener, MouseListener, ItemListener {

	static Logger log = Logger.getLogger(SnapshotWindow.class.getName());

	private JDesktopPane desktop;
	private JFrame mainFrame;
	protected ScreenshotSessionInternalFrame screenshotSessionInternalFrame;
	private DefaultMutableTreeNode screenshotsNode;
	private DefaultMutableTreeNode scheduledJobsNode;
	private JTree tree;
	protected DefaultTreeModel treeModel;

	private JTextField statusTextField;

	private final Icon[] busyIcons = new Icon[15];
	private javax.swing.Timer busyIconTimer = null;
	private JLabel iconStatusLabel;
	private JTextArea messageTextArea = new JTextArea();
	private DefaultMutableTreeNode rootNode;
	private JPanel centerPanel;
	private ScheduledJobsList scheduledJobsList = new ScheduledJobsList();
	private List<GrabberTimerTask> grabberTimerTaskList = new ArrayList<GrabberTimerTask>();

	private Settings settings;
	private JCheckBoxMenuItem testMenuItem;
	private String title;

	private SettingsInternalFrame settingsInternalFrame;

	private JMenu streamMenu;
	private Map<Integer,DefaultMutableTreeNode> screenshotJobsNodeMap = new HashMap<Integer,DefaultMutableTreeNode>();

	private JMenuItem addIpCameraMenuItem;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

//	private DefaultMutableTreeNode ipCameraNode;

	public SnapshotWindow(Settings settings) {
		setSettings(settings);
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public Settings getSettings() {
		return settings;
	}

	public ScheduledJobsList getScheduledJobsList() {
		return scheduledJobsList;
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}

	public void createAndShowWindow() {

		initBusyIcons();
		setupLookAndFeel();
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();

		GraphicsDevice[] screens = graphicsEnvironment.getScreenDevices();
		Rectangle screenBounds = screens[0].getDefaultConfiguration().getBounds();

		title = "Snapshot Window";
		if (getSettings().getRunMode().equals("TEST")) {
			title += " : Running in TEST MODE";
		}
		mainFrame = new JFrame(title);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		desktop = new JDesktopPane(); // a specialized layered pane

		// Use the content pane's default BorderLayout. No need for
		desktop.setLayout(new BorderLayout());

		JPanel lineStartPanel = createLeftPanel();
		// Create the nodes.
		rootNode = new DefaultMutableTreeNode("Library");
		treeModel = new DefaultTreeModel(rootNode);
		treeModel.addTreeModelListener(new MyTreeModelListener());
		tree = new JTree(treeModel);
		tree.setEditable(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		createNodes();

		// Create a tree that allows one selection at a time.
		tree = new JTree(rootNode);
		tree.putClientProperty("JTree.lineStyle", "None");
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		tree.setCellRenderer(new TreeRenderer());
		// tree.addMouseListener(this);

		tree.putClientProperty("JTree.lineStyle", "Horizontal");
	    JScrollPane scrollPane = new JScrollPane(tree);
		lineStartPanel.add(scrollPane);
		desktop.add(lineStartPanel, BorderLayout.LINE_START);

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
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.META_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("New screenschot session");
		menuItem.setActionCommand("new-screenshot");
		menuItem.addActionListener(this);

		menu.add(menuItem);

		menuItem = new JMenuItem("Settings", new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_B);
		menu.add(menuItem);
		menuItem.setActionCommand("settings");
		menuItem.addActionListener(this);

		// Build the first menu.
		streamMenu = new JMenu("Stream");
		streamMenu.setMnemonic(KeyEvent.VK_C);
		streamMenu.getAccessibleContext().setAccessibleDescription("Add Stream");

		addIpCameraMenuItem = new JMenuItem("Add IP Camera", new ImageIcon("images/middle.gif"));
		addIpCameraMenuItem.setMnemonic(KeyEvent.VK_D);
		addIpCameraMenuItem.setActionCommand("add-ip-camera");
		addIpCameraMenuItem.addActionListener(this);
		streamMenu.add(addIpCameraMenuItem);

		menuBar.add(streamMenu);

		// test mode
		menu.addSeparator();
		testMenuItem = new JCheckBoxMenuItem("TEST mode");
		testMenuItem.setMnemonic(KeyEvent.VK_C);
		String runMode = getSettings().getRunMode();
		if (runMode != null && runMode.equals("TEST")) {
			testMenuItem.setSelected(true);
		} else {// Real mode

			testMenuItem.setSelected(false);
		}
		testMenuItem.addItemListener(this);
		menu.add(testMenuItem);

		iconStatusLabel = new JLabel();

		mainFrame.setJMenuBar(menuBar);

		// Display the window.
		mainFrame.pack();
		mainFrame.setSize(new Dimension(screenBounds.width / 2, screenBounds.height / 2));
		mainFrame.setLocationRelativeTo(null); // center it
		mainFrame.setVisible(true);

		// start screengrab/ipCameraGrab schedules if we have any schedules saved
		startSchedules();

	}

	private void setupLookAndFeel() {
		String osName = System.getProperty("os.name");
		if (osName != null && osName.equals("Mac OS X")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Stack");
		} else {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void startSchedules() {
		for (Iterator<ScheduledJob> iterator = scheduledJobsList.iterator(); iterator.hasNext();) {
			ScheduledJob scheduledJob = (ScheduledJob) iterator.next();
			if (scheduledJob.getStatus() == ScheduledJobStatus.SCHEDULED) {
				String clazz = scheduledJob.getClazz();
				if (clazz.equals(IpCameraScheduledJob.class.getName()))
					scheduleIpCameraScheduledJob(scheduledJob);
				else if (clazz.equals(ScreenGrabScheduledJob.class.getName()))
					scheduleScreenGrab(scheduledJob);
			} else if (scheduledJob.isRunPeriodically()) {
				updateStartTimeAndSchedule(scheduledJob);
			}
		}
	}

	private void scheduleIpCameraScheduledJob(ScheduledJob scheduledJob) {
		IpCameraScheduledJob ipCameraScheduledJob = (IpCameraScheduledJob) scheduledJob;
		String ipCameraURL = ipCameraScheduledJob.getIpCameraURL();
		int id = scheduledJob.getId();

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
		log.info("startDateTime = " + startDateTime);
		Date startAt = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
		String endTimeString = scheduledJob.getEndAt();
		LocalDateTime endDateTime = LocalDateTime.parse(endTimeString, dateTimeFormatter);
		Date endAt = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
		String periodicity = "00:" + scheduledJob.getPeriodicity();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.CANADA);
		LocalTime period = LocalTime.parse(periodicity, dtf);
		int secondOfDay = period.toSecondOfDay();
		int millis = secondOfDay * 1000;
		Timer timer = new Timer("ip-camera-grabber-timer");
		IpCameraGrabberTimerTask task = new IpCameraGrabberTimerTask(id, getSettings(), ipCameraURL, timer, startAt,
				endAt, millis);

		log.info("startAt = " + startAt + " . endAt = " + endAt + " . period = " + period + " . millis = " + millis);
		if (scheduledJob.isRunPeriodically()) {
			int interval = scheduledJob.getInterval();
			task.setRunPeriodically(true);

			if (getSettings().getRunMode().equals("TEST")) {
				task.setInterval(1);
				timer.schedule(task, startAt, 1 * 60 * 1000L);
			} else {
				task.setInterval(interval);
				timer.schedule(task, startAt, interval * 60 * 60 * 1000L);
			}
		} else {
			task.setRunPeriodically(false);
			timer.schedule(task, startAt);
		}

		grabberTimerTaskList.add(task);
		task.setScreenshotEventListener(this);

		// create new scheduled job and add to tree view .
		scheduledJob.setStatus(ScheduledJobStatus.SCHEDULED);

	}

	public void initBusyIcons() {

		for (int i = 0; i < busyIcons.length; i++) {
			ClassLoader classLoader = getClass().getClassLoader();
			URL resource = classLoader.getResource("busy-icons/busy-icon" + i + ".png");
			busyIcons[i] = new ImageIcon(resource);
		}
	}

	private void createNodes() {

//		screenshotsNode = new DefaultMutableTreeNode("Screenshots");
//		top.add(screenshotsNode);
//		scheduledJobsMasterNode = new DefaultMutableTreeNode("Scheduled Jobs");
//
//		List<ScheduledJob> schedules = settings.getSchedules();
//		if (schedules != null && schedules.size() != 0) {
//			addSchedulesToTreeView(schedules);
//		}
//		top.add(scheduledJobsMasterNode);

		String screenshotsName = new String("Screenshots");
		String scheduledJobsName = new String("Scheduled Jobs");


		screenshotsNode = addObject(rootNode, screenshotsName);
		scheduledJobsNode = addObject(rootNode, scheduledJobsName);

		List<ScheduledJob> schedules = settings.getSchedules();
		if (schedules != null && schedules.size() != 0) {
			addSchedulesToTreeView(schedules);
		}
	}

	/** Add child to the currently selected node. */
	public DefaultMutableTreeNode addObject(Object child) {
		DefaultMutableTreeNode parentNode = null;
		TreePath parentPath = tree.getSelectionPath();

		if (parentPath == null) {
			parentNode = rootNode;
		} else {
			parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
		}

		return addObject(parentNode, child, true);
	}

	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child) {
		 DefaultMutableTreeNode addObject = addObject(parent, child, true);
		 return addObject;
	}

	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

		if (parent == null) {
			parent = rootNode;
		}

		
		// It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
		log.info("childNode = "+childNode+ " .parent = "+parent +".  parent.getChildCount()= "+parent.getChildCount());
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
		treeModel.reload(rootNode);
		tree.expandPath(tree.getSelectionPath());

		if (shouldBeVisible) {
			log.info("childNode.getPath() = "+childNode.getPath());
			tree.makeVisible(new TreePath(childNode.getPath()));
			tree.scrollPathToVisible(new TreePath(childNode.getPath()));
			tree.updateUI();
		}
		return childNode;
	}

	private void addSchedulesToTreeView(List<ScheduledJob> schedules) {
		for (Iterator<ScheduledJob> iterator = schedules.iterator(); iterator.hasNext();) {
			ScheduledJob scheduledJob = iterator.next();
			addObject(scheduledJobsNode, scheduledJob.getId());
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

	private JPanel createMiddlePanel(ScheduledJob scheduledJob, String clazz) {

		Border blackline = BorderFactory.createLineBorder(Color.white);
		JPanel jPanel = new JPanel();
		jPanel.setLayout(null);
		jPanel.setBorder(blackline);

		int line = 1;

		JLabel statusLabel = new JLabel("Status");
//		Border redline = BorderFactory.createLineBorder(Color.red);
//		statusLabel.setBorder(redline);
		statusLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		statusLabel.setBounds(1, line, 100, 30);
		jPanel.add(statusLabel);

		statusTextField = new JTextField();
		statusTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		statusTextField.setBounds(135, line, 228, 30);
		jPanel.add(statusTextField);
		statusTextField.setColumns(10);
		statusTextField.setText(scheduledJob.getStatus().toString());
//		statusTextField.setText("Screenshot Job Scheduled");
		statusTextField.setEditable(false);

		Image image = getStatusIcon(scheduledJob);
		if (image != null)
			iconStatusLabel.setIcon(new ImageIcon(image));
		iconStatusLabel.setBounds(365, line, 25, 25);
//		iconStatusLabel.setBorder(redline);
		jPanel.add(iconStatusLabel);

		/////////////

		if (clazz.equals(IpCameraScheduledJob.class.getName())) {
			line += 30;
			JLabel urlLabel = new JLabel("URL");
			urlLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
			urlLabel.setBounds(1, line, 100, 30);
			jPanel.add(urlLabel);
			JTextField urlTextField = new JTextField();
			urlTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
			urlTextField.setBounds(135, line, 228, 30);
			urlTextField.setColumns(10);
			urlTextField.setText(((IpCameraScheduledJob) scheduledJob).getIpCameraURL());
			urlTextField.setEditable(false);
			jPanel.add(urlTextField);
		}

		////////
		line += 30;
		JLabel startAtLabel = new JLabel("Start Time");
//		startAtLabel.setBorder(redline);
		startAtLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		startAtLabel.setBounds(1, line, 100, 30);
		jPanel.add(startAtLabel);

		JTextField startAtTextField = new JTextField();
		startAtTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		startAtTextField.setBounds(135, line, 228, 30);
		startAtTextField.setColumns(10);
		startAtTextField.setText(scheduledJob.getStartAt().toString());
		startAtTextField.setEditable(false);
		jPanel.add(startAtTextField);

		////////
		line += 30;
		JLabel endAtLabel = new JLabel("End Time");
//		endAtLabel.setBorder(redline);
		endAtLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		endAtLabel.setBounds(1, line, 100, 30);
		jPanel.add(endAtLabel);

		JTextField endAtTextField = new JTextField();
		endAtTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		endAtTextField.setBounds(135, line, 228, 30);
		jPanel.add(endAtTextField);
		endAtTextField.setColumns(10);
		endAtTextField.setText(scheduledJob.getEndAt().toString());
		endAtTextField.setEditable(false);

		////////
		line += 30;
		JLabel periodicityLabel = new JLabel("Take screenshot every:");
//		periodicityLabel.setBorder(redline);
		periodicityLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		periodicityLabel.setBounds(1, line, 135, 30);
		jPanel.add(periodicityLabel);

		JTextField periodicityTextField = new JTextField();
		periodicityTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		periodicityTextField.setBounds(135, line, 228, 30);
		jPanel.add(periodicityTextField);
		periodicityTextField.setColumns(10);
		periodicityTextField.setText(scheduledJob.getPeriodicity() + "    (mm:ss)");
		periodicityTextField.setEditable(false);
		///////////////
		line += 30;
		JLabel runIntervalLabel = new JLabel();
//		periodicityLabel.setBorder(redline);
		runIntervalLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		runIntervalLabel.setBounds(1, line, 245, 30);
		if (scheduledJob.isRunPeriodically()) {
			if (getSettings().getRunMode().equals("TEST")) {
				runIntervalLabel.setText("This task run every " + scheduledJob.getInterval() + "minute.(test mode)");

			} else {
				runIntervalLabel.setText("This task run every " + scheduledJob.getInterval() + " hours.");

			}
		}
		jPanel.add(runIntervalLabel);

		// String classType = scheduledJob.getType();
		line += 30;
		messageTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		messageTextArea = new JTextArea();
		messageTextArea.setEditable(false);
		messageTextArea.setLineWrap(true);
		messageTextArea.setWrapStyleWord(true);
		messageTextArea.setColumns(20);
		messageTextArea.setRows(5);
		messageTextArea.setBackground(new Color(238, 238, 238));
		messageTextArea.setBounds(1, line, 380, 60);
		if (scheduledJob.isRunPeriodically()) {
			GrabberTimerTask screenshotGrabberTaskById = getGrabberTimerTaskById(scheduledJob.getId());
			if (screenshotGrabberTaskById != null) {
				Date startAt = screenshotGrabberTaskById.getStartAt();
				Date endAt = screenshotGrabberTaskById.getEndAt();
				messageTextArea.setText("Next Start Time :" + startAt + " . \n         End Time : " + endAt);
			}
		}
		Border borderline = BorderFactory.createLineBorder(Color.black);
		messageTextArea.setBorder(borderline);
		jPanel.add(messageTextArea);

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
		// log.info("getIconStatus methof entred :scheduledJob.getStatus() = " +
		// scheduledJob.getStatus());
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
			showSettingsWindow();
		} else if ("new-screenshot".equals(e.getActionCommand())) { // new
			startNewScreenshotSession();
		} else if ("add-ip-camera".equals(e.getActionCommand())) { // new
			showIpCameraSettingFrame();
		}
	}

	private void showIpCameraSettingFrame() {
		IpCameraSettingInternalFrame ipCameraSettingInternalFrame = new IpCameraSettingInternalFrame(mainFrame, desktop,
				centerPanel, settings, new Callback() {

					@Override
					public void taskTerminated(ScheduledJob scheduledJob, Settings settings) {

						setSettings(settings);
						scheduleIpCameraGrab(scheduledJob);
						scheduledJobsList.add(scheduledJob);
						updateScheduledJobTreeView(scheduledJob);
					}

					@Override
					public void taskTerminated(boolean success, String message, Settings settings) {

					}
				});
		ipCameraSettingInternalFrame.createAndShowGUI();
		desktop.add(ipCameraSettingInternalFrame, BorderLayout.CENTER);
		try {
			ipCameraSettingInternalFrame.setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			log.error(e.getMessage());
		}

	}

	protected void scheduleIpCameraGrab(ScheduledJob scheduledJob) {
		IpCameraScheduledJob ipCameraScheduledJob = (IpCameraScheduledJob) scheduledJob;
		String ipCameraURL = ipCameraScheduledJob.getIpCameraURL();
		int id = scheduledJob.getId();

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
		log.info("startDateTime = " + startDateTime);
		Date startAt = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
		String endTimeString = scheduledJob.getEndAt();
		LocalDateTime endDateTime = LocalDateTime.parse(endTimeString, dateTimeFormatter);
		Date endAt = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
		String periodicity = "00:" + scheduledJob.getPeriodicity();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.CANADA);
		LocalTime period = LocalTime.parse(periodicity, dtf);
		int secondOfDay = period.toSecondOfDay();
		int millis = secondOfDay * 1000;
		Timer timer = new Timer("ip-camera-grabber-timer");

//		ScreenshotGrabberTimerTask task = new ScreenshotGrabberTimerTask(id, getSettings(), selectedRectangle, timer,
//				startAt, endAt, millis);
		IpCameraGrabberTimerTask task = new IpCameraGrabberTimerTask(id, getSettings(), ipCameraURL, timer, startAt,
				endAt, millis);

		log.info("IpCameraGrabberTimerTask --> startAt = " + startAt + " . endAt = " + endAt + " . period = " + period
				+ " . millis = " + millis);
		if (scheduledJob.isRunPeriodically()) {
			int interval = scheduledJob.getInterval();
			task.setRunPeriodically(true);

			if (getSettings().getRunMode().equals("TEST")) {
				task.setInterval(1);
				timer.schedule(task, startAt, 1 * 60 * 1000L);
			} else {
				task.setInterval(interval);
				timer.schedule(task, startAt, interval * 60 * 60 * 1000L);
			}
		} else {
			task.setRunPeriodically(false);
			timer.schedule(task, startAt);
		}

		grabberTimerTaskList.add(task);
		task.setScreenshotEventListener(this);

		// create new scheduled job and add to tree view .
		scheduledJob.setStatus(ScheduledJobStatus.SCHEDULED);

	}

	private void showSettingsWindow() {

		if (settingsInternalFrame != null)
			return;

		settingsInternalFrame = new SettingsInternalFrame(desktop, getSettings());

		settingsInternalFrame.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				settingsInternalFrame = null;
			}
		});
		desktop.add(settingsInternalFrame);
		try {
			settingsInternalFrame.setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			log.error(e.getMessage());
		}
	}

	public JDesktopPane getDesktop() {
		return desktop;
	}

	protected void startNewScreenshotSession() {

		if (screenshotSessionInternalFrame != null)
			return;
		screenshotSessionInternalFrame = new ScreenshotSessionInternalFrame(mainFrame, desktop, centerPanel, settings,
				new Callback() {

					@Override
					public void taskTerminated(ScheduledJob scheduledJob, Settings settings) {
						log.info("taskTerminated with callback---> scheduledJob = " + scheduledJob);
						setSettings(settings);
						scheduleScreenGrab(scheduledJob);
						scheduledJobsList.add(scheduledJob);
						updateScheduledJobTreeView(scheduledJob);
					}

					@Override
					public void taskTerminated(boolean success, String message, Settings settings) {

					}
				});
		screenshotSessionInternalFrame.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				screenshotSessionInternalFrame = null;
				// SettingsInternalFrame settingsInternalFrame = (SettingsInternalFrame)
				// e.getSource();
				// ScheduledJob scheduledJob = settingsInternalFrame.getScheduledJob();
			}
		});
		desktop.add(screenshotSessionInternalFrame);
		try {
			screenshotSessionInternalFrame.setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			log.error(e.getMessage());
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

	private void updateStartTimeAndSchedule(ScheduledJob scheduledJob) {
		GrabberTimerTask task = null;
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
		log.info("startDateTime = " + startDateTime);
		Date startAt = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
		String endTimeString = scheduledJob.getEndAt();
		LocalDateTime endDateTime = LocalDateTime.parse(endTimeString, dateTimeFormatter);
		Date endAt = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

		Date now = new Date();
		int interval = 0;
		if (getSettings().getRunMode().equals("TEST")) {
			interval = 60;
		} else {
			interval = scheduledJob.getInterval() * 60 * 60;
		}
		boolean b = true;
		while (b) {
			startAt = DateTimeUtils.addSecondsToDate(startAt, interval);
			endAt = DateTimeUtils.addSecondsToDate(endAt, interval);
			b = now.compareTo(startAt) > 0 || now.compareTo(startAt) == 0;
		}

		String periodicity = "00:" + scheduledJob.getPeriodicity();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.CANADA);
		LocalTime period = LocalTime.parse(periodicity, dtf);
		int secondOfDay = period.toSecondOfDay();
		int millis = secondOfDay * 1000;
		Timer timer = new Timer("Timer");
		String clazz = scheduledJob.getClazz();
		if (clazz.equals(IpCameraScheduledJob.class.getName()))
			task = new IpCameraGrabberTimerTask(scheduledJob.getId(), getSettings(),
					((IpCameraScheduledJob) scheduledJob).getIpCameraURL(), timer, startAt, endAt, millis);
		else if (clazz.equals(ScreenGrabScheduledJob.class.getName())) {
			task = new ScreenshotGrabberTimerTask(scheduledJob.getId(), getSettings(),
					scheduledJob.getSelectedRectangle(), timer, startAt, endAt, millis);
		}

		log.info("startAt = " + startAt + " . endAt = " + endAt + " . period = " + period + " . millis = " + millis);
//		if (scheduledJob.isRunPeriodically()) {
		// interval = scheduledJob.getInterval();
//			task.setRunPeriodically(true);

		if (getSettings().getRunMode().equals("TEST")) {
			task.setInterval(1);
			timer.schedule(task, startAt, 1 * 60 * 1000L);
		} else {
			task.setInterval(interval);
			timer.schedule(task, startAt, interval * 60 * 60 * 1000L);
		}
//		} else {
//			task.setRunPeriodically(false);
//			timer.schedule(task, startAt);
//		}

		grabberTimerTaskList.add(task);
		task.setScreenshotEventListener(this);

		// create new scheduled job and add to tree view .
		scheduledJob.setStatus(ScheduledJobStatus.SCHEDULED);

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
		log.info("startDateTime = " + startDateTime);
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

		log.info("startAt = " + startAt + " . endAt = " + endAt + " . period = " + period + " . millis = " + millis);
		if (scheduledJob.isRunPeriodically()) {
			int interval = scheduledJob.getInterval();
			task.setRunPeriodically(true);

			if (getSettings().getRunMode().equals("TEST")) {
				task.setInterval(1);
				timer.schedule(task, startAt, 1 * 60 * 1000L);
			} else {
				task.setInterval(interval);
				timer.schedule(task, startAt, interval * 60 * 60 * 1000L);
			}
		} else {
			task.setRunPeriodically(false);
			timer.schedule(task, startAt);
		}

		grabberTimerTaskList.add(task);
		task.setScreenshotEventListener(this);

		// create new scheduled job and add to tree view .
		scheduledJob.setStatus(ScheduledJobStatus.SCHEDULED);
	}

	private void updateScheduledJobTreeView(ScheduledJob scheduledJob) {
		
		addObject(scheduledJobsNode, scheduledJob.getId());
	}

//	private void updateIpCameraScheduledJobTreeView(ScheduledJob scheduledJob) {
//		DefaultMutableTreeNode scheduledJobTreeNode = new DefaultMutableTreeNode(scheduledJob.getId());
//		ipCameraNode.add(scheduledJobTreeNode);
//		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
//		model.reload(top);
//		tree.expandPath(tree.getSelectionPath());
//	}

	private void updateScreenshotsTreeView(ScreenshotEvent screenshotEvent) {
		int scheduledJobId = screenshotEvent.getScheduledJobId();
		DefaultMutableTreeNode screenshotParentNode = screenshotJobsNodeMap.get(scheduledJobId);
		//DefaultMutableTreeNode screenshotParentNode = null;
		if(screenshotParentNode == null) {
			screenshotParentNode = addObject(screenshotsNode,screenshotEvent.getScheduledJobId());
			screenshotJobsNodeMap.put(scheduledJobId, screenshotParentNode);
		}
		addObject(screenshotParentNode, screenshotEvent.getScreenshotFile().getName());
		
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
			String clazz = scheduledJob.getClazz();
			centerPanel = createMiddlePanel(scheduledJob, clazz);

			centerPanel.setVisible(true);
			desktop.add(centerPanel, BorderLayout.CENTER);
		} else if (node.isLeaf() && (nodeInfo instanceof String)) {
			String fileName = (String) nodeInfo;

			if (fileName.equals("Screenshots")) {
				return;
			}
			if (fileName.equals("Scheduled Jobs")) {
				return;
			}
			if (fileName.equals("IP Cameras")) {
				return;
			}
			centerPanel = createMiddlePanel(fileName);
			centerPanel.setVisible(true);
			desktop.add(centerPanel, BorderLayout.CENTER);
		}
		desktop.revalidate();
		mainFrame.repaint();
	}

	private JPanel createMiddlePanel(String screenshotFileName) {
		Border whiteLine = BorderFactory.createLineBorder(Color.white);
		JPanel jPanel = new JPanel();
		jPanel.setLayout(null);

		File screenshotFile = new File(getSettings().getScreenshotsFolder() + File.separator + "Fajr Screenshots",
				screenshotFileName);

		BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(screenshotFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		int width = myPicture.getWidth() / 2;
		int height = myPicture.getHeight() / 2;
		picLabel.setBounds(1, 1, width, height);
		picLabel.setBorder(whiteLine);
		jPanel.add(picLabel);
		return jPanel;
	}

	private ScheduledJob getScheduledJobById(int scheduledJobId) {
		for (ScheduledJob scheduledJob : scheduledJobsList) {
			if (scheduledJobId == scheduledJob.getId()) {
				return scheduledJob;
			}
		}
		return null;
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
		updateStatus(id, ScheduledJobStatus.RUNNING, null, null);
		// persist status to file
		Utilities.updateStatusForScheduleId(id, ScheduledJobStatus.RUNNING);
	}

	@Override
	public void screenshotGrabberEnded(int id, Date nextStartAt, Date nextEndAt) {
		updateStatus(id, ScheduledJobStatus.ENDED, nextStartAt, nextEndAt);
		// persist status to file
		Utilities.updateStatusForScheduleId(id, ScheduledJobStatus.ENDED);
	}

	private void updateStatus(int id, ScheduledJobStatus newStatus, Date nextStartAt, Date nextEndAt) {
		log.info(Thread.currentThread().getName());
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					updateStatus(id, newStatus, nextStartAt, nextEndAt);

				}
			});

		} else {
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

				if (nextStartAt != null && nextEndAt != null && scheduledJob.isRunPeriodically()) {
					messageTextArea
							.setText("Next Start Time :" + nextStartAt + " . \n           Ends at : " + nextEndAt);
				}
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

	private void showPopupMenu(MouseEvent e, final int id) {
		final JPopupMenu popupmenu = new JPopupMenu("Edit");
		JMenuItem removeJItemMenu = new JMenuItem("Remove");
		removeJItemMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				GrabberTimerTask grabberTimerTask = getGrabberTimerTaskById(id);
				if (grabberTimerTask != null) {
					Timer screenshotTimer = grabberTimerTask.getTimer();
					if (screenshotTimer != null)
						screenshotTimer.cancel();
					grabberTimerTask.cancel();
					grabberTimerTaskList.remove(grabberTimerTask);
				}
				removeScheduledJobById(id);
				removeFromJTree(id);

			}

		});
		popupmenu.add(removeJItemMenu);
		popupmenu.show(mainFrame, e.getX(), e.getY() + 30);

	}

	public List<GrabberTimerTask> getGrabberTimerTaskList() {
		return grabberTimerTaskList;
	}

	protected GrabberTimerTask getGrabberTimerTaskById(int id) {
		for (Iterator<GrabberTimerTask> iterator = grabberTimerTaskList.iterator(); iterator.hasNext();) {
			GrabberTimerTask grabberTimerTask = iterator.next();
			if (grabberTimerTask.getId() == id)
				return grabberTimerTask;
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
		model.reload(rootNode);
		tree.expandPath(tree.getSelectionPath());
		if (centerPanel != null) {
			desktop.remove(centerPanel);
			centerPanel.setVisible(false);
		}
		desktop.revalidate();
		mainFrame.repaint();
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

	@Override
	public void itemStateChanged(ItemEvent e) {
		title = "Snapshot Window";
		if (testMenuItem.isSelected()) {
			title += " : Running in TEST MODE";
			getSettings().setRunMode("TEST");
		} else {
			getSettings().setRunMode("REAL");
		}
		mainFrame.setTitle(title);
		boolean runMode = testMenuItem.isSelected();
		Utilities.updateRunModeStatus(runMode);

	}

	@Override
	public void screenshotGrabberDidTakeScreenshot(ScreenshotEvent screenshotEvent) {
		updateScreenshotsTreeView(screenshotEvent);
	}

	@Override
	public void screenshotGrabberEndedWithException(String message, int id, Date startAt, Date endAt) {
		// TODO Auto-generated method stub
		log.info("job id=" + id + message);

	}

	class TreeRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

			Object userObject = node.getUserObject();
			if (userObject instanceof Integer) {
				ScheduledJob scheduledJob = getScheduledJobById((int) userObject);
				if (scheduledJob.getClazz().equals(IpCameraScheduledJob.class.getName())) {
					ImageIcon cameraImageIcon = getImageIcon("camera.jpg");
					this.setIcon(cameraImageIcon);
				} else if (scheduledJob.getClazz().equals(ScreenGrabScheduledJob.class.getName())) {
					ImageIcon screenshotImageIcon = getImageIcon("screenshot.png");
					this.setIcon(screenshotImageIcon);
				}
			}
			return this;
		}

		ImageIcon getImageIcon(String iconName) {
			ClassLoader classLoader = getClass().getClassLoader();
			URL resource = classLoader.getResource(iconName);
			ImageIcon cameraIcon = new ImageIcon(resource);
			Image image = cameraIcon.getImage();
			Image newimg = image.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
			ImageIcon imageIcon = new ImageIcon(newimg);
			return imageIcon;
		}
	}

	class MyTreeModelListener implements TreeModelListener {
		public void treeNodesChanged(TreeModelEvent e) {
			DefaultMutableTreeNode node;
			node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());

			/*
			 * If the event lists children, then the changed node is the child of the node
			 * we've already gotten. Otherwise, the changed node and the specified node are
			 * the same.
			 */

			int index = e.getChildIndices()[0];
			node = (DefaultMutableTreeNode) (node.getChildAt(index));

			System.out.println("The user has finished editing the node.");
			System.out.println("New value: " + node.getUserObject());
		}

		public void treeNodesInserted(TreeModelEvent e) {
		}

		public void treeNodesRemoved(TreeModelEvent e) {
		}

		public void treeStructureChanged(TreeModelEvent e) {
		}
	}

}
