package org.geogebra.common.properties.impl.graphics;

import org.geogebra.common.main.Localization;
import org.geogebra.common.main.settings.EuclidianSettings;
import org.geogebra.common.properties.AbstractProperty;
import org.geogebra.common.properties.BooleanProperty;

/**
 * This property controls the visibility of the axes.
 */
public class AxesVisibilityProperty extends AbstractProperty
		implements BooleanProperty {

	private EuclidianSettings euclidianSettings;

	/**
	 * Constructs an AxesVisibility property.
	 *
	 * @param localization
	 *            localization for the name
	 * @param euclidianSettings
	 *            euclidian settings
	 */
	public AxesVisibilityProperty(Localization localization,
			EuclidianSettings euclidianSettings) {
		super(localization, "ShowAxes");
		this.euclidianSettings = euclidianSettings;
	}

	@Override
	public boolean getValue() {
		boolean[] showAxes = euclidianSettings.getShowAxes();
		boolean value = false;
		for (boolean showAxis : showAxes) {
			value |= showAxis;
		}

		return value;
	}

	@Override
	public void setValue(boolean value) {
		euclidianSettings.setShowAxes(value);
	}
}
