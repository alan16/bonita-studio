/**
 * Copyright (C) 2010 BonitaSoft S.A.
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
package org.bonitasoft.studio.intro.content;

import java.io.PrintWriter;

import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.diagram.custom.repository.DiagramRepositoryStore;
import org.bonitasoft.studio.intro.actions.OpenSpecificProcessAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;
import org.eclipse.ui.intro.config.IIntroXHTMLContentProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author Mickael Istria
 *
 */
public class RecentProcessContentProvider implements IIntroXHTMLContentProvider {

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#init(org.eclipse.ui.intro.config.IIntroContentProviderSite)
     */
    public void init(IIntroContentProviderSite site) {


    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#createContent(java.lang.String, java.io.PrintWriter)
     */
    public void createContent(String id, PrintWriter out) {


    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#createContent(java.lang.String, org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    public void createContent(String id, Composite parent, FormToolkit toolkit) {


    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#dispose()
     */
    public void dispose() {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroXHTMLContentProvider#createContent(java.lang.String, org.w3c.dom.Element)
     */
    public void createContent(String id, Element parent) {
        DiagramRepositoryStore diagramSotre = (DiagramRepositoryStore) RepositoryManager.getInstance().getRepositoryStore(DiagramRepositoryStore.class) ;
        int nbProc = Integer.parseInt(id.split(",")[0]);
        //        final List<DiagramFileStore> allArtifacts = diagramSotre.getChildren() ;
        //        nbProc = Math.min(nbProc, allArtifacts.size());
        Document doc = parent.getOwnerDocument();
        Element ul = doc.createElement("ul");
        parent.appendChild(ul);
        if (nbProc == 0) {
            return;
        }
        //        PriorityQueue<IRepositoryFileStore> queue = new PriorityQueue<IRepositoryFileStore>(nbProc, new Comparator<IRepositoryFileStore>() {
        //            public int compare(IRepositoryFileStore arg0, IRepositoryFileStore arg1) {
        //                final long lastModifiedArg1 = arg1.getResource().getLocation().toFile().lastModified();
        //                final long lastModifiedArg0 = arg0.getResource().getLocation().toFile().lastModified();
        //                return Long.valueOf(lastModifiedArg1).compareTo(Long.valueOf(lastModifiedArg0));
        //            }
        //        });
        //        for (IRepositoryFileStore proc : allArtifacts) {
        //            if(queue.size() == nbProc) {
        //                break;
        //            }
        //            queue.add(proc);
        //        }
        //        Assert.isTrue(nbProc == queue.size());
        for (IRepositoryFileStore proc : diagramSotre.getRecentChildren(nbProc)) {
            Element li = doc.createElement("li");
            Element a = doc.createElement("a");
            li.appendChild(a);
            a.setAttribute("href", createOpenProcessHref(proc));
            String displayNameForLabelProvider = proc.getDisplayName();
            a.setAttribute("title", displayNameForLabelProvider) ;
            if (displayNameForLabelProvider.length() > 100) {
                displayNameForLabelProvider = displayNameForLabelProvider.substring(0,100 ) + "...";
            }
            Text procName = doc.createTextNode(displayNameForLabelProvider);
            a.appendChild(procName);

            ul.appendChild(li);
        }

    }

    /**
     * @param proc
     * @return
     */
    private String createOpenProcessHref(IRepositoryFileStore proc) {
        return "http://org.eclipse.ui.intro/runAction?pluginId=org.bonitasoft.studio.intro&class=" + OpenSpecificProcessAction.class.getName() + "&file=" + proc.getName();
    }

}
