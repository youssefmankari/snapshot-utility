package org.fajr.snapshot_utility;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class Main {
	static Logger log = Logger.getLogger(Main.class.getName());

	private SnapshotWindow snapshotWindow;

	public static void main(String[] args) throws InterruptedException {
		log.debug("Fajr App started.");
		Main main = new Main();
		main.start();
		
	}

	private void start() {
		addshutdownHook();
		Utilities.startKeepAwakeThread();
		Utilities.readSettingsWithCallback(new Callback() {

			@Override
			public void taskTerminated(boolean success, String message, final Settings settings) {
				log.debug(message);
				if (success) {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							snapshotWindow = new SnapshotWindow(settings);
							log.debug("Run Mode : "+ settings.getRunMode());

							snapshotWindow.createAndShowWindow();
						}
					});
				} else {

				}
			}

			@Override
			public void taskTerminated(ScheduledJob scheduledJob, Settings settings) {
			}
		});

	}

	private void addshutdownHook() {
		Runtime.getRuntime().addShutdownHook(new ShutDownHookThread());
		log.debug("Shutdown Hook initialized.");
	}

	class ShutDownHookThread extends Thread {
		@Override
		public void run() {
			if (snapshotWindow != null) {
				cancelTimerTasksAndUpdateTasksStatuses();
			}

		}

		private void cancelTimerTasksAndUpdateTasksStatuses() {
			List<GrabberTimerTask> grabberTimerTaskList = snapshotWindow
					.getGrabberTimerTaskList();
			ScheduledJobsList scheduledJobsList = snapshotWindow.getScheduledJobsList();

			for (Iterator<ScheduledJob> iterator = scheduledJobsList.iterator(); iterator.hasNext();) {

				ScheduledJob scheduledJob = iterator.next();
				if (scheduledJob.getStatus() == ScheduledJobStatus.RUNNING) {

					int id = scheduledJob.getId();

					for (Iterator<GrabberTimerTask> iterator2 = grabberTimerTaskList
							.iterator(); iterator2.hasNext();) {
						GrabberTimerTask grabberTimerTask = iterator2.next();
						if (grabberTimerTask.getId() == id) {
							grabberTimerTask.getTimer().cancel();
							grabberTimerTask.cancel();
							Utilities.updateStatusForScheduleId(id, ScheduledJobStatus.ENDED);
							break;
						}
					}
				}
			}
		}
	}
}