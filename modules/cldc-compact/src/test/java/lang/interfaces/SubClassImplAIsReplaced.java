// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package lang.interfaces;

/**
 * Class replaces method in super class.
 *
 * @since 2021/02/14
 */
public class SubClassImplAIsReplaced
	extends ImplementsA
{
	/**
	 * {@inheritDoc}
	 * @since 2021/02/14
	 */
	@Override
	public int methodA()
	{
		return 42;
	}
}
