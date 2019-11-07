package eu.crushedpixel.camerastudio.api.paths;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.spigot.animations.SpigotAsyncAnimation;
import eu.crushedpixel.camerastudio.CameraStudio;
import eu.crushedpixel.camerastudio.api.animations.NonTickBasedAnimation;
import eu.crushedpixel.camerastudio.api.events.CameraPathCompleteEvent;
import eu.crushedpixel.camerastudio.api.utils.GeneralUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CameraPath {

	private List<Location> travelPath;
	private long travelTime;
	private EnumEndPoint endPoint;
	private Location customEnd;

	private boolean inProgress = false;

	private transient DataHolder data;
	private transient List<Executor> runningExecutors = Lists.newArrayList();

	public CameraPath(List<Location> locations, long seconds, EnumEndPoint endPoint, Location customEnd) {
		this.travelPath = locations;
		this.travelTime = seconds;
		this.endPoint = endPoint;
		if(endPoint == EnumEndPoint.Custom) {
			if(customEnd == null) {
				this.customEnd = this.travelPath.get(this.travelPath.size() - 1);
			} else {
				this.customEnd = customEnd;
			}
		}

		this.init();
	}

	private void init() {
		this.data = new DataHolder();
		this.data.load(this.travelPath, this.travelTime);
		this.runningExecutors = Lists.newArrayList();
	}

	public void play(Player player, Consumer<UUID> callback) {
		if(this.data == null) {
			this.init();
		}

		for(Player other : Bukkit.getOnlinePlayers()) {
			if(!other.getUniqueId().equals(player.getUniqueId())) {
				player.hidePlayer(CameraStudio.instance, player);
			}
		}

		Executor executor = new Executor(player, callback.andThen(id -> {
			Bukkit.getPluginManager().callEvent(new CameraPathCompleteEvent(player));
		}));
		executor.load(this.travelTime, this.endPoint, this.customEnd, this.data);
		player.setGameMode(GameMode.SPECTATOR);
		this.inProgress = true;
		executor.play(false);
		this.runningExecutors.add(executor);
	}

	public boolean isInProgress() {
		return this.inProgress;
	}

	public void kill(Player player) {
		this.runningExecutors.stream().filter(executor -> executor.player.equals(player.getUniqueId())).findAny().ifPresent(executor -> {
			executor.stop();
			this.runningExecutors.remove(executor);
		});
	}

	public class Executor extends NonTickBasedAnimation {

		private UUID player;
		private Consumer<UUID> callback;

		private long time;

		private EnumEndPoint endPoint;
		private Location lEndPoint;

		private DataHolder data;

		private GameMode priorMode;

		Executor(Player player, Consumer<UUID> callback) {
			super(CameraStudio.instance);
			this.player = player.getUniqueId();
			this.priorMode = player.getGameMode();
			this.callback = callback;
		}

		void load(long time, EnumEndPoint endPoint, Location customEnd, DataHolder data) {
			this.time = time;
			this.endPoint = endPoint;
			this.lEndPoint = customEnd;

			this.data = data;
		}

		@Override
		protected void playFrame(int i) {
			if(i >= data.tps.size()) {
				this.stop();
				return;
			}
			Bukkit.getScheduler().runTask(CameraStudio.instance, () -> Bukkit.getPlayer(player).teleport(data.tps.get(i)));
		}

		@Override
		public int getFPS() {
			return 20;
		}

		@Override
		public int getNumFrames() {
			return (int) this.time * 20;
		}

		@Override
		public void clean() {}

		@Override
		public boolean isComplete() {
			return this.getCurrentFrame() >= this.getNumFrames();
		}

		@Override
		public Runnable whenComplete() {
			return () -> {
				Bukkit.getScheduler().runTask(CameraStudio.instance, () -> {
					Player p = Bukkit.getPlayer(player);
					if(this.endPoint == EnumEndPoint.Custom) {
						p.teleport(this.lEndPoint, PlayerTeleportEvent.TeleportCause.PLUGIN);
						p.setGameMode(this.priorMode);
						for(Player o : Bukkit.getOnlinePlayers()) {
							if(!o.getUniqueId().equals(p.getUniqueId())) {
								p.showPlayer(CameraStudio.instance, o);
							}
						}
					}
				});
				callback.accept(player);
				runningExecutors.removeIf(executor -> executor.player.equals(player));
			};
		}
	}

	public class DataHolder {
		private List<Double> diffs = Lists.newArrayList();
		private List<Integer> travelTimes = Lists.newArrayList();
		private List<Location> tps = Lists.newArrayList();

		double totalDiff = 0.0;

		void load(List<Location> locations, long time) {
			for(int i = 0; i < locations.size() - 1; i++) {
				double diff = GeneralUtils.positionDifference(locations.get(i), locations.get(i + 1));
				totalDiff += diff;
				diffs.add(diff);
			}

			for(double d : diffs) {
				travelTimes.add((int) (d / totalDiff * time));
			}

			World world = locations.get(0).getWorld();
			for(int i = 0; i < locations.size() - 1; i++) {
				Location s = locations.get(i);
				Location n = locations.get(i + 1);
				int t = travelTimes.get(i);

				double moveX = n.getX() - s.getX();
				double moveY = n.getY() - s.getY();
				double moveZ = n.getZ() - s.getZ();
				double movePitch = n.getPitch() - s.getPitch();
				double yawDiff = Math.abs(n.getYaw() - s.getYaw());
				double c;

				if(yawDiff <= 180.0) {
					if(s.getYaw() < n.getYaw()) {
						c = yawDiff;
					} else {
						c = -yawDiff;
					}
				} else if(s.getYaw() < n.getYaw()) {
					c = -(360.0 - yawDiff);
				} else {
					c = 360 - yawDiff;
				}

				double d = c / t;
				for(int x = 0; x < t; x++) {
					Location l = new Location(
							world,
							s.getX() + moveX / t * x,
							s.getY() + moveY / t * x,
							s.getZ() + moveZ / t * x,
							(float) (s.getYaw() + d * x),
							(float) (s.getPitch() + movePitch / t * x)
					);
					tps.add(l);
				}
			}
		}
	}
}
