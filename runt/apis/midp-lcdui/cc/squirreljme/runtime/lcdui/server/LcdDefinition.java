// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.runtime.lcdui.server;

import cc.squirreljme.runtime.cldc.service.ServiceDefinition;
import cc.squirreljme.runtime.cldc.service.ServiceServer;
import cc.squirreljme.runtime.cldc.task.SystemTask;
import cc.squirreljme.runtime.lcdui.LcdServiceCall;

/**
 * This class implements the base of the graphical LCDUI display system which
 * is used by the public facing LCDUI code to enable the use of graphics.
 *
 * @since 2018/03/15
 */
public abstract class LcdDefinition
	extends ServiceDefinition
{
	/**
	 * Initializes the base definition.
	 *
	 * @since 2018/03/15
	 */
	public LcdDefinition()
	{
		super(LcdServiceCall.Provider.class);
	}
	
	/**
	 * Initializes an implementation of the LCDUI server that clients
	 * directly interact with.
	 *
	 * @param __task The task using the service.
	 * @return The server for the task.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/16
	 */
	protected abstract LcdServer newLcdServer(SystemTask __task)
		throws NullPointerException;
	
	/**
	 * {@inheritDoc}
	 * @since 2018/03/15
	 */
	@Override
	public final ServiceServer newServer(SystemTask __task)
		throws NullPointerException
	{
		if (__task == null)
			throw new NullPointerException("NARG");
		
		return this.newLcdServer(__task);
	}
}

