/*******************************************************************************
 * Copyright (c) 2013 Angelo ZERR.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:      
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.angularjs.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.angularjs.internal.core.AngularCorePlugin;
import org.eclipse.angularjs.internal.core.Trace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;

import tern.angular.modules.AngularModulesManager;
import tern.angular.modules.Directive;
import tern.eclipse.ide.core.IDETernProject;
import tern.eclipse.ide.core.scriptpath.ITernScriptPath;
import tern.server.ITernServer;

/**
 * Angular project.
 * 
 */
public class AngularProject {

	private static final QualifiedName ANGULAR_PROJECT = new QualifiedName(
			AngularCorePlugin.PLUGIN_ID + ".sessionprops", "AngularProject");

	private final IProject project;

	private final Map<ITernScriptPath, List<BaseModel>> folders;

	private final CustomAngularModulesRegistry customDirectives;

	AngularProject(IProject project) throws CoreException {
		this.project = project;
		this.folders = new HashMap<ITernScriptPath, List<BaseModel>>();
		this.customDirectives = new CustomAngularModulesRegistry(project);
		AngularModulesManager.getInstance().addRegistry(project,
				customDirectives);
		project.setSessionProperty(ANGULAR_PROJECT, this);
	}

	public static AngularProject getAngularProject(IProject project)
			throws CoreException {
		if (!hasAngularNature(project)) {
			throw new CoreException(
					new Status(IStatus.ERROR, AngularCorePlugin.PLUGIN_ID,
							"The project " + project.getName()
									+ " is not an angular project."));
		}
		AngularProject angularProject = (AngularProject) project
				.getSessionProperty(ANGULAR_PROJECT);
		if (angularProject == null) {
			angularProject = new AngularProject(project);
		}
		return angularProject;
	}

	public IProject getProject() {
		return project;
	}

	public static IDETernProject getTernProject(IProject project)
			throws CoreException {
		return IDETernProject.getTernProject(project);
	}

	/**
	 * Return true if the given project have angular nature
	 * "org.eclipse.angularjs.core.angularnature" and false otherwise.
	 * 
	 * @param project
	 *            Eclipse project.
	 * @return true if the given project have angular nature
	 *         "org.eclipse.angularjs.core.angularnature" and false otherwise.
	 */
	public static boolean hasAngularNature(IProject project) {
		try {
			return project.hasNature(AngularNature.ID);
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Error angular nature", e);
			return false;
		}
	}

	public Collection<BaseModel> getFolders(ITernScriptPath scriptPath) {
		List<BaseModel> folders = this.folders.get(scriptPath);
		if (folders == null) {
			folders = new ArrayList<BaseModel>();
			this.folders.put(scriptPath, folders);
			folders.add(new ScriptsFolder(scriptPath));
			folders.add(new ModulesFolder(scriptPath));
		}
		return folders;
	}

	public void cleanModel() {
		this.folders.clear();
	}

	public Directive getDirective(String tagName, String name) {
		return AngularModulesManager.getInstance().getDirective(project,
				tagName, name);
	}
}
