// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.plugin.util;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.gradle.api.Task;

/**
 * This takes the output of a task and provides a {@link Callable} so that
 * it produces that single file.
 *
 * @since 2020/09/06
 */
public class SingleTaskOutputFile
	implements Callable<Path>
{
	/** The task to get. */
	protected final Task task;
	
	/**
	 * Initializes the utility.
	 * 
	 * @param __task The task to extract from.
	 * @throws NullPointerException On null arguments.
	 * @since 2020/09/06
	 */
	public SingleTaskOutputFile(Task __task)
		throws NullPointerException
	{
		if (__task == null)
			throw new NullPointerException("NARG");
		
		this.task = __task;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2020/09/06
	 */
	@Override
	public Path call()
	{
		return this.task.getOutputs().getFiles().getSingleFile().toPath();
	}
}
