package org.geogebra.web.full.gui.menubar;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GFont;
import org.geogebra.common.gui.toolbar.ToolBar;
import org.geogebra.common.javax.swing.GOptionPane;
import org.geogebra.common.main.App;
import org.geogebra.common.main.ExamEnvironment;
import org.geogebra.common.main.ExamLogBuilder;
import org.geogebra.common.main.Feature;
import org.geogebra.common.main.Localization;
import org.geogebra.common.main.MaterialsManagerI;
import org.geogebra.common.move.events.BaseEvent;
import org.geogebra.common.move.ggtapi.events.LoginEvent;
import org.geogebra.common.move.views.BooleanRenderable;
import org.geogebra.common.move.views.EventRenderable;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.web.full.css.GuiResources;
import org.geogebra.web.full.css.MaterialDesignResources;
import org.geogebra.web.full.export.PrintPreviewW;
import org.geogebra.web.full.gui.app.HTMLLogBuilder;
import org.geogebra.web.full.gui.browser.SignInButton;
import org.geogebra.web.full.gui.dialog.DialogManagerW;
import org.geogebra.web.full.gui.exam.ExamDialog;
import org.geogebra.web.full.gui.layout.LayoutW;
import org.geogebra.web.full.gui.util.SaveDialogW;
import org.geogebra.web.full.gui.util.ShareDialogW;
import org.geogebra.web.html5.Browser;
import org.geogebra.web.html5.awt.GFontW;
import org.geogebra.web.html5.awt.GGraphics2DW;
import org.geogebra.web.html5.gui.util.AriaMenuItem;
import org.geogebra.web.html5.main.AppW;

import com.google.gwt.canvas.client.Canvas;
import com.himamis.retex.editor.share.util.Unicode;

/**
 * Web implementation of FileMenu
 */
public class FileMenuW extends GMenuBar implements BooleanRenderable {
	
	private static final double PADDING = 24;
	/** Canvas line height */
	protected static final int LINE_HEIGHT = 24;
	private AriaMenuItem shareItem;
	/** clear construction and reset GUI */
	Runnable newConstruction;
	private AriaMenuItem printItem;
	private Localization loc;
	
	/**
	 * @param app application
	 */
	public FileMenuW(final AppW app) {
		super("file", app);
		this.loc = app.getLocalization();
	    this.newConstruction = new Runnable() {
			
			@Override
			public void run() {
				app.setWaitCursor();
				app.fileNew();
				app.setDefaultCursor();
				
				if (!app.isUnbundledOrWhiteboard()) {
					app.showPerspectivesPopup();
				}
				if (app.has(Feature.MOW_MULTI_PAGE)
						&& app.getPageController() != null) {
					app.getPageController().resetPageControl();
				}
			}
		};
		if (app.isUnbundledOrWhiteboard()) {
			addStyleName("matStackPanel");
		} else {
			addStyleName("GeoGebraMenuBar");
		}
	    initActions();
	}
	
	/**
	 * @return whether native JS function for sharing is present
	 */
	public native static boolean nativeShareSupported()/*-{
		if ($wnd.android && $wnd.android.share) {
			return true;
		}
		return false;
	}-*/;

	/**
	 * Exit exam and restore normal mode
	 */
	protected void exitAndResetExam() {
		getApp().getLAF().toggleFullscreen(false);
		ExamEnvironment exam = getApp().getExam();
		exam.exit();
		boolean examFile = getApp().getArticleElement().hasDataParamEnableGraphing();
		String buttonText = null;
		AsyncOperation<String[]> handler = null;
		AsyncOperation<String[]> welcomeHandler = null;
		if (examFile) {
			if (getApp().getVersion().isAndroidWebview()) {
				handler = new AsyncOperation<String[]>() {
					@Override
					public void callback(String[] dialogResult) {
						// for android tablets we just want to exit app
						ExamDialog.exitApp();
					}
				};
				buttonText = loc.getMenu("Exit");
			} else {
				handler = new AsyncOperation<String[]>() {
					@Override
					public void callback(String[] dialogResult) {
						getApp().setNewExam();
						ExamDialog.startExam(null, getApp());
					}
				};
				welcomeHandler = new AsyncOperation<String[]>() {

					@Override
					public void callback(String[] obj) {
						getApp().getLAF().toggleFullscreen(true);
						getApp().setNewExam();
						getApp().examWelcome();
					}
				};
				buttonText = loc.getMenu("Restart");
			}
			exam.setHasGraph(true);
			boolean supportsCAS = getApp().getSettings().getCasSettings()
					.isEnabled();
			boolean supports3D = getApp().getSettings().getEuclidian(-1).isEnabled();
			if (!supports3D && supportsCAS) {
				showFinalLog(loc.getMenu("ExamCAS"), buttonText, handler);
			} else if (!supports3D && !supportsCAS) {
				if (getApp().enableGraphing()) {
					showFinalLog(loc.getMenu("ExamGraphingCalc.long"),
							buttonText, handler);
				} else {
					showFinalLog(loc.getMenu("ExamSimpleCalc.long"), buttonText,
							handler);
				}
			} else {
				showFinalLog(loc.getMenu("exam_log_header") + " "
								+ getApp().getVersionString(),
						buttonText,
						welcomeHandler);
			}
		} else {
			handler = new AsyncOperation<String[]>() {
				@Override
				public void callback(String[] dialogResult) {
					getApp().fileNew();
				}
			};
			buttonText = loc.getMenu("OK");
			showFinalLog(loc.getMenu("exam_log_header") + " "
							+ getApp().getVersionString(),
					buttonText, handler);
		}
		resetAfterExam();
	}

	private void showFinalLog(String menu, String buttonText,
			AsyncOperation<String[]> handler) {
		getApp().fileNew();
		HTMLLogBuilder htmlBuilder = new HTMLLogBuilder();
		getApp().getExam().getLog(loc, getApp().getSettings(), htmlBuilder);
		getApp().showMessage(htmlBuilder.getHTML(), menu, buttonText, handler);
		Canvas canvas = Canvas.createIfSupported();
		final GGraphics2DW g2 = new GGraphics2DW(canvas);
		g2.setCoordinateSpaceSize(500,
				getApp().getExam().getEventCount() * LINE_HEIGHT + 200);
		g2.setColor(GColor.WHITE);
		g2.fillRect(0, 0, canvas.getCoordinateSpaceWidth(),
				canvas.getCoordinateSpaceHeight());
		g2.setFont(new GFontW("SansSerif", GFont.PLAIN, 24));
		g2.setColor(GColor.BLACK);
		g2.drawString(menu, PADDING, PADDING);
		ExamLogBuilder canvasLogBuilder = new ExamLogBuilder() {
			private int yOffset = 48;
			@Override
			public void addLine(StringBuilder sb) {
				g2.setFont(new GFontW("SansSerif", GFont.PLAIN, 16));
				g2.drawString(sb.toString(), PADDING, yOffset);
				yOffset += LINE_HEIGHT;
			}

			@Override
			public void addHR() {
				g2.drawStraightLine(PADDING, yOffset - LINE_HEIGHT / 2,
						PADDING + 200, yOffset - LINE_HEIGHT / 2);
				yOffset += LINE_HEIGHT / 2;
			}
		};
		getApp().getExam().getLog(loc, getApp().getSettings(),
				canvasLogBuilder);
		Browser.exportImage(canvas.toDataUrl(), "ExamLog.png");
	}

	private void resetAfterExam() {
		getApp().setExam(null);
		getApp().resetViewsEnabled();
		LayoutW.resetPerspectives(getApp());
		getApp().getLAF().addWindowClosingHandler(getApp());
		getApp().fireViewsChangedEvent();
		getApp().getGuiManager().updateToolbarActions();
		getApp().getGuiManager().setGeneralToolBarDefinition(
				ToolBar.getAllToolsNoMacros(true, false, getApp()));
		getApp().getGuiManager().resetMenu();
		getApp().setActivePerspective(0);

	}

	private void initActions() {
		// if (!app.has(Feature.NEW_START_SCREEN)) {
		if (getApp().isExam()) {
			addItem(MainMenu.getMenuBarHtml(GuiResources.INSTANCE
					.menu_icon_sign_out().getSafeUri().asString(),
					loc.getMenu("exam_menu_exit"), true), true,
					new MenuCommand(getApp()) { // Close

						@Override
						public void doExecute() {
							showExamExitDialog();
						}
					});
			return;
		}
		/*
		 * } else { if (app.isExam()) { return; } }
		 */
		// this is enabled always
		addItem(MainMenu
				.getMenuBarHtml(
						MaterialDesignResources.INSTANCE.add_black()
								.getSafeUri().asString(),
				loc.getMenu("New"), true), true, new MenuCommand(getApp()) {

					@Override
					public void doExecute() {
						((DialogManagerW) getApp().getDialogManager())
								.getSaveDialog().showIfNeeded(newConstruction);
					}
		});
		// open menu is always visible in menu
		addItem(MainMenu.getMenuBarHtml(
				MaterialDesignResources.INSTANCE.search_black().getSafeUri()
						.asString(),
				loc.getMenu("Open"), true), true, new MenuCommand(getApp()) {

					@Override
					public void doExecute() {
						getApp().openSearch(null);
					}
				});
		if (getApp().getLAF().undoRedoSupported()) {
			addItem(MainMenu.getMenuBarHtml(
					MaterialDesignResources.INSTANCE.save_black()
									.getSafeUri().asString(),
					loc.getMenu("Save"), true), true, new MenuCommand(getApp()) {
		
						@Override
						public void doExecute() {
							getApp().getGuiManager().save();
						}
			});			
		}
		addSeparator();
		addItem(MainMenu.getMenuBarHtml(
				MaterialDesignResources.INSTANCE.export_image_black()
						.getSafeUri().asString(),
				loc.getMenu("exportImage"), true), true,
				new MenuCommand(getApp()) {

					@Override
					public void doExecute() {
						app.getGuiManager().getDialogManager()
								.showExportImageDialog(null);
					}
				});
		shareItem = addItem(MainMenu.getMenuBarHtml(
				MaterialDesignResources.INSTANCE.share_black().getSafeUri()
						.asString(),
					loc.getMenu("Share"), true), true, new MenuCommand(getApp()) {

					@Override
					public void doExecute() {
						share(getApp());
					}
			});
		if (getApp().getLAF().exportSupported() && !getApp().isUnbundledOrWhiteboard()) {
			addItem(MainMenu.getMenuBarHtml(
					MaterialDesignResources.INSTANCE.file_download_black()
							.getSafeUri().asString(),
					loc.getMenu("DownloadAs") + Unicode.ELLIPSIS, true),
					true, new ExportMenuW(getApp()), true);
		}
		if (getApp().getLAF().printSupported()) {
			printItem = new AriaMenuItem(
					MainMenu.getMenuBarHtml(
							MaterialDesignResources.INSTANCE.print_black()
									.getSafeUri().asString(),
					loc.getMenu("PrintPreview"), true),
					true, new MenuCommand(getApp()) {

						@Override
						public void doExecute() {
							if (getApp().getGuiManager()
									.showView(App.VIEW_EUCLIDIAN)
									|| getApp().getGuiManager()
											.showView(App.VIEW_EUCLIDIAN2)
									|| getApp().getGuiManager()
											.showView(App.VIEW_ALGEBRA)
									|| getApp().getGuiManager().showView(
											App.VIEW_CONSTRUCTION_PROTOCOL)) {
								new PrintPreviewW(getApp()).show();
							}
						}
			});
			// updatePrintMenu();
			addItem(printItem);
		}
	    getApp().getNetworkOperation().getView().add(this);
	    if (!getApp().getNetworkOperation().isOnline()) {
	    	render(false);    	
	    }
	}

	/**
	 * Open share dialog for given app
	 * 
	 * @param app
	 *            application
	 */
	public static void share(AppW app) {
		if (!nativeShareSupported()) {
			showShareDialog(app);
		} else {
			app.getGgbApi().getBase64(true,
					getShareStringHandler(app));
		}
	}

	/**
	 * Show exit exam dialog
	 */
	protected void showExamExitDialog() {
		// set Firefox dom.allow_scripts_to_close_windows in about:config to
		// true to make this work
		String[] optionNames = { loc.getMenu("Cancel"), loc.getMenu("Exit") };
		getApp().getGuiManager().getOptionPane().showOptionDialog(getApp(),
				loc.getMenu("exam_exit_confirmation"), // ExitExamConfirm
				loc.getMenu("exam_exit_header"), // ExitExamConfirmTitle
				1, GOptionPane.WARNING_MESSAGE, null, optionNames,
				new AsyncOperation<String[]>() {
					@Override
					public void callback(String[] obj) {
						if ("1".equals(obj[0])) {
							exitAndResetExam();
						}
					}
				});
	}

	/**
	 * Show the custom share dialog
	 * 
	 * @param app
	 *            application
	 */
	protected static void showShareDialog(final AppW app) {
		Runnable shareCallback = new Runnable() {

			@Override
			public void run() {
				ShareDialogW sd = new ShareDialogW(app);
				sd.setVisible(true);
				sd.center();
			}
		};
		if (app.getActiveMaterial() == null
				|| "P".equals(app.getActiveMaterial().getVisibility())) {
			if (!app.getLoginOperation().isLoggedIn()) {
				// not saved, not logged in
				app.getLoginOperation().getView().add(new EventRenderable() {

					@Override
					public void renderEvent(BaseEvent event) {
						if (event instanceof LoginEvent
								&& ((LoginEvent) event).isSuccessful()) {
							showShareDialog(app);
						}
					}
				});
				((SignInButton) app.getLAF().getSignInButton(app)).login();
			} else {
				// not saved, logged in
				((DialogManagerW) app.getDialogManager()).getSaveDialog()
						.setDefaultVisibility(SaveDialogW.Visibility.Shared)
					.showIfNeeded(shareCallback, true);
			}
		} else {
			// saved
			shareCallback.run();
		}
	}

	/**
	 * Go to geogebra.org or close iframe if we are running in one
	 */
	protected native void backToGeoGebra() /*-{
		if ($wnd != $wnd.parent) {
			$wnd.parent.postMessage("{\"type\":\"closesingleton\"}",
					location.protocol + "//" + location.host);
		} else {
			$wnd.location.assign("/");
		}
	}-*/;

	/**
	 * 
	 * @param app
	 *            application
	 * @return handler for native sharing
	 */
	public static AsyncOperation<String> getShareStringHandler(final AppW app) {
		return new AsyncOperation<String>() {
			@Override
			public void callback(String s) {
				String title = app.getKernel().getConstruction().getTitle();
				MaterialsManagerI fm = app.getFileManager();
				fm.nativeShare(s, "".equals(title) ? "construction" : title);
			}
		};
	}

	/**
	 * @param online wether the application is online
	 * renders a the online - offline state of the FileMenu
	 */
	@Override
	public void render(boolean online) {
		shareItem.setEnabled(online);
	    if (!online) {
			shareItem.setTitle(loc.getMenu("Offline"));
		} else {
			shareItem.setTitle("");
		}
    }

}
