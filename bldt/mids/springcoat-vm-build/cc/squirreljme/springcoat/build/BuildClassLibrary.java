// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.springcoat.build;

import java.io.InputStream;
import java.io.IOException;
import cc.squirreljme.springcoat.vm.SpringClassLibrary;
import net.multiphasicapps.zip.blockreader.ZipBlockReader;
import net.multiphasicapps.zip.blockreader.ZipBlockEntry;
import net.multiphasicapps.zip.blockreader.ZipEntryNotFoundException;

/**
 * This represents a single library that is layered on top of the binary
 * provided by the build system.
 *
 * @since 2018/09/13
 */
public final class BuildClassLibrary
	implements SpringClassLibrary
{
	/** The binary to source from. */
	protected final Binary binary;
	
	/**
	 * Initializes the library.
	 *
	 * @param __b The binary to wrap.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/09/13
	 */
	public BuildClassLibrary(Binary __b)
		throws NullPointerException
	{
		if (__b == null)
			throw new NullPointerException("NARG");
		
		this.binary = __b;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/09/13
	 */
	@Override
	public final String name()
	{
		return this.binary.name().toString() + ".jar";
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/09/13
	 */
	@Override
	public final InputStream resourceAsStream(String __rc)
		throws IOException, NullPointerException
	{
		if (__rc == null)
			throw new NullPointerException("NARG");
		
		throw new todo.TODO();
	}
}

