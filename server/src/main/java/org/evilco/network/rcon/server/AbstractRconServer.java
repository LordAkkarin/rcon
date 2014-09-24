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
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.evilco.network.rcon.server.command.ICommandRegistry;
import org.evilco.network.rcon.server.command.SimpleCommandRegistry;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
public abstract class AbstractRconServer implements IRconServer {

	/**
	 * Stores the server bootstrap.
	 */
	@Getter (AccessLevel.PROTECTED)
	private ServerBootstrap bootstrap;

	/**
	 * Stores the event bus.
	 */
	@Getter
	private EventBus eventBus;

	/**
	 * Stores the registry.
	 */
	@Getter
	private ICommandRegistry commandRegistry;

	/**
	 * Stores the boss group.
	 */
	@Getter
	private EventLoopGroup groupBoss;

	/**
	 * Stores the worker group.
	 */
	@Getter
	private EventLoopGroup groupWorker;

	@Getter
	@Setter
	@NonNull
	private String password;

	/**
	 * Constructs a new AbstractRconServer instance.
	 * @param password The server password.
	 */
	public AbstractRconServer (@NonNull String password) {
		this ((new EventBus ()), password);
	}

	/**
	 * Constructs a new AbstractRconServer instance.
	 * @param eventBus The event bus.
	 * @param password The server password.
	 */
	public AbstractRconServer (@NonNull EventBus eventBus, @NonNull String password) {
		this (eventBus, new SimpleCommandRegistry (eventBus), password);
	}

	/**
	 * Constructs a new AbstractRconServer instance.
	 * @param eventBus The event bus.
	 * @param registry The command registry.
	 * @param password The server password.
	 */
	public AbstractRconServer (@NonNull EventBus eventBus, @NonNull ICommandRegistry registry, @NonNull String password) {
		// store arguments
		this.eventBus = eventBus;
		this.commandRegistry = registry;
		this.password = password;

		// create groups
		this.groupBoss = this.createEventLoopGroup ();
		this.groupWorker = this.createEventLoopGroup ();

		// create bootstrap
		this.bootstrap = new ServerBootstrap ();

		// set groups
		this.bootstrap.group (this.groupBoss, this.groupWorker);

		// set properties
		this.bootstrap.channel (this.getChannelType ());
		this.bootstrap.childHandler (this.createChannelInitializer ());

		this.bootstrap.option (ChannelOption.SO_BACKLOG, 128);
		this.bootstrap.childOption (ChannelOption.SO_KEEPALIVE, true);
	}

	/**
	 * Returns the channel type.
	 * @return The channel type.
	 */
	protected abstract Class<? extends ServerChannel> getChannelType ();

	/**
	 * Returns the channel initializer.
	 * @return The initializer.
	 */
	protected abstract ChannelInitializer<Channel> createChannelInitializer ();

	/**
	 * Creates an event loop group.
	 * @return The event loop group.
	 */
	public abstract EventLoopGroup createEventLoopGroup ();

	/**
	 * Starts listening.
	 * @param address The address.
	 * @param port The port.
	 * @throws InterruptedException Occurs if something goes horribly wrong.
	 */
	public void listen (String address, int port) throws InterruptedException {
		try {
			// start listening
			ChannelFuture future = this.bootstrap.bind (address, port).sync ();

			// close server
			future.channel ().closeFuture ().sync ();
		} finally {
			if (this.groupWorker != null) this.groupWorker.shutdownGracefully ();
			if (this.groupBoss != null) this.groupBoss.shutdownGracefully ();
		}
	}
}
