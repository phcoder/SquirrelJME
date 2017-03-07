// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.jit.mips;

import java.util.Arrays;
import net.multiphasicapps.squirreljme.jit.Binding;

/**
 * This is the base interface for bindings for the MIPS JIT.
 *
 * @since 2017/03/03
 */
public abstract class MIPSBinding
	implements Binding
{
	/**
	 * Gets the register by the given index.
	 *
	 * @param __i The register index.
	 * @return The register at the given index.
	 * @throws IndexOutOfBoundsException If the index is out of bounds.
	 * @since 2017/03/07
	 */
	public abstract MIPSRegister getRegister(int __i)
		throws IndexOutOfBoundsException;
	
	/**
	 * Returns the number of registers which are used.
	 *
	 * @return The used register count.
	 * @since 2017/03/07
	 */
	public abstract int numRegisters();
	
	/**
	 * Returns the currently assigned registers.
	 *
	 * @return The assigned registers.
	 * @since 2017/03/03
	 */
	public abstract MIPSRegister[] registers();
	
	/**
	 * Returns the length of the value on the stack.
	 *
	 * @return The stack length or {@link Integer#MIN_VALUE} if it is not
	 * valid.
	 * @since 2017/03/03
	 */
	public abstract int stackLength();
	
	/**
	 * Returns the offset on the stack the value will be placed.
	 *
	 * @return The stack offset or {@link Integer#MIN_VALUE} if it is not
	 * valid.
	 * @since 2017/03/03
	 */
	public abstract int stackOffset();
	
	/**
	 * {@inheritDoc}
	 * @since 2017/02/23
	 */
	@Override
	public boolean equals(Object __o)
	{
		// Check
		if (!(__o instanceof MIPSBinding))
			return false;
		
		MIPSBinding o = (MIPSBinding)__o;
		return this.stackOffset() == o.stackOffset() &&
			this.stackLength() == o.stackLength() &&
			Arrays.equals(this.registers(), o.registers());
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2017/02/23
	 */
	@Override
	public int hashCode()
	{
		int rv = 0;
		MIPSRegister[] registers = this.registers();
		for (int i = 0, n = registers.length; i < n; i++)
			rv ^= registers[i].hashCode();
		return rv ^ this.stackOffset() ^ (~this.stackLength());
	}
	
	/**
	 * Checks if this binding uses the specified register.
	 *
	 * @param __r The register to check.
	 * @throws NullPointerException On null arguments.
	 * @since 2017/03/07
	 */
	public boolean usesRegister(MIPSRegister __r)
		throws NullPointerException
	{
		// Check
		if (__r == null)
			throw new NullPointerException("NARG");
		
		// Go through all registers
		for (int i = 0, n = numRegisters(); i < n; i++)
			if (__r.equals(getRegister(i)))
				return true;
		
		// Not found
		return false;
	}
}

