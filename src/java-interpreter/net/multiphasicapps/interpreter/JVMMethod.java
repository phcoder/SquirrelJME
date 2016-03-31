// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.interpreter;

import java.util.Set;
import net.multiphasicapps.descriptors.IdentifierSymbol;
import net.multiphasicapps.descriptors.MethodSymbol;
import net.multiphasicapps.interpreter.program.VMCProgram;

/**
 * This represents an interpreted method within a class.
 *
 * @since 2016/03/01
 */
public class JVMMethod
	extends JVMMember<MethodSymbol, JVMMethodFlags>
{
	/** Lock. */
	protected final Object lock =
		new Object();	
	
	/** Is this a constructor? */
	protected final boolean isconstructor;	
	
	/** Is this a class initialization method? */
	protected final boolean isclassinit;
	
	/** The current method program. */
	private volatile VMCProgram _program;
	
	/**
	 * Initializes the interpreted method.
	 *
	 * @param __owner The class which owns this method.
	 * @param __nat The name and type of the field.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/03/01
	 */
	public JVMMethod(JVMClass __owner, JVMMemberKey<MethodSymbol> __nat)
		throws NullPointerException
	{
		super(__owner, MethodSymbol.class, __nat, JVMMethodFlags.class);
		
		// Is this a constructor?
		isconstructor = name().equals("<init>");
		isclassinit = name().equals("<clinit>");
	}
	
	/**
	 * Returns the program which describes what this method performs.
	 *
	 * @return The method's program.
	 * @since 2016/03/31
	 */
	public VMCProgram getProgram()
	{
		// Lock
		synchronized (lock)
		{
			return _program;
		}
	}
	
	/**
	 * Is this method a class initializer?
	 *
	 * @return {@code true} if a class initializer.
	 * @since 2016/03/20
	 */
	public boolean isClassInitializer()
	{
		return isclassinit;
	}
	
	/**
	 * Is this method a constructor?
	 *
	 * @return {@code true} if a constructor.
	 * @since 2016/03/20
	 */
	public boolean isConstructor()
	{
		return isconstructor;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/03/20
	 */
	@Override
	public JVMMethod setFlags(JVMMethodFlags __fl)
		throws JVMClassFormatError, NullPointerException
	{
		// Check
		if (__fl == null)
			throw new NullPointerException("NARG");
		
		// Get class flags
		JVMClassFlags cl = inclass.getFlags();
		
		// Class initializer flags are ignored for the most part
		if (!isClassInitializer())
		{
			// If the class is an interface...
			if (cl.isInterface())
			{
				// Default methods are not supported
				if (__fl.isPrivate() || !__fl.isAbstract())
					throw new JVMClassFormatError(String.format("IN19 %s",
						__fl));
				
				// Cannot have these flags
				if (__fl.isProtected() || __fl.isFinal() ||
					__fl.isSynchronized() || __fl.isNative())
					throw new JVMClassFormatError(String.format("IN1a %s %s",
						__fl, cl));
			}
			
			// If abstract, cannot have these flags
			if (__fl.isAbstract())
				if (__fl.isPrivate() || __fl.isStatic() || __fl.isFinal() ||
					__fl.isSynchronized() || __fl.isNative() ||
					__fl.isStrict())
					throw new JVMClassFormatError(String.format("IN1b %s",
						__fl));
		}
		
		// Perform super work
		return (JVMMethod)super.setFlags(__fl);
	}
	
	/**
	 * Returns the program to be used when executing this method.
	 *
	 * @param __prg The program to use.
	 * @return {@code this}.
	 * @throws NullPointerException On null arguments.
	 * @since m2016/03/31
	 */
	public JVMMethod setProgram(VMCProgram __prg)
		throws NullPointerException
	{
		// Check
		if (__prg == null)
			throw new NullPointerException("NARG");
		
		// Lock and set
		synchronized (lock)
		{
			_program = __prg;
		}
		
		// Self
		return this;
	}
}

