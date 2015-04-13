/*	This program is released under European Union Public License.
 *  for details please refer to the file LICENSE
 *   
 *  Disclaimer: the program comes "as is" and with absolutely no warranty 
 *  
 *  © 2010,2011 TCM
 */

package converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


import converter.gui.ScriptCreatorObserver;
import core.util.Lists;

import plugin.ScriptIO;


public class ScriptCreatorModel {
	private List<String> links;
	private List<Path> imageFiles;
	private long size;
//	private Path sourceDir;
	private ScriptCreatorObserver obs;
	
	public ScriptCreatorModel() {
		links=new ArrayList<String>();
		imageFiles=new ArrayList<Path>();
		size=0;
	}
	
//	public void setSourceDir(Path sourceDir) {
//		this.sourceDir = sourceDir;
//	}
	
	public int getNumberOfLinks() {
		return links.size();
	}
	
	public void addFiles(List<Path> files) throws IOException {
		Path[] arrFiles=new Path[files.size()];
		files.toArray(arrFiles);
		addFiles(arrFiles);
	}

	public void addFiles(Path[] files) throws IOException {
		size=0;
		Path[] relativeFiles=new Path[files.length];
		
		int i=0;
		for (Path f:files) {
			size+=Files.size(f);
//			Path relativeFile=sourceDir.relativize(f);
			imageFiles.add(f);
			relativeFiles[i++]=f;
		}
		obs.updateAddFiles(relativeFiles);		
	}
	
	public void addLink(String... links ) {
		for (String link:links)
			this.links.add(link);
		obs.updateAddLinks(links);
		
		
	}

	public void moveLinkUp(int index) {		
		Lists.moveUp(links, index);
		obs.updateLinkUp(index);
	}
	
	public void moveLinkDown(int index) {		
		Lists.moveDown(links, index);
		obs.updateLinkDown(index);
	}
	
	public int getNumberOfFiles() {
		return imageFiles.size();
	}


	public List<String> getLinks() {
		return links;
	}
	

	public long getTotalSize() {
		return size;
	}

	public void removeLink(int i) {
		if (i>-1) {
			links.remove(i);
			obs.updateRemoveLink(i);
		}
	}
	
	public void removeFile(int i) {
		if (i>-1) {
			imageFiles.remove(i);
			obs.updateRemoveFile(i);
			
		}
	}

	public void removeFiles() {
		imageFiles.clear();
		size=0;
		
		
	}
	
	public void removeAll() {
		links.clear();
		imageFiles.clear();
		size=0;
		
		
	}

	public void setLink(String link,int i) {
		if (i>=0 && i<links.size()) {
			links.set(i, link);
		}
		
	}

	public void save(Path out) throws IOException,Exception {
		SCDownloadScript s=new SCDownloadScript(out);
		if (imageFiles.size()!=links.size()) throw new Exception();
		for (int i=0;i<this.imageFiles.size();i++) {
			s.addEntry(new SCLinkCollectorEntry(Files.size(imageFiles.get(i)), links.get(i)));
		}
		ScriptIO.writeScript(s);
	}
	
	public List<Path> getImageFiles() {
		return imageFiles;
	}

	public void setObserver(ScriptCreatorObserver obs) {
		this.obs=obs;
	}

	
}
