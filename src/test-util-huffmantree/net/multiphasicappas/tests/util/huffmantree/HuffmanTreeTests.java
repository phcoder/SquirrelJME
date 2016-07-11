// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.tests.util.huffmantree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import net.multiphasicapps.io.bits.BitCompactor;
import net.multiphasicapps.io.bits.BitInputStream;
import net.multiphasicapps.tests.InvalidTestException;
import net.multiphasicapps.tests.TestChecker;
import net.multiphasicapps.tests.TestGroupName;
import net.multiphasicapps.tests.TestInvoker;
import net.multiphasicapps.util.huffmantree.HuffmanTree;

/**
 * This contains tests for the huffman tree.
 *
 * @since 2016/03/28
 */
public class HuffmanTreeTests
	implements TestInvoker
{
	/** A message to be encoded then decoded with. */
	public static final String MESSAGE =
		"GOLDEN EYES USE NO MAGIC";
	
	/** The secret but encoded into a byte array. */
	private static final byte[] _ENCODED =
		new byte[]{-14, 31, 65, 115, -61, 30, -90, 112, -33, -46, -36};
	
	/**
	 * {@inheritDoc}
	 * @since 2016/03/28
	 */
	@Override
	public TestGroupName invokerName()
	{
		return new TestGroupName(
			"net.multiphasicapps.util.huffmantree.HuffmanTree");
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/05/05
	 */
	@Override
	public Iterable<String> invokerTests()
	{
		return Arrays.<String>asList("huffman");
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/03/28
	 */
	@Override
	public void runTest(TestChecker __tc, String __st)
		throws NullPointerException, Throwable
	{
		// Check
		if (__tc == null || __st == null)
			throw new NullPointerException();
		
		// Setup base tree to store characters
		HuffmanTree<Character> htree = new HuffmanTree<>();
		
		// Add them all individually
		htree.add('G', 0b010, 0b111);
		htree.add('O', 0b011, 0b111);
		htree.add('N', 0b000, 0b111);
		htree.add('S', 0b001, 0b111);
		htree.add('E', 0b100, 0b111);
		htree.add(' ', 0b101, 0b111);
		htree.add('A', 0b11010, 0b11111);
		htree.add('C', 0b11011, 0b11111);
		htree.add('D', 0b11000, 0b11111);
		htree.add('I', 0b11001, 0b11111);
		htree.add('U', 0b11100, 0b11111);
		htree.add('Y', 0b11101, 0b11111);
		htree.add('M', 0b11110, 0b11111);
		htree.add('L', 0b11111, 0b11111);
		
		// Characters seem to have been added OK
		__tc.checkEquals(true, true);
		
		// Encode a message with it
		byte[] encodedas = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
		{
			// Setup compactor
			BitCompactor bc = new BitCompactor(new BitCompactor.Callback()
				{
					/**
					 * {@inheritDoc}
					 * @since 2016/03/28
					 */
					@Override
					public void ready(byte __v)
					{
						// Just send to the stream
						baos.write(__v);
					}
				});
			
			// Find symbols in the input
			String msg = MESSAGE;
			int n = msg.length();
			for (int i = 0; i < n; i++)
			{
				// Find the symbol
				long dat = htree.findSequence(msg.charAt(i));
				
				// Not found, stop
				if (dat == -1L)
					break;
				
				// Otherwise add it
				bc.add((int)dat, (int)(dat >>> 32), true);
			}
			
			// Target encoding info
			encodedas = baos.toByteArray();
			
			// Test result
			__tc.checkEquals(_ENCODED, encodedas);
		}
		
		// Decode the message
		// String to build
		StringBuilder sb = new StringBuilder();
		
		// Wrap in case of nulls
		try (ByteArrayInputStream bais =
			new ByteArrayInputStream(encodedas);
			BitInputStream bis = new BitInputStream(bais, false))
		{
			// Read until end of bits
			for (HuffmanTree.Traverser<Character> trav = null;;)
			{
				// Need a new traverser?
				if (trav == null)
					trav = htree.traverser();
				
				// Read single bit
				int val = bis.readBitsInt(1);
				
				// Traverse down
				trav.traverse(val);
				
				// Is there an object here?
				if (trav.hasValue())
				{
					// Get the value and add it to the string
					Character c = trav.getValue();
					sb.append(c);
					
					// Clear traverse
					trav = null;
					
					// Stop when the message length was reached
					if (sb.length() == MESSAGE.length())
						break;
				}
			}
		}
		
		// Check
		__tc.checkEquals(MESSAGE, sb.toString());
	}
}

