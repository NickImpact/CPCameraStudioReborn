package eu.crushedpixel.camerastudio;

import java.util.List;
import java.util.function.Consumer;

import co.aikar.commands.BaseCommand;
import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.spigot.logging.SpigotLogger;
import eu.crushedpixel.camerastudio.api.CameraStudioService;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class CameraStudio extends JavaPlugin implements Listener, ImpactorPlugin {

	public static CameraStudio instance;
	private Logger logger;
	private boolean connected;

	public CameraStudio() {
		this.connect();
	}

	public void onEnable() {
		instance = this;
		this.logger = new SpigotLogger(this);

		this.getPluginLogger().info("CPCameraStudioReborn has been enabled!");
		Bukkit.getServicesManager().register(CameraStudioService.class, new CamStudioServiceImpl(), this, ServicePriority.Highest);
	}

	@Override
	public Platform getPlatform() {
		return Platform.Spigot;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo() {
			@Override
			public String getID() {
				return "camera_studio";
			}

			@Override
			public String getName() {
				return "Unbound Cinematics";
			}

			@Override
			public String getVersion() {
				return "1.0.0";
			}

			@Override
			public String getDescription() {
				return "";
			}
		};
	}

	@Override
	public Logger getPluginLogger() {
		return this.logger;
	}

	@Override
	public List<Config> getConfigs() {
		return Lists.newArrayList();
	}

	@Override
	public List<BaseCommand> getCommands() {
		return Lists.newArrayList();
	}

	@Override
	public List<Object> getListeners() {
		return Lists.newArrayList();
	}

	@Override
	public Consumer<ImpactorPlugin> onReload() {
		return x -> {};
	}

	@Override
	public boolean isConnected() {
		return this.connected;
	}

	@Override
	public void setConnected() {
		this.connected = true;
	}

	@Override
	public void handleDisconnect() {

	}
}