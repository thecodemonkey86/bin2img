/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package plugin;

import model.transfer.TransferManager;
import model.transfer.TransferObserver;

public abstract class ScriptNotification {

	public static final ScriptNotification running = new ScriptNotification() {

		@Override
		public void updateStatus(ScriptTransferObserver observer, TransferManager<TransferObserver> tm, Script info) {
		}

	};

	public static final ScriptNotification finalizing = new ScriptNotification() {

		@Override
		public void updateStatus(ScriptTransferObserver observer, TransferManager<TransferObserver> tm, Script info) {
			observer.updateFinalizing(info, tm);
		}
		
	};
	
	public static final ScriptNotification converting = new ScriptNotification() {

		@Override
		public void updateStatus(ScriptTransferObserver observer, TransferManager<TransferObserver> tm, Script info) {
		}
	};

//	public static final ScriptNotification error = new ScriptNotification() {
//
//		@Override
//		public void updateStatus(ScriptTransferObserver observer, TransferManager<TransferObserver> tm, Script info) {
//		}
//	};	
	
	public static final ScriptNotification finished = new ScriptNotification() {
		@Override
		public void updateStatus(ScriptTransferObserver observer, TransferManager<TransferObserver> tm, Script info) {
			observer.updateFinished(info);
		}
	};

	public static final ScriptNotification stopped = new ScriptNotification() {
		@Override
		public void updateStatus(ScriptTransferObserver observer, TransferManager<TransferObserver> tm, Script info) {
			observer.updateStopped(info, tm);
		}
	};

	public static final ScriptNotification remove= new ScriptNotification() {
		@Override
		public void updateStatus(ScriptTransferObserver observer, TransferManager<TransferObserver> tm, Script info) {
			observer.updateRemove(info, tm);
		}
	};

	public static final ScriptNotification queued=new ScriptNotification() {
		
		@Override
		public void updateStatus(ScriptTransferObserver observer, TransferManager<TransferObserver> tm, Script info) {
			observer.updateQueued(info);
		}
	};
	
	public static final ScriptNotification error=new ScriptNotification() {
		
		@Override
		public void updateStatus(ScriptTransferObserver observer, TransferManager<TransferObserver> tm, Script info) {
			System.out.println("update error");
		}
	};
	
	private String text;

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

	public abstract void updateStatus(ScriptTransferObserver observer, TransferManager<TransferObserver> tm, Script info);

	
}
