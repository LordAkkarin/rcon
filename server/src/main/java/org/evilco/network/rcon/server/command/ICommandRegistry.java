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

import io.netty.channel.Channel;
import org.evilco.network.rcon.server.command.handler.ICommandHandler;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
public interface ICommandRegistry {

	/**
	 * Handles a command.
	 * @param command The command.
	 * @param identifier The request identifier.
	 * @param channel The source channel.
	 * @return True if the command was handled.
	 */
	public boolean handle (String command, int identifier, Channel channel);

	/**
	 * Registers a new command handler.
	 * @param command The command.
	 * @param handler The handler.
	 */
	public void registerHandler (String command, ICommandHandler handler);

	/**
	 * Registers a group of new command handlers.
	 * @param object The handler object.
	 */
	public void registerHandler (Object object);
}
