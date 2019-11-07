package eu.crushedpixel.camerastudio.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CameraPathCompleteEvent extends Event {

	private Player player;

	private static HandlerList handlers = new HandlerList();

	public CameraPathCompleteEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
