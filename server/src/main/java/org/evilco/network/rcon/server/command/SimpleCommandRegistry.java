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

package org.evilco.network.rcon.server.command;

import com.google.common.base.Splitter;
import com.google.common.eventbus.EventBus;
import io.netty.channel.Channel;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.evilco.network.rcon.common.packet.PacketWrapper;
import org.evilco.network.rcon.common.packet.ResponsePacket;
import org.evilco.network.rcon.server.command.annotation.CommandHandler;
import org.evilco.network.rcon.server.command.handler.ICommandHandler;
import org.evilco.network.rcon.server.command.handler.ReflectionCommandHandler;
import org.evilco.network.rcon.server.error.CommandException;
import org.evilco.network.rcon.server.error.CommandUsageException;
import org.evilco.network.rcon.server.event.command.CommandPostProcessEvent;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
@RequiredArgsConstructor
public class SimpleCommandRegistry implements ICommandRegistry {

	/**
	 * Stores the internal logger.
	 */
	@Getter (AccessLevel.PROTECTED)
	private static final Logger logger = LogManager.getFormatterLogger (SimpleCommandRegistry.class);

	/**
	 * Stores the command map.
	 */
	private Map<String, ICommandHandler> commandMap = new HashMap<String, ICommandHandler> ();

	/**
	 * Stores the event bus.
	 */
	@Getter
	private final EventBus eventBus;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean handle (@NonNull String command, int identifier, @NonNull Channel channel) {
		// check registration
		if (!this.commandMap.containsKey (command)) return false;

		// parse command
		List<String> arguments = Splitter.on (' ').omitEmptyStrings ().splitToList (command);

		// remove first element
		command = arguments.remove (0);

		// get handler
		ICommandHandler handler = this.commandMap.get (command);

		// handle
		PacketWrapper<ResponsePacket> wrapper = null;

		try {
			// call handler
			String result = handler.handle (command, arguments);

			// respond
			wrapper = new PacketWrapper (identifier, new ResponsePacket (result));
		} catch (CommandUsageException ex) {
			// log
			getLogger ().debug ("Could not execute command \"%s\": Usage error", command);

			// get usage
			String usage = handler.getUsage ();

			// create error response
			wrapper = new PacketWrapper (identifier, new ResponsePacket ("Usage: " + command + (usage.isEmpty () ? "": " " + usage)));
		} catch (CommandException ex) {
			// log
			getLogger ().debug ("Could not execute command \"%s\": %s", command, ex.getMessage ());

			// create error response
			wrapper = new PacketWrapper (identifier, new ResponsePacket ("Error: " + ex.getMessage ()));
		}

		// fire event
		CommandPostProcessEvent event = new CommandPostProcessEvent (wrapper);
		this.eventBus.post (event);

		// handle
		if (event.isCancelled ()) return false;
		channel.writeAndFlush (event.getPacket ());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerHandler (@NonNull String command, @NonNull ICommandHandler handler) {
		this.commandMap.put (command, handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerHandler (@NonNull Object object) {
		// count elements
		int count = this.commandMap.size ();

		// register methods
		for (Method method : object.getClass ().getDeclaredMethods ()) {
			// skip unknown methods
			if (!method.isAnnotationPresent (CommandHandler.class)) continue;

			// register method
			this.registerHandler (method.getAnnotation (CommandHandler.class).value (), new ReflectionCommandHandler (object, method));
		}

		// log
		getLogger ().debug ("Registered " + (this.commandMap.size () - count) + " commands to handler " + object.getClass ().getName () + ".");
	}
}
