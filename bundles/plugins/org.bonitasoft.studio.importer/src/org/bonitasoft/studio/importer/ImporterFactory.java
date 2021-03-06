/**
 * Copyright (C) 2010-2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 *
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
package org.bonitasoft.studio.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.importer.processors.ToProcProcessor;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

/**
 * @author Mickael Istria
 *
 */
public abstract class ImporterFactory {

	private String name;
	private String filterExtension;
	private String description;
	private int priorityDisplay;
	private Image descriptionImage;

	public abstract boolean appliesTo(String resourceName, InputStream is);

	public abstract ToProcProcessor createProcessor(String resourceName);

	public void configure(IConfigurationElement desc) {
		this.name = desc.getAttribute("inputName");
		this.filterExtension = desc.getAttribute("filterExtensions");
		this.description = desc.getAttribute("description");
		this.priorityDisplay = Integer.valueOf(desc.getAttribute("priorityDisplay"));
		String menuIcon = desc.getAttribute("menuIcon") ;
		try{
			if(menuIcon != null){
				Bundle b = Platform.getBundle(desc.getContributor().getName()) ;
				URL iconURL = b.getResource(menuIcon) ;
				File imageFile = new File(FileLocator.toFileURL(iconURL).getFile())  ;
				FileInputStream inputStream = new FileInputStream(imageFile) ;
				descriptionImage = new Image(Display.getDefault(), inputStream) ;
				inputStream.close() ;
			}
		}catch (Exception e) {
			BonitaStudioLog.error(e) ;
		}
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	public String getFilterExtensions() {
		return filterExtension;
	}

	public String getDescription() {
		return description;
	}

	public int getPriorityDisplay() {
		return priorityDisplay;
	}

	public Image getImageDescription() {
		return descriptionImage;
	}

	public boolean isEnabled(){
		return true ;
	}
}
