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

import io.netty.buffer.ByteBuf;
import org.evilco.network.rcon.common.error.PacketException;
import org.evilco.network.rcon.common.error.UnknownPacketException;
import org.evilco.network.rcon.common.packet.IPacket;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
public interface IPacketRegistry {

	/**
	 * Creates a new packet instance.
	 * @param packetClass The packet class.
	 * @param buffer The packet buffer.
	 * @return The packet.
	 * @throws PacketException Occurs if the packet could not be constructed.
	 */
	public IPacket createPacketInstance (Class<? extends IPacket> packetClass, ByteBuf buffer) throws PacketException;

	/**
	 * Creates a new packet instance.
	 * @param packetID The packet identifier.
	 * @param buffer The packet buffer.
	 * @return The packet.
	 * @throws PacketException Occurs if the packet could not be constructed.
	 */
	public IPacket createPacketInstance (int packetID, ByteBuf buffer) throws PacketException;

	/**
	 * Searches a packet based on it's identifier.
	 * @param packetID The packet identifier.
	 * @return The packet class.
	 * @throws UnknownPacketException Occurs if the packet identifier is unknown.
	 */
	public Class<? extends IPacket> findPacketClass (int packetID) throws UnknownPacketException;

	/**
	 * Searches a packet identifier based on it's class.
	 * @param packetClass The packet class.
	 * @return The packet identifier.
	 * @throws PacketException
	 */
	public int findPacketID (Class<? extends IPacket> packetClass) throws PacketException;

	/**
	 * Searches a packet identifier based on an instance.
	 * @param packet The packet.
	 * @return The packet identifier.
	 * @throws PacketException
	 */
	public int findPacketID (IPacket packet) throws PacketException;
}
