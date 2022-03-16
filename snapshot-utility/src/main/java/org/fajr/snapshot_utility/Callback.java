package org.fajr.snapshot_utility;

public interface Callback {
	void taskTerminated(boolean success, String message, Settings settings);
	public void taskTerminated(ScheduledJob scheduledJob , Settings settings);
}
