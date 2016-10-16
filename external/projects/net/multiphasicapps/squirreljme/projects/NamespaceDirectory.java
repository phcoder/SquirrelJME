// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.projects;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * This contains the directory of namespaces which contain projects.
 *
 * @since 2016/10/16
 */
public class NamespaceDirectory
	extends AbstractMap<ProjectNamespace, ProjectDirectory>
{
	/**
	 * Returns a virtual directory which contains all projects in the
	 * namespace.
	 *
	 * @return The directory of all projects.
	 * @since 2016/10/16
	 */
	public final ProjectDirectory allProjects()
	{
		throw new Error("TODO");
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/10/16
	 */
	@Override
	public final Set<Map.Entry<ProjectNamespace, ProjectDirectory>> entrySet()
	{
		throw new Error("TODO");
	}
}

