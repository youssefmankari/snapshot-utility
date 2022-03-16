package org.fajr.snapshot_utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ScheduledJobsList implements Iterable<ScheduledJob> {
	
	private List<ScheduledJob> scheduledJobsList = new ArrayList<>();
	
	class ScheduledJobsListIterator implements Iterator<ScheduledJob> {
		
		private int currentPosition=0;

				@Override
		public boolean hasNext() {
			
			return currentPosition < scheduledJobsList.size();
		}

		@Override
		public ScheduledJob next() {
			ScheduledJob scheduledJob = scheduledJobsList.get(currentPosition);
			currentPosition++;
			return scheduledJob;
		}
	      
	   }

	@Override
	public Iterator<ScheduledJob> iterator() {
		return new ScheduledJobsListIterator();
	}

	public void add(ScheduledJob scheduledJob) {
		scheduledJobsList.add(scheduledJob);
		
	}
	
	public void remove(int position) {
		scheduledJobsList.remove(position);
	}
	
	
	

}



