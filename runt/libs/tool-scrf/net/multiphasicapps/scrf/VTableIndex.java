// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.scrf;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * This represents an index a VTable for the current class.
 *
 * @since 2019/02/05
 */
public class VTableIndex
	implements Comparable<VTableIndex>, MemorySource
{
	/** The vtable index. */
	protected final int index;
	
	/** The string representation. */
	private Reference<String> _string;
	
	/**
	 * Initializes the index.
	 *
	 * @param __i The index of the vtable entry.
	 * @since 2019/02/05
	 */
	public VTableIndex(int __i)
	{
		this.index = __i;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/02/05
	 */
	@Override
	public final int compareTo(VTableIndex __o)
	{
		return this.index - __o.index;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/02/05
	 */
	@Override
	public final boolean equals(Object __o)
	{
		if (this == __o)
			return true;
		
		if (!(__o instanceof VTableIndex))
			return false;
		
		return this.index == ((VTableIndex)__o).index;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/02/06
	 */
	@Override
	public final String toString()
	{
		Reference<String> ref = this._string;
		String rv;
		
		if (ref == null || null == (rv = ref.get()))
			this._string = new WeakReference<String>((rv = String.format(
			"VT@%d", this.index)));
		
		return rv;
	}
}

