package geogebra.html5.main;

import geogebra.common.javax.swing.GOptionPane;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.main.App;
import geogebra.common.main.KeyCodes;
import geogebra.html5.gui.GuiManagerInterfaceW;
import geogebra.html5.javax.swing.GOptionPaneW;

import java.util.ArrayList;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;

/**
 * Handles keyboard events.
 */
public class GlobalKeyDispatcherW extends
        geogebra.common.main.GlobalKeyDispatcher implements KeyUpHandler, KeyDownHandler, KeyPressHandler {


	private static boolean controlDown = false;
	private static boolean shiftDown = false;

	public static boolean getControlDown() {
		return controlDown;
	}

	public static boolean getShiftDown() {
		return shiftDown;
	}

	public static void setDownKeys(KeyEvent ev) {
		controlDown = ev.isControlKeyDown();
		shiftDown = ev.isShiftKeyDown();
	}

	/**
	 * Used if we need tab working properly
	 */
	public boolean InFocus = true;
	
	/**
	 * @param app application
	 */
	public GlobalKeyDispatcherW(App app) {
		super(app);
    }

	@Override
	public void handleFunctionKeyForAlgebraInput(int i, GeoElement geo) {
		// TODO Auto-generated method stub

	}

	public void onKeyPress(KeyPressEvent event) {
		App.debug("Key pressed:"+event.getCharCode());
		setDownKeys(event);
		event.stopPropagation();
		if (InFocus ) {
			event.preventDefault();
		}
		
		//this needs to be done in onKeyPress -- keyUp is not case sensitive
		if(!event.isAltKeyDown() && !event.isControlKeyDown()){
			App.debug("Key pressed:"+event.getCharCode());
			this.renameStarted(event.getCharCode());
		}
	}

	public void onKeyUp(KeyUpEvent event) {
		setDownKeys(event);
		if (InFocus) {
			event.preventDefault();
		}
		event.stopPropagation();
		//now it is private, but can be public, also it is void, but can return boolean as in desktop, if needed
		dispatchEvent(event);
    }

	private void dispatchEvent(KeyUpEvent event) {
	    //we Must find out something here to identify the component that fired this, like class names for example,
		//id-s or data-param-attributes
		
		//we have keypress here only
		//do this only, if we really have focus
		App.debug(InFocus + "");
		if (InFocus) {
			handleKeyPressed(event);
		} else if (event.getNativeKeyCode() == com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER) {
			InFocus = true;
		}
	    
    }

	private boolean handleKeyPressed(KeyUpEvent event) {
		// GENERAL KEYS:
		// handle ESC, function keys, zooming with Ctrl +, Ctrl -, etc.
		if (handleGeneralKeys(event)) {
			return true;
		}

		// SELECTED GEOS:
		// handle function keys, arrow keys, +/- keys for selected geos, etc.
		//if (handleSelectedGeosKeys(event, app.getSelectionManager().getSelectedGeos())) {
		//	return true;
		//}

		return false;
    }

	/**
	 * Handles key event by disassembling it into primitive types and handling it using the mothod
	 * from common 
	 * @param event event
	 * @return whether event was consumed
	 */
	public boolean handleGeneralKeys(KeyUpEvent event) {
		
		return handleGeneralKeys(KeyCodes.translateGWTcode(event.getNativeKeyCode()), event.isShiftKeyDown(), event.isControlKeyDown(), event.isAltKeyDown(), false, true);

	}
	
	private boolean handleSelectedGeosKeys(KeyUpEvent event,
			ArrayList<GeoElement> geos) {
		
		return handleSelectedGeosKeys(KeyCodes.translateGWTcode(event.getNativeKeyCode()), geos, event.isShiftKeyDown(), event.isControlKeyDown(), event.isAltKeyDown(), false);
	}

	public boolean handleSelectedGeosKeysNative(NativeEvent event) {
		return handleSelectedGeosKeys(
			geogebra.common.main.KeyCodes.translateGWTcode(event.getKeyCode()),
			selection.getSelectedGeos(),
			event.getShiftKey(),
			event.getCtrlKey(),
			event.getAltKey(), false);
	}

	public void onKeyDown(KeyDownEvent event) {
		setDownKeys(event);
		//AbstractApplication.debug("onkeydown");
		
	    event.stopPropagation();

		// SELECTED GEOS:
		// handle function keys, arrow keys, +/- keys for selected geos, etc.
		boolean handled = handleSelectedGeosKeys(
			KeyCodes.translateGWTcode(
				event.getNativeKeyCode()),
			app.getSelectionManager().getSelectedGeos(),
			event.isShiftKeyDown(),
			event.isControlKeyDown(),
			event.isAltKeyDown(),
			false);
		//if not handled, do not consume so that keyPressed works
		if (InFocus && handled) {
			event.preventDefault();
		}
    }

	@Override
    protected boolean handleCtrlShiftN(boolean isAltDown) {
	    App.debug("unimplemented");
	    return false;
    }

	/**
	 * Does copy to OS clipboard, or if it's hard,
	 * put the data into a new browser tab at least
	 * @param str
	 */
	public static native void copyBase64(String str) /*-{
		if ($doc.isChromeWebapp()) {
			// solution copied from geogebra.web.gui.view.spreadsheet.CopyPasteCutW.copyToSystemClipboardChromeWebapp
			// although it's strange that .contentEditable is not set to true
			var copyFrom = @geogebra.web.gui.view.spreadsheet.CopyPasteCutW::getHiddenTextArea()();
			copyFrom.value = str;
			copyFrom.select();
			$doc.execCommand('copy');
		} else {
			var userAgent = $wnd.navigator.userAgent.toLowerCase();
			if ((userAgent.indexOf('msie') > -1) || (userAgent.indexOf('trident') > -1)) {
				// It is a good question what shall we do in Internet Explorer?
				// Security settings may block clipboard, new browser tabs, window.prompt, alert
				// Use a custom alert! but this does not seem to work either
				//this.@geogebra.html5.main.GlobalKeyDispatcherW::showConfirmDialog(Ljava/lang/String;)(str);

				// alternative, better than nothing, but not always working
				//if ($wnd.clipboardData) {
				//	$wnd.clipboardData.setData('Text', str);
				//}

				// then just do the same as in other cases, for now
				$wnd.prompt('Base64', str);
			} else {
				// otherwise, we should do the following:
				$wnd.prompt('Base64', str);
			}
		}
	}-*/;

	public void showConfirmDialog(String mess) {
		GOptionPaneW.INSTANCE.showConfirmDialog(app, mess, "Base64",
				GOptionPane.OK_CANCEL_OPTION,
				GOptionPane.PLAIN_MESSAGE, null);
	}

	@Override
    protected boolean handleEnter() {
		if (((AppW) app).isUsingFullGui()
				&& ((GuiManagerInterfaceW) app.getGuiManager()).noMenusOpen()) {
			if (app.showAlgebraInput()){
//					&& !((GuiManagerW) app.getGuiManager()).getAlgebraInput()
//							.hasFocus()) {

				((GuiManagerInterfaceW) app.getGuiManager()).getAlgebraInput()
						.requestFocus();

				return true;
			}
		}

		return false;
    }

	@Override
    protected void copyDefinitionsToInputBarAsList(ArrayList<GeoElement> geos) {
	    App.debug("unimplemented");
    }

	@Override
    protected void createNewWindow() {
		App.debug("unimplemented");
    }

	@Override
    protected void showPrintPreview(App app2) {
		App.debug("unimplemented");
    }
}
