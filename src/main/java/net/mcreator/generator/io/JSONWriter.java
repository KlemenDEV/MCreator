/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2026, Pylo, opensource contributors
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

/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2020 Pylo and contributors
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

package net.mcreator.generator.io;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import net.mcreator.workspace.Workspace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.StringJoiner;

public class JSONWriter {

	private static final Logger LOG = LogManager.getLogger("JSON Writer");

	/**
	 * Arrays with at most this many elements, all of them primitives or nulls, are written on a single line
	 * (e.g. {@code [0, 0, 16]}) instead of one element per line. This matches the style of vanilla resources
	 * and tools like Blockbench.
	 */
	private static final int MAX_INLINE_ARRAY_SIZE = 16;

	private static final FormattingStyle PRETTY_STYLE = FormattingStyle.PRETTY.withIndent("  ");

	public static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

	public static void writeJSONToFile(@Nullable Workspace workspace, String srcjson, File file) {
		GradleTrackingFileIO.writeFile(workspace, formatJSON(srcjson), file);
	}

	public static String formatJSON(String srcjson) {
		String jsonout;
		try {
			JsonElement json = JsonParser.parseString(srcjson);
			jsonout = toJson(json);
		} catch (Exception e) {
			LOG.error("JSON Prettify failed, error: {}", e.getMessage(), e);
			jsonout = srcjson;
		}
		return jsonout;
	}

	/**
	 * Pretty-prints the given JSON element, keeping key order and writing short arrays of primitives inline.
	 *
	 * @param json JSON element to format
	 * @return formatted JSON string
	 */
	public static String toJson(JsonElement json) {
		try {
			StringWriter stringWriter = new StringWriter();
			JsonWriter writer = new JsonWriter(stringWriter);
			writer.setHtmlSafe(false);
			writer.setStrictness(Strictness.LENIENT);
			writer.setFormattingStyle(PRETTY_STYLE);
			write(json, writer);
			return stringWriter.toString();
		} catch (IOException e) {
			LOG.error("JSON Prettify failed, error: {}", e.getMessage(), e);
			return gson.toJson(json);
		}
	}

	private static void write(JsonElement element, JsonWriter writer) throws IOException {
		if (element.isJsonObject()) {
			writer.beginObject();
			for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
				writer.name(entry.getKey());
				write(entry.getValue(), writer);
			}
			writer.endObject();
		} else if (element.isJsonArray()) {
			JsonArray array = element.getAsJsonArray();
			if (isInlineable(array)) {
				StringJoiner joiner = new StringJoiner(", ", "[", "]");
				for (JsonElement child : array)
					joiner.add(gson.toJson(child));
				writer.jsonValue(joiner.toString());
			} else {
				writer.beginArray();
				for (JsonElement child : array)
					write(child, writer);
				writer.endArray();
			}
		} else {
			gson.toJson(element, writer);
		}
	}

	private static boolean isInlineable(JsonArray array) {
		if (array.size() > MAX_INLINE_ARRAY_SIZE)
			return false;
		for (JsonElement child : array) {
			if (!child.isJsonPrimitive() && !child.isJsonNull())
				return false;
		}
		return true;
	}

}
