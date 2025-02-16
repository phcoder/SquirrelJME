// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package com.nttdocomo.ui;

import cc.squirreljme.jvm.mle.constants.NonStandardKey;
import javax.microedition.lcdui.Canvas;

/**
 * This represents a single display for an application, it is equivalent to
 * {@link javax.microedition.lcdui.Display}.
 *
 * @see javax.microedition.lcdui.Display
 * @since 2021/11/30
 */
public class Display
{
	public static final int KEY_0 =
		0x00;
		
	public static final int KEY_1 =
		0x01;
		
	public static final int KEY_2 =
		0x02;
		
	public static final int KEY_3 =
		0x03;
		
	public static final int KEY_4 =
		0x04;
		
	public static final int KEY_5 =
		0x05;
		
	public static final int KEY_6 =
		0x06;
		
	public static final int KEY_7 =
		0x07;
		
	public static final int KEY_8 =
		0x08;
		
	public static final int KEY_9 =
		0x09;
		
	public static final int KEY_ASTERISK =
		0x0A;

	public static final int KEY_DOWN =
		0x13;

	public static final int KEY_LEFT =
		0x10;

	public static final int KEY_POUND =
		0x0B;

	public static final int KEY_PRESSED_EVENT =
		0;

	public static final int KEY_RELEASED_EVENT =
		1;

	public static final int KEY_RIGHT =
		0x12;

	public static final int KEY_SELECT =
		0x14;

	public static final int KEY_SOFT1 =
		0x15;

	public static final int KEY_SOFT2 =
		0x16;

	public static final int KEY_UP =
		0x11;

	public static final int MAX_VENDOR_EVENT =
		127;

	public static final int MAX_VENDOR_KEY =
		0x1F;

	public static final int MEDIA_EVENT =
		8;

	public static final int MIN_VENDOR_EVENT =
		64;

	public static final int MIN_VENDOR_KEY =
		0x1A;

	public static final int RESET_VM_EVENT =
		5;

	public static final int RESUME_VM_EVENT =
		4;

	public static final int TIMER_EXPIRED_EVENT =
		7;

	public static final int UPDATE_VM_EVENT =
		6;
		
	/** The backing MIDP Display. */
	private static volatile javax.microedition.lcdui.Display _midpDisplay;
	
	/** The current frame being displayed. */
	private static volatile Frame _currentFrame;
	
	/**
	 * Internal only.
	 * 
	 * @since 2021/11/30
	 */
	private Display()
	{
	}
	
	@SuppressWarnings("FinalStaticMethod")
	public static final Frame getCurrent()
	{
		synchronized (Display.class)
		{
			return Display._currentFrame;
		}
	}
	
	@SuppressWarnings("FinalStaticMethod")
	public static final int getHeight()
	{
		return Display.__midpDisplay().getHeight();
	}
	
	@SuppressWarnings("FinalStaticMethod")
	public static final int getWidth()
	{
		return Display.__midpDisplay().getWidth();
	}
	
	@SuppressWarnings("FinalStaticMethod")
	public static final boolean isColor()
	{
		return Display.__midpDisplay().isColor();
	}
	
	@SuppressWarnings("FinalStaticMethod")
	public static final int numColors()
	{
		return Display.__midpDisplay().numColors();
	}
	
	@SuppressWarnings("FinalStaticMethod")
	public static final void setCurrent(Frame __frame)
	{
		synchronized (Display.class)
		{
			// Do nothing if this is the same
			Frame currentFrame = Display._currentFrame;
			if (currentFrame == __frame)
				return;
			
			// Try changing the display first
			javax.microedition.lcdui.Display display = Display.__midpDisplay();
			display.setCurrent((__frame == null ? null :
				__frame.__displayable()));
			
			// Is now the current frame
			Display._currentFrame = __frame;
		}
	}
	
	/**
	 * Returns the true display to use, only the first is used.
	 * 
	 * @return The true display to use.
	 * @since 2022/02/14
	 */
	static javax.microedition.lcdui.Display __midpDisplay()
	{
		javax.microedition.lcdui.Display rv = Display._midpDisplay;
		if (rv != null)
			return rv;
		
		// {@squirreljme.error AH0n No native displays available.}
		javax.microedition.lcdui.Display[] midpDisplays =
			javax.microedition.lcdui.Display.getDisplays(0);
		if (midpDisplays == null || midpDisplays.length == 0)
			throw new IllegalStateException("AH0n");
		
		// Cache and use this display
		Display._midpDisplay = (rv = midpDisplays[0]);
		return rv;
	}
	
	/**
	 * Maps a MIDP Key to i-mode.
	 * 
	 * @param __code The code to map.
	 * @return The mapped key.
	 * @since 2022/02/14
	 */
	static int __mapKey(int __code)
	{
		switch (__code)
		{
			case Canvas.KEY_UP:			return Display.KEY_UP;
			case Canvas.KEY_DOWN:		return Display.KEY_DOWN;
			case Canvas.KEY_LEFT:		return Display.KEY_LEFT;
			case Canvas.KEY_RIGHT:		return Display.KEY_RIGHT;
			case Canvas.KEY_NUM0:		return Display.KEY_0;
			case Canvas.KEY_NUM1:		return Display.KEY_1;
			case Canvas.KEY_NUM2:		return Display.KEY_2;
			case Canvas.KEY_NUM3:		return Display.KEY_3;
			case Canvas.KEY_NUM4:		return Display.KEY_4;
			case Canvas.KEY_NUM5:		return Display.KEY_5;
			case Canvas.KEY_NUM6:		return Display.KEY_6;
			case Canvas.KEY_NUM7:		return Display.KEY_7;
			case Canvas.KEY_NUM8:		return Display.KEY_8;
			case Canvas.KEY_NUM9:		return Display.KEY_9;
			case Canvas.KEY_STAR:		return Display.KEY_ASTERISK;
			case Canvas.KEY_POUND:		return Display.KEY_POUND;
			
			case Canvas.KEY_SELECT:
			case Canvas.KEY_ENTER:		return Display.KEY_SELECT;
			
				// SquirrelJME specific keys
			case NonStandardKey.F1:		return Display.KEY_SOFT1;
			case NonStandardKey.F2:		return Display.KEY_SOFT2;
		}
		
		// Unknown
		return Display.MAX_VENDOR_KEY;
	}
}
