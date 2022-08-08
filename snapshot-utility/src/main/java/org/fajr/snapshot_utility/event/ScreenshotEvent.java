package org.fajr.snapshot_utility.event;

import java.io.File;

public class ScreenshotEvent{

	private File screenshotFile;
	private int scheduledJobId;

	public File getScreenshotFile() {
		return screenshotFile;
	}
	
	public void setScreenshotFile(File screenshotFile) {
		this.screenshotFile = screenshotFile;
	}

	public int getScheduledJobId() {
		return scheduledJobId;
	}
	
	public void setScheduledJobId(int scheduledJobId) {
		this.scheduledJobId = scheduledJobId;
	}

}
