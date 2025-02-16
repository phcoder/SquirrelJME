// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.runtime.lcdui.mle;

/**
 * Terminates the display when called.
 *
 * @since 2022/02/14
 */
final class __TerminateDisplay__
	implements AutoCloseable
{
	/**
	 * {@inheritDoc}
	 *
	 * @since 2022/02/014
	 */
	@Override
	public void close()
	{
		StaticDisplayState.terminate();
	}
}
