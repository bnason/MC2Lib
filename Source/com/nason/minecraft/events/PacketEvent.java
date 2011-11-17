package com.nason.minecraft.events;

public interface PacketEvent
{
	public void trigger(byte[] data, long size);
}