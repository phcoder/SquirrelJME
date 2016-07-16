// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.io.crc32;

import net.multiphasicapps.io.datasink.DataSink;
import net.multiphasicapps.io.datasink.SinkProcessException;

/**
 * This is a data sink which supports the CRC 32 algorithm.
 *
 * @since 2016/07/16
 */
public class CRC32DataSink
	extends DataSink
{
	/** The CRC magic number. */
	protected final int magicnumber;
	
	/**
	 * Initializes the CRC-32 data sink.
	 *
	 * @param __mn The CRC magic number.
	 * @since 2016/07/16
	 */
	public CRC32DataSink(int __mn)
	{
		// Set
		this.magicnumber = __mn;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/07/16
	 */
	@Override
	protected void process(int __n)
		throws SinkProcessException
	{
		throw new Error("TODO");
	}
}

