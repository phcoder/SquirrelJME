// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.vm.summercoat.handles;

import cc.squirreljme.jvm.summercoat.constants.MemHandleKind;
import cc.squirreljme.runtime.cldc.debug.Debugging;

/**
 * Integer array.
 *
 * @since 2021/01/17
 */
public class MemHandleArrayInteger
	extends MemHandleArray
{
	/** The array values. */
	protected final int[] values;
	
	/**
	 * Initializes a new handle.
	 *
	 * @param __id The identifier for this handle.
	 * @param __base The base array size.
	 * @param __array The array used.
	 * @throws IllegalArgumentException If the kind is not valid or the
	 * requested size is negative.
	 * @throws NullPointerException On null arguments.
	 * @since 2021/01/17
	 */
	public MemHandleArrayInteger(int __id, int __base, int[] __array)
		throws IllegalArgumentException, NullPointerException
	{
		super(__id, MemHandleKind.INTEGER_ARRAY,
			__base, 4, __array.length);
		
		this.values = __array;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/02/14
	 */
	@Override
	public int memReadInt(long __addr)
	{
		if (super.checkBase(__addr))
			return super.memReadInt(__addr);
		
		return this.values[super.calcCell(__addr)];
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/01/17
	 */
	@Override
	public void memWriteInt(long __addr, int __v)
	{
		if (super.checkBase(__addr))
			super.memWriteInt(__addr, __v);
		else
			this.values[super.calcCell(__addr)] = __v;
	}
}
