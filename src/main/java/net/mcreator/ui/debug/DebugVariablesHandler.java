/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2023, Pylo, opensource contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.mcreator.ui.debug;

import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.WatchpointEvent;
import net.mcreator.java.debug.JVMDebugClient;
import net.mcreator.java.debug.Watchpoint;

public class DebugVariablesHandler {

	public static void handleVariables(DebugPanel debugPanel) {
		JVMDebugClient debugClient = debugPanel.getDebugClient();
		if (debugClient != null) {
			try {
				debugClient.addWatchpoint(new Watchpoint("net.mcreator.enadvajset.network.EnadvajsetModVariables", "e2",
						new Watchpoint.WatchpointListener() {
							@Override public void watchpointLoaded(Watchpoint watchpoint) {
							}

							@Override public void watchpointModified(Watchpoint watchpoint, WatchpointEvent event) {
								if (event instanceof ModificationWatchpointEvent modificationWatchpointEvent) {
									System.err.println("Modified watchpoint " + watchpoint.getClassname() + "."
											+ watchpoint.getFieldname() + " to "
											+ modificationWatchpointEvent.valueToBe());
								}
							}
						}));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
