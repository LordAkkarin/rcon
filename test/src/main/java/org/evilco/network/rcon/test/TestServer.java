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

package org.evilco.network.rcon.test;

import org.evilco.network.rcon.server.RemoteRconServer;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
public class TestServer {

	/**
	 * Main Entry Point
	 * @param arguments The command line arguments.
	 * @throws InterruptedException
	 */
	public static void main (String[] arguments) throws InterruptedException {
		RemoteRconServer server = new RemoteRconServer ("Test1234");
		server.listen ("127.0.0.1", 27015);
	}
}
