package eu.crushedpixel.camerastudio.api.paths;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CameraPosition {

	private UUID world;
	private int x;
	private int y;
	private int z;
	private float pitch;
	private float yaw;

	public Location getLocation() {
		return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z, this.yaw, this.pitch);
	}

}
