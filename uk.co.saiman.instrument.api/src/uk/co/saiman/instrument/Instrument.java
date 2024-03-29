/*
 * Copyright (C) 2015 Scientific Analysis Instruments Limited <contact@saiman.co.uk>
 *
 * This file is part of uk.co.saiman.instrument.api.
 *
 * uk.co.saiman.instrument.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.saiman.instrument.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.saiman.instrument;

/**
 * An instrument is essentially the aggregation of a collection of
 * {@link HardwareModule}s, and is responsible for supervising their lifecycles
 * and interactions as part of the collected whole.
 * 
 * @author Elias N Vasylenko
 *
 */
public interface Instrument {
	/**
	 * An instrument has a 1 to 1 relationship with a lifecycle state. When
	 * transitions between states are requested of an instrument, the action is
	 * delegated to lifecycle participants registered with that instrument.
	 * 
	 * @author Elias N Vasylenko
	 *
	 */
	public enum InstrumentLifecycleState {
		/**
		 * Instrument is in idle state.
		 */
		STANDBY,

		/**
		 * Make sure vacuum is ready and ramp up voltages, etc.
		 */
		BEGIN_OPERATION,

		/**
		 * Whilst operating, experiments may be processed.
		 */
		OPERATING,

		/**
		 * Ramp down voltages, disengage any operating hardware, etc.
		 */
		END_OPERATION
	}

	boolean operate();

	void standby();

	InstrumentLifecycleState state();

	void registerLifecycleParticipant(InstrumentLifecycleParticipant participant);

	void unregisterLifecycleParticipant(InstrumentLifecycleParticipant participant);
}
