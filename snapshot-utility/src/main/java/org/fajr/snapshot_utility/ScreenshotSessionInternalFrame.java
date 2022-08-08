package org.fajr.snapshot_utility;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.apache.commons.lang3.RandomUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/* Used by InternalFrameDemo.java. */
public class ScreenshotSessionInternalFrame extends JInternalFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7048460840933746682L;
	static Logger log = Logger.getLogger(ScreenshotSessionInternalFrame.class.getName());


	static final int xOffset = 30, yOffset = 30;
	JDesktopPane desktopPane;
	private JTextField outputFolderTextField;
	private JSpinner startTimeSpinner;
	private JSpinner endTimeSpinner;
	private JSpinner minutesSecondsTimeSpinner;

	private SelectedRectangle selectedRectangle;
	private Settings settings;

	private ScheduledJob scheduledJob;
	private Callback callback;

	private JPanel contentPane;

//	private JLabel selectedRectangleLabel;

	private JTextArea selectedRectangleTextArea;

	private JSpinner hoursSpinner;

	private JCheckBox runTaskPeriodicallyCheckBox;
	

	public ScreenshotSessionInternalFrame(JFrame mainFrame, JDesktopPane desktop, JPanel centerPanel, Settings settings, Callback callback) {
		
		super("New Screenshot Session" , false, // resizable
				true, // closable
				false, // maximizable
				false);// iconifiable
		String  title = null;
		setSettings(settings);
		setCallback(callback);
		if(getSettings().getRunMode().equals("TEST")) {
			title = " : RUNNING IN TEST MODE";
		}else {
			title="";
		}
		
		setTitle("New Screenshot Session" +  title);

		this.desktopPane = desktop;
		Dimension desktopSize = desktopPane.getSize();
		Dimension jInternalFrameSize = getSize();
		
		int width2 = (int) ((desktopSize.width - jInternalFrameSize.width) / 1.3);
		int height2 = (int) (desktopSize.height / 1.3);
		setBounds(185, 25, width2, height2);
		setResizable(false);
		setMaximumSize(new Dimension(width2, height2));

		createGUI();

		addInternalFrameListener(new InternalFrameAdapter() {

			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				dispose();
			}
		});
		setVisible(true);
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public Settings getSettings() {
		return settings;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public Callback getCallback() {
		return callback;
	}

	private void createGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setResizable(false);
		contentPane = new JPanel();
//		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		Border border = BorderFactory.createLineBorder(Color.BLUE, 5);
		
		int line = 10; 

		JLabel outputFolderLabel = new JLabel("Output Folder");
		outputFolderLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		outputFolderLabel.setBounds(5, line, 100, 30);
		contentPane.add(outputFolderLabel);

		outputFolderTextField = new JTextField();
		outputFolderTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		outputFolderTextField.setBounds(200, line, 228, 30);
		contentPane.add(outputFolderTextField);
		outputFolderTextField.setColumns(10);
		String screenshotsFolder = getSettings().getScreenshotsFolder();
		if (screenshotsFolder != null) {
			outputFolderTextField.setText(screenshotsFolder);
		}

		JButton browseButton = new JButton("Browse...");
		browseButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		browseButton.setBounds(435, line, 100, 30);
		browseButton.setActionCommand("browse");
		browseButton.addActionListener(this);
		contentPane.add(browseButton);
		
		line+=30;

		JLabel startTimeLabel = new JLabel("Start Time");
		startTimeLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		startTimeLabel.setBounds(5, line, 100, 30);
		contentPane.add(startTimeLabel);

		startTimeSpinner = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(startTimeSpinner, "dd-MM-yyyy HH:mm:ss");
		startTimeSpinner.setEditor(timeEditor);
		((DefaultEditor) startTimeSpinner.getEditor()).getTextField().setEditable(false);
		startTimeSpinner.setBounds(200, line, 170, 30);
		contentPane.add(startTimeSpinner);
		line+=30;
		JLabel endTimeLabel = new JLabel("End Time");
		endTimeLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		endTimeLabel.setBounds(5, line, 200, 30);
		contentPane.add(endTimeLabel);

		endTimeSpinner = new JSpinner(new SpinnerDateModel());
		timeEditor = new JSpinner.DateEditor(endTimeSpinner, "dd-MM-yyyy HH:mm:ss");
		endTimeSpinner.setEditor(timeEditor);
		((DefaultEditor) endTimeSpinner.getEditor()).getTextField().setEditable(false);

		endTimeSpinner.setBounds(200, line, 170, 30);
		contentPane.add(endTimeSpinner);
		line+=30;

		JLabel everySecondLabel = new JLabel("Take screenshots every (mm:ss) ");
		everySecondLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		everySecondLabel.setBounds(5, line, 200, 30);
		contentPane.add(everySecondLabel);

		minutesSecondsTimeSpinner = new JSpinner(new SpinnerDateModel());
		timeEditor = new JSpinner.DateEditor(minutesSecondsTimeSpinner, "mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 5);
		Date date = cal.getTime();

		minutesSecondsTimeSpinner.setValue(date);
		minutesSecondsTimeSpinner.setEditor(timeEditor);
		((DefaultEditor) minutesSecondsTimeSpinner.getEditor()).getTextField().setEditable(false);

		minutesSecondsTimeSpinner.setBounds(200, line, 100, 30);
		contentPane.add(minutesSecondsTimeSpinner);
		line+=30;
		JTextArea label = new JTextArea("Click and drag to select a rectangle that you want to save and hit <ENTER> ");
//		label.setBorder(border);
		label.setEditable(false);
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		label.setColumns(20);
		label.setRows(5);
		label.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		label.setBounds(5, line, 195, 50);
		label.setBackground(new Color(238, 238, 238));
		contentPane.add(label);

		JButton selectRectangleButton = new JButton("Select Rectangle");
		selectRectangleButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		selectRectangleButton.setBounds(200, line, 155, 35);
		selectRectangleButton.setActionCommand("select-rectangle");
		selectRectangleButton.addActionListener(this);
		contentPane.add(selectRectangleButton);

		selectedRectangleTextArea = new JTextArea();
//		selectedRectangleTextArea.setBorder(border);
		selectedRectangleTextArea.setEditable(false);
		selectedRectangleTextArea.setLineWrap(true);
		selectedRectangleTextArea.setWrapStyleWord(true);
		selectedRectangleTextArea.setColumns(20);
		selectedRectangleTextArea.setRows(5);
		selectedRectangleTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		selectedRectangleTextArea.setBounds(365, line, 200, 100);
		selectedRectangleTextArea.setBackground(new Color(238, 238, 238));
		contentPane.add(selectedRectangleTextArea);

		if(getSettings().getRunMode().equals("TEST")) {
			runTaskPeriodicallyCheckBox = new JCheckBox("TEST MODE: Run every minute");
			
		}else {
			runTaskPeriodicallyCheckBox = new JCheckBox("Run this Task Periodically:");
		}
		
		runTaskPeriodicallyCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (runTaskPeriodicallyCheckBox.isSelected()) {
					hoursSpinner.setEnabled(true);
				} else {
					hoursSpinner.setEnabled(false);
				}
			}
		});
		
		line+=100;
		runTaskPeriodicallyCheckBox.setBorder(border);
		runTaskPeriodicallyCheckBox.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		runTaskPeriodicallyCheckBox.setBounds(5, line, 230, 30);
		contentPane.add(runTaskPeriodicallyCheckBox);

		SpinnerNumberModel m_numberSpinnerModel;
		Integer current = Integer.valueOf(1);
		Integer min = Integer.valueOf(1);
		Integer max = Integer.valueOf(24);
		Integer step = Integer.valueOf(1);
		m_numberSpinnerModel = new SpinnerNumberModel(current, min, max, step);
		
		hoursSpinner = new JSpinner(m_numberSpinnerModel);
		hoursSpinner.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		hoursSpinner.setBounds(235, line, 55, 30);
		((DefaultEditor) hoursSpinner.getEditor()).getTextField().setEditable(false);

		border = BorderFactory.createLineBorder(Color.RED, 2);
//	    m_numberSpinner.setBorder(border);
		hoursSpinner.setEnabled(false);
		contentPane.add(hoursSpinner);

		JLabel hoursLabel = new JLabel("hours");
//		runningJobPeriodicallyLabel.setBorder(border);
		hoursLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		hoursLabel.setBounds(295, line, 65, 30);
		contentPane.add(hoursLabel);
		
		line+=65;

		JButton saveButton = new JButton("Save");
		saveButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		saveButton.setBounds(450, line, 100, 30);
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		contentPane.add(saveButton);
	}

	private void startNewScrrenshotSession() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource(Preferences.CURSOR_FILE);
		Image image = toolkit.getImage(resource);
		Cursor cursor = toolkit.createCustomCursor(image, new Point(0, 0), "png");
		FullScreenGrabberJFrame fullScreenGrabberInternalFrame = new FullScreenGrabberJFrame(this, null);
		fullScreenGrabberInternalFrame.setCursor(cursor);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		switch (command) {
		case "save":

			String outputFolder = outputFolderTextField.getText();
			if (outputFolder == null || outputFolder.trim().length() == 0) {
				JOptionPane.showMessageDialog(desktopPane, "Select output folder for screenshots!");
				return;
			}
			Date now = new Date();
			Date startDate = (Date) startTimeSpinner.getValue();
			Date endDate = (Date) endTimeSpinner.getValue();
			Date periodicity = (Date) minutesSecondsTimeSpinner.getValue();
			if (startDate.before(now)) {
				JOptionPane.showMessageDialog(desktopPane, "Invalid start time: start time is in the past!");
				return;
			}

			if (endDate.before(now)) {
				JOptionPane.showMessageDialog(desktopPane, "Invalid end time: end time is in the past!");
				return;
			}

			if (endDate.before(startDate)) {
				JOptionPane.showMessageDialog(desktopPane, "Invalid end time: end time is before start time!");
				return;
			}

			if (periodicity.getMinutes() == 0 && periodicity.getSeconds() == 0) {
				JOptionPane.showMessageDialog(desktopPane, "00:00 is invalid value.");
				return;
			}

			if (selectedRectangle == null) {
				JOptionPane.showMessageDialog(desktopPane,
						"Drag your mouse to select screen rectangle to capture, then hit <Enter>");
				return;
			}

			File settingsFile = new File(Preferences.APPLICATION_SETTINGS_FOLDER, Preferences.SETTINGS_FILE);

			if (settingsFile.exists()) {
				log.info(settingsFile + " already exists");
			} else
				try {
					log.info(settingsFile + " doesn't exists. trying to create one...");
					if (settingsFile.createNewFile()) {
						log.info(settingsFile + " was created");
					} else {
						log.info(settingsFile + " was not created");
					}
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

			saveSchedule();

			break;

		case "select-rectangle":
			startNewScrrenshotSession();
			break;

		case "browse":
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fileChooser.showOpenDialog(desktopPane);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				outputFolderTextField.setText(fileChooser.getSelectedFile().getAbsoluteFile().toString());
			}
			break;

		case "close":
			try {
				setClosed(true);
			} catch (PropertyVetoException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;

		default:
			break;
		}

	}

	public ScheduledJob getScheduledJob() {
		return scheduledJob;
	}

	public void setScheduledJob(ScheduledJob scheduledJob) {
		this.scheduledJob = scheduledJob;
	}

	private void saveSchedule() {

//		File jsonSettingFile = new File(Preferences.APPLICATION_SETTINGS_FOLDER, Preferences.SETTINGS_FILE);
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Settings settings;
//		String settingsJson = null;
		settings = getSettings();

		if (settings == null)
			settings = new Settings();

		List<ScheduledJob> scheduledJobList = settings.getSchedules();

		if (scheduledJobList == null)
			scheduledJobList = new ArrayList<>();

		String screenshotOutputFolder = outputFolderTextField.getText();
		settings.setScreenshotsFolder(screenshotOutputFolder);
		int id = RandomUtils.nextInt();
		Date startDate = (Date) startTimeSpinner.getValue();
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date endDate = (Date) endTimeSpinner.getValue();
		Date periodicity = (Date) minutesSecondsTimeSpinner.getValue();
		String period = null;
		if (periodicity instanceof Date) {
			format = new SimpleDateFormat("mm:ss");
			period = format.format(periodicity);
		}

		boolean runTaskPeriodically = runTaskPeriodicallyCheckBox.isSelected();
		int taskRunInterval = (Integer) hoursSpinner.getValue();

		scheduledJob = new ScreenGrabScheduledJob(id, startDate.toString(), endDate.toString(), period, getSelectedRectangle(),
				runTaskPeriodically, taskRunInterval);
		scheduledJob.setStatus(ScheduledJobStatus.SCHEDULED);
		scheduledJobList.add(scheduledJob);
		settings.setSchedules(scheduledJobList);
		Utilities.persistSettings(settings);
		getCallback().taskTerminated(scheduledJob, settings);
		dispose();
	}

	public void screenRectangleSelected(SelectedRectangle selectedRectangle, Object object) {
		setSelectedRectangle(selectedRectangle);
		String text = "Selected Rectangle :\n X = " + selectedRectangle.getX() + " .\n Y = " + selectedRectangle.getY()
				+ " .\n Width = " + selectedRectangle.getWidth() + " .\n Height = " + selectedRectangle.getHeight();
		selectedRectangleTextArea.setText(text);

	}

	public void setSelectedRectangle(SelectedRectangle selectedRectangle) {
		this.selectedRectangle = selectedRectangle;
	}

	public SelectedRectangle getSelectedRectangle() {
		return selectedRectangle;
	}
}
