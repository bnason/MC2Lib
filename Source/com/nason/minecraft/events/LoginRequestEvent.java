package com.nason.minecraft.events;

public interface LoginRequestEvent
{
	public void trigger(long protocolVersion, String username, long mapSeed, int serverMode, byte dimension, byte difficulty, int worldHeight, int maxPlayers);
}