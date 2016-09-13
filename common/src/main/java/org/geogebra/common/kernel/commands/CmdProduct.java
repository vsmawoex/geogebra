package org.geogebra.common.kernel.commands;

import org.geogebra.common.kernel.CircularDefinitionException;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.algos.AlgoProduct;
import org.geogebra.common.kernel.algos.AlgoProductMatrices;
import org.geogebra.common.kernel.algos.AlgoFoldFunctions;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.MyError;
import org.geogebra.common.plugin.Operation;

/**
 * Product[ list ]
 * 
 * @author Michael Borcherds
 * @version 2008-02-16
 */
public class CmdProduct extends CommandProcessor {
	/**
	 * Creates new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdProduct(Kernel kernel) {
		super(kernel);
	}

	@Override
	public GeoElement[] process(Command c)
			throws MyError, CircularDefinitionException {
		int n = c.getArgumentNumber();
		GeoElement[] arg;
		arg = resArgs(c);

		// needed for Sum[]
		if (arg.length == 0) {
			throw argNumErr(app, c.getName(), n);
		}
		if (!arg[0].isGeoList())
			throw argErr(app, c.getName(), arg[0]);
		GeoList list = (GeoList) arg[0];
		switch (n) {
		case 1:
			if (list.get(0).isMatrix()) {
				AlgoProductMatrices algo = new AlgoProductMatrices(cons,
						c.getLabel(), list);

				GeoElement[] ret = { algo.getResult() };
				return ret;
			}
			if (((GeoList) arg[0]).getGeoElementForPropertiesDialog() instanceof GeoNumberValue) {
				AlgoProduct algo = new AlgoProduct(cons, c.getLabel(), list);

				GeoElement[] ret = { algo.getResult() };
				return ret;
			}
			if (((GeoList) arg[0]).getGeoElementForPropertiesDialog() instanceof GeoFunction) {
				AlgoFoldFunctions algo = new AlgoFoldFunctions(cons,
						c.getLabel(), list, null, Operation.MULTIPLY);

				GeoElement[] ret = { algo.getResult() };
				return ret;
			}
			throw argErr(app, c.getName(), arg[0]);
		case 2:
			// Product[<List of Numbers>, <Number>]
			if (arg[1].isGeoNumeric()) {

				if (((GeoList) arg[0]).getGeoElementForPropertiesDialog() instanceof GeoNumberValue) {

					AlgoProduct algo = new AlgoProduct(cons, c.getLabel(),
							list, (GeoNumeric) arg[1]);

					GeoElement[] ret = { algo.getResult() };
					return ret;
				}
				if (((GeoList) arg[0]).getGeoElementForPropertiesDialog() instanceof GeoFunction) {
					AlgoFoldFunctions algo = new AlgoFoldFunctions(cons,
							c.getLabel(), list, (GeoNumeric) arg[1],
							Operation.MULTIPLY);

					GeoElement[] ret = { algo.getResult() };
					return ret;
				}
				throw argErr(app, c.getName(), arg[0]);
			}
			// Product[<List of Numbers>, <Frequency>]
			else if (arg[1].isGeoList()) {
				if (((GeoList) arg[0]).getGeoElementForPropertiesDialog() instanceof GeoNumberValue) {

					AlgoProduct algo = new AlgoProduct(cons, c.getLabel(),
							list, (GeoList) arg[1]);

					GeoElement[] ret = { algo.getResult() };
					return ret;
				}
				throw argErr(app, c.getName(), arg[0]);
			}
			throw argErr(app, c.getName(), arg[1]);

		default:
			throw argNumErr(app, c.getName(), n);
		}
	}

}
