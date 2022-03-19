package org.fajr.snapshot_utility;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

import org.apache.log4j.Logger;

public class KeepAwakeThread implements Runnable {
	static Logger log = Logger.getLogger(KeepAwakeThread.class.getName());

	@Override
	public void run() {
		log.debug("keep awake thread starting...");
		Robot hal;
		try {
			hal = new Robot();
			while (true) {
				hal.delay(1000 * 60);
				Point location = MouseInfo.getPointerInfo().getLocation();
				hal.mouseMove(location.x+1, location.y+1);
			}
		} catch (AWTException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
