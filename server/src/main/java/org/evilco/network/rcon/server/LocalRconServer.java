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

package org.evilco.network.rcon.server;

import com.google.common.eventbus.EventBus;
import io.netty.channel.*;
import io.netty.channel.local.LocalServerChannel;
import lombok.NonNull;
import org.evilco.network.rcon.common.codec.ServerCodecConfiguration;
import org.evilco.network.rcon.server.command.ICommandRegistry;
import org.evilco.network.rcon.server.protocol.ServerChannelInitializer;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
public class LocalRconServer extends AbstractRconServer {

	/**
	 * Constructs a new LocalRconServer instance.
	 * @param password The password.
	 */
	public LocalRconServer (@NonNull String password) {
		super (password);
	}

	/**
	 * Constructs a new LocalRconServer instance.
	 * @param eventBus The event bus.
	 * @param password The password.
	 */
	public LocalRconServer (@NonNull EventBus eventBus, @NonNull String password) {
		super (eventBus, password);
	}

	/**
	 * Constructs a new LocalRconServer instance.
	 * @param eventBus The event bus.
	 * @param registry The registry.
	 * @param password The password.
	 */
	public LocalRconServer (@NonNull EventBus eventBus, @NonNull ICommandRegistry registry, @NonNull String password) {
		super (eventBus, registry, password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<? extends ServerChannel> getChannelType () {
		return LocalServerChannel.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ChannelInitializer<Channel> createChannelInitializer () {
		return (new ServerChannelInitializer (this, ServerCodecConfiguration.DEFAULT, this.getEventBus ()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EventLoopGroup createEventLoopGroup () {
		return (new DefaultEventLoopGroup ());
	}
}
