package converter.gui.settings;

import static init.Bin2ImgInit.rm;

import java.io.IOException;
import util.exception.CancelException;
import gui.settings.dialog.AbstractListSettingsDialog;
import gui.settings.dialog.page.SettingsPage;

public class NewSettingsDialog extends AbstractListSettingsDialog {

	private static final long serialVersionUID = 1L;

	
	public NewSettingsDialog() {
		super(null, rm().getLocalizedString("settings"));
		setSize(800, 480);
		SettingsPage pGeneral = new GeneralSettingsPage(rm().getLocalizedString("settingPageGeneral"));
		ConverterSettingsPage pConv= new ConverterSettingsPage(rm().getLocalizedString("settingPageConvert"));
		NetworkPage nsp=new NetworkPage(rm().getLocalizedString("settingPageNetwork"));
		
		addPage(pGeneral);
		addPage(pConv);
		addPage(nsp);
		
		setCurrentSettingsPage(pConv);
		split.setDividerLocation(200);
		
	}
	
	public void showDialog(String entryName) throws CancelException, IOException {
		if (entryName!=null){
			for (int i=0;i<entriesModel.size();i++) {
				if (entriesModel.get(i).getEntry().equals(entryName)) {
					super.showDialog(i);
					return;
				}
			}
		}
		super.showDialog(0);
			
	}
}
