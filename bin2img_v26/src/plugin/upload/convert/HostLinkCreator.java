package plugin.upload.convert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import jbiu.model.ULInfo;
import jbiu.model.UploaderConfig;

import nio.converter.ByteInput;
import nio.converter.ToImgNewIO;

import plugin.Script;
import plugin.ScriptIO;
import plugin.ScriptLinkCollectorEntry;
import util.StringUtil;
import util.base64.Base64Encoder;
import util.exception.CancelException;

public class HostLinkCreator implements LinkCreator {

	private UploaderConfig cfg;
	private Map<String, String> tokens;
	
	public HostLinkCreator(UploaderConfig cfg,Map<String, String> tokens) {
		this.cfg=cfg;
		this.tokens=tokens;
	}
	
	@Override
	public ScriptLinkCollectorEntry create(Script s) throws IOException, CancelException {
		System.out.println("create start");
		ToImgNewIO toImg=new ToImgNewIO();
		Path dir=s.getFile().getParent();
		Files.createDirectories(dir);
		Path p=dir.resolve(s.getName()+".png");
		ScriptLinkCollectorEntry lc=new Bin2imgProtocolEntry(s.getFile());
		ByteInput scriptBytes=ScriptIO.writeScriptToBytes(s);
		toImg.toImg(scriptBytes,s.getName(),scriptBytes.size(),0L,p);
		ULInfo ulInfo=new ULInfo(p, lc); 
		ulInfo.setFileTokens(tokens);
//		ulInfo.setFileTokens(cfg.getDefaultFileTokens());		
		cfg.getEngine(ulInfo).startTransfer();
//		Files.delete(p);
		if (tokens!=null) {
			tokens.clear();
			tokens=null;
		}
		System.out.println("create end");
		return lc;
	}

	private class Bin2imgProtocolEntry extends ScriptLinkCollectorEntry{

		public Bin2imgProtocolEntry(Path file) {
			super(file);
		}
		
		@Override
		public void setLink(int index, String link) {
			if (link!=null)
				super.setLink(index, "b2i://"+Base64Encoder.encode(StringUtil.dropFirst(link, "http://")));
		}
	}
}
