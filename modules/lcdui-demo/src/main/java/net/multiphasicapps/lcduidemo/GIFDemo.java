// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.lcduidemo;

/**
 * Demos a GIF.
 *
 * @since 2021/12/05
 */
public class GIFDemo
	extends AbstractImageDemo
{
	/**
	 * Initializes the image demo.
	 *
	 * @since 2021/12/05
	 */
	public GIFDemo()
		throws NullPointerException
	{
		super(GIFDemo.class.getResourceAsStream("image.gif"));
	}
}
