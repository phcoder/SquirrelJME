// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.jvm.aot.summercoat.base;

import cc.squirreljme.jvm.summercoat.constants.ClassInfoConstants;
import cc.squirreljme.jvm.summercoat.constants.PackProperty;
import cc.squirreljme.jvm.summercoat.ld.pack.JarRom;
import cc.squirreljme.jvm.summercoat.ld.pack.PackRom;
import cc.squirreljme.runtime.cldc.debug.Debugging;
import java.io.IOException;
import net.multiphasicapps.io.ChunkSection;
import net.multiphasicapps.io.ChunkWriter;

/**
 * Writer that is capable of writing anything based on table of contents
 * and headers such as {@link PackRom} and {@link JarRom}.
 *
 * @since 2021/09/03
 */
public final class StandardPackWriter
{
	/** The magic number to write. */
	protected final int magic;
	
	/** Header writer. */
	protected final HeaderStructWriter header;
	
	/** The table of contents writer. */
	protected final TableOfContentsWriter toc;
	
	/** The main chunk. */
	public final ChunkWriter mainChunk;
	
	/** The header chunk. */
	public final ChunkSection headerChunk;
	
	/** The table of contents chunk. */
	public final ChunkSection tocChunk;
	
	/**
	 * Writes a standard pack file.
	 * 
	 * @param __magic The magic number of the pack.
	 * @param __numPackProperties The number of properties to store.
	 * @param __numTocProperties The number of properties to store in a single
	 * table of contents entry.
	 * @throws IllegalArgumentException If the number of properties is zero
	 * or negative.
	 * @since 2021/09/03
	 */
	public StandardPackWriter(int __magic, int __numPackProperties,
		int __numTocProperties)
		throws IllegalArgumentException
	{
		this.magic = __magic;
		this.header = new HeaderStructWriter(__numPackProperties);
		this.toc = new TableOfContentsWriter(__numTocProperties);
		
		// Setup chunk where everything is written to
		ChunkWriter mainChunk = new ChunkWriter();
		this.mainChunk = mainChunk;
		
		// Sections for chunks
		this.headerChunk = mainChunk.addSection(
			ChunkWriter.VARIABLE_SIZE, 8);
		this.tocChunk = mainChunk.addSection(
			ChunkWriter.VARIABLE_SIZE, 8);
	}
	
	/**
	 * Returns the header for writing.
	 * 
	 * @return The header to write into.
	 * @since 2021/09/03
	 */
	public HeaderStructWriter header()
	{
		return this.header;
	}
	
	/**
	 * Initializes writing the pack file.
	 * 
	 * @throws IOException On write errors.
	 * @since 2021/09/06
	 */
	public void initialize()
		throws IOException
	{
		// Store shared header information
		ChunkUtils.storeCommonSharedHeader(this.headerChunk,
			this.magic,
			ClassInfoConstants.CLASS_VERSION_20201129,
			this.header.properties.count());
		
		// The first property is always the version
		this.header.set(0, ClassInfoConstants.CLASS_VERSION_20201129);
	}
	
	/**
	 * Returns the table of contents.
	 * 
	 * @return The table of contents.
	 * @since 2021/09/05
	 */
	public TableOfContentsWriter toc()
	{
		return this.toc;
	}
}
