package eu.crushedpixel.camerastudio;

import com.google.common.collect.Maps;
import eu.crushedpixel.camerastudio.api.paths.CameraPath;
import eu.crushedpixel.camerastudio.api.CameraStudioService;
import eu.crushedpixel.camerastudio.api.paths.EnumEndPoint;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CamStudioServiceImpl implements CameraStudioService {

	private Map<UUID, CameraPath> paths = Maps.newHashMap();

	@Override
	public void travel(Player player, long time, World world, List<Vector> locations, List<CameraPath.Rotation> rotations, EnumEndPoint endPoint, Location customEnd) {
		CameraPath path = new CameraPath(world, locations, rotations, time, endPoint, customEnd);
		this.paths.put(player.getUniqueId(), path);
		path.play(player, id -> paths.remove(id));
	}

	@Override
	public void travel(Player player, CameraPath path) {
		this.paths.put(player.getUniqueId(), path);
		path.play(player, id -> paths.remove(id));
	}

	@Override
	public boolean isTravelling(Player player) {
		return paths.entrySet().stream().filter(entry -> entry.getKey().equals(player.getUniqueId()) && entry.getValue().isInProgress()).map(entry -> true).findAny().orElse(false);
	}

	@Override
	public void stop(Player player) {
		if(paths.containsKey(player.getUniqueId())) {
			paths.get(player.getUniqueId()).kill(player);
		}
	}

}
