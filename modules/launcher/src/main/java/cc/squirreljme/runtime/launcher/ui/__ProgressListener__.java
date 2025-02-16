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
import javax.microedition.lcdui.Display;
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
	/** The scale shift. */
	private static final short _SCALE_SHIFT =
		8;
	
	/** The scale mask. */
	private static final short _SCALE_MASK =
		0xFF;
	
	/** The suites that have been listed. */
	protected final ArrayList<Application> listedSuites;
	
	/** The program list. */
	protected final List programList;
	
	/** The main display. */
	protected final Display mainDisplay;
	
	/** Comparator for sorting applications. */
	private final Comparator<Application> _comparator =
		new __ApplicationComparator__();
	
	/**
	 * Initializes the progress listener.
	 * 
	 * @param __programList The program list used.
	 * @param __listedSuites The suites that are available.
	 * @param __mainDisplay The main display.
	 * @throws NullPointerException On null arguments.
	 * @since 2020/12/29
	 */
	public __ProgressListener__(List __programList,
		ArrayList<Application> __listedSuites, Display __mainDisplay)
		throws NullPointerException
	{
		if (__programList == null || __listedSuites == null)
			throw new NullPointerException("NARG");
		
		this.programList = __programList;
		this.listedSuites = __listedSuites;
		this.mainDisplay = __mainDisplay;
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
		
		// Update title to reflect this discovery
		programList.setTitle(String.format(
			"Querying Suites (Found %d of %d)...", __dx + 1, __total));
		
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
		
		// Make the icon fit nicely?
		if (icon != null)
		{
			// Get the preferred icon size
			Display mainDisplay = this.mainDisplay;
			int prefW = mainDisplay.getBestImageWidth(Display.LIST_ELEMENT);
			int prefH = mainDisplay.getBestImageHeight(Display.LIST_ELEMENT);
			
			// Scale the icon
			if (icon.getWidth() > prefW ||
				icon.getHeight() > prefH)
				try
				{
					icon = __ProgressListener__.__scaleIcon(
						icon, prefW, prefH);
				}
				catch (IndexOutOfBoundsException e)
				{
					e.printStackTrace();
				}
		}
		
		// Add entry to the list
		programList.insert(at, __app.displayName(), icon);
	}
	
	/**
	 * Scales the given icon to the given size.
	 * 
	 * @param __icon The icon to scale.
	 * @param __prefW The target width.
	 * @param __prefH The target height.
	 * @return The new resultant icon.
	 * @throws NullPointerException On null arguments.
	 * @since 2022/02/14
	 */
	private static Image __scaleIcon(Image __icon, int __prefW, int __prefH)
		throws NullPointerException
	{
		if (__icon == null)
			throw new NullPointerException("NARG");
		
		// Get original image size
		int srcW = __icon.getWidth();
		int srcH = __icon.getHeight();
		
		// Determine the pixels to scale
		int scaleX = (srcW << __ProgressListener__._SCALE_SHIFT) / __prefW;
		int scaleY = (srcH << __ProgressListener__._SCALE_SHIFT) / __prefH;
		
		// Read original image
		int[] src = new int[srcW * srcH];
		__icon.getRGB(src, 0, srcW, 0, 0, srcW, srcH);
		
		// Setup new target image
		int[] dest = new int[__prefW * __prefH];
		
		// Perform the scaling operation
		for (int destY = 0, baseSrcY = 0; destY < __prefH;
			destY++, baseSrcY += scaleY)
		{
			int srcI = srcW * (baseSrcY & (~__ProgressListener__._SCALE_MASK));
			for (int destI = __prefW * destY, endDestI = destI + __prefW;
				destI < endDestI; destI++, srcI += scaleX)
				dest[destI] = src[srcI >>> __ProgressListener__._SCALE_SHIFT]; 
		}
		
		// Return the resultant scaled image now
		return Image.createRGBImage(dest, __prefW, __prefH, __icon.hasAlpha());
	}
}
