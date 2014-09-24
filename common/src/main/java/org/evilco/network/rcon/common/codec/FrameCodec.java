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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteOrder;
import java.util.List;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
@ChannelHandler.Sharable
public class FrameCodec extends ByteToMessageCodec<ByteBuf> {

	/**
	 * Stores an internal logger instance.
	 */
	@Getter (AccessLevel.PROTECTED)
	private static final Logger logger = LogManager.getLogger (FrameCodec.class);

	/**
	 * Stores the internal singleton instance.
	 */
	private static FrameCodec INSTANCE = null;

	/**
	 * Internal Constructor
	 */
	protected FrameCodec () { }

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void encode (ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) throws Exception {
		// set order
		byteBuf2 = byteBuf2.order (ByteOrder.LITTLE_ENDIAN);

		// log
		getLogger ().debug ("Encoding frame for " + byteBuf.readableBytes () + " bytes of data.");

		// ensure output buffer is writable
		byteBuf2.ensureWritable ((4 + byteBuf.readableBytes ()));

		// write packet length
		byteBuf2.writeInt (byteBuf.readableBytes ());

		// write packet
		byteBuf2.writeBytes (byteBuf);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void decode (ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> objects) throws Exception {
		// set order
		byteBuf = byteBuf.order (ByteOrder.LITTLE_ENDIAN);

		// read all available packets
		while (byteBuf.readableBytes () >= 4) {
			// log
			getLogger ().trace ("Decoding frame with maximal size of " + byteBuf.readableBytes () + " bytes.");

			// mark reader index
			byteBuf.markReaderIndex ();

			// read length
			int length = byteBuf.readInt ();

			// log
			getLogger ().trace ("Detected frame length of " + length + " bytes.");

			// check whether enough data is available
			if (length > byteBuf.readableBytes ()) {
				// log
				getLogger ().debug ("There are only " + byteBuf.readableBytes () + " out of " + length + " bytes available. Skipping frame until more data is available.");

				// reset buffer
				byteBuf.resetReaderIndex ();

				// exit loop
				break;
			}

			// log
			getLogger ().trace ("Frame seems to be complete reading data.");

			// construct buffer
			ByteBuf packetBuffer = channelHandlerContext.alloc ().buffer (length);

			// read data
			byteBuf.readBytes (packetBuffer, length);

			// add to list
			objects.add (packetBuffer);

			// log
			getLogger ().trace ("Frame decoded. " + byteBuf.readableBytes () + " bytes left in buffer.");
		}
	}

	/**
	 * Returns an instance of FrameCodec.
	 * @return The instance.
	 */
	public static FrameCodec getInstance () {
		if (INSTANCE == null) INSTANCE = new FrameCodec ();
		return INSTANCE;
	}
}
