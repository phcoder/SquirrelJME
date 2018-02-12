// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.kernel.lib;

/**
 * Packet types for the library.
 *
 * @since 2018/01/05
 */
public interface LibrariesPacketTypes
{
	/** List programs which are available. */
	public static final int LIST_PROGRAMS =
		1;
	
	/** Install a program. */
	public static final int INSTALL_PROGRAM =
		2;
	
	/** Load bytes from a resource. */
	public static final int LOAD_RESOURCE_BYTES =
		3;
}

