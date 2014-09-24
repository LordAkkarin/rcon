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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.evilco.network.rcon.common.packet.*;
import org.evilco.network.rcon.common.packet.annotation.Packet;
import org.evilco.network.rcon.common.packet.registry.IWritablePacketRegistry;
import org.evilco.network.rcon.common.packet.registry.SimplePacketRegistry;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
@RequiredArgsConstructor
public class ClientCodecConfiguration implements IWritableCodecConfiguration {

	/**
	 * Stores the default configuration.
	 */
	public static final ClientCodecConfiguration DEFAULT = new ClientCodecConfiguration (true);

	/**
	 * Stores the inbound registry.
	 */
	@Getter
	@NonNull
	private final IWritablePacketRegistry inboundRegistry;

	/**
	 * The outbound registry.
	 */
	@Getter
	@NonNull
	private final IWritablePacketRegistry outboundRegistry;

	/**
	 * Constructs a new ClientCodecConfiguration instance.
	 */
	public ClientCodecConfiguration () {
		this ((new SimplePacketRegistry ()), (new SimplePacketRegistry ()));
	}

	/**
	 * Constructs a default codec configuration.
	 * @param dummy A dummy variable.
	 */
	protected ClientCodecConfiguration (boolean dummy) {
		this ();
		if (!dummy) return;

		this.registerPacketClass (AuthenticationPacket.class);
		this.registerPacketClass (ExecutePacket.class);

		this.registerPacketClass (AuthenticationResponsePacket.class);
		this.registerPacketClass (ResponsePacket.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerPacketClass (Class<? extends IPacket> packetClass) {
		// check annotation
		Preconditions.checkArgument ((packetClass.isAnnotationPresent (Packet.class)), "Required @Packet annotation is missing.");

		// register
		switch (packetClass.getAnnotation (Packet.class).direction ()) {
			case CLIENTBOUND:
				this.getOutboundRegistry ().registerPacket (packetClass.getAnnotation (Packet.class).packetID (), packetClass);
				break;
			case SERVERBOUND:
				this.getInboundRegistry ().registerPacket (packetClass.getAnnotation (Packet.class).packetID (), packetClass);
				break;
		}
	}
}
