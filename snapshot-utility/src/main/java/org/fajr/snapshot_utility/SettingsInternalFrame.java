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

import javax.swing.JButton;
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
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.apache.commons.lang3.RandomUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/* Used by InternalFrameDemo.java. */
public class SettingsInternalFrame extends JInternalFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7048460840933746682L;

	static int openFrameCount = 0;
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

	public SettingsInternalFrame(JDesktopPane desktop, Settings settings, Callback callback) {
		super("Settings" + (++openFrameCount), false, // resizable
				true, // closable
				false, // maximizable
				false);// iconifiable

		setSettings(settings);
		setCallback(callback);
		// ...Create the GUI and put it in the window...
		this.desktopPane = desktop;
		Dimension desktopSize = desktopPane.getSize();
		Dimension jInternalFrameSize = getSize();

		int width2 = (int) ((desktopSize.width - jInternalFrameSize.width) / 1.5);
		int height2 = (int) (desktopSize.height / 1.5);
		setBounds(150, 150, width2,	height2);
		setResizable(false);
		setMaximumSize(new Dimension(width2,height2));

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
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

//		Border border = BorderFactory.createLineBorder(Color.BLUE, 5);

		// set the border of this component

		JLabel outputFolderLabel = new JLabel("Output Folder");
		outputFolderLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		outputFolderLabel.setBounds(5, 10, 100, 30);
//		outputFolderLabel.setBorder(border);
		contentPane.add(outputFolderLabel);

		outputFolderTextField = new JTextField();
		outputFolderTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		outputFolderTextField.setBounds(200, 10, 228, 30);
		contentPane.add(outputFolderTextField);
		outputFolderTextField.setColumns(10);
		String screenshotsFolder = getSettings().getScreenshotsFolder();
		if (screenshotsFolder != null) {
			outputFolderTextField.setText(screenshotsFolder);
		}
		// outputFolderTextField.setText("/Users/youssef/Pictures");

		JButton browseButton = new JButton("Browse...");
		browseButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		browseButton.setBounds(435, 10, 100, 30);
		browseButton.setActionCommand("browse");
		browseButton.addActionListener(this);
		contentPane.add(browseButton);

		JLabel startTimeLabel = new JLabel("Start Time");
		startTimeLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		startTimeLabel.setBounds(5, 40, 100, 30);
		contentPane.add(startTimeLabel);

		startTimeSpinner = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(startTimeSpinner, "dd-MM-yyyy HH:mm:ss");
		startTimeSpinner.setEditor(timeEditor);
//		if (FajrAppSettings.startTime != null) {
//
//				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
//				try {
//					Date startAt = format.parse(FajrAppSettings.startTime );
//					startTimeSpinner.setValue(startAt);
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}
//		}else {
//			startTimeSpinner.setValue(new Date());
//		}

		startTimeSpinner.setBounds(200, 40, 170, 30);
		contentPane.add(startTimeSpinner);

		JLabel endTimeLabel = new JLabel("End Time");
		endTimeLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		endTimeLabel.setBounds(5, 70, 200, 30);
		contentPane.add(endTimeLabel);

		endTimeSpinner = new JSpinner(new SpinnerDateModel());
		timeEditor = new JSpinner.DateEditor(endTimeSpinner, "dd-MM-yyyy HH:mm:ss");
		endTimeSpinner.setEditor(timeEditor);
//		if (FajrAppSettings.endTime  != null) {
//			
//
//				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
//				try {
//					Date endAt = format.parse(FajrAppSettings.endTime);
//					endTimeSpinner.setValue(endAt);
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}
//		}else {
//			endTimeSpinner.setValue(new Date());
//		}

		endTimeSpinner.setBounds(200, 70, 170, 30);
		contentPane.add(endTimeSpinner);

		JLabel everySecondLabel = new JLabel("Take screenshots every (mm:ss) ");
		everySecondLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		everySecondLabel.setBounds(5, 100, 200, 30);
		contentPane.add(everySecondLabel);

		minutesSecondsTimeSpinner = new JSpinner(new SpinnerDateModel());
		timeEditor = new JSpinner.DateEditor(minutesSecondsTimeSpinner, "mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 5);
		Date date = cal.getTime();

//		if (FajrAppSettings.periodicity != null) {
//			
//
//				SimpleDateFormat format = new SimpleDateFormat("mm:ss");
//				try {
//					Date period = format.parse(FajrAppSettings.periodicity);
//					minutesSecondsTimeSpinner.setValue(period);
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}
//		}else {
		minutesSecondsTimeSpinner.setValue(date);
//		}

		minutesSecondsTimeSpinner.setEditor(timeEditor);
		minutesSecondsTimeSpinner.setBounds(200, 100, 100, 30);
		contentPane.add(minutesSecondsTimeSpinner);

		JTextArea label = new JTextArea("Click and drag to create a rectangle that you want to save ");
//		label.setBorder(border);
		label.setEditable(false);
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		label.setColumns(20);
		label.setRows(5);
		label.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		label.setBounds(5, 150, 200, 40);
		label.setBackground(new Color(238, 238, 238));
		contentPane.add(label);
		
		

		JButton selectRectangleButton = new JButton("Select Rectangle");
		selectRectangleButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		selectRectangleButton.setBounds(200, 150, 155, 35);
		selectRectangleButton.setActionCommand("select-rectangle");
		selectRectangleButton.addActionListener(this);
		contentPane.add(selectRectangleButton);
		

		JButton saveButton = new JButton("Save");
		saveButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		saveButton.setBounds(300, 225, 100, 30);
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		contentPane.add(saveButton);
	}

	private void startNewScrrenshotSession() {
		//
		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource(Preferences.cursorFileName);
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
			if(startDate.before(now) ) {
				JOptionPane.showMessageDialog(desktopPane, "Invalid start time: start time is in the past!");
				return;
			}
			
			if(endDate.before(now)) {
				JOptionPane.showMessageDialog(desktopPane, "Invalid end time: end time is in the past!");
				return;
			}

			if(endDate.before(startDate)){
				JOptionPane.showMessageDialog(desktopPane, "Invalid end time: end time is before start time!");
				return;
			}
			
			if(periodicity.getMinutes()==0&&periodicity.getSeconds()==0) {
				JOptionPane.showMessageDialog(desktopPane, "00:00 is invalid value.");
				return;
			}
			
			if(selectedRectangle == null) {
				JOptionPane.showMessageDialog(desktopPane, "Drag your mouse to select screen rectangle to capture, then hit <Enter>");
				return;
			}

			
			

			File settingsFile = new File(Preferences.applicationSettingsFolder, Preferences.settingsFile);

			if (settingsFile.exists()) {
				System.out.println(settingsFile + " already exists");
			} else
				try {
					System.out.println(settingsFile + " doesn't exists. trying to create one...");
					if (settingsFile.createNewFile()) {
						System.out.println(settingsFile + " was created");
					} else {
						System.out.println(settingsFile + " was not created");
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

		File jsonSettingFile = new File(Preferences.applicationSettingsFolder, Preferences.settingsFile);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Settings settings;
		String settingsJson = null;
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

		scheduledJob = new ScheduledJob(id, startDate.toString(), endDate.toString(), period, getSelectedRectangle());
		scheduledJob.setStatus(ScheduledJobStatus.SCHEDULED);
		scheduledJobList.add(scheduledJob);
		settings.setSchedules(scheduledJobList);
		settingsJson = gson.toJson(settings, Settings.class);

		try {
			FileWriter file = new FileWriter(jsonSettingFile);
			file.write(settingsJson);
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		getCallback().taskTerminated(scheduledJob, settings);
		dispose();
	}

	public void screenRectangleSelected(SelectedRectangle selectedRectangle, Object object) {
		setSelectedRectangle(selectedRectangle);
	}

	public void setSelectedRectangle(SelectedRectangle selectedRectangle) {
		this.selectedRectangle = selectedRectangle;
	}

	public SelectedRectangle getSelectedRectangle() {
		return selectedRectangle;
	}
}
