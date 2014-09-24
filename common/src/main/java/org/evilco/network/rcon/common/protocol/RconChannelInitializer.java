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

package org.evilco.network.rcon.common.protocol;

import com.google.common.eventbus.EventBus;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.evilco.network.rcon.common.codec.FrameCodec;
import org.evilco.network.rcon.common.codec.ICodecConfiguration;
import org.evilco.network.rcon.common.codec.RconCodec;

import java.util.concurrent.TimeUnit;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
@AllArgsConstructor
public abstract class RconChannelInitializer extends ChannelInitializer<Channel> {

	/**
	 * Stores the codec configuration.
	 */
	@Getter
	@Setter
	@NonNull
	private ICodecConfiguration codecConfiguration;

	/**
	 * Stores the event bus.
	 */
	@Getter
	@Setter
	@NonNull
	private EventBus eventBus;

	/**
	 * Returns the channel handler.
	 * @return The handler.
	 */
	protected abstract ChannelHandler getHandler ();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initChannel (Channel serverChannel) throws Exception {
		// read timeout
		serverChannel.pipeline ().addLast ("readTimeoutHandler", new ReadTimeoutHandler (120, TimeUnit.SECONDS));

		// add frame codec
		serverChannel.pipeline ().addLast ("frameCodec", FrameCodec.getInstance ());

		// add protocol codec
		serverChannel.pipeline ().addLast ("protocol", new RconCodec (this.codecConfiguration, this.eventBus));

		// add handler
		serverChannel.pipeline ().addLast ("handler", this.getHandler ());
	}
}
