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

package org.evilco.network.rcon.server.protocol;

import com.google.common.eventbus.EventBus;
import io.netty.channel.ChannelHandler;
import lombok.Getter;
import lombok.NonNull;
import org.evilco.network.rcon.common.codec.ICodecConfiguration;
import org.evilco.network.rcon.common.protocol.RconChannelInitializer;
import org.evilco.network.rcon.server.IRconServer;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
public class ServerChannelInitializer extends RconChannelInitializer {

	/**
	 * Stores the parent server instance.
	 */
	@Getter
	@NonNull
	private final IRconServer server;

	/**
	 * Constructs a new ServerChannelInitializer instance.
	 * @param server The parent server.
	 * @param codecConfiguration The codec configuration.
	 * @param eventBus The event bus.
	 */
	public ServerChannelInitializer (@NonNull IRconServer server, ICodecConfiguration codecConfiguration, EventBus eventBus) {
		super (codecConfiguration, eventBus);

		this.server = server;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ChannelHandler getHandler () {
		return (new ServerChannelHandler (this.getServer ()));
	}
}
