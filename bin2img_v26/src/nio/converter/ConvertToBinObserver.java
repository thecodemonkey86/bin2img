package nio.converter;

import java.nio.file.Path;

import core.util.Observer;

public interface ConvertToBinObserver extends Observer{
	void updateStart(int numberOfFiles);
	void updateStartConvert(Path input);
	void updateFinishConvert(Path output, int count,int numberOfFiles);
}
