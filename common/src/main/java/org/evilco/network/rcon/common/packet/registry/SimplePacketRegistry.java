/*
 * Copyright 2014 Johannes Donath <johannesd@evil-co.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.evilco.network.rcon.common.packet.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.evilco.network.rcon.common.error.InvalidPacketException;
import org.evilco.network.rcon.common.error.PacketException;
import org.evilco.network.rcon.common.error.UnknownPacketException;
import org.evilco.network.rcon.common.packet.IPacket;
import org.evilco.network.rcon.common.packet.annotation.Packet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
public class SimplePacketRegistry implements IWritablePacketRegistry {

	/**
	 * Stores the internal logger instance.
	 */
	private static final Logger logger = LogManager.getFormatterLogger (SimplePacketRegistry.class);

	/**
	 * Stores the packet map.
	 */
	private BiMap<Integer, Class<? extends IPacket>> map = HashBiMap.create ();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPacket createPacketInstance (@NonNull Class<? extends IPacket> packetClass, @NonNull ByteBuf buffer) throws PacketException {
		try {
			// find constructor
			Constructor<? extends IPacket> constructor = packetClass.getConstructor (ByteBuf.class);

			// ensure constructor is accessible
			constructor.setAccessible (true);

			// create a new instance
			return constructor.newInstance (buffer);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
			throw new InvalidPacketException ("Invalid packet implementation: " + ex.getMessage (), ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPacket createPacketInstance (int packetID, ByteBuf buffer) throws PacketException {
		return this.createPacketInstance (this.findPacketClass (packetID), buffer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends IPacket> findPacketClass (int packetID) throws UnknownPacketException {
		if (!this.map.containsKey (packetID)) throw new UnknownPacketException ("Unknown packet identifier: " + packetID);
		return this.map.get (packetID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int findPacketID (@NonNull Class<? extends IPacket> packetClass) throws PacketException {
		if (!this.map.inverse ().containsKey (packetClass)) {
			// check for annotation
			if (!packetClass.isAnnotationPresent (Packet.class)) throw new InvalidPacketException ("Could not find packetID for packet type " + packetClass.getName () + ".");

			// debug log
			logger.debug ("Could not find identifier for packet type %s. Falling back to annotation.", packetClass.getName ());

			// use annotation
			return packetClass.getAnnotation (Packet.class).packetID ();
		}

		// return registered ID
		return this.map.inverse ().get (packetClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int findPacketID (@NonNull IPacket packet) throws PacketException {
		return this.findPacketID (packet.getClass ());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerPacket (int packetID, @NonNull Class<? extends IPacket> packetClass) {
		this.map.put (packetID, packetClass);
	}
}
