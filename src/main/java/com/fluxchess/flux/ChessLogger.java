/*
 * Copyright 2007-2014 the original author or authors.
 *
 * This file is part of Flux Chess.
 *
 * Flux Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flux Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.fluxchess.flux;

import com.fluxchess.jcpi.commands.IProtocol;
import com.fluxchess.jcpi.commands.ProtocolInformationCommand;

public class ChessLogger {

	// Singleton Pattern
	private static final ChessLogger instance = new ChessLogger();
	
	private static IProtocol protocol = null;
	private static boolean debug = false;
	
	private ChessLogger() {
	}
	
	public static ChessLogger getLogger() {
		return instance;
	}
	
	public static void setProtocol(IProtocol newProtocol) {
		protocol = newProtocol;
	}
	
	public static void setDebug(boolean mode) {
		debug = mode;
	}
	
	public void debug(String information) {
		if (debug && protocol != null) {
			ProtocolInformationCommand command = new ProtocolInformationCommand();
			command.setString(information);
			protocol.send(command);
		}
	}
	
	public static void showTrace() {
		try {
			throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
