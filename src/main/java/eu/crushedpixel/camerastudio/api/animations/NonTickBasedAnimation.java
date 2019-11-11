package eu.crushedpixel.camerastudio.api.animations;

import com.nickimpact.impactor.api.animations.SyncAnimation;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.spigot.SpigotImpactorPlugin;
import org.bukkit.Bukkit;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class NonTickBasedAnimation extends SyncAnimation<ScheduledExecutorService> {

	public NonTickBasedAnimation(ImpactorPlugin plugin) {
		super(plugin);
	}

	@Override
	public void play(long delay, boolean loop) {
		this.runner = Executors.newScheduledThreadPool(1);
		this.runner.scheduleAtFixedRate(() -> this.run(loop), delay, Math.round(1000.0 / getFPS()), TimeUnit.MILLISECONDS);
	}

	@Override
	public void stop() {
		this.runner.shutdownNow();
	}

	@Override
	public void fireAsync(Runnable runnable) {
		Bukkit.getScheduler().runTaskAsynchronously(SpigotImpactorPlugin.getInstance(), runnable);
	}
}
