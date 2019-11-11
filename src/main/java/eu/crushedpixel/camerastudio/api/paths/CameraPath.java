package eu.crushedpixel.camerastudio.api.paths;

import com.google.common.collect.Lists;
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
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CameraPath {

	private UUID world;
	private List<Vector> travelPath;
	private long travelTime;
	private EnumEndPoint endPoint;
	private Location customEnd;

	private boolean inProgress = false;

	private transient DataHolder data;
	private transient List<Executor> runningExecutors = Lists.newArrayList();

	public CameraPath(World world, List<Vector> locations, long seconds, EnumEndPoint endPoint, Location customEnd) {
		this.world = world.getUID();
		this.travelPath = locations;
		this.travelTime = seconds * 20;
		this.endPoint = endPoint;
		if(endPoint == EnumEndPoint.Custom) {
			if(customEnd == null) {
				Vector end = this.travelPath.get(this.travelPath.size() - 1);
				this.customEnd = new Location(world, end.getX(), end.getY(), end.getZ());
			} else {
				this.customEnd = customEnd;
			}
		}

		this.init();
	}

	private void init() {
		this.data = new DataHolder();
		this.data.load(Bukkit.getWorld(this.world), this.travelPath, this.travelTime);
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

		Executor2 executor = new Executor2(player, callback.andThen(id -> {
			Bukkit.getPluginManager().callEvent(new CameraPathCompleteEvent(player));
		}));
		executor.load(this.travelTime, this.endPoint, this.customEnd, this.data);
		player.setGameMode(GameMode.SPECTATOR);
		this.inProgress = true;
		executor.run();
		//this.runningExecutors.add(executor);
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

	public class Executor2 {
		private UUID player;
		private Consumer<UUID> callback;

		private EnumEndPoint endPoint;
		private Location lEndPoint;

		private DataHolder data;

		private GameMode priorMode;

		private int frames = 0;

		Executor2(Player player, Consumer<UUID> callback) {
			this.player = player.getUniqueId();
			this.priorMode = player.getGameMode();
			this.callback = callback;
		}

		void load(long time, EnumEndPoint endPoint, Location customEnd, DataHolder data) {
			this.endPoint = endPoint;
			this.lEndPoint = customEnd;

			this.data = data;
		}

		public void run() {
			Player player = Bukkit.getPlayer(this.player);
			player.teleport(data.tps.get(0));
			player.setGameMode(GameMode.SPECTATOR);
			Bukkit.getScheduler().scheduleSyncDelayedTask(CameraStudio.instance, new Runnable() {
				@Override
				public void run() {
					if(frames < data.tps.size()) {
						player.teleport(data.tps.get(frames++));
						Bukkit.getScheduler().scheduleSyncDelayedTask(CameraStudio.instance, this, 2L);
					} else {
						if(endPoint == EnumEndPoint.Custom) {
							player.teleport(lEndPoint, PlayerTeleportEvent.TeleportCause.PLUGIN);
						}

						player.setGameMode(priorMode);
						for(Player o : Bukkit.getOnlinePlayers()) {
							if(!o.getUniqueId().equals(player.getUniqueId())) {
								player.showPlayer(CameraStudio.instance, o);
							}
						}
						callback.accept(player.getUniqueId());
					}
				}
			}, 2L);
		}

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
				this.end();
				return;
			}

			Bukkit.getPlayer(player).teleport(data.tps.get(i), PlayerTeleportEvent.TeleportCause.PLUGIN);
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
				Player p = Bukkit.getPlayer(player);
				if(this.endPoint == EnumEndPoint.Custom) {
					p.teleport(this.lEndPoint, PlayerTeleportEvent.TeleportCause.PLUGIN);
				}

				p.setGameMode(this.priorMode);
				for(Player o : Bukkit.getOnlinePlayers()) {
					if(!o.getUniqueId().equals(p.getUniqueId())) {
						p.showPlayer(CameraStudio.instance, o);
					}
				}
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

		void load(World world, List<Vector> locations, long time) {
			for(int i = 0; i < locations.size() - 1; i++) {
				Vector o = locations.get(i);
				Vector p = locations.get(i + 1);

				double diff = GeneralUtils.positionDifference(new Location(world, o.getX(), o.getY(), o.getZ()), new Location(world, p.getX(), p.getY(), p.getZ()));
				totalDiff += diff;
				diffs.add(diff);
			}

			for(double d : diffs) {
				travelTimes.add((int) (d / totalDiff * time));
			}

			for(int i = 0; i < locations.size() - 1; i++) {
				Vector o = locations.get(i);
				Vector p = locations.get(i + 1);

				Location s = new Location(world, o.getX(), o.getY(), o.getZ());
				Location n = new Location(world, p.getX(), p.getY(), p.getZ());
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
