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

package org.evilco.network.rcon.common.packet;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.evilco.network.rcon.common.packet.annotation.Packet;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
@RequiredArgsConstructor
@Packet (packetID = 0x02, direction = ProtocolDirection.SERVERBOUND)
public class ExecutePacket implements IPacket {

	/**
	 * Stores the command.
	 */
	@Getter
	@NonNull
	private final String command;

	/**
	 * Constructs a new ExecutePacket instance.
	 * @param buffer The packet buffer.
	 */
	public ExecutePacket (@NonNull ByteBuf buffer) {
		// get length
		int length = buffer.readableBytes ();

		// create string array
		byte[] data = new byte[length];

		// read data
		buffer.readBytes (data);

		// store command
		this.command = new String (data, Charsets.US_ASCII);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write (@NonNull ByteBuf buffer) {
		buffer.writeBytes (this.command.getBytes (Charsets.US_ASCII));
	}
}
