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

package org.evilco.network.rcon.server.event.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.evilco.network.rcon.common.event.AbstractCancellableEvent;
import org.evilco.network.rcon.common.packet.PacketWrapper;
import org.evilco.network.rcon.common.packet.ResponsePacket;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
@AllArgsConstructor
public class CommandPostProcessEvent extends AbstractCancellableEvent {

	/**
	 * Stores the response packet.
	 */
	@Getter
	@Setter
	@NonNull
	private PacketWrapper<ResponsePacket> packet;
}
