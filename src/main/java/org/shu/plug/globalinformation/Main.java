package org.shu.plug.globalinformation;

import com.biglybt.pif.PluginException;
import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.UnloadablePlugin;
import com.biglybt.pif.download.Download;
import com.biglybt.pif.ui.UIInstance;
import com.biglybt.pif.ui.UIManagerListener;
import com.biglybt.pif.ui.UIMessage;
import com.biglybt.pif.ui.config.ActionParameter;
import com.biglybt.pif.ui.config.Parameter;
import com.biglybt.pif.ui.config.ParameterListener;
import com.biglybt.pif.ui.config.StringParameter;
import com.biglybt.pif.ui.menus.MenuItem;
import com.biglybt.pif.ui.menus.MenuItemFillListener;
import com.biglybt.pif.ui.menus.MenuItemListener;
import com.biglybt.pif.ui.menus.MenuManager;
import com.biglybt.pif.ui.model.BasicPluginConfigModel;
import com.biglybt.pif.ui.tables.TableContextMenuItem;
import com.biglybt.pif.ui.tables.TableManager;
import com.biglybt.pif.ui.tables.TableRow;
import com.biglybt.pif.utils.UTTimer;
import com.biglybt.pif.utils.UTTimerEvent;
import com.biglybt.pif.utils.UTTimerEventPerformer;
import com.biglybt.ui.swt.pif.UISWTInstance;
import com.biglybt.ui.swt.pif.UISWTStatusEntry;
import com.biglybt.ui.swt.pif.UISWTStatusEntryListener;

public class Main implements UnloadablePlugin {

	private RefreshInformation info;
	private GlobalInformation globalInformation;
	private UISWTInstance swtInstance;
	private PluginInterface pluginInterface;
	private UTTimer timer;
	private TableContextMenuItem menuRemainings, menuPeersIncomplete, menuPeersComplete;
	private UISWTStatusEntry uiSWTStatusEntry;
	
	public static final String CFG_CUSTOM_TEXT = "shu.plugin.customize.text";
	public static final String CFG_CUSTOM_TEXT_MINI = "shu.plugin.customize.text.mini";
	public static final String CFG_CUSTOM_TEXT_REMAINING_MINI = "shu.plugin.customize.text.remaining.mini";
	public static final String CFG_CUSTOM_TEXT_PEERS_MINI = "shu.plugin.customize.text.peers.mini";
	public static final String CFG_CUSTOM_BOOL = "shu.plugin.customize.bool";
	public static final String CFG_DEFAULT = "shu.plugin.customize.default";
	public static final String CFG_SAVE = "shu.plugin.customize.save";
	public static final String CFG_LEGEND = "shu.plugin.customize.legend";
	
	public void initialize(PluginInterface pluginInterface) throws PluginException {
		this.pluginInterface = pluginInterface;
		this.pluginInterface.getUtilities().getLocaleUtilities().integrateLocalisedMessageBundle("GlobalInfoMessage");
		
		info = RefreshInformation.getInstance(this.pluginInterface);
		
		ConfigPanel();
		
        this.pluginInterface.getUIManager().addUIListener(new UIManagerListener() {

			@Override
			public void UIAttached(UIInstance instance) {
            	
                if (instance instanceof UISWTInstance) {
                    swtInstance = ((UISWTInstance) instance);
                    
                    
                    createStatusEntry(swtInstance);
                    addMyTorrentsMenu();
                    uiTimerListerner();
                    
                }
            }

			@Override
            public void UIDetached(UIInstance instance) {
            	if (instance instanceof UISWTInstance){
                    swtInstance = null;
            	}
            }
        });
        
	}
	
	private void ConfigPanel() {
		BasicPluginConfigModel model = pluginInterface.getUIManager().createBasicPluginConfigModel("shu.plugin.globalinfo.title");
		
		model.addBooleanParameter2(CFG_CUSTOM_BOOL, CFG_CUSTOM_BOOL, false);
		final StringParameter spTextTotal = model.addStringParameter2(CFG_CUSTOM_TEXT, CFG_CUSTOM_TEXT, info.getDefaultTextTotal());
		final StringParameter spTextTotalMini = model.addStringParameter2(CFG_CUSTOM_TEXT_MINI, CFG_CUSTOM_TEXT_MINI, info.getDefaultTextTotalMini());
		final StringParameter spTextRemainingMini = model.addStringParameter2(CFG_CUSTOM_TEXT_REMAINING_MINI, CFG_CUSTOM_TEXT_REMAINING_MINI, info.getDefaultTextRemainingMini());
		final StringParameter spTextPeersMini = model.addStringParameter2(CFG_CUSTOM_TEXT_PEERS_MINI, CFG_CUSTOM_TEXT_PEERS_MINI, info.getDefaultTextPeersMini());
		model.addLabelParameter2(CFG_LEGEND);
		
		final ActionParameter apDefault = model.addActionParameter2(null, CFG_DEFAULT);
		
		apDefault.addListener(new ParameterListener() {
			@Override
			public void parameterChanged(Parameter param) {
				
				try {
					spTextTotal.setValue(info.getDefaultTextTotal());
					spTextTotalMini.setValue(info.getDefaultTextTotalMini());
					spTextRemainingMini.setValue(info.getDefaultTextRemainingMini());
					spTextPeersMini.setValue(info.getDefaultTextPeersMini());
				} catch (NoSuchMethodError e) {
					//TODO ex
				}
			}
		});
		
		final ActionParameter apSave = model.addActionParameter2(null, CFG_SAVE);
		
		apSave.addListener(new ParameterListener() {
			@Override
			public void parameterChanged(Parameter param) {
				
				try {
					info.setUserTextTotal(spTextTotal.getValue());
					info.setUserTextTotalMini(spTextTotalMini.getValue());
					info.setUserTextRemainingMini(spTextRemainingMini.getValue());
					info.setUserTextPeersMini(spTextPeersMini.getValue());
				} catch (NoSuchMethodError e) {
					//TODO ex
				}
			}
		});
	}

	public void updateStatusBarItem() {
		if(this.uiSWTStatusEntry != null) {
			globalInformation = info.refresh(pluginInterface, pluginInterface.getDownloadManager().getDownloads());
			
			MenuManager menuManager = pluginInterface.getUIManager().getMenuManager();
			MenuItem menu = menuManager.addMenuItem(uiSWTStatusEntry.getMenuContext(), "shu.plugin.globalinfo.popup.credit.title");
			menu.setStyle(MenuItem.STYLE_PUSH);
			
			menu.addListener(new MenuItemListener(){

				@Override
				public void selected(MenuItem menu, Object target) {
					UIMessage uiMessage = swtInstance.createMessage();
					uiMessage.setMessage("shu.plugin.globalinfo.popup.credit.text");
					uiMessage.setTitle("shu.plugin.globalinfo.popup.credit.title");
					uiMessage.setInputType(UIMessage.INPUT_OK);
					uiMessage.setMessageType(UIMessage.MSG_INFO);
					uiMessage.ask();
				}
				
			});
			String text = globalInformation.toString();
			this.uiSWTStatusEntry.setText(text);
			this.uiSWTStatusEntry.setTooltipText(globalInformation.toString(false));
			if (text.trim().equals("")){
				this.uiSWTStatusEntry.setVisible(false);
			} else {
				this.uiSWTStatusEntry.setVisible(true);
			}
		}
	}
	
	public void createStatusEntry(UISWTInstance swtInstance) {
		this.uiSWTStatusEntry = swtInstance.createStatusEntry();
		
		UISWTStatusEntryListener uiSWTStatusEntryListener = new UISWTStatusEntryListener(){

			@Override
			public void entryClicked(UISWTStatusEntry uiSWTStatusEntry) {
				updateStatusBarItem();
			}
			
		};
		this.uiSWTStatusEntry.setListener(uiSWTStatusEntryListener);
		this.uiSWTStatusEntry.setVisible(true);
		updateStatusBarItem();
	}
	
	private void uiTimerListerner() {
		this.timer = this.pluginInterface.getUtilities().createTimer("TotalRemainingRefresh", true);
		
		this.timer.addPeriodicEvent(5000, new UTTimerEventPerformer(){
			@Override
			public void perform(UTTimerEvent event){
				updateStatusBarItem();
			}
		});
	}
	
	private void addMyTorrentsMenu()  {
		MenuItemFillListener remainingsFillListener = new MenuItemFillListener(){

			@Override
			public void menuWillBeShown(MenuItem menu, Object data) {
				TableRow[] rows = ((TableRow[])data);
		    	Download[] downloads = new Download[rows.length];
		    	for(int i=0 ; i<rows.length; i++){
		    		downloads[i] = (Download)rows[i].getDataSource();
		    	}
		    	
		    	if ( downloads == null || downloads.length <= 0 ){
		    		return;
		    	}
		    	menu.setText(info.refresh(pluginInterface, downloads).toStringRemainings(true));
		    	if (menu.getText().equals("")) menu.setVisible(false);
		    	else menu.setVisible(true);
			}
		};
		
		MenuItemFillListener peersFillListener = new MenuItemFillListener(){

			@Override
			public void menuWillBeShown(MenuItem menu, Object data) {
				TableRow[] rows = ((TableRow[])data);
		    	Download[] downloads = new Download[rows.length];
		    	for(int i=0 ; i<rows.length; i++){
		    		downloads[i] = (Download)rows[i].getDataSource();
		    	}
		    	
		    	if ( downloads == null || downloads.length <= 0 ){
		    		return;
		    	}
		    	menu.setText(info.refresh(pluginInterface, downloads).toStringPeers(true));
		    	if (menu.getText().equals("")) menu.setVisible(false);
		    	else menu.setVisible(true);
			}
		};
		
		String stringRemainings = globalInformation.toStringRemainings(true);
		this.menuRemainings = pluginInterface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_INCOMPLETE, stringRemainings );
		this.menuRemainings.addFillListener(remainingsFillListener);
		
		String stringPeers = globalInformation.toStringPeers(true);
		this.menuPeersIncomplete = pluginInterface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_INCOMPLETE, stringPeers );
		this.menuPeersComplete = pluginInterface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_COMPLETE, stringPeers );
		this.menuPeersIncomplete.addFillListener(peersFillListener);  
		this.menuPeersComplete.addFillListener(peersFillListener);
	}

	public void unload() throws PluginException {
		
		if (timer != null){
			this.timer.destroy();
		}
		this.globalInformation = null;
		this.info = null;
		if (uiSWTStatusEntry != null){
			this.uiSWTStatusEntry.destroy();
		}
		if (menuRemainings != null){
			this.menuRemainings.remove();
		}
		if (menuPeersComplete != null){
			this.menuPeersComplete.remove();
		}
		if (menuPeersIncomplete != null){
			this.menuPeersIncomplete.remove();
		}
		
	}
	
}
