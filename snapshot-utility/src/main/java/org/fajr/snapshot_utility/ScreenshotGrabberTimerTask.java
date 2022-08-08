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
import org.apache.log4j.Logger;
import org.fajr.snapshot_utility.event.ScreenshotEvent;
import org.fajr.snapshot_utility.event.listener.ScreenshotEventListener;

public class ScreenshotGrabberTimerTask extends GrabberTimerTask {
	static Logger log = Logger.getLogger(ScreenshotGrabberTimerTask.class.getName());

	private Rectangle selectedRectangle;
	private Timer screenshotTimer;
	private Robot robot;
	public ScreenshotGrabberTimerTask(int id, Settings settings, SelectedRectangle selectedRectangle, Timer timer, Date startAt, Date endAt, int millis) {
		super(id,timer,startAt,endAt,millis, settings,  millis);
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
	
	@Override
	public void run() {
		log.info(LocalDateTime.now() + " : daily  task running...");
		executeTimerTask();
	}

	private void executeTimerTask() {
		screenshotTimer = new Timer();
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {

				getScreenshotEventListener().screenshotGrabberStarted(getId());

				try {
					captureSelectedRectangle();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					screenshotTimer.cancel();
					getScreenshotEventListener().screenshotGrabberEndedWithException(e.getMessage() ,getId(), startAt, endAt);
				}
				Date now = new Date();
				
				//log.info("now = "+now + " . getEndAt() = "+getEndAt());

				if (now.after(getEndAt())) {
					
					if(getSettings().getRunMode().equals("TEST")) {
						log.info(LocalDateTime.now() + " : child task done! adding"+getInterval()*60 +" to endAt.");
						startAt = DateTimeUtils.addSecondsToDate(startAt,60);//for test
						endAt = DateTimeUtils.addSecondsToDate(endAt,60);//for test
					}else {
						log.info(LocalDateTime.now() + " : child task done! adding"+getInterval()*60*60 +" to endAt.");
						startAt = DateTimeUtils.addSecondsToDate(startAt,getInterval()*60*60);
						endAt   = DateTimeUtils.addSecondsToDate(endAt,getInterval()*60*60); 
					}
					log.info("Now, endAt = "+endAt);
					screenshotTimer.cancel();
					getScreenshotEventListener().screenshotGrabberEnded(getId(), startAt, endAt);
				}

			}
		};
		screenshotTimer.schedule(timerTask, getStartAt(), getPeriodicity());
	}
	
	public Timer getScreenshotTimer() {
		return screenshotTimer;
	}

	protected void captureSelectedRectangle() throws IOException {
		
		BufferedImage screenShot = robot.createScreenCapture(getSelectedRectangle());
	
		File outputfile = saveBufferedImage(screenShot);
		ScreenshotEvent sce = new ScreenshotEvent();
		sce.setScreenshotFile(outputfile);
		sce.setScheduledJobId(getId());
		getScreenshotEventListener().screenshotGrabberDidTakeScreenshot(sce);//, startAt, endAt);
		
	}

	

	public void setTimer(Timer timer) {
		this.timer = timer;

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
		GrabberTimerTask  task = (GrabberTimerTask)obj;
		return getId() == task.getId();
	}
	
	

}
