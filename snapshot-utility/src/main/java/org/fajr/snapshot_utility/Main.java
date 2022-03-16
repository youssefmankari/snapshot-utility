package org.fajr.snapshot_utility;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		
		
//		Utilities.startKeepAwakeThread();
		Utilities.readSettingsWithCallback(new Callback() {

			@Override
			public void taskTerminated(boolean success, String message,Settings settings) {
				System.out.println(message);
				if (success) {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							SnapshotWindow snapshotWindow = new SnapshotWindow(settings);
							snapshotWindow.createAndShowWindow();
						}
					});
				}else {
					
				}

			}

			@Override
			public void taskTerminated(ScheduledJob scheduledJob , Settings settings) {
				// TODO Auto-generated method stub
				
			}
		});

	}

}
