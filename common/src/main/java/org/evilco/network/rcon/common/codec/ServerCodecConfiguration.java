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

import org.evilco.network.rcon.common.packet.registry.IWritablePacketRegistry;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
public class ServerCodecConfiguration extends ClientCodecConfiguration {

	/**
	 * Stores the default configuration.
	 */
	public static final ServerCodecConfiguration DEFAULT = new ServerCodecConfiguration (true);

	/**
	 * Constructs a new ServerCodecConfiguration instance.
	 */
	public ServerCodecConfiguration () {
		super ();
	}

	/**
	 * Constructs a new ServerCodecConfiguration instance.
	 * @param dummy A dummy variable.
	 */
	protected ServerCodecConfiguration (boolean dummy) {
		super (dummy);
	}

	/**
	 * Constructs a new ServerCodecConfiguration instance.
	 * @param inboundRegistry The inbound registry.
	 * @param outboundRegistry The outbound registry.
	 */
	public ServerCodecConfiguration (IWritablePacketRegistry inboundRegistry, IWritablePacketRegistry outboundRegistry) {
		super (inboundRegistry, outboundRegistry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWritablePacketRegistry getInboundRegistry () {
		return super.getOutboundRegistry ();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWritablePacketRegistry getOutboundRegistry () {
		return super.getInboundRegistry ();
	}
}
