package layout;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ResourceBundle;
//import java.util.Timer;
import javax.swing.Timer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class TestBusyIcons implements ActionListener {

	private static JLabel spinningLabel;
	private final Icon[] busyIcons = new Icon[15];
	Timer busyIconTimer;

	public void initBusyIcons1() {
		ResourceBundle fajrResourceBundle = ResourceBundle.getBundle("tv/bug/brain/ui/resources/BrainUI");
		for (int i = 0; i < busyIcons.length; i++) {
			busyIcons[i] = new ImageIcon(
					getClass().getResource(fajrResourceBundle.getString("StatusBar.busyIcons[" + i + "]")));
		}
	}

	public void initBusyIcons() {
		// ResourceBundle fajrResourceBundle =
		// ResourceBundle.getBundle("tv/bug/brain/ui/resources/BrainUI");
		for (int i = 0; i < busyIcons.length; i++) {
			ClassLoader classLoader = getClass().getClassLoader();
//			URL resource = classLoader.getResource(fileName);
			URL resource = classLoader.getResource("busy-icons/busy-icon" + i + ".png");
			System.out.println("resource = " + resource);
			busyIcons[i] = new ImageIcon(resource);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				TestBusyIcons testBusyIcons = new TestBusyIcons();
				testBusyIcons.createAndShowGUI();
			}
		});

	}

	private void createAndShowGUI() {

		initBusyIcons();

		// Create and set up the window.
		JFrame frame = new JFrame("Test Busy icons");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Set up the content pane.
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();

		GraphicsDevice[] screens = graphicsEnvironment.getScreenDevices();
		frame.setLayout(null);
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

	private void addComponentsToPane(Container contentPane) {
		// TODO Auto-generated method stub
		JLabel label = new JLabel("sssss");
		contentPane.add(label);

		JLabel outputFolderLabel = new JLabel("Output Folder");
		outputFolderLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		outputFolderLabel.setBounds(5, 10, 100, 30);
//		outputFolderLabel.setBorder(border);
		contentPane.add(outputFolderLabel);

		JTextField outputFolderTextField = new JTextField();
		outputFolderTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		outputFolderTextField.setBounds(200, 10, 228, 30);
		contentPane.add(outputFolderTextField);
		outputFolderTextField.setColumns(10);

		spinningLabel = new JLabel();

		spinningLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		spinningLabel.setBounds(5, 50, 100, 30);
//		outputFolderLabel.setBorder(border);
		contentPane.add(spinningLabel);

		JButton startSpinButton = new JButton("Start");
		startSpinButton.setActionCommand("start");

		startSpinButton.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		startSpinButton.setBounds(105, 60, 100, 30);
//		outputFolderLabel.setBorder(border);
		startSpinButton.addActionListener(this);
		contentPane.add(startSpinButton);

		JButton stopSpinButton = new JButton("Stop");
		stopSpinButton.setActionCommand("stop");
		stopSpinButton.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		stopSpinButton.setBounds(205, 60, 100, 30);
//		outputFolderLabel.setBorder(border);
		stopSpinButton.addActionListener(this);
		contentPane.add(stopSpinButton);

	}

	public void startBusyTimer() {
		if (busyIconTimer == null) {
			ActionListener actionListener = new ActionListener() {
				private int busyIconIndex;

				@Override
				public void actionPerformed(ActionEvent e) {
					busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
					spinningLabel.setIcon(busyIcons[busyIconIndex]);
					System.out.println("timer still running1");
				}
			};
			busyIconTimer = new Timer(100, actionListener);
//			busyIconTimer.setRepeats(true);
		}
		busyIconTimer.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		switch (actionCommand) {
		case "start":
			startBusyTimer();
			break;

		case "stop":
			stopBusyTimer();
			break;

		default:
			break;
		}
	}

	private void stopBusyTimer() {
		if (busyIconTimer != null) {
			System.out.println("stopping timer");
			busyIconTimer.stop();
		}

		if (busyIconTimer.isRunning()) {
			System.out.println("timer still running");
		} else {
			System.out.println("timer stopped");
		}

	}

}
