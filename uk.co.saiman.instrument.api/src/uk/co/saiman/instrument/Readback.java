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

import java.util.Set;

import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

/**
 * @author Elias N Vasylenko
 *
 * @param <T>
 */
public interface Readback<T> {
	/**
	 * If the readback is not providing readings when it is not necessarily
	 * expected to, such as when it is switched off, this method should return
	 * null. If the readback is expected to be able to return values but something
	 * has gone wrong, this method should throw an exception.
	 * 
	 * @return The current value of the readback.
	 */
	T getValue();

	/**
	 * 
	 * @return
	 */
	Set<String> getStatus();

	default TypeToken<T> getDataType() {
		return TypeToken.over(getClass()).resolveSupertypeParameters(Readback.class)
				.resolveTypeArgument(new TypeParameter<T>() {}).infer();
	}
}
