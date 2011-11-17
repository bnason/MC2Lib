package com.nason.minecraft.events;

public interface PlayerListItemEvent
{
	public void trigger(String name, Boolean online, int ping);
}