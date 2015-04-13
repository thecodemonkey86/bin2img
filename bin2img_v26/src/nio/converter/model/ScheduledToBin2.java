package nio.converter.model;

import java.io.IOException;
import java.nio.file.Path;

import nio.converter.ConvertToBinObserver;

import scheduler.Scheduler2;
import scheduler.SchedulerClient;
import scheduler.SchedulerRunnable2;

public class ScheduledToBin2 extends BasicToBin implements SchedulerClient{
	protected Scheduler2 scheduler;
	protected ConvertToBinObserver obs;
	protected int counter;
	protected int numberOfFiles;
	
	public void setObserver(ConvertToBinObserver obs) {
		this.obs=obs;		
	}
	
	public ScheduledToBin2(int threads) {	
		scheduler=new Scheduler2(this, threads);
	}
	

	public void toBin(Path[] files) throws IOException {
		if (files!=null) {
			synchronized (this) {
				numberOfFiles+=files.length;
			}
			for (Path file:files) {
				scheduler.addTaskStopped(new ConvertThread(file));
			}
		}
	}
	
	@Override
	public Path toBin(Path file) throws IOException {
		synchronized (this) {
			numberOfFiles++;
		}
		scheduler.addTaskStopped(new ConvertThread(file));
		return null;
	}
	
	@Override
	public void allDone() {
		
	}
	
	public void start() throws IOException {
		if (!scheduler.isRunning()) {
			obs.updateStart(numberOfFiles);
			scheduler.enqueueAllStopped();
			scheduler.start();
		}
	}
	
	public void cancel() {
		if (scheduler.isRunning()) scheduler.stopAll();
	}
	
	protected class ConvertThread extends SchedulerRunnable2 {

		protected Path file;
		public ConvertThread(Path file) {
			this.file=file;
		}
		
		@Override
		public void run() {
			try {
				obs.updateStartConvert(file);
				Path result=perform(file);
				synchronized (ScheduledToBin2.this) {
					counter++;
					obs.updateFinishConvert(result,counter,numberOfFiles);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void stop() {
		}

		@Override
		public void enqueue() {
			
		}

		@Override
		public boolean errorOccoured() {
			return false;
		}
		
	}
}
