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

package net.mcreator.ui.action.impl.workspace;

import net.mcreator.element.GeneratableElement;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.action.ActionRegistry;
import net.mcreator.ui.action.BasicAction;
import net.mcreator.ui.action.impl.gradle.GradleAction;
import net.mcreator.ui.blockly.BlocklyPanel;
import net.mcreator.ui.dialogs.ProgressDialog;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.modgui.IBlocklyPanelHolder;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.Validator;
import net.mcreator.workspace.elements.ModElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ValidateWorkspaceAction extends BasicAction {

	private static final Logger LOG = LogManager.getLogger("Validate Workspace");

	public ValidateWorkspaceAction(ActionRegistry actionRegistry) {
		super(actionRegistry, L10N.t("action.workspace.validate_workspace"), e -> {
			int reply = JOptionPane.showConfirmDialog(actionRegistry.getMCreator(),
					L10N.t("action.workspace.validate_workspace.confirmation"),
					L10N.t("action.workspace.validate_workspace.confirmation.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null);
			if (reply == JOptionPane.YES_OPTION) {
				validateWorkspace(actionRegistry.getMCreator());
			}
		});
	}

	public static void validateWorkspace(MCreator mcreator) {
		ProgressDialog dial = new ProgressDialog(mcreator, L10N.t("dialog.workspace.validate_workspace.title"));
		Thread thread = new Thread(() -> {
			ProgressDialog.ProgressUnit p1 = new ProgressDialog.ProgressUnit(
					L10N.t("dialog.workspace.validate_workspace.progress.validating"));
			dial.addProgressUnit(p1);

			Map<ModElement, List<Validator.ValidationResult>> results = new HashMap<>();

			int modstoload = mcreator.getWorkspace().getModElements().size();
			int i = 0;
			for (ModElement mod : mcreator.getWorkspace().getModElements()) {
				p1.setPercent((int) (i / (float) modstoload * 100));
				i++;

				if (mod.isCodeLocked()) {
					continue; // do not validate locked mods
				}

				GeneratableElement generatableElement = mod.getGeneratableElement();
				if (generatableElement == null) {
					results.put(mod, Collections.singletonList(
							new Validator.ValidationResult(Validator.ValidationResultType.ERROR,
									L10N.t("action.workspace.validate_workspace.no_generatableelement"))));
					continue;
				}

				try {
					ModElementGUI<?> modElementGUI = openModElementGUIForValidation(mcreator, generatableElement);
					if (modElementGUI != null) {
						List<Validator.ValidationResult> validationResults = modElementGUI.validateAllPages()
								.getGroupedValidationResults().stream()
								.filter(result -> (result.getValidationResultType()
										== Validator.ValidationResultType.ERROR)).collect(Collectors.toList());

						if (!validationResults.isEmpty())
							results.put(mod, validationResults);
					} else {
						throw new NullPointerException("ModElementGUI is null");
					}
				} catch (Exception e) {
					LOG.warn("Failed to open ModElementGUI for validation", e);
				}

				System.gc();
			}

			dial.hideDialog();

			if (results.isEmpty()) {
				JOptionPane.showMessageDialog(mcreator, L10N.t("action.workspace.validate_workspace.success"),
						L10N.t("action.workspace.validate_workspace.success.title"), JOptionPane.INFORMATION_MESSAGE,
						UIRES.get("18px.ok"));
			} else {
				// Print results
				for (Map.Entry<ModElement, List<Validator.ValidationResult>> entry : results.entrySet()) {
					System.err.println("Validation results for " + entry.getKey().getName());
					for (Validator.ValidationResult result : entry.getValue()) {
						System.err.println(result.getValidationResultType() + ": " + result.getMessage());
					}
				}
			}
		}, "ValidateWorkspace");
		thread.start();
		dial.setVisible(true);
	}

	@Nullable
	public static ModElementGUI<?> openModElementGUIForValidation(MCreator mcreator,
			GeneratableElement generatableElement) throws Exception {
		ModElementGUI<?> modElementGUI = generatableElement.getModElement().getType()
				.getModElementGUI(mcreator, generatableElement.getModElement(), false);
		modElementGUI.reloadDataLists();

		Field field = modElementGUI.getClass().getSuperclass().getDeclaredField("editingMode");
		field.setAccessible(true);
		field.set(modElementGUI, true);

		CountDownLatch latch = new CountDownLatch(1);
		if (modElementGUI instanceof IBlocklyPanelHolder panelHolder) {
			Set<BlocklyPanel> blocklyPanels = new HashSet<>();
			panelHolder.addBlocklyChangedListener(blocklyPanel -> {
				blocklyPanels.add(blocklyPanel);
				if (blocklyPanels.equals(panelHolder.getBlocklyPanels()))
					latch.countDown();
			});
		}

		// Open GeneratableElement in editing mode
		Method method = modElementGUI.getClass().getDeclaredMethod("openInEditingMode", GeneratableElement.class);
		method.setAccessible(true);
		method.invoke(modElementGUI, generatableElement);

		// If ModElementGUI<?> contains BlocklyPanel, give it time to fully load by waiting for all panels to report change
		if (modElementGUI instanceof IBlocklyPanelHolder) {
			boolean success = latch.await(5, TimeUnit.SECONDS);
			if (!success)
				return null;
		}

		return modElementGUI;
	}

}
