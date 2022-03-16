package org.fajr.snapshot_utility;

import java.awt.*;
import java.util.*;

public class KeepAwakeThread implements Runnable{

	@Override
	public void run() {
		System.out.println("keep awake thread started");
		Robot hal;
		try {
			hal = new Robot();
			Random random = new Random();
			while (true) {
				hal.delay(1000 * 60);
				int x = random.nextInt() % 640;
				int y = random.nextInt() % 480;
				hal.mouseMove(x, y);
			}
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
