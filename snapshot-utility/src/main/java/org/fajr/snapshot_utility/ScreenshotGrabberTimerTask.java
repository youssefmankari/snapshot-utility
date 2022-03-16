package org.fajr.snapshot_utility;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.time.DateUtils;
import org.fajr.snapshot_utility.event.listener.ScreenshotEventListener;

public class ScreenshotGrabberTimerTask extends TimerTask {

	private int id;
	private Timer timer;
	private Date startAt, endAt;
	private int periodicity;
	private Rectangle selectedRectangle;
	private ScreenshotEventListener screenshotEventListener;
	private Settings settings;
	private Timer screenshotTimer;
	private Robot robot;
	
	

//	//	Rectangle[] rectangleArray = new Rectangle[screens.length];
//		int i = 0;
//		Robot[] robots = new Robot[screens.length];
//		for (GraphicsDevice screen : screens) {
//			Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
//		//	rectangleArray[i] = screenBounds;
//
//			try {
//				robots[i] = new Robot(screen);
//			} catch (AWTException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			i++;
//		}
	
	

	public ScreenshotGrabberTimerTask(int id, Settings settings, SelectedRectangle selectedRectangle, Timer timer, Date startAt, Date endAt, int millis) {
		initRobot();
		setId(id);
		setTimer(timer);
		setStartAt(startAt);
		setEndAt(endAt);
		setPeriodicity(millis);
		Rectangle r = new Rectangle(selectedRectangle.getX(),selectedRectangle.getY(),selectedRectangle.getWidth(),selectedRectangle.getHeight());
		setSelectedRectangle(r);
		setSettings(settings);
	}
	
	private void initRobot() {
		
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] screens = ge.getScreenDevices();
			try {
				robot = new Robot(screens[0]);
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setSettings(Settings settings) {
		this.settings = settings;
	}
	
	public Settings getSettings() {
		return settings;
	}

	@Override
	public void run() {
		System.out.println(LocalDateTime.now() + " : daily  task running");
		executeTimerTask();
	}

	private void executeTimerTask() {
		screenshotTimer = new Timer();
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {

				getScreenshotEventListener().screenshotGrabberStarted(getId());

				captureSelectedRectangle();
				Date now = new Date();
				
				System.out.println("now = "+now + " . getEndAt() = "+getEndAt());

				if (now.after(getEndAt())) {
					System.out.println(LocalDateTime.now() + " : child task done! adding 120secs to endAt.");
					endAt = DateTimeUtils.addSecondsToDate(endAt,120);
					System.out.println("Now, endAt = "+endAt);
					screenshotTimer.cancel();
					getScreenshotEventListener().screenshotGrabberEnded(getId());
				}

			}
		};
		screenshotTimer.schedule(timerTask, getStartAt(), getPeriodicity());
	}
	
	public Timer getScreenshotTimer() {
		return screenshotTimer;
	}

	protected void captureSelectedRectangle() {
		

		String random = randomAlphanumeric(7);
		
		BufferedImage screenShot = robot.createScreenCapture(getSelectedRectangle());
		File screenshotsFolder = new File(getSettings().getScreenshotsFolder(),"Fajr Screenshots");
		if(!screenshotsFolder.exists()) 
			screenshotsFolder.mkdirs();
		File outputfile = new File(getSettings().getScreenshotsFolder()+File.separator+"Fajr Screenshots", "image_" + random + ".jpg");

		try {
			ImageIO.write(screenShot, "jpg", outputfile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

	public void setTimer(Timer timer) {
		this.timer = timer;

	}

	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	public Date getEndAt() {
		return endAt;
	}

	public void setEndAt(Date endAt) {
		this.endAt = endAt;
	}

	public int getPeriodicity() {
		return periodicity;
	}

	public void setPeriodicity(int periodicity) {
		this.periodicity = periodicity;
	}

	public Timer getTimer() {
		return timer;
	}

	public Rectangle getSelectedRectangle() {
		return selectedRectangle;
	}

	public void setSelectedRectangle(Rectangle selectedRectangle) {
		this.selectedRectangle = selectedRectangle;
	}

	public void setScreenshotEventListener(ScreenshotEventListener screenshotEventListener) {
		this.screenshotEventListener = screenshotEventListener;
	}
	
	public ScreenshotEventListener getScreenshotEventListener() {
		return screenshotEventListener;
	}

	
	@Override
	public boolean equals(Object obj) {
		ScreenshotGrabberTimerTask  task = (ScreenshotGrabberTimerTask)obj;
		return getId() == task.getId();
	}
	
	

}
