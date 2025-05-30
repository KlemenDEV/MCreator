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

package net.mcreator.ui.validation.validators;

import net.mcreator.java.JavaConventions;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.validation.Validator;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.util.StringUtils;

import java.util.Locale;
import java.util.Set;

public class JavaMemberNameValidator implements Validator {

	private final VTextField textField;
	private final boolean firstLetterUppercase;
	private final boolean allowInitialUnderscore;

	public JavaMemberNameValidator(VTextField textField, boolean requireFirstLetterUppercase) {
		this(textField, requireFirstLetterUppercase, true);
	}

	public JavaMemberNameValidator(VTextField textField, boolean requireFirstLetterUppercase,
			boolean allowInitialUnderscore) {
		this.textField = textField;
		this.firstLetterUppercase = requireFirstLetterUppercase;
		this.allowInitialUnderscore = allowInitialUnderscore;
	}

	@Override public ValidationResult validate() {
		String text = textField.getText();

		if (text == null || text.isEmpty())
			return new Validator.ValidationResult(Validator.ValidationResultType.ERROR,
					L10N.t("validators.java_name.needs_name"));

		if (!allowInitialUnderscore && text.charAt(0) == '_')
			return new Validator.ValidationResult(Validator.ValidationResultType.ERROR,
					L10N.t("validators.java_name.cannot_start_with_underscore"));

		if (firstLetterUppercase && Character.isLowerCase(text.charAt(0))) {
			if (text.length() > 1)
				text = text.substring(0, 1).toUpperCase(Locale.ENGLISH) + text.substring(1);
			else
				text = text.substring(0, 1).toUpperCase(Locale.ENGLISH);
			textField.setText(text);
		}

		text = textField.getText();
		if (!JavaConventions.isValidJavaIdentifier(text)) {
			return new Validator.ValidationResult(Validator.ValidationResultType.ERROR,
					L10N.t("validators.java_name.invalid_name"));
		} else if (JavaConventions.isStringReservedJavaWord(text)) {
			return new Validator.ValidationResult(Validator.ValidationResultType.ERROR,
					L10N.t("validators.java_name.reserved_keywords"));
		} else if (common_names.contains(text)) {
			return new Validator.ValidationResult(Validator.ValidationResultType.ERROR,
					L10N.t("validators.java_name.vanilla_names"));
		} else if (JavaConventions.containsInvalidJavaNameCharacters(text)) {
			return new Validator.ValidationResult(ValidationResultType.ERROR,
					L10N.t("validators.java_name.characters_not_allowed"));
		} else if (firstLetterUppercase && !text.isEmpty() && !StringUtils.isUppercaseLetter(
				textField.getText().charAt(0))) {
			return new Validator.ValidationResult(Validator.ValidationResultType.WARNING,
					L10N.t("validators.java_names.upper_case_first_character"));
		} else {
			return Validator.ValidationResult.PASSED;
		}
	}

	private static final Set<String> common_names = Set.of("Axe", "Pickaxe", "Spade", "Hoe", "Shovel", "Sword",
			"Shears", "FishingRod", "Compass", "Clock", "Shield", "Overworld", "Nether", "World", "Living", "Mob",
			"Monster", "Animal", "End", "Stairs", "Slab", "Fence", "Wall", "Leaves", "TrapDoor", "Pane", "Door",
			"FenceGate", "Creature", "Item", "Block", "BoneMeal", "Diamond", "Ore", "Gem", "Gold", "Iron", "Stack",
			"Emerald", "Entity", "Surface", "WoodButton", "StoneButton", "Flower", "Falling", "Furnace", "Bush", "Crop",
			"Structure", "Blocks", "Items", "Biomes", "Timer", "Direction", "Number", "Tool", "Console");

}
