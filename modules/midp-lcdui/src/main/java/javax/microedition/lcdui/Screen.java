// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package javax.microedition.lcdui;

/**
 * This is the base class for all user interactable displays.
 *
 * It is only recommended to change the screen contents when it is not being
 * displayed.
 *
 * @since 2017/02/28
 */
public abstract class Screen
	extends Displayable
{
	/**
	 * Initializes the base screen.
	 *
	 * @since 2017/02/28
	 */
	Screen()
	{
	}
}


