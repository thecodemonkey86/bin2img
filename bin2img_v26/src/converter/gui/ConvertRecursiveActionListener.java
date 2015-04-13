package converter.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public abstract class ConvertRecursiveActionListener implements ActionListener,FileVisitor<Path> {

	protected abstract Path getSourcePath();

	private ArrayList<Path> files=new ArrayList<>();
	
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		files.add(file);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		files.clear();
		try {
			Files.walkFileTree(getSourcePath(), this);
			Path[] input=new Path[files.size()];
			files.toArray(input);
			process(input);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	protected abstract void process(Path[] input);
}