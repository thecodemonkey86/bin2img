package nio.converter.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;


import core.io.Application;

import nio.converter.ConvertToImgObserver;
import nio.converter.ToImgInfo2;
import nio.converter.ToImgInfoList;

import scheduler.Scheduler2;
import scheduler.SchedulerClient;
import scheduler.SchedulerRunnable2;

public abstract class ScheduledToImg2 extends BasicToImg implements SchedulerClient{
	protected Scheduler2 scheduler;
	protected ConvertToImgObserver obs;
	protected int counter;
	protected int numberOfFiles;
	
	
	public void setObserver(ConvertToImgObserver obs) {
		this.obs=obs;		
	}
	
	
	public ScheduledToImg2(int threads) throws IOException {
		scheduler=new Scheduler2(this, threads);
	}
	
	public void toImg(ToImgInfoList infos) throws IOException {
		
		if (infos!=null) {
			synchronized (this) {
				numberOfFiles+= infos.size();
			}
			for (ToImgInfo2 info:infos) {
				scheduler.addTask(createThread(info));
			}
		}
	}
	
	
	@Override
	public void toImg(ToImgInfo2 info) throws IOException {
		synchronized (this) {
			numberOfFiles++;
		}
		scheduler.addTask(createThread(info));
	}
	
	@Override
	public void allDone() {
		synchronized (this) {
			obs.updateFinished(numberOfFiles);
			numberOfFiles=0;
			counter=0;
		}
	}
	
	public void start() {
		if (!scheduler.isRunning()){
			obs.updateStart(numberOfFiles);
			scheduler.enqueueAllStopped();
			scheduler.start();
		}
	}
	
	public void cancel() {
		if (scheduler.isRunning()) scheduler.stopAll();
	}

	protected SchedulerRunnable2 createThread(ToImgInfo2 info) {
		return new ConvertThread(info);
	}
	
	private class ConvertThread extends SchedulerRunnable2 {

		protected ToImgInfo2 info;
		public ConvertThread(ToImgInfo2 info) {
			this.info=info;
		}
		
		@Override
		public void run() {
			try {
//				preConvertAction(info);
				if (!stop) {
					obs.updateStartConvert(info);
					performToImg(info);
					synchronized (this) {
						counter++;
						obs.updateFinishConvert(info, counter, numberOfFiles);
					}
					postConvertAction(info);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void stop() {
			stop=true;
			try {
				cancelConvertAction(info);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public String toString() {
			return info.toString()+'\n';
		}

		@Override
		public void enqueue() {
			
		}

		@Override
		public boolean errorOccoured() {
			return false;
		}
		
	}
	
	public void loadSession(int convertFinished) throws IOException {
		this.counter+=convertFinished;
		this.numberOfFiles+=convertFinished;
	}
	
	public void saveSession(OutputStream out) throws IOException {
		Collection<SchedulerRunnable2> unfinished =scheduler.getUnfinished();
		for (SchedulerRunnable2 thread:unfinished) {
			ConvertThread t=(ConvertThread) thread;
			out.write(t.toString().getBytes(Application.UTF8 ));
		}
	}
	
//	protected abstract void preConvertAction(ToImgInfo2 info) throws IOException;
	protected abstract void postConvertAction(ToImgInfo2 info) throws IOException;
	protected abstract void cancelConvertAction(ToImgInfo2 info) throws IOException;
}
