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

package net.mcreator.ui;

import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.ui.DefaultDockingPanel;
import net.mcreator.Launcher;
import net.mcreator.generator.GeneratorFlavor;
import net.mcreator.generator.setup.WorkspaceGeneratorSetup;
import net.mcreator.plugin.MCREvent;
import net.mcreator.plugin.events.workspace.MCreatorLoadedEvent;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.ui.action.ActionRegistry;
import net.mcreator.ui.action.impl.workspace.RegenerateCodeAction;
import net.mcreator.ui.browser.WorkspaceFileBrowser;
import net.mcreator.ui.component.JEmptyBox;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.debug.DebugPanel;
import net.mcreator.ui.dialogs.workspace.WorkspaceGeneratorSetupDialog;
import net.mcreator.ui.gradle.GradleConsole;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.laf.themes.Theme;
import net.mcreator.ui.search.GlobalSearchListener;
import net.mcreator.ui.variants.modmaker.ModMaker;
import net.mcreator.ui.variants.resourcepackmaker.ResourcePackMaker;
import net.mcreator.ui.workspace.AbstractMainWorkspacePanel;
import net.mcreator.util.MCreatorVersionNumber;
import net.mcreator.workspace.ShareableZIPManager;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class MCreator extends MCreatorFrame {

	private static final Logger LOG = LogManager.getLogger("MCreator");

	private final GradleConsole gradleConsole;

	private final WorkspaceFileBrowser workspaceFileBrowser;

	private final ActionRegistry actionRegistry;

	private final MCreatorTabs mcreatorTabs;

	private final MainMenuBar menuBar;
	private final MainToolBar toolBar;

	private final DebugPanel debugPanel;

	public final MCreatorTabs.Tab workspaceTab;
	public final MCreatorTabs.Tab consoleTab;

	private final boolean hasProjectBrowser;

	public static MCreator create(@Nullable MCreatorApplication application, @Nonnull Workspace workspace) {
		if (workspace.getGeneratorConfiguration().getGeneratorFlavor() == GeneratorFlavor.RESOURCEPACK) {
			return new ResourcePackMaker(application, workspace);
		} else {
			return new ModMaker(application, workspace);
		}
	}

	protected MCreator(@Nullable MCreatorApplication application, @Nonnull Workspace workspace,
			boolean hasProjectBrowser) {
		super(application, workspace);
		LOG.info("Opening MCreator workspace: {}", workspace.getWorkspaceSettings().getModID());

		this.hasProjectBrowser = hasProjectBrowser;

		this.gradleConsole = new GradleConsole(this);

		this.mcreatorTabs = new MCreatorTabs();

		this.actionRegistry = new ActionRegistry(this);

		this.workspaceFileBrowser = new WorkspaceFileBrowser(this);

		this.menuBar = createMenuBar();
		this.toolBar = createToolBar();

		setTitle(WindowTitleHelper.getWindowTitle(this));

		addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent arg0) {
				closeThisMCreator(false);
			}
		});

		GlobalSearchListener.install(this, () -> mcreatorTabs.getCurrentTab().getContent());

		debugPanel = new DebugPanel(this);

		JPanel pon = new JPanel(new BorderLayout(0, 0));
		pon.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.current().getSecondAltBackgroundColor()));

		workspaceTab = new MCreatorTabs.Tab(L10N.t("tab.workspace"), createWorkspaceTabContent(), "Workspace", true,
				false);
		mcreatorTabs.addTab(workspaceTab);
		pon.add("West", workspaceTab);

		mcreatorTabs.addTabShownListener(tab -> {
			reloadWorkspaceTabContents();

			menuBar.refreshMenuBar();

			setTitle(WindowTitleHelper.getWindowTitle(this));
		});

		consoleTab = new MCreatorTabs.Tab(L10N.t("tab.console") + " ", gradleConsole, "Console", true, false) {
			@Override public void paintComponent(Graphics g) {
				super.paintComponent(g);
				switch (gradleConsole.getStatus()) {
				case GradleConsole.READY:
					g.setColor(Theme.current().getForegroundColor());
					break;
				case GradleConsole.RUNNING:
					g.setColor(new Color(158, 247, 89));
					break;
				case GradleConsole.ERROR:
					g.setColor(new Color(0xFF5956));
					break;
				}
				if (gradleConsole.isGradleSetupTaskRunning())
					g.setColor(new Color(106, 247, 244));
				g.fillRect(getWidth() - 15, getHeight() - 18, 3, 3);
			}
		};
		consoleTab.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)
					actionRegistry.buildWorkspace.doAction();
			}
		});
		mcreatorTabs.addTab(consoleTab);
		pon.add("East", consoleTab);

		mcreatorTabs.showTabNoNotify(workspaceTab);

		pon.add("Center", mcreatorTabs.getTabsStrip());

		workspace.getFileManager().setDataSavedListener(() -> getStatusBar().setPersistentMessage(
				L10N.t("workspace.statusbar.autosave_message", new SimpleDateFormat("HH:mm").format(new Date()))));

		JComponent rightPanel = PanelUtils.northAndCenterElement(pon, mcreatorTabs.getContainer());

		Docking docking = new Docking(this);
		RootDockingPanel dockingPanelRoot = new RootDockingPanel(docking, this);

		DefaultDockingPanel dockingPanelTest = new DefaultDockingPanel("workspace", "Workspace") {
			@Override public boolean isWrappableInScrollpane() {
				return false;
			}
		};
		dockingPanelTest.setLayout(new BorderLayout(0, 0));
		dockingPanelTest.add("Center", rightPanel);
		docking.registerDockable(dockingPanelTest);
		docking.dock(dockingPanelTest, this, DockingRegion.CENTER);

		if (hasProjectBrowser) {
			DefaultDockingPanel dockingPanelTest2 = new DefaultDockingPanel("project_browser", "Project browser") {
				@Override public boolean isWrappableInScrollpane() {
					return false;
				}
			};
			dockingPanelTest2.setLayout(new BorderLayout(0, 0));
			dockingPanelTest2.add("Center", workspaceFileBrowser);
			docking.registerDockable(dockingPanelTest2);
			docking.dock(dockingPanelTest2, this, DockingRegion.WEST);
			//docking.minimize(dockingPanelTest2);
		}

		DefaultDockingPanel dockingPanelTest3 = new DefaultDockingPanel("gradle_console", "Console") {
			@Override public boolean isWrappableInScrollpane() {
				return false;
			}
		};
		dockingPanelTest3.setLayout(new BorderLayout(0, 0));
		dockingPanelTest3.add("Center", gradleConsole);
		docking.registerDockable(dockingPanelTest3);
		docking.dock(dockingPanelTest3, this, DockingRegion.SOUTH);
		//docking.minimize(dockingPanelTest3);

		DefaultDockingPanel dockingPanelTest4 = new DefaultDockingPanel("debugger", "Debugger") {
			@Override public boolean isWrappableInScrollpane() {
				return false;
			}
		};
		dockingPanelTest4.setLayout(new BorderLayout(0, 0));
		dockingPanelTest4.add("Center", debugPanel);
		docking.registerDockable(dockingPanelTest4);
		docking.dock("debugger", "gradle_console", DockingRegion.CENTER);
		//docking.minimize(dockingPanelTest4);

		//docking.unpinDockable(dockingPanelTest2);
		//docking.unpinDockable(dockingPanelTest3);
		//docking.unpinDockable(dockingPanelTest4);

		docking.display(dockingPanelTest);

		setMainContent(dockingPanelRoot);

		add("North", toolBar);

		addWindowListener(new WindowAdapter() {
			@Override public void windowOpened(WindowEvent e) {
				super.windowOpened(e);
				// Finalize MCreator initialization when the window is fully opened
				initializeMCreator();
			}
		});

		MCREvent.event(new MCreatorLoadedEvent(this));
	}

	protected abstract MainMenuBar createMenuBar();

	protected abstract MainToolBar createToolBar();

	protected abstract JPanel createWorkspaceTabContent();

	public abstract AbstractMainWorkspacePanel getWorkspacePanel();

	public final void reloadWorkspaceTabContents() {
		if (mcreatorTabs.getCurrentTab().equals(workspaceTab)) {
			getWorkspacePanel().reloadWorkspaceTab();
		}
	}

	private void initializeMCreator() {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		if (MCreatorVersionNumber.isBuildNumberDevelopment(workspace.getMCreatorVersion())) {
			workspace.setMCreatorVersion(
					Launcher.version.versionlong); // if we open dev version, store new version number in it
		}

		// backup if new version and backups are enabled
		if (workspace.getMCreatorVersion() < Launcher.version.versionlong
				&& PreferencesManager.PREFERENCES.backups.backupOnVersionSwitch.get()) {
			ShareableZIPManager.exportZIP(L10N.t("dialog.workspace.export_backup"),
					new File(workspace.getFolderManager().getWorkspaceCacheDir(),
							"FullBackup" + workspace.getMCreatorVersion() + ".zip"), this, true);
		}

		// if we need to set up the workspace, we do so
		if (WorkspaceGeneratorSetup.shouldSetupBeRan(workspace.getGenerator())) {
			WorkspaceGeneratorSetupDialog.runSetup(this,
					PreferencesManager.PREFERENCES.notifications.openWhatsNextPage.get()
							&& !Launcher.version.isDevelopment());
		}

		if (workspace.getMCreatorVersion()
				< Launcher.version.versionlong) { // if this is the case, update the workspace files
			RegenerateCodeAction.regenerateCode(this, true, true);
			workspace.setMCreatorVersion(Launcher.version.versionlong);
			workspace.getFileManager().saveWorkspaceDirectlyAndWait();
		} else if (workspace.isRegenerateRequired()) { // if workspace is marked for regeneration, we do so
			RegenerateCodeAction.regenerateCode(this, true, true);
		}

		// it is not safe to do user operations on workspace while it is being preloaded, so we lock the UI
		setGlassPane(getPreloaderPane());
		getGlassPane().setVisible(true);

		// Preload workspace file browser
		new Thread(this.workspaceFileBrowser::reloadTree, "File browser preloader").start();

		// reinit (preload) MCItems (also loads GEs and performs conversions if needed)
		new Thread(() -> {
			workspace.getModElements().forEach(ModElement::getMCItems);

			SwingUtilities.invokeLater(() -> {
				getGlassPane().setVisible(false);
				setGlassPane(new JEmptyBox());
				setJMenuBar(menuBar);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				workspaceFullyLoaded();
				workspaceGeneratorSwitched();
			});
		}, "ME preloader").start();
	}

	public void workspaceFullyLoaded() {
	}

	/**
	 * Called every time generator is switched. Also called when MCreator is loaded for the first time.
	 */
	public void workspaceGeneratorSwitched() {
	}

	public boolean closeThisMCreator(boolean returnToProjectSelector) {
		boolean safetoexit = gradleConsole.getStatus() != GradleConsole.RUNNING;
		if (!safetoexit) {
			if (gradleConsole.isGradleSetupTaskRunning()) {
				JOptionPane.showMessageDialog(this, L10N.t("action.gradle.close_mcreator_while_installation_message"),
						L10N.t("action.gradle.close_mcreator_while_installation_title"), JOptionPane.WARNING_MESSAGE);
				return false;
			}

			int reply = JOptionPane.showConfirmDialog(this,
					L10N.t("action.gradle.close_mcreator_while_running_message"),
					L10N.t("action.gradle.close_mcreator_while_running_title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null);
			if (reply == JOptionPane.YES_OPTION) {
				safetoexit = true;
				gradleConsole.cancelTask();
			}
		}

		if (safetoexit) {
			LOG.info("Closing MCreator window ...");
			PreferencesManager.PREFERENCES.hidden.fullScreen.set(getExtendedState() == MAXIMIZED_BOTH);
			//TODO: remove this preference
			//if (splitPane != null)
			//	workspace.getWorkspaceUserSettings().projectBrowserSplitPos = splitPane.getDividerLocation();

			mcreatorTabs.getTabs().forEach(tab -> {
				if (tab.getTabClosedListener() != null)
					tab.getTabClosedListener().tabClosed(tab);
			});

			workspace.close();

			dispose(); // close the window

			application.getOpenMCreators().remove(this);

			if (application.getOpenMCreators()
					.isEmpty()) { // no MCreator windows left, close the app, or return to project selector if selected
				if (returnToProjectSelector)
					application.showWorkspaceSelector();
				else
					application.closeApplication();
			}

			return true;
		}
		return false;
	}

	@Override public void setTitle(String title) {
		super.setTitle(title);

		if (application != null) {
			String tabAddition = "";

			if (mcreatorTabs.getCurrentTab() != null) {
				tabAddition = " - " + mcreatorTabs.getCurrentTab().getText();
			}

			// Do not externalize this text
			application.getDiscordClient()
					.updatePresence("Working on " + workspace.getWorkspaceSettings().getModName() + tabAddition,
							Launcher.version.getMajorString() + " for " + workspace.getGenerator()
									.getGeneratorMinecraftVersion(),
							"type-" + workspace.getGeneratorConfiguration().getGeneratorFlavor().name()
									.toLowerCase(Locale.ENGLISH));
		}
	}

	public void showProjectBrowser(boolean visible) {
		//TODO: implement docking
	}

	public GradleConsole getGradleConsole() {
		return gradleConsole;
	}

	public WorkspaceFileBrowser getProjectBrowser() {
		return workspaceFileBrowser;
	}

	public ActionRegistry getActionRegistry() {
		return actionRegistry;
	}

	public MCreatorTabs getTabs() {
		return mcreatorTabs;
	}

	public DebugPanel getDebugPanel() {
		return debugPanel;
	}

	public MainMenuBar getMainMenuBar() {
		return menuBar;
	}

	public MainToolBar getToolBar() {
		return toolBar;
	}

	public final boolean hasProjectBrowser() {
		return hasProjectBrowser;
	}

}
