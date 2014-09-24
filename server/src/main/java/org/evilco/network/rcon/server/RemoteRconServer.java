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
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.NonNull;
import org.evilco.network.rcon.common.codec.ServerCodecConfiguration;
import org.evilco.network.rcon.server.command.ICommandRegistry;
import org.evilco.network.rcon.server.protocol.ServerChannelInitializer;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
public class RemoteRconServer extends AbstractRconServer {

	/**
	 * Constructs a new RemoteRconServer instance.
	 * @param password The password.
	 */
	public RemoteRconServer (@NonNull String password) {
		super (password);
	}

	/**
	 * Constructs a new RemoteRconServer instance.
	 * @param eventBus The event bus.
	 * @param password The password.
	 */
	public RemoteRconServer (@NonNull EventBus eventBus, @NonNull String password) {
		super (eventBus, password);
	}

	/**
	 * Constructs a new RemoteRconServer instance.
	 * @param eventBus The event bus.
	 * @param registry The command registry.
	 * @param password The password.
	 */
	public RemoteRconServer (@NonNull EventBus eventBus, @NonNull ICommandRegistry registry, @NonNull String password) {
		super (eventBus, registry, password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<? extends ServerChannel> getChannelType () {
		return NioServerSocketChannel.class;
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
		return (new NioEventLoopGroup ());
	}
}
