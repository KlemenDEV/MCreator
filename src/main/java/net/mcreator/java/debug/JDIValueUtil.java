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

package net.mcreator.java.debug;

import com.sun.jdi.*;

public class JDIValueUtil {

	public static String getValueString(Value value) {
		if (value == null) {
			return "null";
		} else if (value instanceof StringReference) {
			return ((StringReference) value).value();
		} else if (value instanceof BooleanValue) {
			return Boolean.toString(((BooleanValue) value).value());
		} else if (value instanceof ByteValue) {
			return Byte.toString(((ByteValue) value).value());
		} else if (value instanceof CharValue) {
			return Character.toString(((CharValue) value).value());
		} else if (value instanceof ShortValue) {
			return Short.toString(((ShortValue) value).value());
		} else if (value instanceof IntegerValue) {
			return Integer.toString(((IntegerValue) value).value());
		} else if (value instanceof LongValue) {
			return Long.toString(((LongValue) value).value());
		} else if (value instanceof FloatValue) {
			return Float.toString(((FloatValue) value).value());
		} else if (value instanceof DoubleValue) {
			return Double.toString(((DoubleValue) value).value());
		} else if (value instanceof ArrayReference arrayReference) {
			return "[size: " + arrayReference.getValues().size() + "]";
		} else {
			return value.toString();
		}
	}

}
