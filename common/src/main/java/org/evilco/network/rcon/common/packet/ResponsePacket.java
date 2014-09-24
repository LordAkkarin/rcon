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
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.evilco.network.rcon.common.packet.annotation.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
@RequiredArgsConstructor
@Packet (packetID = 0x00, direction = ProtocolDirection.CLIENTBOUND)
public class ResponsePacket implements ISplittablePacket {

	/**
	 * Stores the response.
	 */
	@Getter
	private final String response;

	/**
	 * De-Serializes a ResponsePacket instance.
	 * @param buffer The packet buffer.
	 */
	public ResponsePacket (@NonNull ByteBuf buffer) {
		// get length
		int length = buffer.readableBytes ();

		// skip empty packet
		if (length == 0) {
			this.response = null;
			return;
		}

		// create string array
		byte[] data = new byte[length];

		// read data
		buffer.readBytes (data);

		// re-construct string
		this.response = new String (data, Charsets.US_ASCII);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IPacket> split () {
		// create buffer
		ByteBuf buffer = Unpooled.buffer ();

		// encode
		this.write (buffer);

		// calculate amount of packets
		int packetCount = ((int) Math.ceil ((buffer.readableBytes () / 4096)));

		// verify amount
		if (packetCount <= 1) return null;

		// create list
		List<IPacket> packetList = new ArrayList<IPacket> ();

		// encode packets
		while (buffer.readableBytes () > 0) {
			// read a chunk
			byte[] data = new byte[Math.min (buffer.readableBytes (), 4096)];
			buffer.readBytes (data);

			// create and add wrapper packet
			packetList.add (new ResponseWrapperPacket (data));
		}

		// return finished list
		return packetList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write (@NonNull ByteBuf buffer) {
		if (this.response != null && !this.response.isEmpty ())
			buffer.writeBytes (this.response.getBytes (Charsets.US_ASCII));
		else
			buffer.writeBytes (new byte[] { 0x00, 0x00 });
	}

	/**
	 * A wrapper packet used for message splitting.
	 */
	@RequiredArgsConstructor
	@Packet (packetID = 0x03, direction = ProtocolDirection.CLIENTBOUND)
	public class ResponseWrapperPacket implements IPacket {

		/**
		 * Stores the chunk.
		 */
		@NonNull
		private final byte[] data;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void write (@NonNull ByteBuf buffer) {
			buffer.writeBytes (this.data);
		}
	}
}
