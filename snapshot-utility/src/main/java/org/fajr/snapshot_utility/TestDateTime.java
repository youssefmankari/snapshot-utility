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
		
	}

}
