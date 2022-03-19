package org.fajr.snapshot_utility.event.listener;

import java.util.Date;

import org.fajr.snapshot_utility.event.ScreenshotEvent;

public interface ScreenshotEventListener {
	
	void didTakeScreenshot(ScreenshotEvent screenshotEvent);
	void screenshotGrabberStarted(int id);
	void screenshotGrabberEnded(int id, Date nextEndAtDate, Date nextEndAt);

	

}
