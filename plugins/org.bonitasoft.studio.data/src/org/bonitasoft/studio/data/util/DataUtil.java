/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.studio.data.util;


import java.net.URLClassLoader;
import java.util.Date;

import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.data.i18n.Messages;
import org.bonitasoft.studio.model.process.AbstractProcess;
import org.bonitasoft.studio.model.process.BooleanType;
import org.bonitasoft.studio.model.process.Data;
import org.bonitasoft.studio.model.process.DataType;
import org.bonitasoft.studio.model.process.DateType;
import org.bonitasoft.studio.model.process.DoubleType;
import org.bonitasoft.studio.model.process.EnumType;
import org.bonitasoft.studio.model.process.FloatType;
import org.bonitasoft.studio.model.process.IntegerType;
import org.bonitasoft.studio.model.process.JavaObjectData;
import org.bonitasoft.studio.model.process.JavaType;
import org.bonitasoft.studio.model.process.LongType;
import org.bonitasoft.studio.model.process.StringType;
import org.bonitasoft.studio.model.process.XMLType;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.w3c.dom.Document;

/**
 * @author Romain Bioteau
 *
 */
public class DataUtil {

    /**
     * @param text
     * @return
     */
    public static String getTechnicalTypeFor(AbstractProcess container,String type) {
        StringBuilder sb = new StringBuilder();
        type = ModelHelper.getDataTypeID(type);
        for (DataType t : (container).getDatatypes()) {
            if (t.getName().equals(type)) {
                sb.append(Messages.dataTechnicalTypeLabel);
                sb.append(' ');
                if(t instanceof IntegerType) {
                    sb.append(Integer.class.getName());
                }else if(t instanceof DoubleType)  {
                    sb.append(Double.class.getName());
                }else if(t instanceof LongType)  {
                    sb.append(Long.class.getName());
                } else if(t instanceof FloatType)  {
                    sb.append(Float.class.getName());
                } else if(t instanceof DateType)  {
                    sb.append(Date.class.getName());
                } else if(t instanceof StringType)  {
                    sb.append(String.class.getName());
                } else if(t instanceof BooleanType)  {
                    sb.append(Boolean.class.getName());
                }else if(t instanceof XMLType)  {
                    sb.append(Document.class.getName());
                }else if(t instanceof EnumType)  {
                    sb.append(String.class.getName());
                } else {
                    return "";
                }
                break;
            }
        }
        return sb.toString();
    }


    public static Class<?> getClassForData(Data data) {
        Class<?> clazz = null;
        final DataType type = data.getDataType();
        if (type instanceof DateType) {
            clazz = Date.class;
        } else if (type instanceof StringType) {
            clazz = String.class;
        } else if (type instanceof IntegerType) {
            clazz = Long.class;
        } else if (type instanceof FloatType) {
            clazz = Float.class;
        } else if (type instanceof BooleanType) {
            clazz = Boolean.class;
        } else if (type instanceof JavaType) {
            try {
                IJavaProject project = RepositoryManager.getInstance().getCurrentRepository().getJavaProject() ;
                IType javaType = project.findType(((JavaObjectData) data).getClassName()) ;
                if(javaType != null){
                    final URLClassLoader urlClassLoader = RepositoryManager.getInstance().getCurrentRepository().createProjectClassloader() ;
                    clazz = urlClassLoader.loadClass(((JavaObjectData) data).getClassName()) ;

                }else{
                    clazz = Object.class;
                }
            } catch (Exception ex) {
                BonitaStudioLog.error(ex);
                clazz = Object.class;
            }
        } else if (type instanceof EnumType) {
            clazz = String.class;
        } else if (type instanceof XMLType) {
            clazz = Document.class;
        }

        return clazz ;
    }

}
