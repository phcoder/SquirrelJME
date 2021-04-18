// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.vm.springcoat;

import cc.squirreljme.jdwp.JDWPMethod;
import net.multiphasicapps.classfile.ByteCode;
import net.multiphasicapps.classfile.ClassName;
import net.multiphasicapps.classfile.Method;
import net.multiphasicapps.classfile.MethodFlags;
import net.multiphasicapps.classfile.MethodName;
import net.multiphasicapps.classfile.MethodNameAndType;

/**
 * This contains and stores the definition of a single method.
 *
 * @since 2018/07/22
 */
public final class SpringMethod
	implements JDWPMethod, SpringMember
{
	/** The class this technically belongs to. */
	protected final ClassName inclass;
	
	/** The backing method and its information. */
	protected final Method method;
	
	/** The file this method is in. */
	protected final String infile;
	
	/** The line table (cached). */
	private volatile int[] _lineTable;
	
	/** The method index. */
	protected final int methodIndex;
	
	/**
	 * Initializes the method representation.
	 *
	 * @param __ic The class this belongs to.
	 * @param __m The method to wrap.
	 * @param __if The file this is in.
	 * @param __dx The method index.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/09/03
	 */
	SpringMethod(ClassName __ic, Method __m, String __if, int __dx)
		throws NullPointerException
	{
		if (__ic == null || __m == null)
			throw new NullPointerException("NARG");
		
		this.inclass = __ic;
		this.method = __m;
		this.infile = __if;
		this.methodIndex = __dx;
	}
	
	/**
	 * Returns the byte code of the method.
	 *
	 * @return The method byte code.
	 * @since 2018/09/03
	 */
	public final ByteCode byteCode()
	{
		return this.method.byteCode();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/03/21
	 */
	@Override
	public byte[] debuggerByteCode()
	{
		// If there is no method byte code then ignore
		ByteCode byteCode = this.method.byteCode();
		if (byteCode == null)
			return null;
		
		return byteCode.rawByteCode();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/03/13
	 */
	@Override
	public int debuggerId()
	{
		return System.identityHashCode(this);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/03/14
	 */
	@Override
	public int[] debuggerLineTable()
	{
		// Pre-cached?
		int[] lineTable = this._lineTable;
		if (lineTable != null)
			return lineTable.clone();
		
		// If there is no method byte code then ignore
		ByteCode byteCode = this.method.byteCode();
		if (byteCode == null)
			return null;
		
		// Otherwise map each unique address to a line number
		int[] addrs = byteCode.validAddresses();
		int n = addrs.length;
		int[] rv = new int[n];
		for (int i = 0; i < n; i++)
			rv[i] = byteCode.lineOfAddress(addrs[i]);
		
		// Cache it and return a safe copy of it
		this._lineTable = rv;
		return rv.clone();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/03/17
	 */
	@Override
	public long debuggerLocationCount()
	{
		// If there is no method byte code then ignore
		ByteCode byteCode = this.method.byteCode();
		if (byteCode == null)
			return 0;
		
		return byteCode.instructionCount();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/09/09
	 */
	@Override
	public final MethodFlags flags()
	{
		return this.method.flags();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/09/03
	 */
	@Override
	public final ClassName inClass()
	{
		return this.inclass;
	}
	
	/**
	 * Returns the file this method is in.
	 *
	 * @return The file this method is in, may be {@code null}.
	 * @since 2018/09/20
	 */
	public final String inFile()
	{
		return this.infile;
	}
	
	/**
	 * Returns whether this method is abstract.
	 *
	 * @return Whether this method is abstract.
	 * @since 2018/09/03
	 */
	public final boolean isAbstract()
	{
		return this.method.flags().isAbstract();
	}
	
	/**
	 * Returns whether this is a constructor or not.
	 *
	 * @return Whether this is a constructor or not.
	 * @since 2018/09/03
	 */
	public final boolean isInstanceInitializer()
	{
		return this.method.isInstanceInitializer();
	}
	
	/**
	 * Returns if this method is static.
	 *
	 * @return {@code true} if the method is static.
	 * @since 2018/09/03
	 */
	public final boolean isStatic()
	{
		return this.method.flags().isStatic();
	}
	
	/**
	 * Returns whether this is a static initializer or not.
	 *
	 * @return Whether this is a static initializer or not.
	 * @since 2018/09/03
	 */
	public final boolean isStaticInitializer()
	{
		return this.method.isStaticInitializer();
	}
	
	/**
	 * Returns the name of this method.
	 *
	 * @return The name of this method.
	 * @since 2018/09/09
	 */
	public final MethodName name()
	{
		return this.method.name();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/09/09
	 */
	@Override
	public final MethodNameAndType nameAndType()
	{
		return this.method.nameAndType();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/12/06
	 */
	@Override
	public final String toString()
	{
		return this.inclass + "::" + this.method.nameAndType().toString();
	}
}

