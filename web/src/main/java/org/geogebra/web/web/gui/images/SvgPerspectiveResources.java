package org.geogebra.web.web.gui.images;

import org.geogebra.web.resources.SVGResource;
import org.geogebra.web.web.gui.ImageFactory;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * SVGs of the main buttons
 */
public interface SvgPerspectiveResources extends ClientBundle {
	final static SvgPerspectiveResources INSTANCE = ImageFactory
			.getPerspectiveResources();
	@Source("org/geogebra/common/icons/svg/web/menu_icons/menu_view_algebra.svg")
	SVGResource menu_icon_algebra();
	

	@Source("org/geogebra/common/icons/svg/web/menu_icons/perspectives_geometry.svg")
	SVGResource menu_icon_geometry();
	

	@Source("org/geogebra/common/icons/svg/web/menu_icons/menu_view_cas.svg")
	SVGResource menu_icon_cas();
	

	@Source("org/geogebra/common/icons/svg/web/menu_icons/menu_view_graphics.svg")
	SVGResource menu_icon_exam();


	@Source("org/geogebra/common/icons/svg/web/menu_icons/menu_view_graphics.svg")
	SVGResource menu_icon_graphics();
	

	@Source("org/geogebra/common/icons/svg/web/menu_icons/menu_view_graphics1.svg")
	SVGResource menu_icon_graphics1();
	

	@Source("org/geogebra/common/icons/svg/web/menu_icons/menu_view_graphics2.svg")
	SVGResource menu_icon_graphics2();
	

	@Source("org/geogebra/common/icons/svg/web/menu_icons/menu_view_graphics_extra.svg")
	SVGResource menu_icon_graphics_extra();


	@Source("org/geogebra/common/icons/svg/web/menu_icons/menu_view_spreadsheet.svg")
	SVGResource menu_icon_spreadsheet();
	

	@Source("org/geogebra/common/icons/svg/web/menu_icons/perspectives_algebra_3Dgraphics.svg")
	SVGResource menu_icon_graphics3D();
	

	@Source("org/geogebra/common/icons/svg/web/menu_icons/menu_view_construction_protocol.svg")
	SVGResource menu_icon_construction_protocol();
	

	@Source("org/geogebra/common/icons/svg/web/menu_icons/menu_view_probability.svg")
	SVGResource menu_icon_probability();
	

	@Source("org/geogebra/common/icons/svg/web/stylingbar/stylingbar_icon_algebra.svg")
	SVGResource styleBar_algebraView();


	@Source("org/geogebra/common/icons/svg/web/stylingbar/stylingbar_icon_graphics.svg")
	SVGResource styleBar_graphicsView();


	@Source("org/geogebra/common/icons/svg/web/stylingbar/stylingbar_icon_cas.svg")
	SVGResource styleBar_CASView();


	@Source("org/geogebra/common/icons/svg/web/stylingbar/stylingbar_icon_construction_protocol.svg")
	SVGResource styleBar_ConstructionProtocol();


	@Source("org/geogebra/common/icons/svg/web/stylingbar/stylingbar_icon_graphics3D.svg")
	SVGResource styleBar_graphics3dView();


	@Source("org/geogebra/common/icons/svg/web/stylingbar/stylingbar_icon_graphics2.svg")
	SVGResource styleBar_graphics2View();


	@Source("org/geogebra/common/icons/svg/web/stylingbar/stylingbar_icon_spreadsheet.svg")
	SVGResource styleBar_spreadsheetView();
	

	@Source("org/geogebra/common/icons/svg/web/menu-edit-redo.svg")
	SVGResource menu_header_redo();


	@Source("org/geogebra/common/icons/svg/web/menu-edit-redo.svg")
	SVGResource menu_header_redo_hover();


	@Source("org/geogebra/common/icons/svg/web/menu-edit-undo.svg")
	SVGResource menu_header_undo();


	@Source("org/geogebra/common/icons/svg/web/menu-edit-undo.svg")
	SVGResource menu_header_undo_hover();


	@Source("org/geogebra/common/icons/svg/web/menu-button-open-search.svg")
	SVGResource menu_header_open_search();


	@Source("org/geogebra/common/icons/svg/web/menu-button-open-search.svg")
	SVGResource menu_header_open_search_hover();


	@Source("org/geogebra/common/icons/svg/web/menu-button-open-menu.svg")
	SVGResource menu_header_open_menu();


	@Source("org/geogebra/common/icons/svg/web/menu-button-open-menu.svg")
	SVGResource menu_header_open_menu_hover();


	@Source("org/geogebra/common/icons/svg/web/stylingbar/stylebar_icon_graphics_extra.svg")
	SVGResource styleBar_graphics_extra();


	@Source("org/geogebra/common/icons/svg/web/menu_icons/menu_view_whiteboard.svg")
	SVGResource menu_icon_whiteboard();

	@Source("org/geogebra/common/icons/png/web/menu_icons24/menu_view_algebra.png")
	ImageResource menu_icon_algebra24();


	@Source("org/geogebra/common/icons/png/web/menu_icons24/perspectives_geometry.png")
	ImageResource menu_icon_geometry24();


	@Source("org/geogebra/common/icons/png/web/menu_icons24/menu_view_cas.png")
	ImageResource menu_icon_cas24();


	@Source("org/geogebra/common/icons/png/web/menu_icons24/menu_view_graphics.png")
	ImageResource menu_icon_graphics24();


	@Source("org/geogebra/common/icons/png/web/menu_icons24/menu_view_graphics2.png")
	ImageResource menu_icon_graphics224();

	@Source("org/geogebra/common/icons/png/web/menu_icons24/menu_view_graphics_extra.png")
	ImageResource menu_icon_graphics_extra24();


	@Source("org/geogebra/common/icons/png/web/menu_icons24/menu_view_spreadsheet.png")
	ImageResource menu_icon_spreadsheet24();


	@Source("org/geogebra/common/icons/png/web/menu_icons24/perspectives_algebra_3Dgraphics.png")
	ImageResource menu_icon_graphics3D24();


	@Source("org/geogebra/common/icons/png/web/menu_icons24/menu_view_construction_protocol.png")
	ImageResource menu_icon_construction_protocol24();


	@Source("org/geogebra/common/icons/png/web/menu_icons24/menu_view_probability.png")
	ImageResource menu_icon_probability24();


	@Source("org/geogebra/common/icons/png/web/menu_icons24/menu_view_whiteboard.png")
	ImageResource menu_icon_whiteboard24();


	@Source("org/geogebra/common/icons/png/web/menu_icons24/menu_view_exam.png")
	ImageResource menu_icon_exam24();

	// StyleBar

	@Source("org/geogebra/common/icons/png24x24/settings.png")
	ImageResource settings();

	@Source("org/geogebra/common/icons/png/web/menu_icons/back_right.png")
	ImageResource back_right();

	@Source("org/geogebra/common/menu_header/p32/menu_back.png")
	ImageResource menu_header_back();

}
