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

package org.evilco.network.rcon.common.codec;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.evilco.network.rcon.common.event.packet.PacketReceiveEvent;
import org.evilco.network.rcon.common.event.packet.PacketSendEvent;
import org.evilco.network.rcon.common.event.packet.PacketSentEvent;
import org.evilco.network.rcon.common.packet.IPacket;
import org.evilco.network.rcon.common.packet.PacketWrapper;

import java.nio.ByteOrder;
import java.util.List;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
@AllArgsConstructor
public class RconCodec extends ByteToMessageCodec<PacketWrapper> {

	/**
	 * Stores an internal logger instance.
	 */
	@Getter (AccessLevel.PROTECTED)
	private static final Logger logger = LogManager.getFormatterLogger (RconCodec.class);

	/**
	 * Stores the codec configuration.
	 */
	@Getter
	@Setter
	@NonNull
	private ICodecConfiguration configuration;

	/**
	 * Stores the event bus.
	 */
	@Getter
	@Setter
	@NonNull
	private EventBus eventBus;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void encode (ChannelHandlerContext channelHandlerContext, PacketWrapper packetWrapper, ByteBuf byteBuf) throws Exception {
		// set order
		byteBuf = byteBuf.order (ByteOrder.LITTLE_ENDIAN);

		// fire event
		PacketSendEvent event = new PacketSendEvent (packetWrapper);
		this.eventBus.post (event);

		// cancel
		if (event.isCancelled ()) return;
		packetWrapper = event.getPacket ();

		// find packetID
		int packetID = this.configuration.getOutboundRegistry ().findPacketID (packetWrapper.getPacket ());

		// log
		getLogger ().debug ("Encoding packet of type " + packetWrapper.getPacket ().getClass ().getName () + " (packetID: " + packetID + ") with identifier " + packetWrapper.getIdentifier () + ".");

		// write identifier
		byteBuf.writeInt (packetWrapper.getIdentifier ());

		// write packet identifier
		byteBuf.writeInt (packetID);

		// allocate buffer
		ByteBuf buffer = channelHandlerContext.alloc ().buffer ();

		// write body
		packetWrapper.getPacket ().write (buffer);

		// write data
		byteBuf.writeBytes (buffer);

		// write terminator
		buffer.writeBytes (new byte[] { 0x00, 0x00 });

		// fire event
		this.eventBus.post (new PacketSentEvent (packetWrapper));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void decode (ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> objects) throws Exception {
		// set order
		byteBuf = byteBuf.order (ByteOrder.LITTLE_ENDIAN);

		// read identifier
		int identifier = byteBuf.readInt ();

		// read packet identifier
		int packetID = byteBuf.readInt ();

		// allocate buffer
		ByteBuf buffer = channelHandlerContext.alloc ().buffer ((byteBuf.readableBytes () - 1));

		// read data
		byteBuf.readBytes (buffer, (byteBuf.readableBytes () - 2));

		// read null byte
		Preconditions.checkState ((byteBuf.readByte () == 0x00 && byteBuf.readByte () == 0x00), "The last byte is not null");
		Preconditions.checkState ((byteBuf.readableBytes () == 0), "There is still data left in the packet.");

		// read data
		IPacket packet = this.configuration.getInboundRegistry ().createPacketInstance (packetID, buffer);

		// log
		getLogger ().debug ("Decoded packet of type " + packet.getClass ().getName () + " (packetID: " + packetID + ", identifier: " + identifier + ").");

		// construct wrapper
		PacketWrapper wrapper = new PacketWrapper (identifier, packet);

		// fire event
		PacketReceiveEvent event = new PacketReceiveEvent (wrapper);
		this.eventBus.post (event);

		// process
		if (!event.isCancelled ())
			objects.add (event.getPacket ());
		else
			getLogger ().debug ("Ignoring packet with identifier %s (packetID: %s, type: %s): Event has been cancelled.", identifier, packetID, packet.getClass ());
	}
}
