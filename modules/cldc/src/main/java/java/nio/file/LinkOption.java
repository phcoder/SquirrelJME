// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package java.nio.file;

public enum LinkOption
	implements OpenOption, CopyOption
{
	NOFOLLOW_LINKS(),
	
	/** End. */
	;
	
	LinkOption()
	{
		throw new todo.TODO();
	}
}

