package org.zaproxy.zap.extension.customFire;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.zaproxy.zap.extension.ascan.ScanPolicy;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 */
public class CustomScanPolicy extends ScanPolicy
{

	private String name;
	private PluginFactory pluginFactory = new PluginFactory();
	private AlertThreshold defaultThreshold;
	private AttackStrength defaultStrength;
	private ZapXmlConfiguration conf;

	public CustomScanPolicy () {
		//    	super.setDefaultThreshold(AlertThreshold.MEDIUM);
		//    	super.setDefaultStrength(AttackStrength.MEDIUM);
		conf = new ZapXmlConfiguration();
		name = conf.getString("policy", "");
		pluginFactory.loadAllPlugin(conf);
		setDefaultThreshold(AlertThreshold.MEDIUM);

		setDefaultStrength(AttackStrength.MEDIUM);


	}

	public CustomScanPolicy (ZapXmlConfiguration conf) throws ConfigurationException {
		this.conf = conf;
		name = conf.getString("policy", "");
		pluginFactory.loadAllPlugin(conf);

		setDefaultThreshold(AlertThreshold.valueOf(conf.getString("scanner.level", AlertThreshold.MEDIUM.name())));

		setDefaultStrength(AttackStrength.valueOf(conf.getString("scanner.strength", AttackStrength.MEDIUM.name())));

	}

	public CustomScanPolicy (FileConfiguration conf) throws ConfigurationException {
		pluginFactory.loadAllPlugin(conf);
		this.conf = new ZapXmlConfiguration();
		name = "";
		setDefaultThreshold(AlertThreshold.MEDIUM);

		setDefaultStrength(AttackStrength.MEDIUM);

	}

	public CustomScanPolicy clonePolicy () throws ConfigurationException {
		return new CustomScanPolicy((ZapXmlConfiguration)this.conf.clone()); 
	}

	public void cloneInto(CustomScanPolicy policy) {
		policy.pluginFactory.loadFrom(this.pluginFactory);
		policy.defaultStrength = this.getDefaultStrength();
		policy.defaultThreshold = this.getDefaultThreshold();
	}

	public String getName() {
		return name;
	}

	public PluginFactory getPluginFactory() {
		return pluginFactory;
	}

	public AlertThreshold getDefaultThreshold() {
		return defaultThreshold;
	}

	public AttackStrength getDefaultStrength() {
		return defaultStrength;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setDefaultThreshold(AlertThreshold defaultThreshold) {

		this.defaultThreshold = defaultThreshold;
		
		/*if (pluginFactory == null) {
			pluginFactory = new PluginFactory();
			
		}
		for (Plugin plugin : pluginFactory.getAllPlugin()) {//here
			
			plugin.setDefaultAlertThreshold(defaultThreshold);

		}*/
	}
	
	@Override
	public void setDefaultStrength(AttackStrength defaultStrength) {
		this.defaultStrength = defaultStrength;
	/*	for (Plugin plugin : pluginFactory.getAllPlugin()) {
			plugin.setDefaultAttackStrength(defaultStrength);
		}*/
	}

	public void save() throws ConfigurationException {
		this.conf.save();
	}

}
