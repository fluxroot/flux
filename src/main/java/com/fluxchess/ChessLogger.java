/*
** Copyright 2007-2012 Phokham Nonava
**
** This file is part of Flux Chess.
**
** Flux Chess is free software: you can redistribute it and/or modify
** it under the terms of the GNU Lesser General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** Flux Chess is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fluxchess;

import com.fluxchess.jcpi.AbstractCommunication;
import com.fluxchess.jcpi.commands.GuiInformationCommand;


/**
 * ChessLogger
 *
 * @author Phokham Nonava
 */
public final class ChessLogger {

    // Singleton Pattern
    private static final ChessLogger instance = new ChessLogger();

    private static AbstractCommunication protocol = null;
    private static boolean debug = false;

    private ChessLogger() {
    }

    public static ChessLogger getLogger() {
        return instance;
    }

    public static void setProtocol(AbstractCommunication newProtocol) {
        protocol = newProtocol;
    }

    public static boolean getDebug() {
        return debug;
    }

    public static void setDebug(boolean mode) {
        debug = mode;
    }

    public void debug(String information) {
        if (debug && protocol != null) {
            GuiInformationCommand command = new GuiInformationCommand();
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
