// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.runtime.launcher.ui;

import cc.squirreljme.jvm.launch.Application;
import cc.squirreljme.jvm.launch.SuiteScanListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

/**
 * This listens for when suites have been detected by the scanner.
 *
 * @since 2020/12/29
 */
final class __ProgressListener__
	implements SuiteScanListener
{
	/** The suites that have been listed. */
	protected final ArrayList<Application> listedSuites;
	
	/** The program list. */
	protected final List programList;
	
	/** Comparator for sorting applications. */
	private final Comparator<Application> _comparator =
		new __ApplicationComparator__();
	
	/** The canvas to refresh on updates. */
	protected final SplashScreen refreshCanvas;
	
	/** The current refresh state. */
	protected final __RefreshState__ refreshState;
	
	/**
	 * Initializes the progress listener.
	 * 
	 * @param __programList The program list used.
	 * @param __listedSuites The suites that are available.
	 * @param __refreshCanvas The canvas to update on refreshes.
	 * @param __refreshState The current refresh state.
	 * @throws NullPointerException On null arguments.
	 * @since 2020/12/29
	 */
	public __ProgressListener__(List __programList,
		ArrayList<Application> __listedSuites, SplashScreen __refreshCanvas,
		__RefreshState__ __refreshState)
		throws NullPointerException
	{
		if (__programList == null || __listedSuites == null)
			throw new NullPointerException("NARG");
		
		this.programList = __programList;
		this.listedSuites = __listedSuites;
		this.refreshCanvas = __refreshCanvas;
		this.refreshState = __refreshState;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2020/12/29
	 */
	@Override
	public void scanned(Application __app, int __dx, int __total)
	{
		// Do nothing if this is not to appear on the launcher
		if (__app.isNoLauncher())
			return;
		
		ArrayList<Application> listedSuites = this.listedSuites;
		List programList = this.programList;
		SplashScreen refreshCanvas = this.refreshCanvas;
		__RefreshState__ refreshState = this.refreshState;
		
		// Update title to reflect this discovery
		String updateMessage = String.format(
			"Querying Suites (Found %d of %d)...", __dx + 1, __total);
		programList.setTitle(updateMessage);
		
		// Update splash screen
		refreshState.set(updateMessage, __dx + 1, __total);
		if (refreshCanvas != null)
			refreshCanvas.requestRepaint();
		
		// Determine where this should go and remember the suite
		int at = Collections.binarySearch(listedSuites, __app,
			this._comparator);
		if (at < 0)
			at = -(at + 1);
		listedSuites.add(at, __app);
		
		// Try to load the image for the application
		Image icon = null;
		try (InputStream iconData = __app.iconStream())
		{
			if (iconData != null)
				icon = Image.createImage(iconData);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// Add entry to the list
		programList.insert(at, __app.displayName(), icon);
	}
}
