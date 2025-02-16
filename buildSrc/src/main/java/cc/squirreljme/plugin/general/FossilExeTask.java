// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.plugin.general;

import cc.squirreljme.plugin.util.FossilExe;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;

/**
 * Task which essentially just outputs the executable path to Fossil.
 *
 * @since 2020/06/24
 */
public class FossilExeTask
	extends DefaultTask
{
	/**
	 * Initializes the task.
	 * 
	 * @since 2020/06/24
	 */
	@Inject
	public FossilExeTask()
	{
		// Set details of this task
		this.setGroup("squirreljmeGeneral");
		this.setDescription("Prints the Fossil executable path.");
		
		// Action to perform
		this.doLast(this::action);
	}
	
	/**
	 * Performs the task action.
	 * 
	 * @param __task The called task.
	 * @since 2020/06/24
	 */
	private void action(Task __task)
	{
		__task.getLogger().lifecycle(
			FossilExe.instance().exePath().toString());
	}
}
