package org.fajr.snapshot_utility.event.listener;

import java.util.Date;

import org.fajr.snapshot_utility.event.ScreenshotEvent;

public interface ScreenshotEventListener {
	void screenshotGrabberStarted(int id);
	void screenshotGrabberEnded(int id, Date nextEndAtDate, Date nextEndAt);
	void screenshotGrabberDidTakeScreenshot(ScreenshotEvent screenshotEvent);
	void screenshotGrabberEndedWithException(String message, int id, Date startAt, Date endAt);
}
