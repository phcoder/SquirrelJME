// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.vm.summercoat;

import cc.squirreljme.emulator.vm.VMException;
import cc.squirreljme.jvm.summercoat.SummerCoatUtil;
import cc.squirreljme.jvm.summercoat.constants.MemHandleKind;
import cc.squirreljme.runtime.cldc.debug.Debugging;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This represents a basic memory handle.
 *
 * @since 2020/11/28
 */
public class MemHandle
	extends AbstractWritableMemory
{
	/** The identifier of the handle. */
	protected final int id;
	
	/** The kind of handle this is. */
	protected final int kind;
	
	/** The size of this handle. */
	protected final int size;
	
	/** The raw byte size of this handle. */
	protected final int rawSize;
	
	/** The handle count. */
	private final AtomicInteger _count =
		new AtomicInteger();
	
	/** The raw handle byte data. */
	private final byte[] _bytes;
	
	/**
	 * Initializes a new handle.
	 * 
	 * @param __id The identifier for this handle.
	 * @param __kind The kind of memory handle to allocate.
	 * @param __byteSize The number of bytes to allocate.
	 * @param __rawSize The raw size of the handle.
	 * @throws IllegalArgumentException If the kind is not valid or the
	 * requested size is negative.
	 * @since 2021/01/17
	 */
	public MemHandle(int __id, int __kind, int __byteSize, int __rawSize)
		throws IllegalArgumentException
	{
		if (__kind <= 0 || __kind >= MemHandleKind.NUM_KINDS)
			throw new IllegalArgumentException("Invalid kind: " + __kind);
		if (__byteSize < 0 || __rawSize < 0)
			throw new IllegalArgumentException("Negative allocation size.");
		if (!SummerCoatUtil.isArrayKind(__kind) && __byteSize != __rawSize) 
			throw new IllegalArgumentException("Byte/raw size mismatch");
		if (__byteSize < __rawSize)
			throw new IllegalArgumentException("Byte size smaller than raw.");
		
		this.id = __id;
		this.kind = __kind;
		this.size = __byteSize;
		this.rawSize = __rawSize;
		this._bytes = new byte[__rawSize];
	}
	
	/**
	 * Changes the count on the memory handle.
	 * 
	 * @param __up Count this handle up?
	 * @return The new count.
	 * @since 2020/11/28
	 */
	public final int count(boolean __up)
	{
		if (__up)
			return this._count.incrementAndGet();
		return this._count.decrementAndGet();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/01/17
	 */
	@Override
	public int memReadByte(int __addr)
	{
		if (__addr < 0 || __addr >= this.rawSize)
			throw new VMMemoryAccessException("Invalid memReadByte: " + __addr);
		
		return this._bytes[__addr] & 0xFF;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/01/17
	 */
	@Override
	public final int memRegionOffset()
	{
		return 0;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/01/17
	 */
	@Override
	public final int memRegionSize()
	{
		return this.size;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/01/17
	 */
	@Override
	public void memWriteByte(int __addr, int __v)
	{
		if (__addr < 0 || __addr >= this.rawSize)
			throw new VMMemoryAccessException(String.format(
				"Invalid memWriteByte: @%#08x (sz: %d)",
				__addr, this.rawSize));
		
		this._bytes[__addr] = (byte)__v;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/01/17
	 */
	@Override
	public void memWriteBytes(int __addr, byte[] __b, int __o, int __l)
		throws IndexOutOfBoundsException, NullPointerException
	{
		if (__b == null)
			throw new NullPointerException("NARG");
		if (__o < 0 || __l < 0 || (__o + __l) > __b.length)
			throw new IndexOutOfBoundsException("IOOB");
		
		// Out of bounds?
		int size = this.size;
		int endAddr = __addr + __l;
		if (__addr < 0 || endAddr > size)
			throw new VMMemoryAccessException("Invalid memWriteByte: " + __addr);
		
		// Split off to the special region, if there is one?
		int rawSize = this.rawSize;
		if (endAddr > rawSize)
		{
			// Write special region
			int diff = Math.max(0, rawSize - __addr);
			this.specialWriteBytes(Math.max(__addr, rawSize),
				__b, __o + diff, __l - diff);
			
			// Write normal area, do not write past the raw area
			super.memWriteBytes(__addr,
				__b, __o, Math.min(__l, rawSize - __addr));
		}
		
		// Use normal non-special writing
		else
			super.memWriteBytes(__addr, __b, __o, __l);
	}
	
	/**
	 * Special byte writing methods, used for arrays.
	 * 
	 * @param __addr The address to write to.
	 * @param __b The data to write.
	 * @param __o The offset.
	 * @param __l The length.
	 * @throws IndexOutOfBoundsException If the offset and/or length are
	 * negative or exceed the array bounds.
	 * @throws NullPointerException On null arguments.
	 * @since 2021/01/17
	 */
	protected void specialWriteBytes(int __addr, byte[] __b, int __o, int __l)
		throws IndexOutOfBoundsException, NullPointerException
	{
		throw new VMMemoryAccessException("Invalid specialWriteBytes: " + __addr);
	}
	
	/**
	 * Sets the explicit memory handle count.
	 * 
	 * @param __count The count to set to.
	 * @since 2021/01/17
	 */
	public final void setCount(int __count)
	{
		this._count.set(__count);
	}
}
