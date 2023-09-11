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
import net.mcreator.generator.GeneratorTokens;
import net.mcreator.java.debug.JVMDebugClient;
import net.mcreator.java.debug.Watchpoint;
import net.mcreator.ui.MCreator;
import net.mcreator.workspace.elements.VariableElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class DebugVariablesHandler {

	private static final Logger LOG = LogManager.getLogger("DebugVariablesHandler");

	public static void handleVariables(MCreator mcreator, DebugPanel debugPanel) {
		JVMDebugClient debugClient = debugPanel.getDebugClient();
		if (debugClient != null) {
			for (VariableElement variableElement : mcreator.getWorkspace().getVariableElements()) {
				try {
					Map<?, ?> definition = variableElement.getType()
							.getScopeDefinition(mcreator.getWorkspace(), variableElement.getScope().name());
					if (definition == null)
						continue;

					Object classFieldTargetObj = definition.get("debug_watchpoint");
					if (classFieldTargetObj == null)
						continue;

					String classFieldTarget = classFieldTargetObj.toString();

					String[] classFieldTargetSplit = GeneratorTokens.replaceTokens(mcreator.getWorkspace(),
							classFieldTarget.replace("@Name", variableElement.getName())).split("#");
					String className = classFieldTargetSplit[0];
					String fieldName = classFieldTargetSplit[1];

					debugClient.addWatchpoint(new Watchpoint(className, fieldName, new Watchpoint.WatchpointListener() {
						@Override public void watchpointLoaded(Watchpoint watchpoint) {
						}

						@Override public void watchpointModified(Watchpoint watchpoint, WatchpointEvent event) {
							if (event instanceof ModificationWatchpointEvent modificationWatchpointEvent) {
								mcreator.mv.variablesPan.setVariableDebugValue(variableElement,
										event.object() == null ? 0 : event.object().uniqueID(),
										modificationWatchpointEvent.valueToBe());
							}
						}
					}));
				} catch (Exception e) {
					LOG.warn("Failed to add watchpoint", e);
				}
			}
		}
	}

}
