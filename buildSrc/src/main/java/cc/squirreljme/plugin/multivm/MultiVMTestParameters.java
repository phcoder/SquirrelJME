// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.plugin.multivm;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.workers.WorkParameters;

/**
 * This contains the parameters for work units, which are used to divide and
 * execute the actual work.
 *
 * @since 2020/09/06
 */
public interface MultiVMTestParameters
	extends WorkParameters
{
	/**
	 * Returns the result of the test.
	 * 
	 * @return The result of the test.
	 * @since 2020/09/06
	 */
	RegularFileProperty getResultFile();
	
	/**
	 * The test to be ran.
	 * 
	 * @return The test to be ran.
	 * @since 2020/09/06
	 */
	String getTestName();
}
