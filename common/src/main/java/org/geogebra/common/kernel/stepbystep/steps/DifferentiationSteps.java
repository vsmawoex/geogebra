package org.geogebra.common.kernel.stepbystep.steps;

import static org.geogebra.common.kernel.stepbystep.steptree.StepExpression.nonTrivialPower;
import static org.geogebra.common.kernel.stepbystep.steptree.StepNode.differentiate;
import static org.geogebra.common.kernel.stepbystep.steptree.StepNode.divide;
import static org.geogebra.common.kernel.stepbystep.steptree.StepNode.logarithm;
import static org.geogebra.common.kernel.stepbystep.steptree.StepNode.minus;
import static org.geogebra.common.kernel.stepbystep.steptree.StepNode.multiply;
import static org.geogebra.common.kernel.stepbystep.steptree.StepNode.power;
import static org.geogebra.common.kernel.stepbystep.steptree.StepNode.root;
import static org.geogebra.common.kernel.stepbystep.steptree.StepNode.subtract;

import org.geogebra.common.kernel.stepbystep.solution.SolutionBuilder;
import org.geogebra.common.kernel.stepbystep.solution.SolutionStepType;
import org.geogebra.common.kernel.stepbystep.steptree.StepConstant;
import org.geogebra.common.kernel.stepbystep.steptree.StepExpression;
import org.geogebra.common.kernel.stepbystep.steptree.StepNode;
import org.geogebra.common.kernel.stepbystep.steptree.StepOperation;
import org.geogebra.common.kernel.stepbystep.steptree.StepVariable;
import org.geogebra.common.plugin.Operation;

public enum DifferentiationSteps implements SimplificationStepGenerator {
	
	DIFFERENTIATE_SUM {
		@Override
		public StepNode apply(StepNode sn, SolutionBuilder sb, RegroupTracker tracker) {
			if (sn.isOperation(Operation.DIFF)) {
				StepOperation so = (StepOperation) sn;
				
				if (so.getSubTree(0).isOperation(Operation.PLUS)) {
					StepOperation sum = (StepOperation) so.getSubTree(0);
					StepVariable variable = (StepVariable) so.getSubTree(1);

					StepOperation result = new StepOperation(Operation.PLUS);
					for(StepExpression subtree : sum) {
						subtree.setColor(tracker.incColorTracker());
						result.addSubTree(differentiate(subtree, variable));
					}

					sb.add(SolutionStepType.DIFF_SUM);

					return result;
				}
			}
			
			return StepStrategies.iterateThrough(this, sn, sb, tracker);
		}
	},
	
	DIFFERENTIATE_CONSTANT {
		@Override
		public StepNode apply(StepNode sn, SolutionBuilder sb, RegroupTracker tracker) {
			if (sn.isOperation(Operation.DIFF)) {
				StepOperation so = (StepOperation) sn;

				StepOperation toDifferentiate = (StepOperation) so.getSubTree(0);
				StepVariable variable = (StepVariable) so.getSubTree(1);

				if (toDifferentiate.isConstantIn(variable)) {
					StepConstant result = new StepConstant(1);

					toDifferentiate.setColor(tracker.getColorTracker());
					result.setColor(tracker.incColorTracker());
					sb.add(SolutionStepType.DIFF_CONSTANT);

					return result;
				}
			}

			return StepStrategies.iterateThrough(this, sn, sb, tracker);
		}
	},

	CONSTANT_COEFFICIENT {
		@Override
		public StepNode apply(StepNode sn, SolutionBuilder sb, RegroupTracker tracker) {
			if (sn.isOperation(Operation.DIFF)) {
				StepOperation so = (StepOperation) sn;
				
				if (so.getSubTree(0).isOperation(Operation.MULTIPLY)) {
					StepOperation product = (StepOperation) so.getSubTree(0);
					StepVariable variable = (StepVariable) so.getSubTree(1);

					StepExpression constantCoefficient = product.getCoefficientIn(variable);
					
					if (constantCoefficient != null) {
						constantCoefficient.setColor(tracker.incColorTracker());
						StepExpression nonConstant = product.getVariableIn(variable);

						StepExpression result = multiply(constantCoefficient, differentiate(nonConstant, variable));

						sb.add(SolutionStepType.DIFF_CONSTANT_COEFFICIENT);

						return result;
					}
				}
			}

			return StepStrategies.iterateThrough(this, sn, sb, tracker);
		}
	},

	DIFFERENTIATE_PRODUCT {
		@Override
		public StepNode apply(StepNode sn, SolutionBuilder sb, RegroupTracker tracker) {
			if (sn.isOperation(Operation.DIFF)) {
				StepOperation so = (StepOperation) sn;

				if (so.getSubTree(0).isOperation(Operation.MULTIPLY)) {
					StepOperation product = (StepOperation) so.getSubTree(0);
					StepVariable variable = (StepVariable) so.getSubTree(1);

					StepExpression firstPart = product.getSubTree(0);
					StepExpression secondPart = new StepOperation(Operation.MULTIPLY);

					for (int i = 1; i < product.noOfOperands(); i++) {
						((StepOperation) secondPart).addSubTree(product.getSubTree(i));
					}

					if (((StepOperation) secondPart).noOfOperands() == 1) {
						secondPart = ((StepOperation) secondPart).getSubTree(0);
					}

					firstPart.setColor(tracker.incColorTracker());
					secondPart.setColor(tracker.incColorTracker());

					StepOperation result = new StepOperation(Operation.PLUS);
					result.addSubTree(multiply(firstPart, differentiate(secondPart, variable)));
					result.addSubTree(multiply(differentiate(firstPart, variable), secondPart));

					sb.add(SolutionStepType.DIFF_PRODUCT);

					return result;
				}
			}
			
			return StepStrategies.iterateThrough(this, sn, sb, tracker);
		}
	},

	DIFFERENTIATE_FRACTION {
		@Override
		public StepNode apply(StepNode sn, SolutionBuilder sb, RegroupTracker tracker) {
			if (sn.isOperation(Operation.DIFF)) {
				StepOperation so = (StepOperation) sn;

				if (so.getSubTree(0).isOperation(Operation.DIVIDE)) {
					StepOperation fraction = (StepOperation) so.getSubTree(0);
					StepVariable variable = (StepVariable) so.getSubTree(1);

					StepExpression numerator = fraction.getSubTree(0);
					StepExpression denominator = fraction.getSubTree(1);
					
					numerator.setColor(tracker.incColorTracker());
					denominator.setColor(tracker.incColorTracker());
					
					StepExpression resultNumerator = subtract(
							multiply(differentiate(numerator, variable), denominator),
							multiply(numerator, differentiate(denominator, variable)));

					StepExpression result = divide(resultNumerator, power(denominator, 2));

					sb.add(SolutionStepType.DIFF_FRACTION);

					return result;
				}
			}

			return StepStrategies.iterateThrough(this, sn, sb, tracker);
		}
	},

	DIFFERENTIATE_POLYNOMIAL {
		@Override
		public StepNode apply(StepNode sn, SolutionBuilder sb, RegroupTracker tracker) {
			if (sn.isOperation(Operation.DIFF)) {
				StepOperation so = (StepOperation) sn;

				if (so.getSubTree(0).equals(so.getSubTree(1))) {
					StepExpression result = new StepConstant(1);

					so.getSubTree(0).setColor(tracker.getColorTracker());
					result.setColor(tracker.getColorTracker());

					sb.add(SolutionStepType.DIFF_VARIABLE, tracker.incColorTracker());

					return result;
				}

				if (so.getSubTree(0).isOperation(Operation.POWER)) {
					StepOperation power = (StepOperation) so.getSubTree(0);
					StepVariable variable = (StepVariable) so.getSubTree(1);

					StepExpression base = power.getSubTree(0);
					StepExpression exponent = power.getSubTree(1);

					if (exponent.isConstantIn(variable)) {
						base.setColor(tracker.incColorTracker());
						exponent.setColor(tracker.incColorTracker());

						StepExpression result = multiply(exponent, nonTrivialPower(base, exponent.getValue() - 1));

						if (base.equals(variable)) {
							sb.add(SolutionStepType.DIFF_POWER);
						} else {
							result = multiply(result, differentiate(base, variable));
							sb.add(SolutionStepType.DIFF_POWER_CHAIN);
						}

						return result;
					}
				}
			}

			return StepStrategies.iterateThrough(this, sn, sb, tracker);
		}
	},

	DIFFERENTIATE_EXPONENTIAL {
		@Override
		public StepNode apply(StepNode sn, SolutionBuilder sb, RegroupTracker tracker) {
			if (sn.isOperation(Operation.DIFF)) {
				StepOperation so = (StepOperation) sn;

				if (so.getSubTree(0).isOperation(Operation.POWER)) {
					StepOperation power = (StepOperation) so.getSubTree(0);
					StepVariable variable = (StepVariable) so.getSubTree(1);

					StepExpression base = power.getSubTree(0);
					StepExpression exponent = power.getSubTree(1);

					if (base.equals(StepConstant.E)) {
						power.setColor(tracker.getColorTracker());
						
						StepExpression result = power;

						if (exponent.equals(variable)) {
							sb.add(SolutionStepType.DIFF_EXPONENTIAL_E, tracker.incColorTracker());
						} else {
							result = multiply(result, differentiate(exponent, variable));
							sb.add(SolutionStepType.DIFF_EXPONENTIAL_E_CHAIN, tracker.incColorTracker());
						}

						return result;
					}

					base.setColor(tracker.incColorTracker());
					exponent.setColor(tracker.incColorTracker());

					if (base.isConstantIn(variable)) {
						StepExpression result = multiply(logarithm(Math.E, base), power);

						if (exponent.equals(variable)) {
							sb.add(SolutionStepType.DIFF_EXPONENTIAL);
						} else {
							result = multiply(result, differentiate(exponent, variable));
							sb.add(SolutionStepType.DIFF_EXPONENTIAL_CHAIN);
						}
						
						return result;
					} else if (!exponent.isConstantIn(variable)) {
						StepExpression result = power(StepConstant.E,
								multiply(logarithm(StepConstant.E, base), exponent));
						sb.add(SolutionStepType.REWRITE_AS, power, result);

						return result;
					}
				}
			}

			return StepStrategies.iterateThrough(this, sn, sb, tracker);
		}
	},

	DIFFERENTIATE_ROOT {
		@Override
		public StepNode apply(StepNode sn, SolutionBuilder sb, RegroupTracker tracker) {
			if (sn.isOperation(Operation.DIFF)) {
				StepOperation so = (StepOperation) sn;

				if (so.getSubTree(0).isOperation(Operation.NROOT)) {
					StepOperation root = (StepOperation) so.getSubTree(0);
					StepVariable variable = (StepVariable) so.getSubTree(1);

					StepExpression base = root.getSubTree(0);
					StepExpression exponent = root.getSubTree(1);
					
					base.setColor(tracker.incColorTracker());
					exponent.setColor(tracker.incColorTracker());

					StepExpression result = divide(1,
							multiply(exponent, root(nonTrivialPower(base, exponent.getValue() - 1), exponent)));

					if (base.equals(variable)) {
						sb.add(SolutionStepType.DIFF_ROOT);
					} else {
						result = multiply(result, differentiate(base, variable));
						sb.add(SolutionStepType.DIFF_ROOT_CHAIN);
					}

					return result;
				}
			}

			return StepStrategies.iterateThrough(this, sn, sb, tracker);
		}
	},

	DIFFERENTIATE_LOG {
		@Override
		public StepNode apply(StepNode sn, SolutionBuilder sb, RegroupTracker tracker) {
			if (sn.isOperation(Operation.DIFF)) {
				StepOperation so = (StepOperation) sn;

				if (so.getSubTree(0).isOperation(Operation.LOG)) {
					StepOperation logarithm = (StepOperation) so.getSubTree(0);
					StepVariable variable = (StepVariable) so.getSubTree(1);
					
					StepExpression base = logarithm.getSubTree(0);
					StepExpression argument = logarithm.getSubTree(1);

					if (logarithm.isNaturalLog()) {
						StepExpression result = divide(1, argument);

						logarithm.setColor(tracker.getColorTracker());
						result.setColor(tracker.getColorTracker());

						if (argument.equals(variable)) {
							sb.add(SolutionStepType.DIFF_NATURAL_LOG, tracker.incColorTracker());
						} else {
							result = multiply(result, differentiate(argument, variable));
							sb.add(SolutionStepType.DIFF_NATURAL_LOG_CHAIN, tracker.incColorTracker());
						}

						return result;
					}

					base.setColor(tracker.incColorTracker());
					argument.setColor(tracker.incColorTracker());
					
					StepExpression result = divide(1, multiply(logarithm(StepConstant.E, base), argument));

					if (argument.equals(variable)) {
						sb.add(SolutionStepType.DIFF_LOG);
					} else {
						result = multiply(result, differentiate(argument, variable));
						sb.add(SolutionStepType.DIFF_LOG_CHAIN);
					}

					return result;
				}
			}

			return StepStrategies.iterateThrough(this, sn, sb, tracker);
		}
	},

	DIFFERENTIATE_TRIGO {
		@Override
		public StepNode apply(StepNode sn, SolutionBuilder sb, RegroupTracker tracker) {
			if (sn.isOperation(Operation.DIFF)) {
				StepOperation so = (StepOperation) sn;

				if (so.getSubTree(0).isTrigonometric()) {
					StepOperation trigo = (StepOperation) so.getSubTree(0);
					StepVariable variable = (StepVariable) so.getSubTree(1);

					StepExpression argument = trigo.getSubTree(0);

					StepExpression result = null;
					if (trigo.isOperation(Operation.SIN)) {
						result = StepNode.apply(argument, Operation.COS);

						if (argument.equals(variable)) {
							sb.add(SolutionStepType.DIFF_SIN, tracker.getColorTracker());
						} else {
							sb.add(SolutionStepType.DIFF_SIN_CHAIN, tracker.getColorTracker());
						}
					} else if (trigo.isOperation(Operation.COS)) {
						result = minus(StepNode.apply(argument, Operation.SIN));

						if (argument.equals(variable)) {
							sb.add(SolutionStepType.DIFF_COS, tracker.getColorTracker());
						} else {
							sb.add(SolutionStepType.DIFF_COS_CHAIN, tracker.getColorTracker());
						}
					} else if(trigo.isOperation(Operation.TAN)) {
						result = divide(1, power(StepNode.apply(argument, Operation.COS), 2));

						if (argument.equals(variable)) {
							sb.add(SolutionStepType.DIFF_TAN, tracker.getColorTracker());
						} else {
							sb.add(SolutionStepType.DIFF_TAN_CHAIN, tracker.getColorTracker());
						}
					}

					if (!argument.equals(variable)) {
						result = multiply(result, differentiate(argument, variable));
					}

					if (result != null) {
						trigo.setColor(tracker.getColorTracker());
						result.setColor(tracker.incColorTracker());

						return result;
					}
				}
			}

			return StepStrategies.iterateThrough(this, sn, sb, tracker);
		}
	},

	DIFFERENTIATE_INVERSE_TRIGO {
		@Override
		public StepNode apply(StepNode sn, SolutionBuilder sb, RegroupTracker tracker) {
			if (sn.isOperation(Operation.DIFF)) {
				StepOperation so = (StepOperation) sn;

				if (so.getSubTree(0).isInverseTrigonometric()) {
					StepOperation trigo = (StepOperation) so.getSubTree(0);
					StepVariable variable = (StepVariable) so.getSubTree(1);

					StepExpression argument = trigo.getSubTree(0);

					StepExpression result = null;
					if (trigo.isOperation(Operation.ARCSIN)) {
						result = divide(1, root(subtract(1, power(argument, 2)), 2));

						if (argument.equals(variable)) {
							sb.add(SolutionStepType.DIFF_ARCSIN, tracker.getColorTracker());
						} else {
							sb.add(SolutionStepType.DIFF_ARCSIN_CHAIN, tracker.getColorTracker());
						}
					} else if (trigo.isOperation(Operation.ARCCOS)) {
						result = minus(divide(1, root(subtract(1, power(argument, 2)), 2)));

						if (argument.equals(variable)) {
							sb.add(SolutionStepType.DIFF_ARCCOS, tracker.getColorTracker());
						} else {
							sb.add(SolutionStepType.DIFF_ARCCOS_CHAIN, tracker.getColorTracker());
						}
					} else if (trigo.isOperation(Operation.ARCTAN)) {
						result = divide(1, StepNode.add(power(argument, 2), 1));

						if (argument.equals(variable)) {
							sb.add(SolutionStepType.DIFF_ARCTAN, tracker.getColorTracker());
						} else {
							sb.add(SolutionStepType.DIFF_ARCTAN_CHAIN, tracker.getColorTracker());
						}
					}
					

					if (!argument.equals(variable)) {
						result = multiply(result, differentiate(argument, variable));
					}


					if (result != null) {
						trigo.setColor(tracker.getColorTracker());
						result.setColor(tracker.incColorTracker());

						return result;
					}
				}
			}

			return StepStrategies.iterateThrough(this, sn, sb, tracker);
		}
	};

	public int type() {
		return 0;
	}
}
