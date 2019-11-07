package eu.crushedpixel.camerastudio.api.utils;

import com.nickimpact.impactor.api.utilities.Time;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class GeneralUtils {

	public static double positionDifference(Location cLoc, Location eLoc) {
		double cX = cLoc.getX();
		double cY = cLoc.getY();
		double cZ = cLoc.getZ();

		double eX = eLoc.getX();
		double eY = eLoc.getY();
		double eZ = eLoc.getZ();

		double dX = eX - cX;
		if (dX < 0.0D) {
			dX = -dX;
		}
		double dZ = eZ - cZ;
		if (dZ < 0.0D) {
			dZ = -dZ;
		}
		double dXZ = Math.hypot(dX, dZ);

		double dY = eY - cY;
		if (dY < 0.0D) {
			dY = -dY;
		}

		return Math.hypot(dXZ, dY);
	}

	/**
	 * Takes in a time formatted string, and attempts to convert the amount of requested time into ticks by
	 * the number of seconds it would normally take.
	 *
	 * @param input The time request
	 * @return The time converted into ticks
	 * @throws IllegalArgumentException If the time request does not meet the proper format
	 */
	public static long parseTimeString(String input) throws IllegalArgumentException {
		return new Time(input).getTime() * 20;
	}

	public static String getSerializedLocation(Location loc) {
		return loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getWorld().getUID() + ";" + loc.getPitch() + ";" + loc.getYaw();
	}

	public static Location getDeserializedLocation(String s) {
		String [] parts = s.split(";");
		double x = Double.parseDouble(parts[0]);
		double y = Double.parseDouble(parts[1]);
		double z = Double.parseDouble(parts[2]);
		UUID u = UUID.fromString(parts[3]);
		float pitch = Float.parseFloat(parts[4]);
		float yaw = Float.parseFloat(parts[5]);
		World w = Bukkit.getServer().getWorld(u);
		return new Location(w, x, y, z, yaw, pitch);
	}

}
