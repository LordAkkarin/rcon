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

package org.evilco.network.rcon.server.command.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.evilco.network.rcon.server.command.annotation.CommandHandler;
import org.evilco.network.rcon.server.error.CommandException;
import org.evilco.network.rcon.server.error.CommandHandlerException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
@RequiredArgsConstructor
public class ReflectionCommandHandler implements ICommandHandler {

	/**
	 * Stores the object.
	 */
	@NonNull
	private final Object object;

	/**
	 * Stores the method.
	 */
	@NonNull
	private final Method method;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUsage () {
		return this.method.getAnnotation (CommandHandler.class).usage ();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String handle (String command, List<String> arguments) throws CommandException {
		try {
			return ((String) this.method.invoke (command, arguments));
		} catch (IllegalAccessException | InvocationTargetException | ClassCastException ex) {
			throw new CommandHandlerException ("Could not invoke method " + this.object.getClass ().getName () + "#" + this.method.getName () + ": " + ex.getMessage (), ex);
		}
	}
}
