// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package javax.microedition.rms;

/**
 * This is thrown when the operation could not be completed because the record
 * store is full.
 *
 * @since 2017/02/26
 */
public class RecordStoreFullException
	extends RecordStoreException
{
	/**
	 * Initializes the exception with no message or cause.
	 *
	 * @since 2017/02/26
	 */
	public RecordStoreFullException()
	{
	}
	
	/**
	 * Initializes the exception with a message except without a cause.
	 *
	 * @param __m The exception message.
	 * @since 2017/02/26
	 */
	public RecordStoreFullException(String __m)
	{
		super(__m);
	}
}

