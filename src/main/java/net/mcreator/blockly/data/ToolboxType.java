/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2022, Pylo, opensource contributors
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

package net.mcreator.blockly.data;

public record ToolboxType(String name) {
	public static final ToolboxType AI_BUILDER = new ToolboxType("ai_builder");
	public static final ToolboxType PROCEDURE = new ToolboxType("procedure");
	public static final ToolboxType COMMAND = new ToolboxType("command");
	public static final ToolboxType FEATURE = new ToolboxType("feature");
	public static final ToolboxType EMPTY = new ToolboxType("empty");
}
