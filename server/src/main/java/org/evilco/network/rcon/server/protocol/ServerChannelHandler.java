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

package org.evilco.network.rcon.server.protocol;

import com.google.common.eventbus.EventBus;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.evilco.network.rcon.common.packet.*;
import org.evilco.network.rcon.server.IRconServer;

import java.net.SocketAddress;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
@RequiredArgsConstructor
public class ServerChannelHandler extends ChannelHandlerAdapter {

	/**
	 * Defines the attribute key for the authentication state.
	 */
	private static final AttributeKey<Boolean> ATTRIBUTE_AUTHENTICATED = AttributeKey.valueOf ("Authenticated");

	/**
	 * Stores the internal logger instance.
	 */
	@Getter (AccessLevel.PROTECTED)
	private static final Logger logger = LogManager.getFormatterLogger (ServerChannelHandler.class);

	/**
	 * Stores the parent server.
	 */
	@Getter
	@NonNull
	private final IRconServer server;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exceptionCaught (ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// log
		getLogger ().warn ("An error occured while handling one or more client packets: " + cause.getMessage (), cause);

		// kick client
		ctx.channel ().close ();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void channelRead (ChannelHandlerContext ctx, Object msg) throws Exception {
		// abort
		if (!(msg instanceof PacketWrapper)) {
			// log
			getLogger ().warn ("Received a message of type " + msg.getClass ().getName () + "! Skipping unknown packet type.");

			// skip execution
			return;
		}

		// cast packet
		PacketWrapper wrapper = ((PacketWrapper) msg);

		// handle authentication commands
		if (!getAuthenticationState (ctx)) {
			// check type
			if (!(wrapper.getPacket () instanceof AuthenticationPacket)) {
				// notify client about state
				ctx.channel ().writeAndFlush (new PacketWrapper (-1, new AuthenticationResponsePacket ()));

				// close connection
				ctx.channel ().disconnect ();

				// skip further execution
				return;
			}

			// log
			getLogger ().debug ("Received authentication packet from client " + ctx.channel ().remoteAddress ().toString () + ".");

			// repeat authentication packet
			if (getAuthenticationState (ctx)) {
				// log
				getLogger ().debug ("The client is already authenticated. Repeating response.");

				// repeat packet
				ctx.channel ().writeAndFlush (new PacketWrapper (wrapper.getIdentifier (), new AuthenticationResponsePacket ()));

				// skip further execution
				return;
			}

			// verify server password
			if (this.server.getPassword () == null || this.server.getPassword ().isEmpty ()) {
				// log
				getLogger ().debug ("The server password is unset or empty. Authentication denied.");

				// deny authentication
				ctx.channel ().writeAndFlush (new PacketWrapper (-1, new AuthenticationResponsePacket ()));

				// close connection
				ctx.channel ().disconnect ();

				// skip further execution
				return;
			}

			// cast packet
			AuthenticationPacket packet = ((AuthenticationPacket) wrapper.getPacket ());

			// verify password
			if (this.server.getPassword ().equals (packet.getPassword ())) {
				// log
				getLogger ().debug ("Authentication successful. Client will be notified.");

				// set authentication state
				setAuthenticationState (ctx, true);

				// notify client
				ctx.channel ().writeAndFlush (new PacketWrapper (wrapper.getIdentifier (), new AuthenticationResponsePacket ()));

				// skip further execution
				return;
			}

			// log
			getLogger ().debug ("Failed authentication attempt from client.");
			getLogger ().trace ("Password was: \"" + packet.getPassword () + "\" (expecting: \"" + this.server.getPassword () + "\").");

			// notify client about messed up authentication
			ctx.channel ().writeAndFlush (new PacketWrapper (-1, new AuthenticationResponsePacket ()));

			// close connection
			ctx.channel ().disconnect ();
		}

		// handle command
		if (wrapper.getPacket () instanceof ExecutePacket) {
			// cast packet
			ExecutePacket packet = ((ExecutePacket) wrapper.getPacket ());

			// log
			getLogger ().trace ("Received command \"" + packet.getCommand () + "\" from client " + ctx.channel ().remoteAddress ().toString () + ".");

			// respond to empty commands
			if (packet.getCommand ().isEmpty ()) {
				// log
				getLogger ().trace ("Responding to empty command packet.");

				// respond
				ctx.channel ().writeAndFlush (new PacketWrapper (wrapper.getIdentifier (), new ResponsePacket ("")));

				// skip further execution
				return;
			}

			// call command handler
			if (!this.getServer ().getCommandRegistry ().handle (packet.getCommand (), wrapper.getIdentifier (), ctx.channel ())) {
				// notify client
				ctx.channel ().writeAndFlush (new PacketWrapper (wrapper.getIdentifier (), new ResponsePacket ("Error: No such command or configuration variable.")));

				// skip further execution
				return;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect (ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
		super.connect (ctx, remoteAddress, localAddress, promise);

		// log
		getLogger ().debug ("Incoming connection from %s.", ctx.channel ().remoteAddress ().toString ());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect (ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		super.disconnect (ctx, promise);

		// log
		getLogger ().debug ("Client from %s disconnected.", ctx.channel ().remoteAddress ().toString ());
	}

	/**
	 * Returns the authentication state.
	 * @param channel The channel.
	 * @return True if the user is authenticated.
	 */
	public static boolean getAuthenticationState (@NonNull Channel channel) {
		channel.attr (ATTRIBUTE_AUTHENTICATED).setIfAbsent (false);
		return channel.attr (ATTRIBUTE_AUTHENTICATED).get ();
	}

	/**
	 * Returns the authentication state.
	 * @param ctx The context.
	 * @return True if the user is authenticated.
	 */
	public static boolean getAuthenticationState (@NonNull ChannelHandlerContext ctx) {
		return getAuthenticationState (ctx.channel ());
	}

	/**
	 * Sets the authentication state.
	 * @param channel The channel.
	 * @param state The state.
	 */
	public static void setAuthenticationState (@NonNull Channel channel, boolean state) {
		channel.attr (ATTRIBUTE_AUTHENTICATED).set (state);
	}

	/**
	 * Sets the authentication state.
	 * @param ctx The context.
	 * @param state The state.
	 */
	public static void setAuthenticationState (@NonNull ChannelHandlerContext ctx, boolean state) {
		setAuthenticationState (ctx.channel (), state);
	}
}
