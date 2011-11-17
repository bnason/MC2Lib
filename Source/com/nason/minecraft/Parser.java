package com.nason.minecraft;

import java.io.BufferedInputStream;
/*import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;*/
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.nason.minecraft.events.*;

public class Parser
{
	private Boolean isServer = false;
	private InputStream stream;
	private BufferedInputStream bstream;
	private Thread thread;
//	private FileOutputStream fileStream;
//	private DataOutputStream dataFileStream;

	// Event Handler Lists
	private List<PacketEvent> packetEvents;
	private List<MessageEvent> messageEvents;
	private List<PlayerListItemEvent> playerListItemEvents;
	private List<LoginRequestEvent> loginRequestHandlers;

	public void setIsServer(Boolean isServer) { this.isServer = isServer; }

	public Parser(InputStream stream)
	{
		this.stream = stream;
		this.bstream = new BufferedInputStream(stream, 1048576);
		this.packetEvents = new ArrayList<com.nason.minecraft.events.PacketEvent>();
		this.messageEvents = new ArrayList<com.nason.minecraft.events.MessageEvent>();
		this.playerListItemEvents = new ArrayList<com.nason.minecraft.events.PlayerListItemEvent>();
		this.loginRequestHandlers = new ArrayList<LoginRequestEvent>();
	}

	public void invokeOnPacket(com.nason.minecraft.events.PacketEvent handler) { packetEvents.add(handler); }
	public void invokeOnMessage(com.nason.minecraft.events.MessageEvent handler) { messageEvents.add(handler); }
	public void invokeOnPlayerListItem(com.nason.minecraft.events.PlayerListItemEvent handler) { playerListItemEvents.add(handler); }
	public void invokeOnLoginRequest(LoginRequestEvent handler) { loginRequestHandlers.add(handler); }

	public Thread parseInBackground()
	{
		// Create parsing thread
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				try
				{
					parse();
				}
				catch (IOException e)
				{
					try
					{
						stream.close();
					} catch (IOException e1) { e1.printStackTrace(); }
				}
			}

		};

		thread = new Thread(runnable);
		thread.start();
		return thread;
	}

	public void parse() throws IOException
	{
		final byte[] buffer = new byte[1048576];
		int bytesRead = 0;
		int id = 0;
		long packetLen = 0;
		int failedBytesRead = 0;
		Boolean enoughData = true;
		int lastPacket = -1;

		bstream.mark(1048576);
/*
		try
		{
			this.fileStream = new FileOutputStream("/Users/brandon/mcproto" + isServer.toString(), false);
		} catch (FileNotFoundException e) { e.printStackTrace(); }
		this.dataFileStream = new DataOutputStream(this.fileStream);
*/
		while ((bytesRead = bstream.read(buffer)) != -1 && !Thread.interrupted())
		{
			if (bytesRead > 0 && ((!enoughData && bytesRead > failedBytesRead) || enoughData))
			{
				id = readByteUnsigned(buffer, 0);

//				if (!isServer)
//					System.out.print((isServer ? "C<-S" : "C->S") + ": ID=0x" + Integer.toHexString(id) + " w/ " + bytesRead); // + " bytes: " + new String(buffer, 0, bytesRead));

				switch (id)
				{
					case 0x00:
						packetLen = KeepAlive(buffer, bytesRead); break;
					case 0x01:
						packetLen = LoginRequest(buffer, bytesRead); break;
					case 0x02:
						packetLen = Handshake(buffer, bytesRead); break;
					case 0x03:
						packetLen = ChatMessage(buffer, bytesRead); break;
					case 0x04:
						packetLen = TimeUpdate(buffer, bytesRead); break;
					case 0x05:
						packetLen = EntityEquipment(buffer, bytesRead); break;
					case 0x06:
						packetLen = SpawnPosition(buffer, bytesRead); break;
					case 0x07:
						packetLen = UseEntity(buffer, bytesRead); break;
					case 0x08:
						packetLen = UpdateHealth(buffer, bytesRead); break;
					case 0x09:
						packetLen = Respawn(buffer, bytesRead); break;
					case 0x0A:
						packetLen = Player(buffer, bytesRead); break;
					case 0x0B:
						packetLen = PlayerPosition(buffer, bytesRead); break;
					case 0x0C:
						packetLen = PlayerLook(buffer, bytesRead); break;
					case 0x0D:
						packetLen = PlayerPositionAndLook(buffer, bytesRead); break;
					case 0x0E:
						packetLen = PlayerDigging(buffer, bytesRead); break;
					case 0x0F:
						packetLen = PlayerBlockPlacement(buffer, bytesRead); break;
					case 0x10:
						packetLen = HoldingChange(buffer, bytesRead); break;
					case 0x11:
						packetLen = UseBed(buffer, bytesRead); break;
					case 0x12:
						packetLen = Animation(buffer, bytesRead); break;
					case 0x13:
						packetLen = EntityAction(buffer, bytesRead); break;
					case 0x14:
						packetLen = NamedEntitySpawn(buffer, bytesRead); break;
					case 0x15:
						packetLen = PickupSpawn(buffer, bytesRead); break;
					case 0x16:
						packetLen = CollectItem(buffer, bytesRead); break;
					case 0x17:
						packetLen = AddObjectOrVehicle(buffer, bytesRead); break;
					case 0x18:
						packetLen = MobSpawn(buffer, bytesRead); break;
					case 0x19:
						packetLen = EntityPainting(buffer, bytesRead); break;
					case 0x1A:
						packetLen = ExperienceOrb(buffer, bytesRead); break;
					case 0x1B:
						packetLen = StanceUpdate(buffer, bytesRead); break;
					case 0x1C:
						packetLen = EntityVelocity(buffer, bytesRead); break;
					case 0x1D:
						packetLen = DestroyEntity(buffer, bytesRead); break;
					case 0x1E:
						packetLen = Entity(buffer, bytesRead); break;
					case 0x1F:
						packetLen = EntityRelativeMove(buffer, bytesRead); break;
					case 0x20:
						packetLen = EntityLook(buffer, bytesRead); break;
					case 0x21:
						packetLen = EntityLookAndRelativeMove(buffer, bytesRead); break;
					case 0x22:
						packetLen = EntityTeleport(buffer, bytesRead); break;
					case 0x26:
						packetLen = EntityStatus(buffer, bytesRead); break;
					case 0x27:
						packetLen = AttachEntity(buffer, bytesRead); break;
					case 0x28:
						packetLen = EntityMetadata(buffer, bytesRead); break;
					case 0x29:
						packetLen = EntityEffect(buffer, bytesRead); break;
					case 0x2A:
						packetLen = RemoveEntityEffect(buffer, bytesRead); break;
					case 0x2B:
						packetLen = Experience(buffer, bytesRead); break;
					case 0x32:
						packetLen = PreChunk(buffer, bytesRead); break;
					case 0x33:
						packetLen = MapChunk(buffer, bytesRead); break;
					case 0x34:
						packetLen = MultiBlockChange(buffer, bytesRead); break;
					case 0x35:
						packetLen = BlockChange(buffer, bytesRead); break;
					case 0x36:
						packetLen = BlockAction(buffer, bytesRead); break;
					case 0x3C:
						packetLen = Explosion(buffer, bytesRead); break;
					case 0x3D:
						packetLen = SoundEffect(buffer, bytesRead); break;
					case 0x46:
						packetLen = NewOrInvalidState(buffer, bytesRead); break;
					case 0x47:
						packetLen = Thunderbolt(buffer, bytesRead); break;
					case 0x64:
						packetLen = OpenWindow(buffer, bytesRead); break;
					case 0x65:
						packetLen = CloseWindow(buffer, bytesRead); break;
					case 0x66:
						packetLen = WindowClick(buffer, bytesRead); break;
					case 0x67:
						packetLen = SetSlot(buffer, bytesRead); break;
					case 0x68:
						packetLen = WindowItems(buffer, bytesRead); break;
					case 0x69:
						packetLen = UpdateProgressBar(buffer, bytesRead); break;
					case 0x6A:
						packetLen = Transaction(buffer, bytesRead); break;
					case 0x6B:
						packetLen = CreativeInventoryAction(buffer, bytesRead); break;
					case 0x82:
						packetLen = UpdateSign(buffer, bytesRead); break;
					case 0x83:
						packetLen = ItemData(buffer, bytesRead); break;
					case 0xC8:
						packetLen = IncrementStatistic(buffer, bytesRead); break;
					case 0xC9:
						packetLen = PlayerListItem(buffer, bytesRead); break;
					case 0xFE:
						packetLen = ServerListPing(buffer, bytesRead); break;
					case 0xFF:
						packetLen = DisconnectOrKick(buffer, bytesRead); break;
					default:
//						if (isServer)
							System.err.println((isServer ? "C<-S" : "C->S") + "Unrecognized packet detected: 0x" + Integer.toHexString(id) + " last packet: 0x" + Integer.toHexString(lastPacket));

//							dataFileStream.write(buffer, 0, bytesRead);
							return;
				}

				if (packetLen > 0)
				{
					for (com.nason.minecraft.events.PacketEvent handler: packetEvents)
						handler.trigger(buffer, packetLen);

//					if (!isServer)
//						System.out.print("-" + packetLen);
//					dataFileStream.write(buffer, 0, bytesRead);
					enoughData = true;
					failedBytesRead = 0;
					lastPacket = buffer[0];
				}
				else
				{
					enoughData = false;
					failedBytesRead = bytesRead;
				}

				if (!isServer)
				{
//					System.out.println("");
					if (packetLen > bytesRead)
					{
						System.err.println("Packet parsing f'd!");
						return;
					}
				}

//				java.util.Arrays.fill(buffer, (byte)0);
				bytesRead = 0;
//				id = -1;
			}
			
			bstream.reset();
			bstream.skip(packetLen);
			bstream.mark(1048576);
			packetLen = 0;
		}

		stream.close();
	}

	// Data Types
	// Name      Size    Range                                        Notes
	// Byte      1       -128 to 127                                  Signed, two's complement
	// Short     2       -32879 to 32767                              Signed, two's complement
	// Integer   4       -2147483647 to 2147483647                    Signed, two's complement
	// Long      8       -9223372036854775808 to 9223372036854775807  Signed, two's complement
	// Float     4                                                    Single-precision 32-bit IEEE 754 floating point
	// Double    8                                                    Double-precision 64-bit IEEE 754 floating point
	// String8   >= 2    N/A                                          Modified UTF-8 string. Prefixed by a short containing the length of string
	// String16  >= 2    N/A                                          UCS-2 string, big-endian. Prefixed by a short containing the length of the string in characters. UCS-2 consists of 16-bit words, each of which represent a Unicode code point between U+0000 and U+FFFF inclusive.
	// Boolean   1       0 or 1                                       Value can be either True (0x01) or False (0x00)
	// Metadata  varies  See below                                    See Below

	static public byte readByteSigned(byte[] buffer, int off)
	{
		return buffer[off];
	}

	static public int readByteUnsigned(byte[] buffer, int off)
	{
		return (int)buffer[off] & 0xFF;
	}

	static public int readShortSigned(byte[] buffer, int off)
	{
		int value = 0;

//		for (int i = 0; i < 2; i++)
//			value += ((int)buffer[i + off]) << ((2 - i - 1) * 8);

		int high = buffer[off];
		int low = buffer[off + 1];

		value = high << 8;
		value |= low;

		return value;
	}

	static public int readShortUnsigned(byte[] buffer, int off)
	{
		int value = 0;

		for (int i = 0; i < 2; i++)
			value |= ((int)buffer[i + off] & 0xFF) << ((2 - i - 1) * 8);

		return value;
	}

	static public int readIntSigned(byte[] buffer, int off)
	{
		int value = 0;

		for (int i = 0; i < 4; i++)
			value |= ((int)buffer[i + off] & 0xFF) << ((4 - i - 1) * 8);

		return value;
	}

	static public long readIntUnsigned(byte[] buffer, int off)
	{
		long value = 0;

		for (int i = 0; i < 4; i++)
			value |= ((int)buffer[i + off] & 0xFF) << ((4 - i - 1) * 8);

		return value;
	}

	static public long readLong(byte[] buffer, int off)
	{
		long value = 0;

		for (int i = 0; i < 8; i++)
			value |= ((int)buffer[i + off] & 0xFF) << ((4 - i - 1) * 8);

		return value;
	}
	
	
	// Packet Handlers

	// 0x00 Keep Alive
	// Keep ALive ID    - Integer
	// Total Size       - 5
	private long KeepAlive(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x00 && size >= 5)
		{
//			long ID = readIntUnsigned(buffer, 1); // (buffer[1] << (3 * 8)) + (buffer[2] << (2 * 8)) + (buffer[3] << (1 * 8)) + buffer[4];

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": KeepAlive(ID=" + ID + ")");

			return 5;
		}

		return 0;
	}
	
	// 0x01 Login Request
	// Protocol Version - Integer
	// Username         - String16
	// Not Used         - Long
	// Not Used         - Integer
	// Not Used         - Byte
	// Not Used         - Byte
	// Not Used         - Unsigned Byte
	// Not Used         - Unsigned Byte
	// Max Length       - 22 bytes + length of strings
	private long LoginRequest(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x01 && size >= 23)
		{
			long protocolVersion = readIntUnsigned(buffer, 1); // (buffer[1] << (3 * 4)) + (buffer[2] << (2 * 4)) + (buffer[3] << (1 * 4))  + buffer[4];
			int usernameLength = readShortUnsigned(buffer, 5); // (buffer[5] << (1 * 4)) + buffer[6];

			if (size >= 23 + (usernameLength * 2))
			{
				String username = new String(buffer, 7, (usernameLength * 2), Charset.forName("UTF-16BE"));
				long mapSeed = readLong(buffer, 7 + (usernameLength * 2));
				int serverMode = readIntSigned(buffer, 7 + (usernameLength * 2) + 8);
				byte dimension = readByteSigned(buffer, 7 + (usernameLength * 2) + 8 + 4);
				byte difficulty = readByteSigned(buffer, 7 + (usernameLength * 2) + 8 + 4 + 1);
				int worldHeight = readByteUnsigned(buffer, 7 + (usernameLength * 2) + 8 + 4 + 1 + 1);
				int maxPlayers = readByteUnsigned(buffer, 7 + (usernameLength * 2) + 8 + 4 + 1 + 1 + 1);

//			if (isServer)
//				System.out.println((isServer ? "C<-S" : "C->S") + ": LoginRequest(ProtocolVersion or EntityID=" + protocolVersion + ", Username=\"" + username +"\", MapSeed=" + mapSeed + ", ServerMode=" + serverMode + ", Dimension=" + dimension + ", DIfficulty=" + difficulty + ", WorldHeight=" + worldHeight + ", MaxPlayers=" + maxPlayers + ")");

				for (LoginRequestEvent handler: loginRequestHandlers)
					handler.trigger(protocolVersion, username, mapSeed, serverMode, dimension, difficulty, worldHeight, maxPlayers);

				return 23 + (usernameLength * 2);
			}
		}

		return 0;
	}

	// 0x02 Handshake
	private long Handshake(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x02 && size >= 3)
		{
			int usernameLen = readShortUnsigned(buffer, 1); // (buffer[1] << (1 * 8)) + buffer[2];

			if (size >= 3 + (usernameLen *2))
			{
//				String username = new String(buffer, 3, usernameLen * 2, Charset.forName("UTF-16BE"));

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": Handshake(\"" + username + "\")");
				return 3 + (usernameLen * 2);
			}
		}

		return 0;
	}

	// 0x03 Chat Message
	private long ChatMessage(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x03 && size >= 3)
		{
			int messageLen = readShortUnsigned(buffer, 1); // (buffer[1] << (1 * 8)) + buffer[0];

			if (size >= 3 + (messageLen * 2))
			{
				String message = new String(buffer, 3, (messageLen * 2), Charset.forName("UTF-16BE"));
//				System.out.println(message);

				for (com.nason.minecraft.events.MessageEvent handler: messageEvents)
					handler.trigger(message);

				return 3 + (messageLen * 2);
			}
		}

		return 0;
	}

	// 0x04 Time Update
	private long TimeUpdate(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x04 && size >= 9)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": TimeUodate()");
			return 9;
		}

		return 0;
	}

	// 0x05 Entity Equipment
	private long EntityEquipment(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x05 && size >= 11)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": EntityEquipment()");
			return 11;
		}

		return 0;
	}

	// 0x06 Spawn Position
	private long SpawnPosition(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x06 && size >= 13)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": SpawnPosition()");
			return 13;
		}

		return 0;
	}

	// 0x07 Use Entity
	private long UseEntity(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x07 && size >= 10)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": UseEntity()");
			return 10;
		}

		return 0;
	}

	// 0x08 Update Health
	private long UpdateHealth(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x08 && size >= 9)
		{
			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": UpdateHealth()");

			return 9;
		}

		return 0;
	}

	// 0x09 Respawn
	private long Respawn(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x09 && size >= 14)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": Respawn()");
			return 14;
		}

		return 0;
	}

	// 0x0A Player
	private long Player(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x0A && size >= 2)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": Player()");
			return 2;
		}

		return 0;
	}

	// 0x0B Player Position
	private long PlayerPosition(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x0B && size >= 34)
		{
			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": PlayerPosition()");

			return 34;
		}

		return 0;
	}

	// 0x0C Player Look
	private long PlayerLook(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x0C && size >= 10)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": PlayerLook()");
			return 10;
		}

		return 0;
	}

	// 0x0D Player Position and Look
	private long PlayerPositionAndLook(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x0D && size >= 42)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": PlayerPositionAndLook()");
			return 42;
		}

		return 0;
	}

	// 0x0E Player Digging
	private long PlayerDigging(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x0E && size >= 12)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": PlayerDigging()");
			return 12;
		}

		return 0;
	}

	// 0x0F Player Block Placement
	private long PlayerBlockPlacement(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x0F && size >= 13)
		{
			int blockOrItemID = readShortSigned(buffer, 11); // (buffer[11] >> 1) + buffer[12];

			if (blockOrItemID >= 0)
			{
				return 16;
			}

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": PlayerBlockPlacement()");

			return 13;
		}

		return 0;
	}

	// 0x10 Holding Change
	private long HoldingChange(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x10 && size >= 3)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": HoldingChange()");
			return 3;
		}

		return 0;
	}

	// 0x11 Use Bed
	private long UseBed(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x11 && size >= 15)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": UseBed()");
			return 15;
		}

		return 0;
	}

	// 0x12 Animation
	private long Animation(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x12 && size >= 6)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": Animation()");
			return 6;
		}

		return 0;
	}

	// 0x13 Entity Action
	private long EntityAction(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x13 && size >= 6)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": EntityAction()");
			return 6;
		}

		return 0;
	}

	// 0x14 Named Entity Spawn
	private long NamedEntitySpawn(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x14 && size >= 23)
		{
			int playerNameLen = readShortUnsigned(buffer, 5); // (buffer[5] << (1 * 8)) + buffer[6];

			if (size >= 23 + (playerNameLen * 2))
			{
				//System.out.println((isServer ? "C<-S" : "C->S") + ": NamedEntitySpawn()");
				return 23 + (playerNameLen * 2);
			}
		}

		return 0;
	}

	// 0x15 Pickup Spawn
	private long PickupSpawn(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x15 && size >= 25)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": PickupSpawn()");
			return 25;
		}

		return 0;
	}

	// 0x16 Collect Item
	private long CollectItem(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x16 && size >= 9)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": CollectItem()");
			return 9;
		}

		return 0;
	}

	// 0x17 Add Object/Vehicle
	private long AddObjectOrVehicle(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x17 && size >= 22)
		{
			long fireballThrowersEntityId = readIntUnsigned(buffer, 18); // (buffer[18] << (3 * 8)) + (buffer[19] << (2 * 8)) + (buffer[20] << (1 * 8)) + buffer[21];

			if (fireballThrowersEntityId > 0)
			{
				if (size >= 28)
				{
					//System.out.println((isServer ? "C<-S" : "C->S") + ": AddObjectOrVehicle()");
					return 28;
				}
			}

			return 22;
		}

		return 0;
	}

	// 0x18 Mob Spawn
	private long MobSpawn(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x18 && size >= 20)
		{
			int endOfMetadata = -1;

			int i = 20;
			while (buffer[i] != 0x7F && i < size)
				i++;
// TODO: handle 0x7F not found
			endOfMetadata = i;

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": MobSpawn()");

			return endOfMetadata + 1;
		}

		return 0;
	}

	// 0x19 Entity: Painting
	private long EntityPainting(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x19 && size >= 23)
		{
			int titleLen = readShortUnsigned(buffer, 5); // (buffer[5] << (1 * 8)) + buffer[6];

			if (size >= 23 + (titleLen * 2))
			{
				//System.out.println((isServer ? "C<-S" : "C->S") + ": EntityPainting()");
				return 23 + (titleLen * 2);
			}
		}

		return 0;
	}

	// 0x1A Experience Orb
	private long ExperienceOrb(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x1A && size >= 19)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": ExperienceOrb()");
			return 19;
		}

		return 0;
	}

	// 0x1B Stance Update (?)
	private long StanceUpdate(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x1B && size >= 19)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": StanceUpdate()");
			return 19;
		}

		return 0;
	}

	// 0x1C Entity Velocity
	private long EntityVelocity(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x1C && size >= 11)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": EntityVelocity()");
			return 11;
		}

		return 0;
	}

	// 0x1D Destroy Entity
	private long DestroyEntity(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x1D && size >= 5)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": DestroyEntity()");
			return 5;
		}

		return 0;
	}

	// 0x1E Entity
	private long Entity(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x1E && size >= 5)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": Entity()");
			return 5;
		}

		return 0;
	}

	// 0x1F Entity Relative move
	private long EntityRelativeMove(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x1F && size >= 8)
		{
			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": EntityRelativeMove()");

			return 8;
		}

		return 0;
	}

	// 0x20 Entity Look
	private long EntityLook(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x20 && size >= 7)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": EntityLook()");
			return 7;
		}

		return 0;
	}

	// 0x21 Entity Look and Relative Move
	private long EntityLookAndRelativeMove(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x21 && size >= 10)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": EntityLookAndRelativeMove()");
			return 10;
		}

		return 0;
	}

	// 0x22 Entity Teleport
	private long EntityTeleport(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x22 && size >= 19)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": EntityTeleport()");
			return 19;
		}

		return 0;
	}

	// 0x26 Entity Status
	private long EntityStatus(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x26 && size >= 6)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": EntityStatus()");
			return 6;
		}

		return 0;
	}

	// 0x27 Attach Entity
	private long AttachEntity(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x27 && size >= 9)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": AttachEntity()");
			return 9;
		}

		return 0;
	}

	// 0x28 Entity Metdata
	private long EntityMetadata(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x28 && size >= 5)
		{
			int endOfMetadata = -1;

			int i = 5;
			while (buffer[i] != 0x7F && i < size)
				i++;
// TODO: handle 0x7F not found
			endOfMetadata = i;

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": EntityMetadata()");

			return endOfMetadata + 1;
		}

		return 0;
	}

	// 0x29 Entity Effect
	private long EntityEffect(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x29 && size >= 9)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": Effect()");
			return 9;
		}

		return 0;
	}

	// 0x2A Remove Entity Effect
	private long RemoveEntityEffect(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x2A && size >= 6)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": RemoveEntityEffect()");
			return 6;
		}

		return 0;
	}

	// 0x2B Experience
	private long Experience(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x2B && size >= 5)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": Experience()");
			return 5;
		}

		return 0;
	}

	// 0x32 Pre-Chunk
	private long PreChunk(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x32 && size >= 10)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": PreChunk()");
			return 10;
		}

		return 0;
	}

	// 0x33 Map Chunk
	private long MapChunk(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x33 && size >= 18)
		{
/*			long X = readIntUnsigned(buffer, 1); // ((int)buffer[1] << (3 * 8)) + (buffer[2] << (2 * 8)) + (buffer[3] << (1 * 8)) + buffer[4];
			int Y = readShortUnsigned(buffer, 5); // ((int)buffer[5] << (1 * 8)) + buffer[6];
			long Z = readIntUnsigned(buffer, 7); // (buffer[7] << (3 * 8)) + (buffer[8] << (2 * 8)) + (buffer[9] << (1 * 8)) + buffer[10];
			int SizeX = readByteUnsigned(buffer, 11); // buffer[11];
			int SizeY = readByteUnsigned(buffer, 12); // buffer[12];
			int SizeZ = readByteUnsigned(buffer, 13); // buffer[13];*/
			long compressedSize = readIntUnsigned(buffer, 14); // (((int)buffer[14] & 0xFF) << (3 * 8)) | (((int)buffer[15] & 0xFF) << (2 * 8)) | (((int)buffer[16] & 0xFF) << (1 * 8)) | ((int)buffer[17] & 0xFF);

			if (size >= 18 + compressedSize)
			{
//				System.out.println((isServer ? "C<-S" : "C->S") + ": MapChunk(X=" + X + ", Y=" + Y + ", Z=" + Z + ", SizeX=" + SizeX + ", SizeY=" + SizeY + ", SizeZ=" + SizeZ + ", Compressed Size=" + compressedSize + ", Compressed Data=binary)");
				return 18 + compressedSize;
			}
		}

		return 0;
	}

	// 0x34 Multi Block Change
	private long MultiBlockChange(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x34 && size >= 11)
		{
			int arraySize = readShortUnsigned(buffer, 9); // (buffer[9] << (1 * 8)) + buffer[10];

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": MultiBlockChange()");

			if (size >= 11 + (arraySize * 4))
				return 11 + (arraySize * 4);
		}

		return 0;
	}

	// 0x35 Block Change
	private long BlockChange(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x35 && size >= 12)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": BlockChange()");
			return 12;
		}

		return 0;
	}

	// 0x36 Block Action
	private long BlockAction(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x36 && size >= 13)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": BlockAction()");
			return 13;
		}

		return 0;
	}

	// 0x3C Explosion
	private long Explosion(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x3C && size >= 33)
		{
			long recordCount = readIntUnsigned(buffer, 29); // (buffer[33] << (3 * 8)) + (buffer[34] << (2 * 8)) + (buffer[35] << (1 * 8)) + buffer[36];
			long recordSize = recordCount * 3;

			if (size >= 33 + recordSize)
			{
				//System.out.println((isServer ? "C<-S" : "C->S") + ": Explosion()");
				return 33 + recordSize;
			}
		}

		return 0;
	}

	// 0x3D Sound Effect
	private long SoundEffect(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x3D && size >= 18)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": SoundEffect()");
			return 18;
		}

		return 0;
	}

	// 0x46 New/Invalid State
	private long NewOrInvalidState(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x46 && size >= 3)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": NewOrInvalidState()");
			return 3;
		}

		return 0;
	}

	// 0x47 Thunderbolt
	private long Thunderbolt(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x47 && size >= 18)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": Thunderbolt()");
			return 18;
		}

		return 0;
	}

	// 0x64 Open Window
	private long OpenWindow(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x64 && size >= 6)
		{
			int windowTitleLen = readShortUnsigned(buffer, 3); // (buffer[3] << (1 * 8)) + buffer[4];

			if (size >= 6 + (windowTitleLen * 2))
			{
				//System.out.println((isServer ? "C<-S" : "C->S") + ": OpenWindow()");
				return 6 + (windowTitleLen * 2);
			}
		}

		return 0;
	}

	// 0x65 CLose Window
	private long CloseWindow(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x65 && size >= 2)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": CloseWindow()");
			return 2;
		}

		return 0;
	}

	// 0x66 Window Click
	private long WindowClick(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x66 && size >= 10)
		{
			int itemID = readShortSigned(buffer, 8); // (buffer[8] << (1 * 8)) + buffer[9];

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": WindowClick()");

			if (itemID != -1)
			{
				if (size >= 13)
				{
					return 13;
				}

				return 0;
			}

			return 10;
		}

		return 0;
	}

	// 0x67 Set Slot
	private long SetSlot(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x67 && size >= 6)
		{
			int itemID = readShortSigned(buffer, 4); // (buffer[4] << (1 * 8)) + buffer[5];

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": SetSlot(Window ID=, Slot=, ItemID=" + itemID + ")");

			if (itemID != -1)
			{
				if (size >= 13)
				{
					return 9;
				}
				return 0;
			}

			return 6;
		}

		return 0;
	}

	// 0x68 Window Items
	private long WindowItems(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x68 && size >= 4)
		{
			int slotCount = readShortUnsigned(buffer, 2); // (buffer[2] << (1 * 8)) + buffer[3];
			
			int slotsRead = 0;
			int p = 4;
			while (slotsRead < slotCount && p < size)
			{
				slotsRead++;

				int itemID = readShortSigned(buffer, p); // (buffer[p] << (1 * 8)) + buffer[p + 1];
//				//System.out.println(buffer[p] + " " + buffer[p+1] + " " + slotsRead + "=" + itemID);
				if (itemID != -1)
					p += 5;
				else
					p += 2;
			}

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": WindowItems() " + slotCount + " " + p);
			if (slotsRead == slotCount)
				return p;
		}

		return 0;
	}

	// 0x69 Update Progress Bar
	private long UpdateProgressBar(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x69 && size >= 6)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": UpdateProgressBar()");
			return 6;
		}

		return 0;
	}

	// 0x6A Transaction
	private long Transaction(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x6A && size >= 5)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": Transaction()");
			return 5;
		}

		return 0;
	}

	// 0x6B Creative Inventory Action
	private long CreativeInventoryAction(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x6B && size >= 9)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": CreativeInventoryAction()");
			return 9;
		}

		return 0;
	}

	// 0x82 Update Sign
	private long UpdateSign(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x82 && size >= 19)
		{
			int line1Len = readShortUnsigned(buffer, 11); // (buffer[4] << (1 * 8)) + buffer[5];
			int line2Len = readShortUnsigned(buffer, 13 + (line1Len * 2)); // (buffer[6 + (line1Len * 2)] << (1 * 8)) + buffer[7 + (line1Len * 2)];
			int line3Len = readShortUnsigned(buffer, 15 + ((line1Len + line2Len) * 2)); // (buffer[8 + (line1Len * 2) + (line2Len * 2)] << (1 * 8)) + buffer[9 + (line1Len * 2) + (line2Len * 2)];
			int line4Len = readShortUnsigned(buffer, 17 + ((line1Len + line2Len + line3Len) * 2)); // (buffer[9 + (line1Len * 2) + (line2Len * 2) + (line3Len * 2)] << (1 * 8)) + buffer[10 + (line1Len * 2) + (line2Len * 2) + (line3Len * 2)];

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": UpdateSign()");
			if (size >= 19 + ((line1Len + line2Len + line3Len + line4Len) * 2))
				return 19 + ((line1Len + line2Len + line3Len + line4Len) * 2);
		}

		return 0;
	}

	// 0x83 Item Data
	private long ItemData(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0x83 && size >= 6)
		{
			int textLen = readShortUnsigned(buffer, 5); // (buffer[5] << (1 * 8)) + buffer[6];

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": ItemData()");

			if (size >= 6 + (textLen * 2))
				return 6 + (textLen * 2);
		}

		return 0;
	}

	// 0xC8 Increment Statistic
	private long IncrementStatistic(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0xC8 && size >= 6)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": IncrementStatistic()");
			return 6;
		}

		return 0;
	}

	// 0xC9 Player List Item
	private long PlayerListItem(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0xC9 && size >= 6)
		{
			int playerNameLen = readShortUnsigned(buffer, 1); // (buffer[1] << (1 * 4)) + buffer[2];
			String playerName = new String(buffer, 3, playerNameLen * 2, Charset.forName("UTF-16BE"));

//			if (isServer)
//				System.out.println((isServer ? "C<-S" : "C->S") + ": PlayerListItem(\"" + playerName + "\") ");

			if (size >= 6 + (playerNameLen * 2))
			{
				for (com.nason.minecraft.events.PlayerListItemEvent handler: playerListItemEvents)
					handler.trigger(playerName, true, 0);
				
				return 6 + (playerNameLen * 2);
			}
		}

		return 0;
	}

	// 0xFE Server List Ping
	private long ServerListPing(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0xFE && size >= 1)
		{

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": ServerListPing()");
			return 1;
		}

		return 0;
	}

	// 0xFF Disconnect/Kick
	private long DisconnectOrKick(byte[] buffer, int size)
	{
		if (buffer[0] == (byte)0xFF && size >= 3)
		{
			int reasonLen = readShortUnsigned(buffer, 1); // (buffer[1] << (1 * 8)) + buffer[2];

			//if (isServer)
				//System.out.println((isServer ? "C<-S" : "C->S") + ": DisconnectOrKick()");

			if (size >= 3 + (reasonLen * 2))
				return 3 + (reasonLen * 2);
		}

		return 0;
	}
}