package org.fajr.snapshot_utility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TestDateTime {

	public static void main(String[] args) {
		LocalDate localDate = LocalDate.now();
		String timeString = "12:13:14";
		LocalTime time = LocalTime.parse(timeString);
		LocalDateTime localDateTime = LocalDateTime.of(localDate, time);
		System.out.println("localDateTime"+localDateTime);
		
		IpCameraScheduledJob ipCameraScheduledJob = new IpCameraScheduledJob();
		ScheduledJob ipCameraScheduledJob2 = new IpCameraScheduledJob();
		System.out.println("ipCameraScheduledJob = "+ipCameraScheduledJob.getClass());
		System.out.println("ipCameraScheduledJob2 = "+ipCameraScheduledJob2.getClass());

		
	}

}
