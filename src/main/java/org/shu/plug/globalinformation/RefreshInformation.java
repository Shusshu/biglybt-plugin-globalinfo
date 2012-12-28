package org.shu.plug.globalinformation;

import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.peers.PeerManager;
import org.gudy.azureus2.plugins.peers.PeerManagerStats;
import org.gudy.azureus2.plugins.utils.LocaleUtilities;

public class RefreshInformation {
	
	private static RefreshInformation instance;
	
	private static final String SEPARATOR = "\\n";
	private static final String SEPARATOR_MINI = " - ";
	
	public static final String CODE_R = "%R%";
	public static final String CODE_D = "%D%";
	public static final String CODE_Q = "%Q%";
	public static final String CODE_ST = "%ST%";
	public static final String CODE_I = "%I%";
	public static final String CODE_SD = "%SD%";
	public static final String CODE_L = "%L%";
	public static final String CODE_P = "%P%";
	/*
	public static String DEFAULT_TEXT_REMAINING_MINI = "R "+CODE_R+ "" +SEPARATOR_MINI+ "D "+CODE_D+ "" +SEPARATOR_MINI+ "Q "+CODE_Q+ "" +SEPARATOR_MINI+ "ST "+CODE_ST+ "" +SEPARATOR_MINI+ "I "+CODE_I;
	public static String DEFAULT_TEXT_PEERS_MINI = "SD "+CODE_SD+ "" +SEPARATOR_MINI+ "L "+CODE_L+ "" +SEPARATOR_MINI+ "P "+CODE_P;
	public static String DEFAULT_TEXT_TOTAL_MINI = DEFAULT_TEXT_REMAINING_MINI+" -- "+DEFAULT_TEXT_PEERS_MINI;
	public static String DEFAULT_TEXT_TOTAL = 
										"Total remaining "+ CODE_R + "" + SEPARATOR +
										"Total downloading "+ CODE_D + "" + SEPARATOR +
										"Total queued "+ CODE_Q + "" + SEPARATOR +
										"Total stopped "+ CODE_ST + "" + SEPARATOR +
										"Total incoming "+ CODE_I + "" + SEPARATOR +
										"Total seeds "+ CODE_SD + "" + SEPARATOR +
										"Total leechers "+ CODE_L + "" + SEPARATOR +
										"Total peers "+ CODE_P;
	*/
	private String defaultTextRemainingMini, defaultTextPeersMini, defaultTextTotalMini, defaultTextTotal;
	private String userTextRemainingMini, userTextPeersMini, userTextTotalMini, userTextTotal;
	
	
	private RefreshInformation(PluginInterface pluginInterface){
		initialize(pluginInterface);
	}

	public static RefreshInformation getInstance(PluginInterface pluginInterface){
		if(instance == null) instance = new RefreshInformation(pluginInterface);
		return instance;
	}
	
	public void initialize(PluginInterface pluginInterface){
		LocaleUtilities local = pluginInterface.getUtilities().getLocaleUtilities();
		
		defaultTextRemainingMini = local.getLocalisedMessageText("shu.plugin.total.remaining.mini") + " " + CODE_R + "" +SEPARATOR_MINI+ 
								local.getLocalisedMessageText("shu.plugin.total.remaining.downloading.mini") + " " + CODE_D + "" +SEPARATOR_MINI+ 
								local.getLocalisedMessageText("shu.plugin.total.remaining.queued.mini") + " " + CODE_Q + "" +SEPARATOR_MINI+ 
								local.getLocalisedMessageText("shu.plugin.total.remaining.stopped.mini") + " " + CODE_ST + "" +SEPARATOR_MINI+ 
								local.getLocalisedMessageText("shu.plugin.total.remaining.dlingandqueued.mini") + " " + CODE_I;
		
		defaultTextPeersMini = local.getLocalisedMessageText("shu.plugin.total.seeds.mini") + " " + CODE_SD + "" +SEPARATOR_MINI+ 
								local.getLocalisedMessageText("shu.plugin.total.leech.mini") + " " + CODE_L + "" +SEPARATOR_MINI+ 
								local.getLocalisedMessageText("shu.plugin.total.peers.mini") + " " + CODE_P;
		
		defaultTextTotalMini = defaultTextRemainingMini+" -- "+defaultTextPeersMini;
		
		defaultTextTotal = 
				local.getLocalisedMessageText("shu.plugin.total.remaining") + " " + CODE_R + "" + SEPARATOR +
				local.getLocalisedMessageText("shu.plugin.total.remaining.downloading") + " " + CODE_D + "" + SEPARATOR +
				local.getLocalisedMessageText("shu.plugin.total.remaining.queued") + " " + CODE_Q + "" + SEPARATOR +
				local.getLocalisedMessageText("shu.plugin.total.remaining.stopped") + " " + CODE_ST + "" + SEPARATOR +
				local.getLocalisedMessageText("shu.plugin.total.remaining.dlingandqueued") + " " + CODE_I + "" + SEPARATOR +
				local.getLocalisedMessageText("shu.plugin.total.seeds") + " " + CODE_SD + "" + SEPARATOR +
				local.getLocalisedMessageText("shu.plugin.total.leech") + " " + CODE_L + "" + SEPARATOR +
				local.getLocalisedMessageText("shu.plugin.total.peers") + " " + CODE_P;
		
		userTextRemainingMini = pluginInterface.getPluginconfig().getPluginStringParameter(Main.CFG_CUSTOM_TEXT_REMAINING_MINI, defaultTextRemainingMini);
		userTextPeersMini = pluginInterface.getPluginconfig().getPluginStringParameter(Main.CFG_CUSTOM_TEXT_PEERS_MINI, defaultTextPeersMini);
		userTextTotalMini = pluginInterface.getPluginconfig().getPluginStringParameter(Main.CFG_CUSTOM_TEXT_MINI, defaultTextTotalMini);
		userTextTotal = pluginInterface.getPluginconfig().getPluginStringParameter(Main.CFG_CUSTOM_TEXT, defaultTextTotal);
		
	}
	
	public GlobalInformation refresh(PluginInterface pluginInterface, Download[] downloads) {
		GlobalInformation information = new GlobalInformation(pluginInterface);
		for(int i=0; i<downloads.length; i++){
			calculateRemaining(information, downloads[i]);
			calculateTotalPeers(information, downloads[i]);
		}
		return information;
	}
	
	public void calculateTotalPeers(GlobalInformation remainings, Download download) {
		PeerManager pm = download.getPeerManager();
		if(pm != null){
			PeerManagerStats peersStats = pm.getStats();
			if (peersStats != null){
				remainings.addToTotalSeeds(peersStats.getConnectedSeeds());
				remainings.addToTotalLeechers(peersStats.getConnectedLeechers());
			}	
		}
		
		
	}

	public void calculateRemaining(GlobalInformation remainings, Download download){
		long remaining = download.getStats().getRemaining();
		
		switch(download.getState()){
			default : ; break;
			case Download.ST_ERROR : 
			case Download.ST_STOPPED : 
			case Download.ST_STOPPING : remainings.addToTotalRemainingStopped(remaining); break;
			case Download.ST_WAITING : 
			case Download.ST_PREPARING : 
			case Download.ST_READY : 
			case Download.ST_DOWNLOADING : remainings.addToTotalRemainingDownloading(remaining); break;
			case Download.ST_QUEUED : remainings.addToTotalRemainingQueued(remaining); break;
		}
		remainings.addToTotalRemaining(remaining);
	}

	public String getDefaultTextRemainingMini() {
		return defaultTextRemainingMini;
	}

	public void setDefaultTextRemainingMini(String defaultTextRemainingMini) {
		this.defaultTextRemainingMini = defaultTextRemainingMini;
	}

	public String getDefaultTextPeersMini() {
		return defaultTextPeersMini;
	}

	public void setDefaultTextPeersMini(String defaultTextPeersMini) {
		this.defaultTextPeersMini = defaultTextPeersMini;
	}

	public String getDefaultTextTotalMini() {
		return defaultTextTotalMini;
	}

	public void setDefaultTextTotalMini(String defaultTextTotalMini) {
		this.defaultTextTotalMini = defaultTextTotalMini;
	}

	public String getDefaultTextTotal() {
		return defaultTextTotal;
	}

	public void setDefaultTextTotal(String defaultTextTotal) {
		this.defaultTextTotal = defaultTextTotal;
	}

	public String getUserTextRemainingMini() {
		return userTextRemainingMini;
	}

	public void setUserTextRemainingMini(String userTextRemainingMini) {
		this.userTextRemainingMini = userTextRemainingMini;
	}

	public String getUserTextPeersMini() {
		return userTextPeersMini;
	}

	public void setUserTextPeersMini(String userTextPeersMini) {
		this.userTextPeersMini = userTextPeersMini;
	}

	public String getUserTextTotalMini() {
		return userTextTotalMini;
	}

	public void setUserTextTotalMini(String userTextTotalMini) {
		this.userTextTotalMini = userTextTotalMini;
	}

	public String getUserTextTotal() {
		return userTextTotal;
	}

	public void setUserTextTotal(String userTextTotal) {
		this.userTextTotal = userTextTotal;
	}


}
