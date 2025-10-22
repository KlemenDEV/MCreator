/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2025, Pylo, opensource contributors
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

package net.mcreator.generator;

import net.mcreator.element.GeneratableElement;
import net.mcreator.util.ListUtils;

import java.util.ArrayList;
import java.util.List;

public class TemplateSplitter {

	/**
	 * Splits the given templates based on the provided generatable elements and list name.
	 * It handles splitting of generatable elements into chunks if the list limit specified
	 * by the template is exceeded. If no limit is set or the total elements are within the limit,
	 * it adds all elements to the template. For templates requiring chunks, it creates indexed
	 * versions of each template for each chunk.
	 *
	 * @param templates           the list of GeneratorTemplate objects to process
	 * @param generatableElements the list of elements to associate with the templates
	 * @param listName            the name of the data model entry where the elements will be added
	 * @return a list of GeneratorTemplate objects with updated data models and indexing applied
	 */
	public static List<GeneratorTemplate> split(List<GeneratorTemplate> templates,
			List<GeneratableElement> generatableElements, String listName) {

		List<GeneratorTemplate> retval = new ArrayList<>();
		for (GeneratorTemplate template : templates) {
			int limit = template.getListLimit();

			if (limit <= 0 || generatableElements.size() <= limit) {
				template.addDataModelEntry(listName, generatableElements);
				retval.add(template);
			} else {
				List<List<GeneratableElement>> chunks = ListUtils.splitList(generatableElements, limit);
				for (int i = 0; i < chunks.size(); i++) {
					GeneratorTemplate indexed = template.withIndex(i + 1);
					indexed.addDataModelEntry(listName, chunks.get(i));
					indexed.addDataModelEntry("chunkIndex", i + 1);
					retval.add(indexed);
				}
			}
		}
		return retval;
	}

}
