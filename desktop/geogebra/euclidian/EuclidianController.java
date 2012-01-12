/* 
 GeoGebra - Dynamic Mathematics for Everyone
 http://www.geogebra.org

 This file is part of GeoGebra.

 This program is free software; you can redistribute it and/or modify it 
 under the terms of the GNU General Public License as published by 
 the Free Software Foundation.

 */

package geogebra.euclidian;

import geogebra.common.awt.Point;
import geogebra.common.awt.Point2D;
import geogebra.common.awt.Rectangle;
import geogebra.common.euclidian.DrawConic;
import geogebra.common.euclidian.DrawConicPart;
import geogebra.common.euclidian.AbstractEuclidianView;
import geogebra.common.euclidian.EuclidianConstants;
import geogebra.common.euclidian.EuclidianViewInterfaceCommon;
import geogebra.common.euclidian.Hits;
import geogebra.common.euclidian.Previewable;
import geogebra.common.euclidian.event.AbstractEvent;
import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.Macro;
import geogebra.common.kernel.algos.AlgoTranslate;
import geogebra.common.kernel.algos.AlgoVector;
import geogebra.common.kernel.arithmetic.MyDouble;
import geogebra.common.kernel.arithmetic.VectorValue;
import geogebra.common.kernel.geos.GeoBoolean;
import geogebra.common.kernel.geos.GeoButton;
import geogebra.common.kernel.geos.GeoConicPart;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoImage;
import geogebra.common.kernel.geos.GeoNumeric;
import geogebra.common.kernel.geos.GeoPoint2;
import geogebra.common.kernel.geos.GeoText;
import geogebra.common.kernel.geos.GeoVec3D;
import geogebra.common.kernel.geos.GeoVector;
import geogebra.common.kernel.geos.PointProperties;
import geogebra.common.kernel.geos.Test;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.main.AbstractApplication;
import geogebra.common.main.GeoElementSelectionListener;
import geogebra.main.Application;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import javax.swing.text.JTextComponent;

/**
 * EuclidianController.java
 * 
 * Created on 16. October 2001, 15:41
 */
public class EuclidianController extends geogebra.common.euclidian.AbstractEuclidianController implements MouseListener, MouseMotionListener,
		MouseWheelListener, ComponentListener, PropertiesPanelMiniListener {

	

	
	

	// protected GeoVec2D b;

	

	// protected GeoSegment movedGeoSegment;

	

	// protected MyPopupMenu popupMenu;

	

	// boolean polygonRigid = false;

	/***********************************************
	 * Creates new EuclidianController
	 **********************************************/
	public EuclidianController(Kernel kernel) {
		setKernel(kernel);
		setApplication(kernel.getApplication());

		// for tooltip manager
		DEFAULT_INITIAL_DELAY = ToolTipManager.sharedInstance()
				.getInitialDelay();

		tempNum = new MyDouble(kernel);
	}

	public void setApplication(AbstractApplication app) {
		this.app = app;
	}

	public Application getApplication() {
		return (Application) app;
	}

	protected void setView(EuclidianViewInterface view) {
		// void setView(EuclidianView view) {
		this.view = view;
	}


	public void setPen(EuclidianPen pen) {
		this.pen = pen;
	}

	public EuclidianPen getPen() {
		return (EuclidianPen) pen;
	}

	public void setMode(int newMode) {

		if ((newMode == EuclidianConstants.MODE_SPREADSHEET_ONEVARSTATS)
				|| (newMode == EuclidianConstants.MODE_SPREADSHEET_TWOVARSTATS)
				|| (newMode == EuclidianConstants.MODE_SPREADSHEET_MULTIVARSTATS)) {
			return;
		}

		endOfMode(mode);

		allowSelectionRectangleForTranslateByVector = true;

		if (AbstractEuclidianView.usesSelectionRectangleAsInput(newMode)
				&& (((EuclidianViewInterface) view).getSelectionRectangle() != null)) {
			initNewMode(newMode);
			if (((Application)app).getActiveEuclidianView() == view) {
				processSelectionRectangle(null);
			}
		} else if (AbstractEuclidianView.usesSelectionAsInput(newMode)) {
			initNewMode(newMode);
			if (((Application)app).getActiveEuclidianView() == view) {
				processSelection();
			}
		} else {
			if (!TEMPORARY_MODE) {
				((Application)app).clearSelectedGeos(false);
			}
			initNewMode(newMode);
		}

		kernel.notifyRepaint();
	}

	protected void initNewMode(int mode) {
		this.mode = mode;
		initShowMouseCoords();
		// Michael Borcherds 2007-10-12
		// clearSelections();
		if (!TEMPORARY_MODE
				&& !AbstractEuclidianView.usesSelectionRectangleAsInput(mode)) {
			clearSelections();
		}
		// Michael Borcherds 2007-10-12
		moveMode = MOVE_NONE;

		closeMiniPropertiesPanel();

		if (mode == EuclidianConstants.MODE_RECORD_TO_SPREADSHEET) {
			if (!((Application) app).getGuiManager().hasSpreadsheetView()) {
				((Application) app).getGuiManager().attachSpreadsheetView();
			}
			if (!((Application) app).getGuiManager().showView(
					AbstractApplication.VIEW_SPREADSHEET)) {
				((Application) app).getGuiManager().setShowView(true,
						AbstractApplication.VIEW_SPREADSHEET);
			}
		}

		((EuclidianViewInterface) view).setPreview(switchPreviewableForInitNewMode(mode));
		toggleModeChangedKernel = false;
	}

	protected Previewable switchPreviewableForInitNewMode(int mode) {

		Previewable previewDrawable = null;
		// init preview drawables
		switch (mode) {

		case EuclidianConstants.MODE_FREEHAND:
			pen.setFreehand(true);

			break;
		case EuclidianConstants.MODE_PEN:
			pen.setFreehand(false);

			/*
			 * boolean createUndo = true; // scale both EVs 1:1 if
			 * (app.getEuclidianView().isVisible()) {
			 * app.getEuclidianView().zoomAxesRatio(1, true); createUndo =
			 * false; }
			 * 
			 * if (app.hasEuclidianView2() &&
			 * app.getEuclidianView2().isVisible()) {
			 * app.getEuclidianView2().zoomAxesRatio(1, createUndo); }//
			 */

			ArrayList<GeoElement> selection = ((Application)app).getSelectedGeos();
			if (selection.size() == 1) {
				GeoElement geo = selection.get(0);
				// getCorner(1) == null as we can't write to transformed images
				if (geo.isGeoImage()) {
					GeoPoint2 c1 = ((GeoImage) geo).getCorner(0);
					GeoPoint2 c2 = ((GeoImage) geo).getCorner(1);
					GeoPoint2 c3 = ((GeoImage) geo).getCorner(2);

					if ((c3 == null)
							&& ((c2 == null // c2 = null -> not transformed
							)
							// or c1 and c2 are the correct spacing for the
							// image not to be transformed
							// (ie image was probably created by the Pen Tool)
							|| ((c1 != null) && (c2 != null)
									&& (c1.inhomY == c2.inhomY) && ((((EuclidianViewInterface) view)
									.toScreenCoordX(c2.inhomX) - ((EuclidianViewInterface) view)
									.toScreenCoordX(c1.inhomX)) == ((GeoImage) geo)
									.getFillImage().getWidth())))) {
						pen.setPenGeo((GeoImage) geo);
					} else {
						pen.setPenGeo(null);
					}

					pen.setPenWritingToExistingImage(pen.getPenGeo() != null);
				}
			}

			// no break;

		case EuclidianConstants.MODE_VISUAL_STYLE:

			// openMiniPropertiesPanel();

			break;

		case EuclidianConstants.MODE_PARALLEL:
			previewDrawable = ((EuclidianViewInterface) view).createPreviewParallelLine(selectedPoints,
					selectedLines);
			break;

		case EuclidianConstants.MODE_ANGULAR_BISECTOR:
			previewDrawable = ((EuclidianViewInterface) view).createPreviewAngleBisector(selectedPoints);
			break;

		case EuclidianConstants.MODE_ORTHOGONAL:
			previewDrawable = ((EuclidianViewInterface) view).createPreviewPerpendicularLine(
					selectedPoints, selectedLines);
			break;

		case EuclidianConstants.MODE_LINE_BISECTOR:
			previewDrawable = ((EuclidianViewInterface) view)
					.createPreviewPerpendicularBisector(selectedPoints);
			break;

		case EuclidianConstants.MODE_JOIN: // line through two points
			useLineEndPoint = false;
			previewDrawable = ((EuclidianViewInterface) view).createPreviewLine(selectedPoints);
			break;

		case EuclidianConstants.MODE_SEGMENT:
			useLineEndPoint = false;
			previewDrawable = ((EuclidianViewInterface) view).createPreviewSegment(selectedPoints);
			break;

		case EuclidianConstants.MODE_RAY:
			useLineEndPoint = false;
			previewDrawable = ((EuclidianViewInterface) view).createPreviewRay(selectedPoints);
			break;

		case EuclidianConstants.MODE_VECTOR:
			useLineEndPoint = false;
			previewDrawable = ((EuclidianViewInterface) view).createPreviewVector(selectedPoints);
			break;

		case EuclidianConstants.MODE_POLYGON:
		case EuclidianConstants.MODE_RIGID_POLYGON:
		case EuclidianConstants.MODE_VECTOR_POLYGON:
			previewDrawable = ((EuclidianViewInterface) view).createPreviewPolygon(selectedPoints);
			break;

		case EuclidianConstants.MODE_POLYLINE:
			previewDrawable = ((EuclidianViewInterface) view).createPreviewPolyLine(selectedPoints);
			break;

		case EuclidianConstants.MODE_CIRCLE_TWO_POINTS:
		case EuclidianConstants.MODE_CIRCLE_THREE_POINTS:
		case EuclidianConstants.MODE_ELLIPSE_THREE_POINTS:
		case EuclidianConstants.MODE_HYPERBOLA_THREE_POINTS:
			previewDrawable = ((EuclidianViewInterface) view).createPreviewConic(mode, selectedPoints);
			break;

		case EuclidianConstants.MODE_ANGLE:
			previewDrawable = ((EuclidianViewInterface) view).createPreviewAngle(selectedPoints);
			break;

		// preview for compass: radius first
		case EuclidianConstants.MODE_COMPASSES:
			previewDrawable = new DrawConic((AbstractEuclidianView) view, mode,
					selectedPoints, selectedSegments, selectedConicsND);
			break;

		// preview for arcs and sectors
		case EuclidianConstants.MODE_SEMICIRCLE:
		case EuclidianConstants.MODE_CIRCLE_ARC_THREE_POINTS:
		case EuclidianConstants.MODE_CIRCUMCIRCLE_ARC_THREE_POINTS:
		case EuclidianConstants.MODE_CIRCLE_SECTOR_THREE_POINTS:
		case EuclidianConstants.MODE_CIRCUMCIRCLE_SECTOR_THREE_POINTS:
			previewDrawable = new DrawConicPart((AbstractEuclidianView) view, mode,
					selectedPoints);
			break;

		case EuclidianConstants.MODE_TRANSLATE_BY_VECTOR:
			useLineEndPoint = false;
			previewDrawable = ((EuclidianViewInterface) view).createPreviewVector(selectedPoints);
			break;

		case EuclidianConstants.MODE_SHOW_HIDE_OBJECT:
			// select all hidden objects
			Iterator<GeoElement> it = kernel.getConstruction()
					.getGeoSetConstructionOrder().iterator();
			while (it.hasNext()) {
				GeoElement geo = it.next();
				// independent numbers should not be set visible
				// as this would produce a slider
				if (!geo.isSetEuclidianVisible()
						&& !((geo.isNumberValue() || geo.isBooleanValue()) && geo
								.isIndependent())) {
					geo.setEuclidianVisible(true);
					((Application)app).addSelectedGeo(geo);
					geo.updateRepaint();
				}
			}
			break;

		case EuclidianConstants.MODE_COPY_VISUAL_STYLE:
			movedGeoElement = null; // this will be the active geo template
			break;

		case EuclidianConstants.MODE_MOVE_ROTATE:
			rotationCenter = null; // this will be the active geo template
			break;

		case EuclidianConstants.MODE_RECORD_TO_SPREADSHEET:

			// G.Sturr 2010-5-14
			if (recordObject != null) {
				((Application) app).getGuiManager().removeSpreadsheetTrace(recordObject);
				// END G.Sturr
			}

			recordObject = null;

			break;

		default:
			previewDrawable = null;

			// macro mode?
			if (mode >= EuclidianConstants.MACRO_MODE_ID_OFFSET) {
				// get ID of macro
				int macroID = mode - EuclidianConstants.MACRO_MODE_ID_OFFSET;
				macro = kernel.getMacro(macroID);
				macroInput = macro.getInputTypes();
				this.mode = EuclidianConstants.MODE_MACRO;
			}
			break;
		}

		return previewDrawable;
	}

	public void mouseClicked(MouseEvent e) {
		AbstractEvent event = geogebra.euclidian.event.MouseEvent.wrapEvent(e);
		wrapMouseclicked(event);
	}

	protected void mousePressedTranslatedView(AbstractEvent e) {

		Hits hits;

		// check if axis is hit
		// hits = view.getHits(mouseLoc);
		((EuclidianViewInterface) view).setHits(mouseLoc);
		hits = ((EuclidianViewInterface) view).getHits();
		hits.removePolygons();
		// Application.debug("MODE_TRANSLATEVIEW - "+hits.toString());

		/*
		 * if (!hits.isEmpty() && hits.size() == 1) { Object hit0 = hits.get(0);
		 * if (hit0 == kernel.getXAxis()) moveMode = MOVE_X_AXIS; else if (hit0
		 * == kernel.getYAxis()) moveMode = MOVE_Y_AXIS; else moveMode =
		 * MOVE_VIEW; } else { moveMode = MOVE_VIEW; }
		 */

		moveMode = MOVE_VIEW;
		if (!hits.isEmpty()) {
			for (Object hit : hits) {
				if (hit == kernel.getXAxis()) {
					moveMode = MOVE_X_AXIS;
				}
				if (hit == kernel.getYAxis()) {
					moveMode = MOVE_Y_AXIS;
				}
			}
		}

		startLoc = mouseLoc;
		if (!TEMPORARY_MODE) {
			if (moveMode == MOVE_VIEW) {
				((EuclidianViewInterface) view).setMoveCursor();
			} else {
				((EuclidianViewInterface) view).setDragCursor();
			}
		}

		// xZeroOld = view.getXZero();
		// yZeroOld = view.getYZero();
		((EuclidianViewInterface) view).rememberOrigins();
		xTemp = xRW;
		yTemp = yRW;
		((EuclidianViewInterface) view).setShowAxesRatio((moveMode == MOVE_X_AXIS)
				|| (moveMode == MOVE_Y_AXIS));
		// view.setDrawMode(EuclidianConstants.DRAW_MODE_DIRECT_DRAW);

	}

	/**
	 * 
	 * @return true if a view button has been pressed (see 3D)
	 */
	protected boolean handleMousePressedForViewButtons() {
		return false;
	}
	
	protected void wrapMousePressed(AbstractEvent event) {
		
		if (app.isUsingFullGui()) {
			// determine parent panel to change focus
			// EuclidianDockPanelAbstract panel =
			// (EuclidianDockPanelAbstract)SwingUtilities.getAncestorOfClass(EuclidianDockPanelAbstract.class,
			// (Component)e.getSource());

			// if(panel != null) {
			// app.getGuiManager().getLayout().getDockManager().setFocusedPanel(panel);
			// }
			app.getGuiManager().setFocusedPanel(event);
		}

		setMouseLocation(event);

		if (handleMousePressedForViewButtons()) {
			return;
		}

		Hits hits;

		if ((mode == EuclidianConstants.MODE_PEN)
				|| (mode == EuclidianConstants.MODE_FREEHAND)) {
			view.setHits(mouseLoc);
			hits = view.getHits();
			hits.removeAllButImages();
			pen.handleMousePressedForPenMode(event, hits);
			return;
		}

		// GeoElement geo;
		transformCoords();

		moveModeSelectionHandled = false;
		DRAGGING_OCCURED = false;
		view.setSelectionRectangle(null);
		selectionStartPoint.setLocation(mouseLoc);

		if (hitResetIcon() || view.hitAnimationButton(event)) {
			// see mouseReleased
			return;
		}

		if (app.isRightClick(event)) {
			// ggb3D - for 3D rotation
			processRightPressFor3D();

			return;
		} else if (app.isShiftDragZoomEnabled() && (
		// MacOS: shift-cmd-drag is zoom
				(event.isShiftDown() && !app.isControlDown(event)) // All
																	// Platforms:
																	// Shift key
						|| (event.isControlDown() && Application.WINDOWS // old
																		// Windows
																		// key:
																		// Ctrl
																		// key
						) || app.isMiddleClick(event))) {
			// Michael Borcherds 2007-12-08 BEGIN
			// bugfix: couldn't select multiple objects with Ctrl

			view.setHits(mouseLoc);
			hits = view.getHits();
			switchModeForRemovePolygons(hits);
			if (!hits.isEmpty()) // bugfix 2008-02-19 removed this:&&
									// ((GeoElement) hits.get(0)).isGeoPoint())
			{
				DONT_CLEAR_SELECTION = true;
			}
			// Michael Borcherds 2007-12-08 END
			TEMPORARY_MODE = true;
			oldMode = mode; // remember current mode
			view.setMode(EuclidianConstants.MODE_TRANSLATEVIEW);
		}

		switchModeForMousePressed(event);
	}

	public void mousePressed(MouseEvent e) {
		AbstractEvent event = geogebra.euclidian.event.MouseEvent.wrapEvent(e);
		wrapMousePressed(event);
		/*if (((Application)app).isUsingFullGui()) {
			// determine parent panel to change focus
			// EuclidianDockPanelAbstract panel =
			// (EuclidianDockPanelAbstract)SwingUtilities.getAncestorOfClass(EuclidianDockPanelAbstract.class,
			// (Component)e.getSource());

			// if(panel != null) {
			// app.getGuiManager().getLayout().getDockManager().setFocusedPanel(panel);
			// }
			((Application) app).getGuiManager().setFocusedPanel(e);
		}

		setMouseLocation(geogebra.euclidian.event.MouseEvent.wrapEvent(e));

		if (handleMousePressedForViewButtons()) {
			return;
		}

		Hits hits;

		if ((mode == EuclidianConstants.MODE_PEN)
				|| (mode == EuclidianConstants.MODE_FREEHAND)) {
			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits();
			hits.removeAllButImages();
			((EuclidianPen) pen).handleMousePressedForPenMode(e, hits);
			return;
		}

		// GeoElement geo;
		transformCoords();

		moveModeSelectionHandled = false;
		DRAGGING_OCCURED = false;
		((EuclidianViewInterface) view).setSelectionRectangle(null);
		selectionStartPoint.setLocation(mouseLoc);

		if (hitResetIcon() || ((EuclidianViewInterface) view).hitAnimationButton(e)) {
			// see mouseReleased
			return;
		}

		if (Application.isRightClick(e)) {
			// ggb3D - for 3D rotation
			processRightPressFor3D();

			return;
		} else if (((Application) app).isShiftDragZoomEnabled() && (
		// MacOS: shift-cmd-drag is zoom
				(e.isShiftDown() && !Application.isControlDown(e)) // All
																	// Platforms:
																	// Shift key
						|| (e.isControlDown() && Application.WINDOWS // old
																		// Windows
																		// key:
																		// Ctrl
																		// key
						) || Application.isMiddleClick(e))) {
			// Michael Borcherds 2007-12-08 BEGIN
			// bugfix: couldn't select multiple objects with Ctrl

			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits();
			switchModeForRemovePolygons(hits);
			if (!hits.isEmpty()) // bugfix 2008-02-19 removed this:&&
									// ((GeoElement) hits.get(0)).isGeoPoint())
			{
				DONT_CLEAR_SELECTION = true;
			}
			// Michael Borcherds 2007-12-08 END
			TEMPORARY_MODE = true;
			oldMode = mode; // remember current mode
			view.setMode(EuclidianConstants.MODE_TRANSLATEVIEW);
		}

		switchModeForMousePressed(e);*/

	}

	protected void createNewPointForModePoint(Hits hits, boolean complex) {
		if ((mode == EuclidianConstants.MODE_POINT)
				|| (mode == EuclidianConstants.MODE_COMPLEX_NUMBER)) {// remove
																		// polygons
																		// :
																		// point
																		// inside
																		// a
																		// polygon
																		// is
																		// created
																		// free,
																		// as in
																		// v3.2
			AbstractApplication.debug("complex" + complex);
			hits.removeAllPolygons();
			hits.removeConicsHittedOnFilling();
			createNewPoint(hits, true, false, true, true, complex);
		} else {// if mode==EuclidianView.MODE_POINT_ON_OBJECT, point can be in
				// a region
			createNewPoint(hits, true, true, true, true, complex);
		}
	}

	protected void createNewPointForModeOther(Hits hits) {
		createNewPoint(hits, true, false, true, true, false);
	}

	protected void switchModeForMousePressed(AbstractEvent e) {

		Hits hits;

		switch (mode) {
		// create new point at mouse location
		// this point can be dragged: see mouseDragged() and mouseReleased()
		case EuclidianConstants.MODE_COMPLEX_NUMBER:
			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits();
			createNewPointForModePoint(hits, true);
			break;
		case EuclidianConstants.MODE_POINT:
		case EuclidianConstants.MODE_POINT_ON_OBJECT:
			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits();
			AbstractApplication.debug(hits);
			// if mode==EuclidianView.MODE_POINT_ON_OBJECT, point can be in a
			// region
			createNewPointForModePoint(hits, false);
			break;

		case EuclidianConstants.MODE_SEGMENT:
		case EuclidianConstants.MODE_SEGMENT_FIXED:
		case EuclidianConstants.MODE_JOIN:
		case EuclidianConstants.MODE_RAY:
		case EuclidianConstants.MODE_VECTOR:
		case EuclidianConstants.MODE_CIRCLE_TWO_POINTS:
		case EuclidianConstants.MODE_CIRCLE_POINT_RADIUS:
		case EuclidianConstants.MODE_CIRCLE_THREE_POINTS:
		case EuclidianConstants.MODE_ELLIPSE_THREE_POINTS:
		case EuclidianConstants.MODE_HYPERBOLA_THREE_POINTS:
		case EuclidianConstants.MODE_CIRCLE_ARC_THREE_POINTS:
		case EuclidianConstants.MODE_CIRCLE_SECTOR_THREE_POINTS:
		case EuclidianConstants.MODE_CIRCUMCIRCLE_ARC_THREE_POINTS:
		case EuclidianConstants.MODE_CIRCUMCIRCLE_SECTOR_THREE_POINTS:
		case EuclidianConstants.MODE_SEMICIRCLE:
		case EuclidianConstants.MODE_CONIC_FIVE_POINTS:
		case EuclidianConstants.MODE_POLYGON:
		case EuclidianConstants.MODE_POLYLINE:
		case EuclidianConstants.MODE_REGULAR_POLYGON:
			// hits = view.getHits(mouseLoc);
			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits();
			hits.removePolygons();
			createNewPointForModeOther(hits);
			break;

		case EuclidianConstants.MODE_VECTOR_POLYGON:
		case EuclidianConstants.MODE_RIGID_POLYGON:
			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits();
			hits.removePolygons();
			createNewPoint(hits, false, false, false, false, false);
			break;

		case EuclidianConstants.MODE_TRANSLATE_BY_VECTOR:
			if (!allowSelectionRectangleForTranslateByVector) {
				((EuclidianViewInterface) view).setHits(mouseLoc);
				hits = ((EuclidianViewInterface) view).getHits();
				hits.removePolygons();
				if (hits.size() == 0) {
					createNewPoint(hits, false, true, true);
				}
			}
			break;

		case EuclidianConstants.MODE_PARALLEL:
		case EuclidianConstants.MODE_PARABOLA: // Michael Borcherds 2008-04-08
		case EuclidianConstants.MODE_ORTHOGONAL:
		case EuclidianConstants.MODE_LINE_BISECTOR:
		case EuclidianConstants.MODE_ANGULAR_BISECTOR:
		case EuclidianConstants.MODE_TANGENTS:
		case EuclidianConstants.MODE_POLAR_DIAMETER:
			// hits = view.getHits(mouseLoc);
			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits();
			hits.removePolygons();
			if (hits.size() == 0) {
				createNewPoint(hits, false, true, true);
			}
			break;

		case EuclidianConstants.MODE_COMPASSES: // Michael Borcherds 2008-03-13
			// hits = view.getHits(mouseLoc);
			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits();
			hits.removePolygons();
			if (hits.isEmpty()) {
				createNewPoint(hits, false, true, true);
			}
			break;

		case EuclidianConstants.MODE_ANGLE:
			// hits = view.getTopHits(mouseLoc);
			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits().getTopHits();
			// check if we got a polygon
			if (hits.isEmpty()) {
				createNewPoint(hits, false, false, true);
			}
			break;

		case EuclidianConstants.MODE_ANGLE_FIXED:
		case EuclidianConstants.MODE_MIDPOINT:
			// hits = view.getHits(mouseLoc);
			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits();
			hits.removePolygons();
			if (hits.isEmpty()
					|| (!hits.get(0).isGeoSegment() && !hits.get(0)
							.isGeoConic())) {
				createNewPoint(hits, false, false, true);
			}
			break;

		case EuclidianConstants.MODE_MOVE_ROTATE:
			handleMousePressedForRotateMode();
			break;

		case EuclidianConstants.MODE_RECORD_TO_SPREADSHEET:
			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits();
			GeoElement tracegeo = hits.getFirstHit(Test.GEOPOINTND);
			if (tracegeo == null) {
				tracegeo = hits.getFirstHit(Test.GEOVECTOR);
			}
			if (tracegeo == null) {
				tracegeo = hits.getFirstHit(Test.GEONUMERIC);
			}
			if (tracegeo == null) {
				tracegeo = hits.getFirstHit(Test.GEOLIST);
			}
			if (tracegeo != null) {
				if (recordObject == null) {
					if (!((Application) app).getTraceManager().isTraceGeo(tracegeo)) {
						((Application) app).getGuiManager().addSpreadsheetTrace(tracegeo);
					}
					recordObject = tracegeo;
				}
				handleMousePressedForMoveMode(e, false);
				tracegeo.updateRepaint();
			}
			break;

		// move an object
		case EuclidianConstants.MODE_MOVE:
		case EuclidianConstants.MODE_VISUAL_STYLE:
			handleMousePressedForMoveMode(e, false);
			break;

		// move drawing pad or axis
		case EuclidianConstants.MODE_TRANSLATEVIEW:

			mousePressedTranslatedView(e);

			break;

		default:
			moveMode = MOVE_NONE;
		}
	}

	protected void handleMousePressedForRotateMode() {
		GeoElement geo;
		Hits hits;

		// we need the center of the rotation
		if (rotationCenter == null) {
			((EuclidianViewInterface) view).setHits(mouseLoc);
			rotationCenter = (GeoPoint2) chooseGeo(
					((EuclidianViewInterface) view).getHits().getHits(Test.GEOPOINT2, tempArrayList),
					true);
			((Application)app).addSelectedGeo(rotationCenter);
			moveMode = MOVE_NONE;
		} else {
			((EuclidianViewInterface) view).setHits(mouseLoc);
			hits = ((EuclidianViewInterface) view).getHits();
			hits.removePolygons();
			// hits = view.getHits(mouseLoc);
			// got rotation center again: deselect
			if (!hits.isEmpty() && hits.contains(rotationCenter)) {
				((Application)app).removeSelectedGeo(rotationCenter);
				rotationCenter = null;
				moveMode = MOVE_NONE;
				return;
			}

			moveModeSelectionHandled = true;

			// find and set rotGeoElement
			hits = hits.getPointRotateableHits(view, rotationCenter);
			if (!hits.isEmpty() && hits.contains(rotGeoElement)) {
				geo = rotGeoElement;
			} else {
				geo = chooseGeo(hits, true);
				((Application)app).addSelectedGeo(geo);
			}
			rotGeoElement = geo;

			if (geo != null) {
				doSingleHighlighting(rotGeoElement);
				// rotGeoElement.setHighlighted(true);

				// init values needed for rotation
				rotStartGeo = rotGeoElement.copy();
				rotStartAngle = Math.atan2(yRW - rotationCenter.inhomY, xRW
						- rotationCenter.inhomX);
				moveMode = MOVE_ROTATE;
			} else {
				moveMode = MOVE_NONE;
			}
		}
	}

	protected void handleMousePressedForMoveMode(AbstractEvent e, boolean drag) {

		// long t0 = System.currentTimeMillis();

		// Application.debug("start");

		// view.resetTraceRow(); // for trace/spreadsheet

		// fix for meta-click to work on Mac/Linux
		if (app.isControlDown(e)) {
			return;
		}

		// move label?
		GeoElement geo = view.getLabelHit(mouseLoc);
		// Application.debug("label("+(System.currentTimeMillis()-t0)+")");
		if (geo != null) {
			moveMode = MOVE_LABEL;
			movedLabelGeoElement = geo;
			oldLoc.setLocation(geo.labelOffsetX, geo.labelOffsetY);
			startLoc = mouseLoc;
			view.setDragCursor();
			return;
		}

		// Application.debug("laps("+(System.currentTimeMillis()-t0)+")");

		// find and set movedGeoElement
		view.setHits(mouseLoc);

		// make sure that eg slider takes precedence over a polygon (in the same
		// layer)
		view.getHits().removePolygons();

		Hits moveableList;

		// if we just click (no drag) on eg an intersection, we want it selected
		// not a popup with just the lines in

		// now we want this behaviour always as
		// * there is no popup
		// * user might do eg click then arrow keys
		// * want drag with left button to work (eg tessellation)

		// consider intersection of 2 circles.
		// On drag, we want to be able to drag a circle
		// on click, we want to be able to select the intersection point
		if (drag) {
			moveableList = view.getHits().getMoveableHits(view);
		} else {
			moveableList = view.getHits();
		}

		Hits hits = moveableList.getTopHits();

		// Application.debug("end("+(System.currentTimeMillis()-t0)+")");

		ArrayList<GeoElement> selGeos = app.getSelectedGeos();

		// if object was chosen before, take it now!
		if ((selGeos.size() == 1) && !hits.isEmpty()
				&& hits.contains(selGeos.get(0))) {
			// object was chosen before: take it
			geo = selGeos.get(0);
		} else {
			// choose out of hits
			geo = chooseGeo(hits, false);

			if (!selGeos.contains(geo)) {
				app.clearSelectedGeos();
				app.addSelectedGeo(geo);
				// app.geoElementSelected(geo, false); // copy definiton to
				// input bar
			}
		}

		if ((geo != null) && !geo.isFixed()) {
			moveModeSelectionHandled = true;
		} else {
			// no geo clicked at
			moveMode = MOVE_NONE;
			resetMovedGeoPoint();
			return;
		}

		handleMovedElement(geo, selGeos.size() > 1);

		view.repaintView();
	}

	private EuclidianViewInterfaceCommon setShowMouseCoords(boolean b) {
		// TODO Auto-generated method stub
		return null;
	}

	protected boolean viewHasHitsForMouseDragged() {
		return !(((EuclidianViewInterface) view).getHits().isEmpty());
	}
	
	protected void wrapMouseDragged(AbstractEvent event) {
		sliderValue = null;

		if (textfieldHasFocus) {
			return;
		}

		if ((mode == EuclidianConstants.MODE_PEN)
				|| (mode == EuclidianConstants.MODE_FREEHAND)) {
			pen.handleMousePressedForPenMode(event, null);
			return;
		}

		clearJustCreatedGeos();

		if (!DRAGGING_OCCURED) {

			DRAGGING_OCCURED = true;

			if ((mode == EuclidianConstants.MODE_TRANSLATE_BY_VECTOR)
					&& (selGeos() == 0)) {
				view.setHits(mouseLoc);

				Hits hits = view.getHits().getTopHits();

				GeoElement topHit = hits.get(0);

				if (topHit.isGeoVector()) {

					if ((topHit.getParentAlgorithm() instanceof AlgoVector)) { // Vector[A,B]
						AlgoVector algo = (AlgoVector) topHit
								.getParentAlgorithm();
						GeoPoint2 p = algo.getInputPoints().get(0);
						GeoPoint2 q = algo.getInputPoints().get(1);
						GeoVector vec = kernel.Vector(null, 0, 0);
						vec.setEuclidianVisible(false);
						vec.setAuxiliaryObject(true);
						GeoElement[] pp = kernel.Translate(null, p, vec);
						GeoElement[] qq = kernel.Translate(null, q, vec);
						AlgoVector newVecAlgo = new AlgoVector(kernel.getConstruction(), null,
								(GeoPointND) pp[0], (GeoPointND) qq[0]);
						transformCoordsOffset[0] = xRW;
						transformCoordsOffset[1] = yRW;

						// make sure vector looks the same when translated
						pp[0].setEuclidianVisible(p.isEuclidianVisible());
						qq[0].update();
						qq[0].setEuclidianVisible(q.isEuclidianVisible());
						qq[0].update();
						newVecAlgo.getGeoElements()[0].setVisualStyleForTransformations(topHit);

						app.setMode(EuclidianConstants.MODE_MOVE);
						movedGeoVector = vec;
						moveMode = MOVE_VECTOR_NO_GRID;
						return;
					} else {// if (topHit.isIndependent()) {
						movedGeoPoint = new GeoPoint2(kernel.getConstruction(),
								null, 0, 0, 0);
						AlgoTranslate algoTP = new AlgoTranslate(
								kernel.getConstruction(), null,
								(GeoElement) movedGeoPoint, (GeoVec3D) topHit);
						GeoPoint2 p = (GeoPoint2) algoTP.getGeoElements()[0];

						AlgoVector newVecAlgo = new AlgoVector(kernel.getConstruction(), null,
								movedGeoPoint, p);
						
						// make sure vector looks the same when translated
						((GeoPoint2) movedGeoPoint).setEuclidianVisible(false);
						((GeoPoint2) movedGeoPoint).update();
						p.setEuclidianVisible(false);
						p.update();
						newVecAlgo.getGeoElements()[0].setVisualStyleForTransformations(topHit);
						
						moveMode = MOVE_POINT;
					}
				}

				if (topHit.isTranslateable() || topHit.isGeoPolygon()) {
					GeoVector vec;
					if (topHit.isGeoPolygon()) {
						// for polygons, we need a labelled vector so that all
						// the vertices move together
						vec = kernel.Vector(null, 0, 0);
						vec.setEuclidianVisible(false);
						vec.setAuxiliaryObject(true);
					} else {
						vec = kernel.Vector(0, 0);
					}
					kernel.Translate(null, hits.get(0), vec);
					transformCoordsOffset[0] = xRW;
					transformCoordsOffset[1] = yRW;

					app.setMode(EuclidianConstants.MODE_MOVE);
					movedGeoVector = vec;
					moveMode = MOVE_VECTOR_NO_GRID;
					return;
				}
			}

			// Michael Borcherds 2007-10-07 allow right mouse button to drag
			// points
			// mathieu : also if it's mode point, we can drag the point
			if (app.isRightClick(event)
					|| (mode == EuclidianConstants.MODE_POINT)
					|| (mode == EuclidianConstants.MODE_COMPLEX_NUMBER)
					|| (mode == EuclidianConstants.MODE_POINT_ON_OBJECT)
					|| (mode == EuclidianConstants.MODE_SLIDER)
					|| (mode == EuclidianConstants.MODE_BUTTON_ACTION)
					|| (mode == EuclidianConstants.MODE_TEXTFIELD_ACTION)
					|| (mode == EuclidianConstants.MODE_SHOW_HIDE_CHECKBOX)
					|| (mode == EuclidianConstants.MODE_TEXT)) {
				view.setHits(mouseLoc);

				// make sure slider tool drags only sliders, not other object
				// types
				if (mode == EuclidianConstants.MODE_SLIDER) {
					if (view.getHits().size() != 1) {
						return;
					}

					if (!(view.getHits().get(0) instanceof GeoNumeric)) {
						return;
					}
				} else if ((mode == EuclidianConstants.MODE_BUTTON_ACTION)
						|| (mode == EuclidianConstants.MODE_TEXTFIELD_ACTION)) {
					if (view.getHits().size() != 1) {
						return;
					}

					if (!(view.getHits().get(0) instanceof GeoButton)) {
						return;
					}
				} else if (mode == EuclidianConstants.MODE_SHOW_HIDE_CHECKBOX) {
					if (view.getHits().size() != 1) {
						return;
					}

					if (!(view.getHits().get(0) instanceof GeoBoolean)) {
						return;
					}
				} else if (mode == EuclidianConstants.MODE_TEXT) {
					if (view.getHits().size() != 1) {
						return;
					}

					if (!(view.getHits().get(0) instanceof GeoText)) {
						return;
					}
				}

				if (viewHasHitsForMouseDragged()) {
					TEMPORARY_MODE = true;
					oldMode = mode; // remember current mode
					view.setMode(EuclidianConstants.MODE_MOVE);
					handleMousePressedForMoveMode(event, true);

					// make sure that dragging doesn't deselect the geos
					DONT_CLEAR_SELECTION = true;

					return;
				}

			}
			if (!app.isRightClickEnabled()) {
				return;
				// Michael Borcherds 2007-10-07
			}

			if (mode == EuclidianConstants.MODE_MOVE_ROTATE) {
				app.clearSelectedGeos(false);
				app.addSelectedGeo(rotationCenter, false);
			}
		}
		lastMouseLoc = mouseLoc;
		setMouseLocation(event);
		transformCoords();

		// ggb3D - only for 3D view
		if (moveMode == MOVE_ROTATE_VIEW) {
			if (processRotate3DView()) {
				return;
			}
		}

		if (app.isRightClick(event)) {
			// if there's no hit, or if first hit is not moveable, do 3D view
			// rotation
			if ((!TEMPORARY_MODE)
					|| (view.getHits().size() == 0)
					|| !view.getHits().get(0).isMoveable(view)
					|| (!view.getHits().get(0).isGeoPoint() &&  view.getHits()
							.get(0).hasDrawable3D())) {
				if (processRotate3DView()) { // in 2D view, return false
					if (TEMPORARY_MODE) {
						TEMPORARY_MODE = false;
						mode = oldMode;
						view.setMode(mode);
					}
					return;
				}
			}
		}

		// dragging eg a fixed point shouldn't start the selection rectangle
		if (view.getHits().isEmpty()) {
			// zoom rectangle (right drag) or selection rectangle (left drag)
			// Michael Borcherds 2007-10-07 allow dragging with right mouse
			// button
			if (((app.isRightClick(event)) || allowSelectionRectangle())
					&& !TEMPORARY_MODE) {
				// Michael Borcherds 2007-10-07
				// set zoom rectangle's size
				// right-drag: zoom
				// Shift-right-drag: zoom without preserving aspect ratio
				updateSelectionRectangle((app.isRightClick(event) && !event
						.isShiftDown())
				// MACOS:
				// Cmd-left-drag: zoom
				// Cmd-shift-left-drag: zoom without preserving aspect ratio
						|| (Application.MAC_OS && app.isControlDown(event)
								&& !event.isShiftDown() && !app
									.isRightClick(event)));
				view.repaintView();
				return;
			}
		}

		// update previewable
		if (view.getPreviewDrawable() != null) {
			view.getPreviewDrawable().updateMousePos(
					view.toRealWorldCoordX(mouseLoc.x),
					view.toRealWorldCoordY(mouseLoc.y));
		}

		/*
		 * Conintuity handling
		 * 
		 * If the mouse is moved wildly we take intermediate steps to get a more
		 * continous behaviour
		 */
		if (kernel.isContinuous() && (lastMouseLoc != null)) {
			double dx = mouseLoc.x - lastMouseLoc.x;
			double dy = mouseLoc.y - lastMouseLoc.y;
			double distsq = (dx * dx) + (dy * dy);
			if (distsq > MOUSE_DRAG_MAX_DIST_SQUARE) {
				double factor = Math.sqrt(MOUSE_DRAG_MAX_DIST_SQUARE / distsq);
				dx *= factor;
				dy *= factor;

				// number of continuity steps <= MAX_CONTINUITY_STEPS
				int steps = Math
						.min((int) (1.0 / factor), MAX_CONTINUITY_STEPS);
				int mx = mouseLoc.x;
				int my = mouseLoc.y;

				// Application.debug("BIG drag dist: " + Math.sqrt(distsq) +
				// ", steps: " + steps );
				for (int i = 1; i <= steps; i++) {
					mouseLoc.x = (int) Math.round(lastMouseLoc.x + (i * dx));
					mouseLoc.y = (int) Math.round(lastMouseLoc.y + (i * dy));
					calcRWcoords();

					handleMouseDragged(false);
				}

				// set endpoint of mouse movement if we are not already there
				if ((mouseLoc.x != mx) || (mouseLoc.y != my)) {
					mouseLoc.x = mx;
					mouseLoc.y = my;
					calcRWcoords();
				}
			}
		}

		if (pastePreviewSelected != null) {
			if (!pastePreviewSelected.isEmpty()) {
				updatePastePreviewPosition();
			}
		}

		handleMouseDragged(true);
	}

	public void mouseDragged(MouseEvent e) {
		AbstractEvent event = geogebra.euclidian.event.MouseEvent.wrapEvent(e);
		wrapMouseDragged(event);
		/*sliderValue = null;

		if (textfieldHasFocus) {
			return;
		}

		if ((mode == EuclidianConstants.MODE_PEN)
				|| (mode == EuclidianConstants.MODE_FREEHAND)) {
			((EuclidianPen) pen).handleMousePressedForPenMode(e, null);
			return;
		}

		clearJustCreatedGeos();

		if (!DRAGGING_OCCURED) {

			DRAGGING_OCCURED = true;

			if ((mode == EuclidianConstants.MODE_TRANSLATE_BY_VECTOR)
					&& (selGeos() == 0)) {
				((EuclidianViewInterface) view).setHits(mouseLoc);

				Hits hits = ((EuclidianViewInterface) view).getHits().getTopHits();

				GeoElement topHit = hits.get(0);

				if (topHit.isGeoVector()) {

					if ((topHit.getParentAlgorithm() instanceof AlgoVector)) { // Vector[A,B]
						AlgoVector algo = (AlgoVector) topHit
								.getParentAlgorithm();
						GeoPoint2 p = algo.getInputPoints().get(0);
						GeoPoint2 q = algo.getInputPoints().get(1);
						GeoVector vec = kernel.Vector(null, 0, 0);
						vec.setEuclidianVisible(false);
						vec.setAuxiliaryObject(true);
						GeoElement[] pp = kernel.Translate(null, p, vec);
						GeoElement[] qq = kernel.Translate(null, q, vec);
						AlgoVector newVecAlgo = new AlgoVector(kernel.getConstruction(), null,
								(GeoPointND) pp[0], (GeoPointND) qq[0]);
						transformCoordsOffset[0] = xRW;
						transformCoordsOffset[1] = yRW;

						// make sure vector looks the same when translated
						pp[0].setEuclidianVisible(p.isEuclidianVisible());
						qq[0].update();
						qq[0].setEuclidianVisible(q.isEuclidianVisible());
						qq[0].update();
						newVecAlgo.getGeoElements()[0].setVisualStyleForTransformations(topHit);

						((Application)app).setMode(EuclidianConstants.MODE_MOVE);
						movedGeoVector = vec;
						moveMode = MOVE_VECTOR_NO_GRID;
						return;
					} else {// if (topHit.isIndependent()) {
						movedGeoPoint = new GeoPoint2(kernel.getConstruction(),
								null, 0, 0, 0);
						AlgoTranslate algoTP = new AlgoTranslate(
								kernel.getConstruction(), null,
								(GeoElement) movedGeoPoint, (GeoVec3D) topHit);
						GeoPoint2 p = (GeoPoint2) algoTP.getGeoElements()[0];

						AlgoVector newVecAlgo = new AlgoVector(kernel.getConstruction(), null,
								movedGeoPoint, p);
						
						// make sure vector looks the same when translated
						((GeoPoint2) movedGeoPoint).setEuclidianVisible(false);
						((GeoPoint2) movedGeoPoint).update();
						p.setEuclidianVisible(false);
						p.update();
						newVecAlgo.getGeoElements()[0].setVisualStyleForTransformations(topHit);
						
						moveMode = MOVE_POINT;
					}
				}

				if (topHit.isTranslateable() || topHit.isGeoPolygon()) {
					GeoVector vec;
					if (topHit.isGeoPolygon()) {
						// for polygons, we need a labelled vector so that all
						// the vertices move together
						vec = kernel.Vector(null, 0, 0);
						vec.setEuclidianVisible(false);
						vec.setAuxiliaryObject(true);
					} else {
						vec = kernel.Vector(0, 0);
					}
					kernel.Translate(null, hits.get(0), vec);
					transformCoordsOffset[0] = xRW;
					transformCoordsOffset[1] = yRW;

					((Application)app).setMode(EuclidianConstants.MODE_MOVE);
					movedGeoVector = vec;
					moveMode = MOVE_VECTOR_NO_GRID;
					return;
				}
			}

			// Michael Borcherds 2007-10-07 allow right mouse button to drag
			// points
			// mathieu : also if it's mode point, we can drag the point
			if (Application.isRightClick(e)
					|| (mode == EuclidianConstants.MODE_POINT)
					|| (mode == EuclidianConstants.MODE_COMPLEX_NUMBER)
					|| (mode == EuclidianConstants.MODE_POINT_ON_OBJECT)
					|| (mode == EuclidianConstants.MODE_SLIDER)
					|| (mode == EuclidianConstants.MODE_BUTTON_ACTION)
					|| (mode == EuclidianConstants.MODE_TEXTFIELD_ACTION)
					|| (mode == EuclidianConstants.MODE_SHOW_HIDE_CHECKBOX)
					|| (mode == EuclidianConstants.MODE_TEXT)) {
				((EuclidianViewInterface) view).setHits(mouseLoc);

				// make sure slider tool drags only sliders, not other object
				// types
				if (mode == EuclidianConstants.MODE_SLIDER) {
					if (((EuclidianViewInterface) view).getHits().size() != 1) {
						return;
					}

					if (!(((EuclidianViewInterface) view).getHits().get(0) instanceof GeoNumeric)) {
						return;
					}
				} else if ((mode == EuclidianConstants.MODE_BUTTON_ACTION)
						|| (mode == EuclidianConstants.MODE_TEXTFIELD_ACTION)) {
					if (((EuclidianViewInterface) view).getHits().size() != 1) {
						return;
					}

					if (!(((EuclidianViewInterface) view).getHits().get(0) instanceof GeoButton)) {
						return;
					}
				} else if (mode == EuclidianConstants.MODE_SHOW_HIDE_CHECKBOX) {
					if (((EuclidianViewInterface) view).getHits().size() != 1) {
						return;
					}

					if (!(((EuclidianViewInterface) view).getHits().get(0) instanceof GeoBoolean)) {
						return;
					}
				} else if (mode == EuclidianConstants.MODE_TEXT) {
					if (((EuclidianViewInterface) view).getHits().size() != 1) {
						return;
					}

					if (!(((EuclidianViewInterface) view).getHits().get(0) instanceof GeoText)) {
						return;
					}
				}

				if (viewHasHitsForMouseDragged()) {
					TEMPORARY_MODE = true;
					oldMode = mode; // remember current mode
					view.setMode(EuclidianConstants.MODE_MOVE);
					handleMousePressedForMoveMode(e, true);

					// make sure that dragging doesn't deselect the geos
					DONT_CLEAR_SELECTION = true;

					return;
				}

			}
			if (!((Application) app).isRightClickEnabled()) {
				return;
				// Michael Borcherds 2007-10-07
			}

			if (mode == EuclidianConstants.MODE_MOVE_ROTATE) {
				((Application)app).clearSelectedGeos(false);
				((Application)app).addSelectedGeo(rotationCenter, false);
			}
		}
		lastMouseLoc = mouseLoc;
		setMouseLocation(geogebra.euclidian.event.MouseEvent.wrapEvent(e));
		transformCoords();

		// ggb3D - only for 3D view
		if (moveMode == MOVE_ROTATE_VIEW) {
			if (processRotate3DView()) {
				return;
			}
		}

		if (Application.isRightClick(e)) {
			// if there's no hit, or if first hit is not moveable, do 3D view
			// rotation
			if ((!TEMPORARY_MODE)
					|| (((EuclidianViewInterface) view).getHits().size() == 0)
					|| !((EuclidianViewInterface) view).getHits().get(0).isMoveable(view)
					|| (!((EuclidianViewInterface) view).getHits().get(0).isGeoPoint() && ((EuclidianViewInterface) view).getHits()
							.get(0).hasDrawable3D())) {
				if (processRotate3DView()) { // in 2D view, return false
					if (TEMPORARY_MODE) {
						TEMPORARY_MODE = false;
						mode = oldMode;
						view.setMode(mode);
					}
					return;
				}
			}
		}

		// dragging eg a fixed point shouldn't start the selection rectangle
		if (((EuclidianViewInterface) view).getHits().isEmpty()) {
			// zoom rectangle (right drag) or selection rectangle (left drag)
			// Michael Borcherds 2007-10-07 allow dragging with right mouse
			// button
			if (((Application.isRightClick(e)) || allowSelectionRectangle())
					&& !TEMPORARY_MODE) {
				// Michael Borcherds 2007-10-07
				// set zoom rectangle's size
				// right-drag: zoom
				// Shift-right-drag: zoom without preserving aspect ratio
				updateSelectionRectangle((Application.isRightClick(e) && !e
						.isShiftDown())
				// MACOS:
				// Cmd-left-drag: zoom
				// Cmd-shift-left-drag: zoom without preserving aspect ratio
						|| (Application.MAC_OS && Application.isControlDown(e)
								&& !e.isShiftDown() && !Application
									.isRightClick(e)));
				view.repaintView();
				return;
			}
		}

		// update previewable
		if (((EuclidianViewInterface) view).getPreviewDrawable() != null) {
			((EuclidianViewInterface) view).getPreviewDrawable().updateMousePos(
					view.toRealWorldCoordX(mouseLoc.x),
					view.toRealWorldCoordY(mouseLoc.y));
		}

		/*
		 * Conintuity handling
		 * 
		 * If the mouse is moved wildly we take intermediate steps to get a more
		 * continous behaviour
		 */
		/*
		if (kernel.isContinuous() && (lastMouseLoc != null)) {
			double dx = mouseLoc.x - lastMouseLoc.x;
			double dy = mouseLoc.y - lastMouseLoc.y;
			double distsq = (dx * dx) + (dy * dy);
			if (distsq > MOUSE_DRAG_MAX_DIST_SQUARE) {
				double factor = Math.sqrt(MOUSE_DRAG_MAX_DIST_SQUARE / distsq);
				dx *= factor;
				dy *= factor;

				// number of continuity steps <= MAX_CONTINUITY_STEPS
				int steps = Math
						.min((int) (1.0 / factor), MAX_CONTINUITY_STEPS);
				int mx = mouseLoc.x;
				int my = mouseLoc.y;

				// Application.debug("BIG drag dist: " + Math.sqrt(distsq) +
				// ", steps: " + steps );
				for (int i = 1; i <= steps; i++) {
					mouseLoc.x = (int) Math.round(lastMouseLoc.x + (i * dx));
					mouseLoc.y = (int) Math.round(lastMouseLoc.y + (i * dy));
					calcRWcoords();

					handleMouseDragged(false);
				}

				// set endpoint of mouse movement if we are not already there
				if ((mouseLoc.x != mx) || (mouseLoc.y != my)) {
					mouseLoc.x = mx;
					mouseLoc.y = my;
					calcRWcoords();
				}
			}
		}

		if (pastePreviewSelected != null) {
			if (!pastePreviewSelected.isEmpty()) {
				updatePastePreviewPosition();
			}
		}

		handleMouseDragged(true);*/
	}

	protected boolean allowSelectionRectangle() {
		switch (mode) {
		// move objects
		case EuclidianConstants.MODE_MOVE:
			return moveMode == MOVE_NONE;

			// move rotate objects
		case EuclidianConstants.MODE_MOVE_ROTATE:
			return selPoints() > 0; // need rotation center

			// object selection mode
		case EuclidianConstants.MODE_SELECTION_LISTENER:
			GeoElementSelectionListener sel = ((Application) app).getCurrentSelectionListener();
			if (sel == null) {
				return false;
			}
			if (((Application)app).isUsingFullGui()) {
				return !((Application) app).getGuiManager().isInputFieldSelectionListener();
			} else {
				return sel != null;
			}

			// transformations
		case EuclidianConstants.MODE_TRANSLATE_BY_VECTOR:
			return allowSelectionRectangleForTranslateByVector;

		case EuclidianConstants.MODE_DILATE_FROM_POINT:
		case EuclidianConstants.MODE_MIRROR_AT_POINT:
		case EuclidianConstants.MODE_MIRROR_AT_LINE:
		case EuclidianConstants.MODE_MIRROR_AT_CIRCLE: // Michael Borcherds
														// 2008-03-23
		case EuclidianConstants.MODE_ROTATE_BY_ANGLE:
		case EuclidianConstants.MODE_FITLINE:
		case EuclidianConstants.MODE_CREATE_LIST:
		case EuclidianConstants.MODE_VISUAL_STYLE:
		case EuclidianConstants.MODE_COPY_VISUAL_STYLE:
			return true;

			// checkbox, button
		case EuclidianConstants.MODE_SHOW_HIDE_CHECKBOX:
		case EuclidianConstants.MODE_BUTTON_ACTION:
		case EuclidianConstants.MODE_TEXTFIELD_ACTION:
			return true;

		default:
			return false;
		}
	}

	// square of maximum allowed pixel distance
	// for continuous mouse movements
	protected static double MOUSE_DRAG_MAX_DIST_SQUARE = 36;
	protected static int MAX_CONTINUITY_STEPS = 4;

	protected void handleMouseDragged(boolean repaint) {
		// moveMode was set in mousePressed()
		switch (moveMode) {
		case MOVE_ROTATE:
			rotateObject(repaint);
			break;

		case MOVE_POINT:
			movePoint(repaint);
			break;

		case MOVE_POINT_WITH_OFFSET:
			movePointWithOffset(repaint);
			break;

		case MOVE_LINE:
			moveLine(repaint);
			break;

		case MOVE_VECTOR:
		case MOVE_VECTOR_NO_GRID:
			moveVector(repaint);
			break;

		case MOVE_VECTOR_STARTPOINT:
			moveVectorStartPoint(repaint);
			break;

		case MOVE_CONIC:
			moveConic(repaint);
			break;

		case MOVE_IMPLICITPOLY:
			moveImplicitPoly(repaint);
			break;

		case MOVE_FUNCTION:
			moveFunction(repaint);
			break;

		case MOVE_LABEL:
			moveLabel();
			break;

		case MOVE_TEXT:
			moveText(repaint);
			break;

		case MOVE_IMAGE:
			moveImage(repaint);
			break;

		case MOVE_NUMERIC:
			// view.incrementTraceRow(); // for spreadsheet/trace

			moveNumeric(repaint);
			break;

		case MOVE_SLIDER:
			moveSlider(repaint);
			break;

		case MOVE_BOOLEAN:
			moveBoolean(repaint);
			break;

		case MOVE_BUTTON:
			moveButton(repaint);
			break;

		case MOVE_DEPENDENT:
			moveDependent(repaint);
			break;

		case MOVE_MULTIPLE_OBJECTS:
			moveMultipleObjects(repaint);
			break;

		case MOVE_VIEW:
			if (repaint) {
				if (TEMPORARY_MODE) {
					((EuclidianViewInterface) view).setMoveCursor();
				}
				/*
				 * view.setCoordSystem(xZeroOld + mouseLoc.x - startLoc.x,
				 * yZeroOld + mouseLoc.y - startLoc.y, view.getXscale(),
				 * view.getYscale());
				 */
				((EuclidianViewInterface) view).setCoordSystemFromMouseMove(mouseLoc.x - startLoc.x,
						mouseLoc.y - startLoc.y, MOVE_VIEW);
			}
			break;

		case MOVE_X_AXIS:
			if (repaint) {
				if (TEMPORARY_MODE) {
					((EuclidianViewInterface) view).setResizeXAxisCursor();
				}

				// take care when we get close to the origin
				if (Math.abs(mouseLoc.x - ((EuclidianViewInterface) view).getXZero()) < 2) {
					mouseLoc.x = (int) Math
							.round(mouseLoc.x > ((EuclidianViewInterface) view).getXZero() ? ((EuclidianViewInterface) view)
									.getXZero() + 2 : ((EuclidianViewInterface) view).getXZero() - 2);
				}
				double xscale = (mouseLoc.x - ((EuclidianViewInterface) view).getXZero()) / xTemp;
				((EuclidianViewInterface) view).setCoordSystem(((EuclidianViewInterface) view).getXZero(), ((EuclidianViewInterface) view).getYZero(), xscale,
						view.getYscale());
			}
			break;

		case MOVE_Y_AXIS:
			if (repaint) {
				if (TEMPORARY_MODE) {
					((EuclidianViewInterface) view).setResizeYAxisCursor();
				}
				// take care when we get close to the origin
				if (Math.abs(mouseLoc.y - ((EuclidianViewInterface) view).getYZero()) < 2) {
					mouseLoc.y = (int) Math
							.round(mouseLoc.y > ((EuclidianViewInterface) view).getYZero() ? ((EuclidianViewInterface) view)
									.getYZero() + 2 : ((EuclidianViewInterface) view).getYZero() - 2);
				}
				double yscale = (((EuclidianViewInterface) view).getYZero() - mouseLoc.y) / yTemp;
				((EuclidianViewInterface) view).setCoordSystem(((EuclidianViewInterface) view).getXZero(), ((EuclidianViewInterface) view).getYZero(),
						view.getXscale(), yscale);
			}
			break;

		default: // do nothing
		}
	}

	protected void updateSelectionRectangle(boolean keepScreenRatio) {
		if (view.getSelectionRectangle() == null) {
			view.setSelectionRectangle(geogebra.common.factories.AwtFactory.prototype.newRectangle(0,0));
		}

		int dx = mouseLoc.x - selectionStartPoint.x;
		int dy = mouseLoc.y - selectionStartPoint.y;
		int dxabs = Math.abs(dx);
		int dyabs = Math.abs(dy);

		int width = dx;
		int height = dy;

		// the zoom rectangle should have the same aspect ratio as the view
		if (keepScreenRatio) {
			double ratio = (double) view.getViewWidth()
					/ (double) view.getViewHeight();
			if (dxabs >= (dyabs * ratio)) {
				height = (int) (Math.round(dxabs / ratio));
				if (dy < 0) {
					height = -height;
				}
			} else {
				width = (int) Math.round(dyabs * ratio);
				if (dx < 0) {
					width = -width;
				}
			}
		}

		Rectangle rect = view.getSelectionRectangle();
		if (height >= 0) {
			if (width >= 0) {
				rect.setLocation(selectionStartPoint.x,selectionStartPoint.y);
				rect.setSize(width, height);
			} else { // width < 0
				rect.setLocation(selectionStartPoint.x + width,
						selectionStartPoint.y);
				rect.setSize(-width, height);
			}
		} else { // height < 0
			if (width >= 0) {
				rect.setLocation(selectionStartPoint.x, selectionStartPoint.y
						+ height);
				rect.setSize(width, -height);
			} else { // width < 0
				rect.setLocation(selectionStartPoint.x + width,
						selectionStartPoint.y + height);
				rect.setSize(-width, -height);
			}
		}
	}

	public void showDrawingPadPopup(Point mouseLoc) {
		((Application) app).getGuiManager().showDrawingPadPopup(((EuclidianViewInterface) view).getJPanel(), mouseLoc);
	}
	
	protected void wrapMouseReleased(AbstractEvent event) {
		
		sliderValue = null;

		if (event != null) {
			mx = event.getX();
			my = event.getY();
		}
		// reset
		transformCoordsOffset[0] = 0;
		transformCoordsOffset[1] = 0;

		if (textfieldHasFocus) {
			return;
		}

		if ((mode == EuclidianConstants.MODE_PEN)
				|| (mode == EuclidianConstants.MODE_FREEHAND)) {
			pen.handleMouseReleasedForPenMode(event);
			return;
		}

		boolean changedKernel0 = false;
		if (pastePreviewSelected != null) {

			mergeStickyPointsAfterPaste();

			// add moved points to sticky points again
			for (int i = 0; i < pastePreviewSelectedAndDependent.size(); i++) {
				GeoElement geo = pastePreviewSelectedAndDependent.get(i);
				if (geo.isGeoPoint()) {
					if (!view.getStickyPointList().contains(geo)) {
						view.getStickyPointList().add((GeoPointND) geo);
					}
				}
			}
			persistentStickyPointList = new ArrayList<GeoPointND>();

			pastePreviewSelected = null;
			pastePreviewSelectedAndDependent = null;
			view.setPointCapturing(previousPointCapturing);
			changedKernel0 = true;
			app.getKernel().getConstruction().getUndoManager()
					.storeUndoInfoAfterPasteOrAdd();
		}

		// if (mode != EuclidianConstants.MODE_RECORD_TO_SPREADSHEET)
		// view.resetTraceRow(); // for trace/spreadsheet
		if (getMovedGeoPoint() != null) {

			processReleaseForMovedGeoPoint(event);
			/*
			 * // deselect point after drag, but not on click if
			 * (movedGeoPointDragged) getMovedGeoPoint().setSelected(false);
			 * 
			 * if (mode != EuclidianConstants.MODE_RECORD_TO_SPREADSHEET)
			 * getMovedGeoPoint().resetTraceColumns();
			 */
		}
		if (movedGeoNumeric != null) {

			// deselect slider after drag, but not on click
			// if (movedGeoNumericDragged) movedGeoNumeric.setSelected(false);

			if ((mode != EuclidianConstants.MODE_RECORD_TO_SPREADSHEET)
					&& app.isUsingFullGui()) {
				movedGeoNumeric.resetTraceColumns();
			}
		}

		movedGeoPointDragged = false;
		movedGeoNumericDragged = false;

		view.requestFocusInWindow();
		setMouseLocation(event);

		setAltDown(event.isAltDown());

		transformCoords();
		Hits hits = null;
		GeoElement geo;

		if (hitResetIcon()) {
			app.reset();
			return;
		} else if (view.hitAnimationButton(event)) {
			if (kernel.isAnimationRunning()) {
				kernel.getAnimatonManager().stopAnimation();
			} else {
				kernel.getAnimatonManager().startAnimation();
			}
			view.repaintView();
			app.setUnsaved();
			return;
		}

		// Michael Borcherds 2007-10-08 allow drag with right mouse button
		if ((app.isRightClick(event) || app.isControlDown(event)))// &&
																			// !TEMPORARY_MODE)
		{
			if (processRightReleaseFor3D()) {
				return;
			}
			if (!TEMPORARY_MODE) {
				if (!app.isRightClickEnabled()) {
					return;
				}
				if (processZoomRectangle()) {
					return;
					// Michael Borcherds 2007-10-08
				}

				// make sure cmd-click selects multiple points (not open
				// properties)
				if ((Application.MAC_OS && app.isControlDown(event))
						|| !app.isRightClick(event)) {
					return;
				}

				// get selected GeoElements
				// show popup menu after right click
				view.setHits(mouseLoc);
				hits = view.getHits().getTopHits();
				if (hits.isEmpty()) {
					// no hits
					if (app.selectedGeosSize() > 0) {
						// GeoElement selGeo = (GeoElement)
						// app.getSelectedGeos().get(0);
						app.getGuiManager().showPopupMenu(
								app.getSelectedGeos(),  view, mouseLoc);
					} else {
						showDrawingPadPopup(mouseLoc);
					}
				} else {
					// there are hits
					if (app.selectedGeosSize() > 0) {

						// right click on already selected geos -> show menu for
						// them
						// right click on object(s) not selected -> clear
						// selection
						// and show menu just for new objects
						if (!app.getSelectedGeos().contains(hits.get(0))) {
							app.clearSelectedGeos();
							app.addSelectedGeos(hits, true);
						} else {
							app.addSelectedGeo(hits.get(0));
						}

						app.getGuiManager().showPopupMenu(
								app.getSelectedGeos(), view, mouseLoc);
					} else {
						// no selected geos: choose geo and show popup menu
						geo = chooseGeo(hits, false);
						if (geo != null) {
							ArrayList<GeoElement> geos = new ArrayList<GeoElement>();
							geos.add(geo);
							app.getGuiManager().showPopupMenu(geos,
									view, mouseLoc);
						} else {
							// for 3D : if the geo hitted is xOyPlane, then
							// chooseGeo return null
							// app.getGuiManager().showDrawingPadPopup((EuclidianView)
							// view, mouseLoc);
							showDrawingPadPopup(mouseLoc);
						}
					}
				}
				return;
			}
		}

		// handle moving
		boolean changedKernel = false;
		if (DRAGGING_OCCURED) {

			DRAGGING_OCCURED = false;
			// // copy value into input bar
			// if (mode == EuclidianView.MODE_MOVE && movedGeoElement != null) {
			// app.geoElementSelected(movedGeoElement,false);
			// }

			// check movedGeoElement.isLabelSet() to stop moving points
			// in Probability Calculator triggering Undo
			changedKernel = ((movedGeoElement != null) && movedGeoElement
					.isLabelSet()) && (moveMode != MOVE_NONE);
			movedGeoElement = null;
			rotGeoElement = null;

			// Michael Borcherds 2007-10-08 allow dragging with right mouse
			// button
			if (!TEMPORARY_MODE) {
				// Michael Borcherds 2007-10-08
				if (allowSelectionRectangle()) {
					processSelectionRectangle(event);

					return;
				}
			}
		} else {
			// no hits: release mouse button creates a point
			// for the transformation tools
			// (note: this cannot be done in mousePressed because
			// we want to be able to select multiple objects using the selection
			// rectangle)

			changedKernel = switchModeForMouseReleased(mode, hits,
					changedKernel);
		}

		// remember helper point, see createNewPoint()
		if (changedKernel && !changedKernel0) {
			app.storeUndoInfo();
		}

		// make sure that when alt is pressed for creating a segment or line
		// it works if the endpoint is on a path
		if (useLineEndPoint && (lineEndPoint != null)) {
			mouseLoc.x = view.toScreenCoordX(lineEndPoint.x);
			mouseLoc.y = view.toScreenCoordY(lineEndPoint.y);
			useLineEndPoint = false;
		}

		// now handle current mode
		view.setHits(mouseLoc);
		hits = view.getHits();
		switchModeForRemovePolygons(hits);
		// Application.debug(mode + "\n" + hits.toString());

		// Michael Borcherds 2007-12-08 BEGIN moved up a few lines (bugfix:
		// Tools eg Line Segment weren't working with grid on)
		// grid capturing on: newly created point should be taken
		// Application.debug("POINT_CREATED="+POINT_CREATED+"\nhits=\n"+hits+"\ngetMovedGeoPoint()="+getMovedGeoPoint());
		if (POINT_CREATED) {
			hits = addPointCreatedForMouseReleased(hits);
		}
		POINT_CREATED = false;
		// Michael Borcherds 2007-12-08 END

		if (TEMPORARY_MODE) {

			// Michael Borcherds 2007-10-13 BEGIN
			view.setMode(oldMode);
			TEMPORARY_MODE = false;
			// Michael Borcherds 2007-12-08 BEGIN bugfix: couldn't select
			// multiple points with Ctrl
			if (DONT_CLEAR_SELECTION == false) {
				clearSelections();
			}
			DONT_CLEAR_SELECTION = false;
			// Michael Borcherds 2007-12-08 END
			// mode = oldMode;
			// Michael Borcherds 2007-10-13 END
		}
		// Michael Borcherds 2007-10-12 bugfix: ctrl-click on a point does the
		// original mode's command at end of drag if a point was clicked on
		// also needed for right-drag
		else {
			if (mode != EuclidianConstants.MODE_RECORD_TO_SPREADSHEET) {
				changedKernel = processMode(hits, event);
			}
			if (changedKernel) {
				app.storeUndoInfo();
			}
		}
		// Michael Borcherds 2007-10-12

		// Michael Borcherds 2007-10-12
		// moved up a few lines
		// changedKernel = processMode(hits, e);
		// if (changedKernel)
		// app.storeUndoInfo();
		// Michael Borcherds 2007-10-12

		if (!hits.isEmpty()) {
			// Application.debug("hits ="+hits);
			view.setDefaultCursor();
		} else {
			view.setHitCursor();
		}

		if ((mode == EuclidianConstants.MODE_RECORD_TO_SPREADSHEET)
				&& (recordObject != null)) {
			clearSelections();
		} else {
			// this is in the else branch to avoid running it twice
			refreshHighlighting(null);
		}

		// reinit vars
		// view.setDrawMode(EuclidianConstants.DRAW_MODE_BACKGROUND_IMAGE);
		moveMode = MOVE_NONE;
		initShowMouseCoords();
		((EuclidianViewInterface) view).setShowAxesRatio(false);
		kernel.notifyRepaint();
	}

	public void mouseReleased(MouseEvent e) {
		AbstractEvent event = geogebra.euclidian.event.MouseEvent.wrapEvent(e);
		wrapMouseReleased(event);
		/*sliderValue = null;

		if (e != null) {
			mx = e.getX();
			my = e.getY();
		}
		// reset
		transformCoordsOffset[0] = 0;
		transformCoordsOffset[1] = 0;

		if (textfieldHasFocus) {
			return;
		}

		if ((mode == EuclidianConstants.MODE_PEN)
				|| (mode == EuclidianConstants.MODE_FREEHAND)) {
			((EuclidianPen) pen).handleMouseReleasedForPenMode(e);
			return;
		}

		boolean changedKernel0 = false;
		if (pastePreviewSelected != null) {

			mergeStickyPointsAfterPaste();

			// add moved points to sticky points again
			for (int i = 0; i < pastePreviewSelectedAndDependent.size(); i++) {
				GeoElement geo = pastePreviewSelectedAndDependent.get(i);
				if (geo.isGeoPoint()) {
					if (!((EuclidianViewInterface) view).getStickyPointList().contains(geo)) {
						((EuclidianViewInterface) view).getStickyPointList().add((GeoPointND) geo);
					}
				}
			}
			persistentStickyPointList = new ArrayList<GeoPointND>();

			pastePreviewSelected = null;
			pastePreviewSelectedAndDependent = null;
			((EuclidianViewInterface) view).setPointCapturing(previousPointCapturing);
			changedKernel0 = true;
			((Application)app).getKernel().getConstruction().getUndoManager()
					.storeUndoInfoAfterPasteOrAdd();
		}

		// if (mode != EuclidianConstants.MODE_RECORD_TO_SPREADSHEET)
		// view.resetTraceRow(); // for trace/spreadsheet
		if (getMovedGeoPoint() != null) {

			processReleaseForMovedGeoPoint(e);
			/*
			 * // deselect point after drag, but not on click if
			 * (movedGeoPointDragged) getMovedGeoPoint().setSelected(false);
			 * 
			 * if (mode != EuclidianConstants.MODE_RECORD_TO_SPREADSHEET)
			 * getMovedGeoPoint().resetTraceColumns();
			 */
//		}
//		if (movedGeoNumeric != null) {

			// deselect slider after drag, but not on click
			// if (movedGeoNumericDragged) movedGeoNumeric.setSelected(false);

//			if ((mode != EuclidianConstants.MODE_RECORD_TO_SPREADSHEET)
//					&& ((Application)app).isUsingFullGui()) {
//				movedGeoNumeric.resetTraceColumns();
//			}
//		}

//		movedGeoPointDragged = false;
//		movedGeoNumericDragged = false;

//		((EuclidianViewInterface) view).requestFocusInWindow();
//		setMouseLocation(geogebra.euclidian.event.MouseEvent.wrapEvent(e));

//		setAltDown(e.isAltDown());

//		transformCoords();
//		Hits hits = null;
//		GeoElement geo;

//		if (hitResetIcon()) {
//			((Application) app).reset();
//			return;
//		} else if (((EuclidianViewInterface) view).hitAnimationButton(e)) {
//			if (kernel.isAnimationRunning()) {
//				kernel.getAnimatonManager().stopAnimation();
//			} else {
//				kernel.getAnimatonManager().startAnimation();
//			}
//			view.repaintView();
//			((Application)app).setUnsaved();
//			return;
//		}

		// Michael Borcherds 2007-10-08 allow drag with right mouse button
//		if ((Application.isRightClick(e) || Application.isControlDown(e)))// &&
																			// !TEMPORARY_MODE)
//		{
//			if (processRightReleaseFor3D()) {
//				return;
//			}
//			if (!TEMPORARY_MODE) {
//				if (!((Application) app).isRightClickEnabled()) {
//					return;
//				}
//				if (processZoomRectangle()) {
//					return;
					// Michael Borcherds 2007-10-08
//				}

				// make sure cmd-click selects multiple points (not open
				// properties)
//				if ((Application.MAC_OS && Application.isControlDown(e))
//						|| !Application.isRightClick(e)) {
//					return;
//				}

				// get selected GeoElements
				// show popup menu after right click
//				((EuclidianViewInterface) view).setHits(mouseLoc);
//				hits = ((EuclidianViewInterface) view).getHits().getTopHits();
//				if (hits.isEmpty()) {
					// no hits
//					if (((Application) app).selectedGeosSize() > 0) {
						// GeoElement selGeo = (GeoElement)
						// app.getSelectedGeos().get(0);
//						((Application) app).getGuiManager().showPopupMenu(
//								((Application)app).getSelectedGeos(),  ((EuclidianViewInterface) view).getJPanel(), mouseLoc);
//					} else {
//						showDrawingPadPopup(mouseLoc);
//					}
//				} else {
					// there are hits
//					if (((Application) app).selectedGeosSize() > 0) {

						// right click on already selected geos -> show menu for
						// them
						// right click on object(s) not selected -> clear
						// selection
						// and show menu just for new objects
//						if (!((Application)app).getSelectedGeos().contains(hits.get(0))) {
//							((Application)app).clearSelectedGeos();
//							((Application)app).addSelectedGeos(hits, true);
//						} else {
//							((Application)app).addSelectedGeo(hits.get(0));
//						}

//						((Application) app).getGuiManager().showPopupMenu(
//								((Application)app).getSelectedGeos(), ((EuclidianViewInterface) view).getJPanel(), mouseLoc);
//					} else {
						// no selected geos: choose geo and show popup menu
//						geo = chooseGeo(hits, false);
//						if (geo != null) {
//							ArrayList<GeoElement> geos = new ArrayList<GeoElement>();
//							geos.add(geo);
//							((Application) app).getGuiManager().showPopupMenu(geos,
//									((EuclidianViewInterface) view).getJPanel(), mouseLoc);
//						} else {
							// for 3D : if the geo hitted is xOyPlane, then
							// chooseGeo return null
							// app.getGuiManager().showDrawingPadPopup((EuclidianView)
							// view, mouseLoc);
//							showDrawingPadPopup(mouseLoc);
//						}
//					}
//				}
//				return;
//			}
//		}

		// handle moving
//		boolean changedKernel = false;
//		if (DRAGGING_OCCURED) {

//			DRAGGING_OCCURED = false;
			// // copy value into input bar
			// if (mode == EuclidianView.MODE_MOVE && movedGeoElement != null) {
			// app.geoElementSelected(movedGeoElement,false);
			// }

			// check movedGeoElement.isLabelSet() to stop moving points
			// in Probability Calculator triggering Undo
//			changedKernel = ((movedGeoElement != null) && movedGeoElement
//					.isLabelSet()) && (moveMode != MOVE_NONE);
//			movedGeoElement = null;
//			rotGeoElement = null;

			// Michael Borcherds 2007-10-08 allow dragging with right mouse
			// button
//			if (!TEMPORARY_MODE) {
				// Michael Borcherds 2007-10-08
//				if (allowSelectionRectangle()) {
//					processSelectionRectangle(e);

//					return;
//				}
//			}
//		} else {
			// no hits: release mouse button creates a point
			// for the transformation tools
			// (note: this cannot be done in mousePressed because
			// we want to be able to select multiple objects using the selection
			// rectangle)

//			changedKernel = switchModeForMouseReleased(mode, hits,
//					changedKernel);
//		}

		// remember helper point, see createNewPoint()
//		if (changedKernel && !changedKernel0) {
//			((Application)app).storeUndoInfo();
//		}

		// make sure that when alt is pressed for creating a segment or line
		// it works if the endpoint is on a path
//		if (useLineEndPoint && (lineEndPoint != null)) {
//			mouseLoc.x = ((EuclidianViewInterface) view).toScreenCoordX(lineEndPoint.x);
//			mouseLoc.y = ((EuclidianViewInterface) view).toScreenCoordY(lineEndPoint.y);
//			useLineEndPoint = false;
//		}

		// now handle current mode
//		((EuclidianViewInterface) view).setHits(mouseLoc);
//		hits = ((EuclidianViewInterface) view).getHits();
//		switchModeForRemovePolygons(hits);
		// Application.debug(mode + "\n" + hits.toString());

		// Michael Borcherds 2007-12-08 BEGIN moved up a few lines (bugfix:
		// Tools eg Line Segment weren't working with grid on)
		// grid capturing on: newly created point should be taken
		// Application.debug("POINT_CREATED="+POINT_CREATED+"\nhits=\n"+hits+"\ngetMovedGeoPoint()="+getMovedGeoPoint());
//		if (POINT_CREATED) {
//			hits = addPointCreatedForMouseReleased(hits);
//		}
//		POINT_CREATED = false;
		// Michael Borcherds 2007-12-08 END

//		if (TEMPORARY_MODE) {

			// Michael Borcherds 2007-10-13 BEGIN
//			view.setMode(oldMode);
//			TEMPORARY_MODE = false;
			// Michael Borcherds 2007-12-08 BEGIN bugfix: couldn't select
			// multiple points with Ctrl
//			if (DONT_CLEAR_SELECTION == false) {
//				clearSelections();
//			}
//			DONT_CLEAR_SELECTION = false;
			// Michael Borcherds 2007-12-08 END
			// mode = oldMode;
			// Michael Borcherds 2007-10-13 END
//		}
		// Michael Borcherds 2007-10-12 bugfix: ctrl-click on a point does the
		// original mode's command at end of drag if a point was clicked on
		// also needed for right-drag
//		else {
//			if (mode != EuclidianConstants.MODE_RECORD_TO_SPREADSHEET) {
//				changedKernel = processMode(hits, e);
//			}
//			if (changedKernel) {
//				((Application)app).storeUndoInfo();
//			}
//		}
		// Michael Borcherds 2007-10-12

		// Michael Borcherds 2007-10-12
		// moved up a few lines
		// changedKernel = processMode(hits, e);
		// if (changedKernel)
		// app.storeUndoInfo();
		// Michael Borcherds 2007-10-12

//		if (!hits.isEmpty()) {
			// Application.debug("hits ="+hits);
//			((EuclidianViewInterface) view).setDefaultCursor();
//		} else {
//			((EuclidianViewInterface) view).setHitCursor();
//		}

//		if ((mode == EuclidianConstants.MODE_RECORD_TO_SPREADSHEET)
//				&& (recordObject != null)) {
//			clearSelections();
//		} else {
			// this is in the else branch to avoid running it twice
//			refreshHighlighting(null);
//		}

		// reinit vars
		// view.setDrawMode(EuclidianConstants.DRAW_MODE_BACKGROUND_IMAGE);
//		moveMode = MOVE_NONE;
//		initShowMouseCoords();
//		((EuclidianViewInterface) view).setShowAxesRatio(false);
//		kernel.notifyRepaint();*/
	}

	// return if we really did zoom
	protected boolean processZoomRectangle() {
		geogebra.awt.Rectangle rect = (geogebra.awt.Rectangle) view.getSelectionRectangle();
		if (rect == null) {
			return false;
		}

		if ((rect.getWidth() < 30) || (rect.getHeight() < 30)
				|| !app.isShiftDragZoomEnabled() // Michael Borcherds 2007-12-11
		) {
			view.setSelectionRectangle(null);
			view.repaintView();
			return false;
		}

		view.resetMode();
		// zoom zoomRectangle to EuclidianView's size
		// double factor = (double) view.width / (double) rect.width;
		// Point p = rect.getLocation();
		view.setSelectionRectangle(null);
		// view.setAnimatedCoordSystem((view.xZero - p.x) * factor,
		// (view.yZero - p.y) * factor, view.xscale * factor, 15, true);

		// zoom without (necessarily) preserving the aspect ratio
		view.setAnimatedRealWorldCoordSystem(
				view.toRealWorldCoordX(rect.getMinX()),
				view.toRealWorldCoordX(rect.getMaxX()),
				view.toRealWorldCoordY(rect.getMaxY()),
				view.toRealWorldCoordY(rect.getMinY()), 15, true);
		return true;
	}

	// select all geos in selection rectangle
	protected void processSelectionRectangle(AbstractEvent e) {
		clearSelections();
		view.setHits(view.getSelectionRectangle());
		Hits hits = ((EuclidianViewInterface) view).getHits();

		switch (mode) {
		case EuclidianConstants.MODE_SELECTION_LISTENER:
			// tell properties dialog
			if ((hits.size() > 0)
					&& ((Application)app).isUsingFullGui()
					&& ((Application) app).getGuiManager()
							.isPropertiesDialogSelectionListener()) {
				GeoElement geo = hits.get(0);
				((Application) app).geoElementSelected(geo, false);
				for (int i = 1; i < hits.size(); i++) {
					((Application) app).geoElementSelected(hits.get(i), true);
				}
			}
			break;

		case EuclidianConstants.MODE_MIRROR_AT_POINT:
		case EuclidianConstants.MODE_MIRROR_AT_LINE:
		case EuclidianConstants.MODE_MIRROR_AT_CIRCLE: // Michael Borcherds
														// 2008-03-23
			processSelectionRectangleForTransformations(hits,
					Test.TRANSFORMABLE);
			break;

		case EuclidianConstants.MODE_ROTATE_BY_ANGLE:
			processSelectionRectangleForTransformations(hits,
					Test.TRANSFORMABLE);
			break;

		case EuclidianConstants.MODE_TRANSLATE_BY_VECTOR:
			processSelectionRectangleForTransformations(hits,
					Test.TRANSFORMABLE);
			break;

		case EuclidianConstants.MODE_DILATE_FROM_POINT:
			processSelectionRectangleForTransformations(hits, Test.DILATEABLE);
			break;

		case EuclidianConstants.MODE_CREATE_LIST:
			removeParentPoints(hits);
			selectedGeos.addAll(hits);
			((Application) app).setSelectedGeos(hits);
			processMode(hits, e);
			((EuclidianViewInterface) view).setSelectionRectangle(null);
			break;

		case EuclidianConstants.MODE_FITLINE:

			// check for list first
			if (hits.size() == 1) {
				if (hits.get(0).isGeoList()) {
					selectedGeos.addAll(hits);
					((Application) app).setSelectedGeos(hits);
					processMode(hits, e);
					((EuclidianViewInterface) view).setSelectionRectangle(null);
					break;
				}
			}

			// remove non-Points
			for (int i = 0; i < hits.size(); i++) {
				GeoElement geo = hits.get(i);
				if (!(GeoPoint2.class.isInstance(geo))) {
					hits.remove(i);
				}
			}

			// Fit line makes sense only for more than 2 points (or one list)
			if (hits.size() < 3) {
				hits.clear();
			} else {
				removeParentPoints(hits);
				selectedGeos.addAll(hits);
				((Application) app).setSelectedGeos(hits);
				processMode(hits, e);
				((EuclidianViewInterface) view).setSelectionRectangle(null);
			}
			break;

		default:
			// STANDARD CASE
			((Application) app).setSelectedGeos(hits);

			// if alt pressed, create list of objects as string and copy to
			// input bar
			if ((hits != null) && (hits.size() > 0) && (e != null)
					&& e.isAltDown() && ((Application)app).isUsingFullGui()
					&& ((Application) app).showAlgebraInput()) {

				JTextComponent textComponent = ((Application) app).getGuiManager()
						.getAlgebraInputTextField();

				StringBuilder sb = new StringBuilder();
				sb.append(" {");
				for (int i = 0; i < hits.size(); i++) {
					sb.append(hits.get(i).getLabel());
					if (i < (hits.size() - 1)) {
						sb.append(", ");
					}
				}
				sb.append("} ");
				// Application.debug(sb+"");
				textComponent.replaceSelection(sb.toString());
			}
			break;
		}

		kernel.notifyRepaint();
	}

	// TODO replace Class<?> by Class<GeoElement> ?
	protected void processSelectionRectangleForTransformations(Hits hits,
			Test test) {
		for (int i = 0; i < hits.size(); i++) {
			GeoElement geo = hits.get(i);
			if (!(test.check(geo))
			// || geo.isGeoPolygon()
			) {
				hits.remove(i);
			}
		}
		removeParentPoints(hits);
		selectedGeos.addAll(hits);
		((Application) app).setSelectedGeos(hits);
	}

	protected void processSelection() {
		Hits hits = new Hits();
		hits.addAll(((Application)app).getSelectedGeos());
		clearSelections();

		switch (mode) {
		case EuclidianConstants.MODE_MIRROR_AT_POINT:
		case EuclidianConstants.MODE_MIRROR_AT_LINE:
		case EuclidianConstants.MODE_MIRROR_AT_CIRCLE: // Michael Borcherds
														// 2008-03-23
			processSelectionRectangleForTransformations(hits,
					Test.TRANSFORMABLE);
			break;

		case EuclidianConstants.MODE_ROTATE_BY_ANGLE:
			processSelectionRectangleForTransformations(hits,
					Test.TRANSFORMABLE);
			break;

		case EuclidianConstants.MODE_TRANSLATE_BY_VECTOR:
			processSelectionRectangleForTransformations(hits,
					Test.TRANSFORMABLE);
			break;

		case EuclidianConstants.MODE_DILATE_FROM_POINT:
			processSelectionRectangleForTransformations(hits, Test.DILATEABLE);
			break;

		// case EuclidianConstants.MODE_CREATE_LIST:
		case EuclidianConstants.MODE_FITLINE:
			for (int i = 0; i < hits.size(); i++) {
				GeoElement geo = hits.get(i);
				if (!(GeoPoint2.class.isInstance(geo))) {
					hits.remove(i);
				}
			}
			// Fit line makes sense only for more than 2 points
			if (hits.size() < 3) {
				hits.clear();
			} else {
				removeParentPoints(hits);
				selectedGeos.addAll(hits);
				((Application) app).setSelectedGeos(hits);
				processMode(hits, null);
				((EuclidianViewInterface) view).setSelectionRectangle(null);
			}
			break;

		default:
			break;
		}

		kernel.notifyRepaint();
	}

	public void mouseMoved(MouseEvent e) {
		AbstractEvent event = geogebra.euclidian.event.MouseEvent.wrapEvent(e);
		wrapMouseMoved(event);
	}
	
	public void mouseEntered(MouseEvent e) {
		AbstractEvent event = geogebra.euclidian.event.MouseEvent.wrapEvent(e);
		wrapMouseEntered(event);
	}
	
	public void mouseExited(MouseEvent e) {
		AbstractEvent event = geogebra.euclidian.event.MouseEvent.wrapEvent(e);
		wrapMouseExited(event);
	}

	/*
	 * public void focusGained(FocusEvent e) { initToolTipManager(); }
	 * 
	 * public void focusLost(FocusEvent e) { resetToolTipManager(); }
	 */

	public void initToolTipManager() {
		// set tooltip manager
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setInitialDelay(DEFAULT_INITIAL_DELAY / 2);
		ttm.setEnabled(((Application) app).getAllowToolTips());
	}

	public void resetToolTipManager() {
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setInitialDelay(DEFAULT_INITIAL_DELAY);
	}

	/* ****************************************************** */

	

	

	

	/*
	 * final protected void transformCoords(boolean usePointCapturing) { // calc
	 * real world coords calcRWcoords();
	 * 
	 * if (usePointCapturing) { double pointCapturingPercentage = 1; switch
	 * (view.getPointCapturingMode()) { case
	 * EuclidianConstants.POINT_CAPTURING_AUTOMATIC: if
	 * (!view.isGridOrAxesShown())break;
	 * 
	 * case EuclidianView.POINT_CAPTURING_ON: pointCapturingPercentage = 0.125;
	 * 
	 * case EuclidianView.POINT_CAPTURING_ON_GRID: // X = (x, y) ... next grid
	 * point double x = Kernel.roundToScale(xRW, view.gridDistances[0]); double
	 * y = Kernel.roundToScale(yRW, view.gridDistances[1]); // if |X - XRW| <
	 * gridInterval * pointCapturingPercentage then take the grid point double a
	 * = Math.abs(x - xRW); double b = Math.abs(y - yRW); if (a <
	 * view.gridDistances[0] * pointCapturingPercentage && b <
	 * view.gridDistances[1] * pointCapturingPercentage) { xRW = x; yRW = y;
	 * mouseLoc.x = view.toScreenCoordX(xRW); mouseLoc.y =
	 * view.toScreenCoordY(yRW); }
	 * 
	 * default: // point capturing off } } }
	 */

	

	

	

	

	

	// fetch the two selected points
	/*
	 * protected void join(){ GeoPoint[] points = getSelectedPoints(); GeoLine
	 * line = kernel.Line(null, points[0], points[1]); }
	 */

	public GeoElement[] createCircle2ForPoints3D(GeoPointND p0, GeoPointND p1) {
		return new GeoElement[] { kernel.getManager3D().Circle3D(null, p0, p1,
				((AbstractEuclidianView) view).getDirection()) };
	}

	public void componentResized(ComponentEvent e) {
		// tell the view that it was resized
		((EuclidianViewInterface) view).updateSize();
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}
	
	protected void wrapMouseWheelMoved(AbstractEvent event) {
		
		if (textfieldHasFocus) {
			return;
		}

		if ((mode == EuclidianConstants.MODE_PEN)
				|| (mode == EuclidianConstants.MODE_FREEHAND)) {
			return;
		}

		// don't allow mouse wheel zooming for applets if mode is not zoom mode
		boolean allowMouseWheel = !((Application)app).isApplet()
				|| (mode == EuclidianConstants.MODE_ZOOM_IN)
				|| (mode == EuclidianConstants.MODE_ZOOM_OUT)
				|| (app.isShiftDragZoomEnabled() && (event.isControlDown()
						|| event.isMetaDown() || event.isShiftDown()));
		if (!allowMouseWheel) {
			return;
		}

		setMouseLocation(event);

		// double px = view.width / 2d;
		// double py = view.height / 2d;
		double px = mouseLoc.x;
		double py = mouseLoc.y;
		// double dx = view.getXZero() - px;
		// double dy = view.getYZero() - py;

		double xFactor = 1;
		if (event.isAltDown()) {
			xFactor = 1.5;
		}

		double reverse = app.isMouseWheelReversed() ? -1 : 1;

		double factor = ((event.getWheelRotation() * reverse) > 0) ? AbstractEuclidianView.MOUSE_WHEEL_ZOOM_FACTOR
				* xFactor
				: 1d / (AbstractEuclidianView.MOUSE_WHEEL_ZOOM_FACTOR * xFactor);

		// make zooming a little bit smoother by having some steps

		((EuclidianViewInterface) view).setAnimatedCoordSystem(
		// px + dx * factor,
		// py + dy * factor,
				px, py, factor, view.getXscale() * factor, 4, false);
		// view.yscale * factor);
		app.setUnsaved();
	}

	/**
	 * Zooms in or out using mouse wheel
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		AbstractEvent event = geogebra.euclidian.event.MouseEvent.wrapEvent(e);
		wrapMouseWheelMoved(event);
		/*if (textfieldHasFocus) {
			return;
		}

		if ((mode == EuclidianConstants.MODE_PEN)
				|| (mode == EuclidianConstants.MODE_FREEHAND)) {
			return;
		}

		// don't allow mouse wheel zooming for applets if mode is not zoom mode
		boolean allowMouseWheel = !((Application)app).isApplet()
				|| (mode == EuclidianConstants.MODE_ZOOM_IN)
				|| (mode == EuclidianConstants.MODE_ZOOM_OUT)
				|| (((Application) app).isShiftDragZoomEnabled() && (e.isControlDown()
						|| e.isMetaDown() || e.isShiftDown()));
		if (!allowMouseWheel) {
			return;
		}

		setMouseLocation(geogebra.euclidian.event.MouseEvent.wrapEvent(e));

		// double px = view.width / 2d;
		// double py = view.height / 2d;
		double px = mouseLoc.x;
		double py = mouseLoc.y;
		// double dx = view.getXZero() - px;
		// double dy = view.getYZero() - py;

		double xFactor = 1;
		if (e.isAltDown()) {
			xFactor = 1.5;
		}

		double reverse = ((Application) app).isMouseWheelReversed() ? -1 : 1;

		double factor = ((e.getWheelRotation() * reverse) > 0) ? EuclidianView.MOUSE_WHEEL_ZOOM_FACTOR
				* xFactor
				: 1d / (EuclidianView.MOUSE_WHEEL_ZOOM_FACTOR * xFactor);

		// make zooming a little bit smoother by having some steps

		((EuclidianViewInterface) view).setAnimatedCoordSystem(
		// px + dx * factor,
		// py + dy * factor,
				px, py, factor, view.getXscale() * factor, 4, false);
		// view.yscale * factor);
		((Application)app).setUnsaved();*/
	}

	public void zoomInOut(KeyEvent event) {
		boolean allowZoom = !((Application)app).isApplet()
				|| (mode == EuclidianConstants.MODE_ZOOM_IN)
				|| (mode == EuclidianConstants.MODE_ZOOM_OUT)
				|| (((Application) app).isShiftDragZoomEnabled());
		if (!allowZoom) {
			return;
		}
		double px, py;
		if (mouseLoc != null) {
			px = mouseLoc.x;
			py = mouseLoc.y;
		} else {
			px = view.getWidth() / 2;
			py = view.getHeight() / 2;
		}

		double factor = event.getKeyCode() == KeyEvent.VK_MINUS ? 1d / AbstractEuclidianView.MOUSE_WHEEL_ZOOM_FACTOR
				: AbstractEuclidianView.MOUSE_WHEEL_ZOOM_FACTOR;

		// accelerated zoom
		if (event.isAltDown()) {
			factor *= event.getKeyCode() == KeyEvent.VK_MINUS ? 2d / 3d : 1.5;
		}

		// make zooming a little bit smoother by having some steps
		((EuclidianViewInterface) view).setAnimatedCoordSystem(
		// px + dx * factor,
		// py + dy * factor,
				px, py, factor, view.getXscale() * factor, 4, false);
		// view.yscale * factor);
		((Application)app).setUnsaved();

	}

	/*
	 * when drawing a line, this is used when alt is down to set the angle to be
	 * a multiple of 15 degrees
	 */
	public void setLineEndPoint(Point2D.Double point) {
		lineEndPoint = point;
		useLineEndPoint = true;
	}

	// /////////////////////////////////////////
	// moved GeoElements

	

	

	// /////////////////////////////////////////
	// EMPTY METHODS USED FOR EuclidianView3D

	/** right-press the mouse makes start 3D rotation */
	protected void processRightPressFor3D() {

	}

	/**
	 * right-drag the mouse makes 3D rotation
	 * 
	 * @return false
	 */
	protected boolean processRotate3DView() {
		return false;
	}

	// ******************************
	// PropertiesPanelMini Listeners
	// ******************************
	int penLineStyle = 0;

	public void setLineStyle(int lineStyle) {
		penLineStyle = lineStyle;

		// if (mode == EuclidianView.MODE_VISUAL_STYLE) {
		ArrayList<GeoElement> geos = ((Application)app).getSelectedGeos();

		for (int i = 0; i < geos.size(); i++) {
			GeoElement geo = geos.get(i);
			geo.setLineType(lineStyle);
			geo.updateRepaint();

		}
		// }
	}

	public void setSize(int size) {
		// if (mode == EuclidianView.MODE_VISUAL_STYLE) {
		ArrayList<GeoElement> geos = ((Application)app).getSelectedGeos();

		for (int i = 0; i < geos.size(); i++) {
			GeoElement geo = geos.get(i);
			if (geo instanceof PointProperties) {
				((PointProperties) geo).setPointSize(size);
				geo.updateRepaint();
			} else {
				geo.setLineThickness(size);
				geo.updateRepaint();
			}
		}
		// }

	}

	Color penColor = Color.black;

	public void setColor(Color color) {
		penColor = color;

		// if (mode == EuclidianView.MODE_VISUAL_STYLE) {
		ArrayList<GeoElement> geos = ((Application)app).getSelectedGeos();

		for (int i = 0; i < geos.size(); i++) {
			GeoElement geo = geos.get(i);
			geo.setObjColor(new geogebra.awt.Color(color));
			geo.updateRepaint();
		}
		// }
	}

	public void setAlpha(float alpha) {
		ArrayList<GeoElement> geos = ((Application)app).getSelectedGeos();
		for (int i = 0; i < geos.size(); i++) {
			GeoElement geo = geos.get(i);
			geo.setAlphaValue(alpha);
			geo.updateRepaint();
		}
	}

	private void openMiniPropertiesPanel() {
		if (!((Application)app).isUsingFullGui()) {
			return;
		}
		if (Application.isMiniPropertiesActive()) {
			((Application) app).getGuiManager().toggleMiniProperties(true);
		}
	}

	private void closeMiniPropertiesPanel() {
		if (!((Application)app).isUsingFullGui()) {
			return;
		}
		((Application) app).getGuiManager().toggleMiniProperties(false);

	}

	public Hits getHighlightedgeos() {
		return highlightedGeos.clone();
	}

	public void setLineEndPoint(geogebra.common.awt.Point2D p) {
		if(p==null)
			lineEndPoint = null;
		else
		lineEndPoint = new Point2D.Double(p.getX(),p.getY());
		useLineEndPoint = true;
	}

	public GeoElement getRecordObject() {
		return recordObject;
	}

}
