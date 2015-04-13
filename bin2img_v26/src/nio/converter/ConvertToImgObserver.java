package nio.converter;

import core.util.Observer;

public interface ConvertToImgObserver extends Observer{
	void updateStart(int numberOfFiles);
	void updateStartConvert(ToImgInfo2 info);
	void updateFinishConvert(ToImgInfo2 info, int count,int numberOfFiles);
	void updateFinished(int numberOfFiles);
}
