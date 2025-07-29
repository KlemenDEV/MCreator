/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2024, Pylo, opensource contributors
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

package net.mcreator.ui.laf;

import io.github.andrewauclair.moderndocking.DockableTabPreference;
import io.github.andrewauclair.moderndocking.internal.DockedTabbedPanel;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.settings.Settings;
import io.github.andrewauclair.moderndocking.ui.DefaultHeaderUI;
import io.github.andrewauclair.moderndocking.ui.DockingSettings;
import io.github.andrewauclair.moderndocking.ui.HeaderController;
import io.github.andrewauclair.moderndocking.ui.HeaderModel;
import net.mcreator.ui.init.UIRES;

import javax.swing.*;

public class DockingUI {

	public static void applyCustomizations() {
		DockingSettings.setHighlighterSelectedBorderProperty("Component.borderColor");
		DockingSettings.setHighlighterNotSelectedBorderProperty("Component.borderColor");

		Settings.setDefaultTabPreference(DockableTabPreference.TOP_ALWAYS);
		Settings.setActiveHighlighterEnabled(false);

		DockedTabbedPanel.setSettingsIcon(UIRES.get("settings"));

		DockingInternal.createHeaderUI = CustomHeaderUI::new;
	}

	private static class CustomHeaderUI extends DefaultHeaderUI {

		public CustomHeaderUI(HeaderController headerController, HeaderModel headerModel) {
			super(headerController, headerModel);
		}

		@Override protected void init() {
			super.init();

			settings.setIcon(UIRES.get("settings"));
			close.setIcon(UIRES.get("close_small"));

			setBackground(UIManager.getColor("Panel.background"));

			this.titleLabel.setBorder(BorderFactory.createEmptyBorder());
		}
	}

}
