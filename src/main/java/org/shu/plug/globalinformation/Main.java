package org.shu.plug.globalinformation;

import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.UnloadablePlugin;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.ui.UIInstance;
import org.gudy.azureus2.plugins.ui.UIManagerListener;
import org.gudy.azureus2.plugins.ui.UIMessage;
import org.gudy.azureus2.plugins.ui.config.ActionParameter;
import org.gudy.azureus2.plugins.ui.config.Parameter;
import org.gudy.azureus2.plugins.ui.config.ParameterListener;
import org.gudy.azureus2.plugins.ui.config.StringParameter;
import org.gudy.azureus2.plugins.ui.menus.MenuItem;
import org.gudy.azureus2.plugins.ui.menus.MenuItemFillListener;
import org.gudy.azureus2.plugins.ui.menus.MenuItemListener;
import org.gudy.azureus2.plugins.ui.menus.MenuManager;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.plugins.ui.tables.TableContextMenuItem;
import org.gudy.azureus2.plugins.ui.tables.TableManager;
import org.gudy.azureus2.plugins.ui.tables.TableRow;
import org.gudy.azureus2.plugins.utils.UTTimer;
import org.gudy.azureus2.plugins.utils.UTTimerEvent;
import org.gudy.azureus2.plugins.utils.UTTimerEventPerformer;
import org.gudy.azureus2.ui.swt.plugins.UISWTInstance;
import org.gudy.azureus2.ui.swt.plugins.UISWTStatusEntry;
import org.gudy.azureus2.ui.swt.plugins.UISWTStatusEntryListener;

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
            
			public void UIAttached(UIInstance instance) {
            	
                if (instance instanceof UISWTInstance) {
                    swtInstance = ((UISWTInstance) instance);
                    
                    
                    createStatusEntry(swtInstance);
                    addMyTorrentsMenu();
                    uiTimerListerner();
                    
                }
            }
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
			public void perform(UTTimerEvent event){
				updateStatusBarItem();
			}
		});
	}
	
	private void addMyTorrentsMenu()  {
		MenuItemFillListener remainingsFillListener = new MenuItemFillListener(){
			
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
