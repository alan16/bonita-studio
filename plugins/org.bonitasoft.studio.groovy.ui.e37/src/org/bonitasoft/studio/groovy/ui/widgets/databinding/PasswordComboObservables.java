/**
 * Copyright (C) 2009 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.groovy.ui.widgets.databinding;

import org.bonitasoft.studio.groovy.ui.widgets.PasswordCombo;
import org.eclipse.core.databinding.observable.value.IObservableValue;

public class PasswordComboObservables {

	public static IObservableValue observeText(PasswordCombo passwordCombo) {
		return new ObservablePasswordComboText(passwordCombo);
	}

	public static IObservableValue observeVisible(PasswordCombo passwordCombo) {
		return new ObservablePasswordComboVisible(passwordCombo);
	}
	
	public static IObservableValue observeEnabled(PasswordCombo passwordCombo){
		return new ObservablePasswordComboEnabled(passwordCombo);
	}

}
