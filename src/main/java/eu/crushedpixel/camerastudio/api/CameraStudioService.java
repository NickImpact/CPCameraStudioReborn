package eu.crushedpixel.camerastudio.api;

import eu.crushedpixel.camerastudio.api.paths.CameraPath;
import eu.crushedpixel.camerastudio.api.paths.CameraPosition;
import eu.crushedpixel.camerastudio.api.paths.EnumEndPoint;
import eu.crushedpixel.camerastudio.api.utils.GeneralUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface CameraStudioService {

	/**
	 * Allows a player to travel along a set of points, and ultimately view the landscape or scene from a cinematic
	 * perspective. A player who is in this state will have their gamemode set to Spectator, and have all players
	 * on the server hidden from them until the cinematic ends.
	 *
	 * @param player The player that'll travel along this set of points
	 * @param timeInput The amount of time the cinematic should take
	 * @param positions The locations to draw the cinematic path on
	 * @param endPoint The type of end point the path should have
	 * @param customEnd If the end point is custom, it must have that value specified. Otherwise, will default to path's end
	 */
	default void travel(Player player, String timeInput, List<CameraPosition> positions, EnumEndPoint endPoint, Location customEnd) {
		this.travel(player, GeneralUtils.parseTimeString(timeInput), positions, endPoint, customEnd);
	}

	void travel(Player player, long time, List<CameraPosition> positions, EnumEndPoint endPoint, Location customEnd);

	void travel(Player player, CameraPath path);

	boolean isTravelling(Player player);

	void stop(Player player);

}
