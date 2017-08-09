package org.shu.plug.globalinformation;

import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.utils.Formatters;
import com.biglybt.pif.utils.LocaleUtilities;

public class GlobalInformation {
	
	private long totalRemaining, totalRemainingQueued, totalRemainingStopped, totalRemainingDownloading;
	private int totalSeeds, totalLeechers;
	private PluginInterface pluginInterface;
	
	public GlobalInformation(PluginInterface pluginInterface, long totalRemaining, long totalRemainingQueued, long totalRemainingStopped, long totalRemainingDownloading, int totalSeeds, int totalLeechers){
		this.pluginInterface = pluginInterface;
		this.totalRemaining = totalRemaining;
		this.totalRemainingQueued = totalRemainingQueued;
		this.totalRemainingStopped = totalRemainingStopped;
		this.totalRemainingDownloading = totalRemainingDownloading;
		this.totalSeeds = totalSeeds;
		this.totalLeechers = totalLeechers;
	}

	public GlobalInformation(PluginInterface pluginInterface){
		this.pluginInterface = pluginInterface;
		resetAll();
	}
	
	private void resetAll(){
		this.totalRemaining = 0;
		this.totalRemainingQueued = 0;
		this.totalRemainingStopped = 0;
		this.totalRemainingDownloading = 0;
		this.totalLeechers = 0;
		this.totalSeeds = 0;
	}
	
	//Getters
	public long getTotalRemaining() {
		return totalRemaining;
	}
	public long getTotalRemainingQueued() {
		return totalRemainingQueued;
	}
	public long getTotalRemainingStopped() {
		return totalRemainingStopped;
	}
	public long getTotalRemainingDownloading() {
		return totalRemainingDownloading;
	}
	public int getTotalSeeds() {
		return totalSeeds;
	}
	public int getTotalLeechers() {
		return totalLeechers;
	}
	
	//Setters
	public void setTotalRemaining(long totalRemaining) {
		this.totalRemaining = totalRemaining;
	}
	public void setTotalRemainingQueued(long totalRemainingQueued) {
		this.totalRemainingQueued = totalRemainingQueued;
	}
	public void setTotalRemainingStopped(long totalRemainingStopped) {
		this.totalRemainingStopped = totalRemainingStopped;
	}
	public void setTotalRemainingDownloading(long totalRemainingDownloading) {
		this.totalRemainingDownloading = totalRemainingDownloading;
	}
	public void setTotalSeeds(int totalSeeds) {
		this.totalSeeds = totalSeeds;
	}
	public void setTotalLeechers(int totalLeechers) {
		this.totalLeechers = totalLeechers;
	}
	
	//Add To
	public void addToTotalRemaining(long totalRemaining) {
		this.totalRemaining += totalRemaining;
	}
	public void addToTotalRemainingQueued(long totalRemainingQueued) {
		this.totalRemainingQueued += totalRemainingQueued;
	}
	public void addToTotalRemainingStopped(long totalRemainingStopped) {
		this.totalRemainingStopped += totalRemainingStopped;
	}
	public void addToTotalRemainingDownloading(long totalRemainingDownloading) {
		this.totalRemainingDownloading += totalRemainingDownloading;
	}
	public void addToTotalSeeds(int totalSeeds) {
		this.totalSeeds += totalSeeds;
	}
	public void addToTotalLeechers(int totalLeechers) {
		this.totalLeechers += totalLeechers;
	}
	
	public String toString() {
		return toString(true);
	}
	
	public String toString(boolean bMini) {
		
		if(pluginInterface.getPluginconfig().getPluginBooleanParameter(Main.CFG_CUSTOM_BOOL)){
			RefreshInformation info = RefreshInformation.getInstance(pluginInterface);
			if (bMini){
				return parseCustomText(info.getUserTextTotalMini());
			} else {
				return parseCustomText(info.getUserTextTotal());
			}
		}
		
		StringBuilder text = new StringBuilder();
		text.append(toStringRemainings(bMini));
		
		if (bMini) text.append(" -- ");
		else text.append("\n");
		
		text.append(toStringPeers(bMini));
		return text.toString();
	}
	
	private String parseCustomText(String customUserText) {
		String text = "";
		Formatters formatter = pluginInterface.getUtilities().getFormatters();
		
		text = customUserText.replace("\\n", "\n");
		text = text.replace(RefreshInformation.CODE_R, ""+formatter.formatByteCountToKiBEtc(this.getTotalRemaining()));
		text = text.replace(RefreshInformation.CODE_D, ""+formatter.formatByteCountToKiBEtc(this.getTotalRemainingDownloading()));
		text = text.replace(RefreshInformation.CODE_Q, ""+formatter.formatByteCountToKiBEtc(this.getTotalRemainingQueued()));
		text = text.replace(RefreshInformation.CODE_ST, ""+formatter.formatByteCountToKiBEtc(this.getTotalRemainingStopped()));
		text = text.replace(RefreshInformation.CODE_I, ""+formatter.formatByteCountToKiBEtc((this.getTotalRemainingDownloading()+this.getTotalRemainingQueued())));
		text = text.replace(RefreshInformation.CODE_SD, ""+this.getTotalSeeds());
		text = text.replace(RefreshInformation.CODE_L, ""+this.getTotalLeechers());
		text = text.replace(RefreshInformation.CODE_P, ""+(this.getTotalLeechers()+this.getTotalSeeds()));
		
		return text.toString();
	}

	public String toStringRemainings(boolean bMini) {
		if(pluginInterface.getPluginconfig().getPluginBooleanParameter(Main.CFG_CUSTOM_BOOL)){
			RefreshInformation info = RefreshInformation.getInstance(pluginInterface);
			if (bMini){
				return parseCustomText(info.getUserTextRemainingMini());
			} else {
				throw new UnsupportedOperationException("This case is not implemented : " + bMini);
			}
		}
		
		String mini = "", separator = "\n";
		StringBuilder text = new StringBuilder();
		if (bMini){
			mini = ".mini";
			separator = " - ";
		}
		
		LocaleUtilities local = pluginInterface.getUtilities().getLocaleUtilities();
		Formatters formatter = pluginInterface.getUtilities().getFormatters();
		text.append(local.getLocalisedMessageText("shu.plugin.total.remaining"+mini) + " " +
				formatter.formatByteCountToKiBEtc(this.getTotalRemaining()));
		text.append(separator + "" + local.getLocalisedMessageText("shu.plugin.total.remaining.downloading"+mini) + " " +
				formatter.formatByteCountToKiBEtc(this.getTotalRemainingDownloading()));
		text.append(separator + "" + local.getLocalisedMessageText("shu.plugin.total.remaining.queued"+mini) + " " + 
				formatter.formatByteCountToKiBEtc(this.getTotalRemainingQueued()));
		text.append(separator + "" + local.getLocalisedMessageText("shu.plugin.total.remaining.stopped"+mini) + " " + 
				formatter.formatByteCountToKiBEtc(this.getTotalRemainingStopped()));
		text.append(separator + "" + local.getLocalisedMessageText("shu.plugin.total.remaining.dlingandqueued"+mini) + " " + 
				formatter.formatByteCountToKiBEtc((this.getTotalRemainingDownloading()+this.getTotalRemainingQueued())));
		return text.toString();
	}
	
	public String toStringPeers(boolean bMini) {
		if(pluginInterface.getPluginconfig().getPluginBooleanParameter(Main.CFG_CUSTOM_BOOL)){
			RefreshInformation info = RefreshInformation.getInstance(pluginInterface);
			if (bMini){
				return parseCustomText(info.getUserTextPeersMini());
			} else {
				throw new UnsupportedOperationException("This case is not implemented : " + bMini);
			}
		}
		
		String mini = "", separator = "\n";
		StringBuilder text = new StringBuilder();
		if (bMini){
			mini = ".mini";
			separator = " - ";
		}
		LocaleUtilities local = pluginInterface.getUtilities().getLocaleUtilities();
		text.append(local.getLocalisedMessageText("shu.plugin.total.seeds"+mini) + " " + 
				this.getTotalSeeds());
		text.append(separator + "" + local.getLocalisedMessageText("shu.plugin.total.leech"+mini) + " " + 
				this.getTotalLeechers());
		text.append(separator + "" + local.getLocalisedMessageText("shu.plugin.total.peers"+mini) + " " + 
				(this.getTotalSeeds()+this.getTotalLeechers()));
		
		return text.toString();
	}
	
}
