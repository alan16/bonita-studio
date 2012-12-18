/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.bonitasoft.studio.presentation;

import org.bonitasoft.studio.pics.Pics;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;


/**
 * @since 3.0
 */
public class PresentationImages {
    public static final String CLOSE = "close.png"; //$NON-NLS-1$
    public static final String CLOSE_VIEW = "close_view.gif"; //$NON-NLS-1$
    public static final String MIN_VIEW = "min_view.gif"; //$NON-NLS-1$
    public static final String MAX_VIEW = "max_view.gif"; //$NON-NLS-1$
    public static final String RESTORE_VIEW = "restore_view.gif"; //$NON-NLS-1$
    public static final String VIEW_MENU = "view_menu.gif"; //$NON-NLS-1$
    public static final String SHOW_TOOLBAR = "show_toolbar.gif"; //$NON-NLS-1$
    public static final String HIDE_TOOLBAR = "hide_toolbar.gif"; //$NON-NLS-1$
    public static final String TOOLBAR_BACKGROUND = "toolbar.gif"; //$NON-NLS-1$
    public static final String COOLBAR_BACKGROUND = "coolbar2.png"; //$NON-NLS-1$
    public static final String CLOSE_ALL = "close-all.png"; //$NON-NLS-1$


    private PresentationImages() {
    }

    public static Image getImage(String imageName) {
        return Pics.getImage(imageName);
    }

    public static ImageDescriptor getImageDescriptor(String imageName) {
        return Pics.getImageDescriptor(imageName);
    }
}
