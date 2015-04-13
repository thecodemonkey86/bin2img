package plugin;


import nio.converter.ConvertToImgObserver;
import model.transfer.TransferManager;
import model.transfer.TransferObserver;


public interface ScriptTransferObserver extends ConvertToImgObserver{
	void updateScriptAdded(Script info);
	void updateSetTransferManager(Script info,TransferManager<TransferObserver>tm);
	void updateTransferred(Script info,TransferManager<TransferObserver>tm,long speed);
	void updateProgress(Script info);
	void updateStopped(Script info,TransferManager<TransferObserver> tm);
	void updateFinalizing(Script info, TransferManager<TransferObserver> tm);
	void updateFinished(Script info);
	void updateFinishedAllScripts();
//	void updateConverting(Script info);
	void updateRemove(Script info, TransferManager<TransferObserver> tm);
//	void updateStarted(Script info, TransferManager<TransferObserver> tm);
	void updateQueued(Script info);
}
