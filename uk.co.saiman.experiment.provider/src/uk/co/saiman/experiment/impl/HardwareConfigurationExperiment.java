/*
 * Copyright (C) 2015 Scientific Analysis Instruments Limited <contact@saiman.co.uk>
 *
 * This file is part of uk.co.saiman.instrument.provider.
 *
 * uk.co.saiman.instrument.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.saiman.instrument.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.saiman.experiment.impl;

import org.osgi.service.component.annotations.Component;

import uk.co.saiman.experiment.ExperimentType;
import uk.co.saiman.instrument.Instrument;
import uk.co.strangeskies.reflection.TypeToken;

@Component
public class HardwareConfigurationExperiment<C> implements ExperimentType<C, Instrument, Instrument> {
	private final TypeToken<C> configurationType;

	public HardwareConfigurationExperiment(TypeToken<C> hardwareConfiguration) {
		this.configurationType = hardwareConfiguration;
	}

	@Override
	public void validate(C configuration) {}

	@Override
	public Instrument process(C configuration, Instrument input) {
		return input;
	}
}
