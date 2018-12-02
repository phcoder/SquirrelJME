// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.runtime.cldc.asm;

import cc.squirreljme.runtime.javase.lcdui.ColorInfo;
import cc.squirreljme.runtime.lcdui.event.EventType;
import cc.squirreljme.runtime.lcdui.event.NonStandardKey;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.lcdui.Display;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import net.multiphasicapps.io.MIMEFileDecoder;

/**
 * Java SE implementation of the native display system using Swing.
 *
 * @since 2018/11/16
 */
public final class NativeDisplayAccess
{
	/** The maximum number of integers for event data. */
	public static final int EVENT_SIZE =
		5;
	
	/** The number of parameters available. */
	public static final int NUM_PARAMETERS =
		8;
	
	/** The pixel format. */
	public static final int PARAMETER_PIXELFORMAT =
		0;
	
	/** The buffer width. */
	public static final int PARAMETER_BUFFERWIDTH =
		1;
	
	/** The buffer height. */
	public static final int PARAMETER_BUFFERHEIGHT =
		2;
	
	/** Alpha channel is used? */
	public static final int PARAMETER_ALPHA =
		3;
	
	/** Buffer pitch. */
	public static final int PARAMETER_PITCH =
		4;
	
	/** Buffer offset. */
	public static final int PARAMETER_OFFSET =
		5;
	
	/** Virtual X offset. */
	public static final int PARAMETER_VIRTXOFF =
		6;
	
	/** Virtual Y offset. */
	public static final int PARAMETER_VIRTYOFF =
		7;
	
	/** The event queue size with the type. */
	public static final int EVENT_SIZE_WITH_TYPE =
		NativeDisplayAccess.EVENT_SIZE + 1;
	
	/** The number of events to store in the buffer. */
	public static final int QUEUE_SIZE =
		96;
	
	/** The limit of the event queue. */
	public static final int QUEUE_LIMIT =
		EVENT_SIZE_WITH_TYPE * QUEUE_SIZE;
	
	/** The event queue, a circular buffer. */
	private static final short[] _eventqueue =
		new short[EVENT_SIZE_WITH_TYPE * QUEUE_SIZE];
	
	/** The read position. */
	private static int _eventread;
	
	/** The write position. */
	private static int _eventwrite;
	
	/** The frame to display. */
	private static volatile JFrame _frame;
	
	/** The panel used to display graphics on. */
	private static volatile SwingPanel _panel;
	
	/** Doing a repaint? */
	static volatile boolean _repaint;
	
	/** Repaint parameters. */
	static volatile int _repaintx,
		_repainty,
		_repaintw,
		_repainth;
	
	/**
	 * Initializes some things
	 *
	 * @since 2018/11/18
	 */
	static
	{
		try
		{
			// But doing this, speed is increased greatly!
			JFrame.setDefaultLookAndFeelDecorated(true);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	/**
	 * Initialize and/or reset accelerated graphics operations.
	 *
	 * @param __id The display to initialize for.
	 * @return {@code true} if acceleration is supported.
	 * @since 2018/11/19
	 */
	public static final boolean accelGfx(int __id)
	{
		// Not supported on Swing because graphics operations are pretty
		// fast already
		return false;
	}
	
	/**
	 * Performs accelerated graphics operation.
	 *
	 * @param __id The display ID.
	 * @param __func The function to call.
	 * @param __args Arguments to the operation.
	 * @return The result of the operation.
	 * @since 2018/11/19
	 */
	public static final Object accelGfxFunc(int __id, int __func,
		Object... __args)
	{
		return null;
	}
	
	/**
	 * Returns the capabilities of the display.
	 *
	 * @param __id The display ID.
	 * @return The capabilities of the display.
	 * @since 2018/11/17
	 */
	public static final int capabilities(int __id)
	{
		return Display.SUPPORTS_INPUT_EVENTS |
			Display.SUPPORTS_TITLE |
			Display.SUPPORTS_ORIENTATION_PORTRAIT |
			Display.SUPPORTS_ORIENTATION_LANDSCAPE;
	}
	
	/**
	 * Returns the object representing the framebuffer data.
	 *
	 * @param __id The display ID.
	 * @return The framebuffer array.
	 * @since 2018/11/18
	 */
	public static final Object framebufferObject(int __id)
	{
		if (__id != 0)
			return null;
		
		return ColorInfo.getArray(NativeDisplayAccess.__panel()._image);
	}
	
	/**
	 * Specifies that the framebuffer has been painted.
	 *
	 * @param __id The display ID.
	 * @since 2018/11/18
	 */
	public static final void framebufferPainted(int __id)
	{
		if (__id != 0)
			return;
		
		NativeDisplayAccess.__panel().repaint();
	}
	
	/**
	 * Returns the palette of the framebuffer.
	 *
	 * @param __id The display ID.
	 * @return The palette of the framebuffer.
	 * @since 2018/11/18
	 */
	public static final int[] framebufferPalette(int __id)
	{
		if (__id != 0)
			return null;
		
		return ColorInfo.getPalette(NativeDisplayAccess.__panel()._image);
	}
	
	/**
	 * Returns the parameters of the framebuffer.
	 *
	 * @param __id The display ID.
	 * @return The framebuffer parameters.
	 * @since 2018/11/18
	 */
	public static final int[] framebufferParameters(int __id)
	{
		if (__id != 0)
			return null;
		
		BufferedImage image = NativeDisplayAccess.__panel()._image;
		
		// Build parameters
		int[] rv = new int[NativeDisplayAccess.NUM_PARAMETERS];
		rv[NativeDisplayAccess.PARAMETER_PIXELFORMAT] =
			ColorInfo.PIXEL_FORMAT.ordinal();
		rv[NativeDisplayAccess.PARAMETER_BUFFERWIDTH] = image.getWidth();
		rv[NativeDisplayAccess.PARAMETER_BUFFERHEIGHT] = image.getHeight();
		rv[NativeDisplayAccess.PARAMETER_ALPHA] = 0;
		rv[NativeDisplayAccess.PARAMETER_PITCH] = image.getWidth();
		rv[NativeDisplayAccess.PARAMETER_OFFSET] = 0;
		rv[NativeDisplayAccess.PARAMETER_VIRTXOFF] = 0;
		rv[NativeDisplayAccess.PARAMETER_VIRTYOFF] = 0;
		
		return rv;
	}
	
	/**
	 * Is the specified display upsidedown?
	 *
	 * @param __id The ID of the display.
	 * @return If the display is upsidedown.
	 * @since 2018/11/17
	 */
	public static final boolean isUpsideDown(int __id)
	{
		return false;
	}
	
	/**
	 * Returns the number of permanent displays which are currently attached to
	 * the system.
	 *
	 * @return The number of displays attached to the system.
	 * @since 2018/11/16
	 */
	public static final int numDisplays()
	{
		// There is ever only a single display that is supported
		return 1;
	}
	
	/**
	 * Polls the next event, blocking until the next one occurs.
	 *
	 * @param __ed Event data.
	 * @return The next event, this will be the even type.
	 * @since 2018/11/17
	 */
	public static final int pollEvent(short[] __ed)
	{
		if (__ed == null)
			throw new NullPointerException("NARG");
		
		// Maximum number of data points to write
		int edlen = Math.min(__ed.length, NativeDisplayAccess.EVENT_SIZE);
		
		// Constantly poll for events
		short[] eventqueue = NativeDisplayAccess._eventqueue;
		for (;;)
			synchronized (eventqueue)
			{
				// Read positions
				int eventread = NativeDisplayAccess._eventread,
					eventwrite = NativeDisplayAccess._eventwrite;
				
				// This a circular buffer, so if the values do not match
				// then that means an event was found.
				if (eventread != eventwrite)
				{
					// Base pointer for reading events
					int baseptr = eventread;
					
					// Read the type for later return
					int type = eventqueue[baseptr++];
					
					// Copy event data
					for (int o = 0; o < edlen;)
						__ed[o++] = eventqueue[baseptr++];
					
					// Make sure the read position does not overflow the
					// buffer
					int nexter = eventread + EVENT_SIZE_WITH_TYPE;
					if (nexter >= QUEUE_LIMIT)
						nexter = 0;
					NativeDisplayAccess._eventread = nexter;
					
					// If this a repaint type, use the single repaint
					// parameters instead
					if (type == EventType.DISPLAY_REPAINT.ordinal())
					{
						// Ignored the stored parameters and instead use
						// the repaint ones
						__ed[1] = (short)NativeDisplayAccess._repaintx;
						__ed[2] = (short)NativeDisplayAccess._repainty;
						__ed[3] = (short)NativeDisplayAccess._repaintw;
						__ed[4] = (short)NativeDisplayAccess._repainth;
						NativeDisplayAccess._repaint = false;
					}
					
					// And the type of the event
					return type;
				}
				
				// Wait for an event to appear
				try
				{
					eventqueue.wait();
				}
				
				// Just treat like an event might have happened
				catch (InterruptedException e)
				{
				}
			}
	}
	
	/**
	 * Posts the specified event to the end of the event queue.
	 *
	 * All fields only have the granularity of {@code short}.
	 *
	 * @param __type The event type to push.
	 * @param __d0 Datapoint 1.
	 * @param __d1 Datapoint 2.
	 * @param __d2 Datapoint 3.
	 * @param __d3 Datapoint 4.
	 * @param __d4 Datapoint 5.
	 * @since 2018/11/18
	 */
	public static final void postEvent(int __type,
		int __d0, int __d1, int __d2, int __d3, int __d4)
	{
		// Lock on the queue
		short[] eventqueue = NativeDisplayAccess._eventqueue;
		synchronized (eventqueue)
		{
			// Prevent repaint flooding
			if (__type == EventType.DISPLAY_REPAINT.ordinal())
			{
				// Doing a repaint
				boolean repaint = NativeDisplayAccess._repaint;
				if (!repaint)
				{
					NativeDisplayAccess._repaintx = __d1;
					NativeDisplayAccess._repainty = __d2;
					NativeDisplayAccess._repaintw = __d3;
					NativeDisplayAccess._repainth = __d4;
					NativeDisplayAccess._repaint = true;
				}
				
				// Otherwise update parameters
				else
				{
					NativeDisplayAccess._repaintx =
						Math.min(NativeDisplayAccess._repaintx, __d1);
					NativeDisplayAccess._repainty =
						Math.min(NativeDisplayAccess._repainty, __d2);
					NativeDisplayAccess._repaintw =
						Math.max(NativeDisplayAccess._repaintw, __d3);
					NativeDisplayAccess._repainth =
						Math.max(NativeDisplayAccess._repainth, __d4);
					
					// Do not write an event, since there is a repaint in
					// the queue
					return;
				}
			}
			
			int eventwrite = NativeDisplayAccess._eventwrite;
			
			// Overwrite all the data
			eventqueue[eventwrite++] = (short)__type;
			eventqueue[eventwrite++] = (short)__d0;
			eventqueue[eventwrite++] = (short)__d1;
			eventqueue[eventwrite++] = (short)__d2;
			eventqueue[eventwrite++] = (short)__d3;
			eventqueue[eventwrite++] = (short)__d4;
			
			// Go back to the start?
			if (eventwrite >= QUEUE_LIMIT)
				eventwrite = 0;
			NativeDisplayAccess._eventwrite = eventwrite;
			
			// Notify that an event was put in the queue
			eventqueue.notify();
		}
	}
	
	/**
	 * Sets the title of the display.
	 *
	 * @param __id The display ID.
	 * @param __t The title to use.
	 * @since 2018/11/18
	 */
	public static final void setDisplayTitle(int __id, String __t)
	{
		if (__id != 0)
			return;
		
		NativeDisplayAccess.__frame().setTitle(
			(__t == null ? "SquirrelJME" : __t));
	}
	
	/**
	 * Returns the current frame.
	 *
	 * @return The current frame.
	 * @since 2018/11/18
	 */
	static final JFrame __frame()
	{
		// Without the lock, the frame is initialized multiple times
		synchronized (NativeDisplayAccess.class)
		{
			JFrame rv = NativeDisplayAccess._frame;
			if (rv != null)
				return rv;
				
			// Debug
			todo.DEBUG.note("Setting up the frame.");
			
			// Setup frame
			NativeDisplayAccess._frame = (rv = new JFrame());
			
			// Exit when close is pressed
			rv.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			
			// Initial title
			rv.setTitle("SquirrelJME");
			
			// Use a better panel size
			rv.setMinimumSize(new Dimension(160, 160));
			rv.setPreferredSize(new Dimension(640, 480));
			
			// Load icons, for those that exist anyway
			List<Image> icons = new ArrayList<>();
			for (int i : new int[]{8, 16, 24, 32, 48, 64})
			{
				String rcname = String.format("head_%dx%d.png", i, i);
				URL rc = ColorInfo.class.getResource(rcname);
				
				// If it does not exist, try loading MIME data instead
				// The new bootstrap does not decode these
				if (rc == null)
				{
					try (InputStream in = ColorInfo.class.getResourceAsStream(
						rcname + ".__mime"))
					{
						// Still does not exist
						if (in == null)
							continue;
						
						// Decode MIME data
						try (ByteArrayOutputStream baos = new
							ByteArrayOutputStream();
							InputStream inx = new MIMEFileDecoder(in, "utf-8"))
						{
							byte[] buf = new byte[512];
							for (;;)
							{
								int rcx = inx.read(buf);
								
								if (rcx < 0)
									break;
								
								baos.write(buf, 0, rcx);
							}
							
							// Load icon
							icons.add(new ImageIcon(baos.toByteArray()).
								getImage());
						}
					}
					
					// Ignore
					catch (IOException e)
					{
					}
					
					// Go to the next icon
					continue;
				}
				
				// Add the icon
				icons.add(new ImageIcon(rc).getImage());
			}
			rv.setIconImages(icons);
			
			return rv;
		}
	}
	
	/**
	 * Returns the current panel.
	 *
	 * @return The current panel.
	 * @since 2018/11/18
	 */
	static final SwingPanel __panel()
	{
		// Without the lock, the panel is initialized multiple times
		synchronized (NativeDisplayAccess.class)
		{
			SwingPanel rv = NativeDisplayAccess._panel;
			if (rv != null)
				return rv;
			
			// Debug
			todo.DEBUG.note("Setting up the panel.");
			
			// Setup panel
			NativeDisplayAccess._panel = (rv = new SwingPanel());
			
			// Add to the frame
			JFrame frame = NativeDisplayAccess.__frame();
			frame.add(rv);
			
			// Record some events
			frame.addComponentListener(rv);
			frame.addWindowListener(rv);
			frame.addKeyListener(rv);
			
			// Pack the frame
			frame.pack();
			
			// Make the frame visible and set its properties
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			
			return rv;
		}
	}
	
	/**
	 * The panel used to display graphics on.
	 *
	 * @since 2018/11/18
	 */
	public static final class SwingPanel
		extends JPanel
		implements ComponentListener, KeyListener, WindowListener
	{
		/** The image to be displayed. */
		volatile BufferedImage _image =
			ColorInfo.create(1, 1, new Color(0xFFFFFFFF));
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void componentHidden(ComponentEvent __e)
		{
			NativeDisplayAccess.postEvent(
				EventType.DISPLAY_HIDDEN.ordinal(),
				0, -1, -1, -1, -1);
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void componentMoved(ComponentEvent __e)
		{
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void componentResized(ComponentEvent __e)
		{
			BufferedImage image = this._image;
			int oldw = image.getWidth(),
				oldh = image.getHeight(),
				xw = this.getWidth(),
				xh = this.getHeight();
		
			// Recreate the image if the size has changed
			if (xw != oldw || xh != oldh)
				this._image = (image = ColorInfo.create(xw, xh,
					new Color(0xFFFFFFFF)));
			
			// Indicate that the size changed
			NativeDisplayAccess.postEvent(
				EventType.DISPLAY_SIZE_CHANGED.ordinal(),
				0, xw, xh, -1, -1);
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void componentShown(ComponentEvent __e)
		{
			NativeDisplayAccess.postEvent(
				EventType.DISPLAY_SHOWN.ordinal(),
				0, -1, -1, -1, -1);
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/12/01
		 */
		@Override
		public void keyPressed(KeyEvent __e)
		{
			NativeDisplayAccess.postEvent(
				EventType.KEY_PRESSED.ordinal(),
				__KeyMap__.__map(__e), (int)__e.getWhen(), -1, -1, -1);
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/12/01
		 */
		@Override
		public void keyReleased(KeyEvent __e)
		{
			NativeDisplayAccess.postEvent(
				EventType.KEY_RELEASED.ordinal(),
				__KeyMap__.__map(__e), (int)__e.getWhen(), -1, -1, -1);
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/12/01
		 */
		@Override
		public void keyTyped(KeyEvent __e)
		{
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void repaint(Rectangle __r)
		{
			// Post repaint event
			/*NativeDisplayAccess.postEvent(
				EventType.DISPLAY_REPAINT.ordinal(),
				0, __r.x, __r.y, __r.width, __r.height);*/
			
			// Forward
			super.repaint(__r);
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void repaint(long __tm, int __x, int __y, int __w, int __h)
		{
			// Post repaint event
			/*NativeDisplayAccess.postEvent(
				EventType.DISPLAY_REPAINT.ordinal(),
				0, __x, __y, __w, __h);*/
			
			// Forward
			super.repaint(__tm, __x, __y, __w, __h);
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		protected void paintComponent(java.awt.Graphics __g)
		{
			// This must always be called
			super.paintComponent(__g);
			
			BufferedImage image = this._image;
			int oldw = image.getWidth(),
				oldh = image.getHeight(),
				xw = this.getWidth(),
				xh = this.getHeight();
		
			// Recreate the image if the size has changed
			if (xw != oldw || xh != oldh)
				this._image = (image = ColorInfo.create(xw, xh,
					new Color(0xFFFFFFFF)));
			
			// Draw the backed buffered image
			__g.drawImage(image, 0, 0, xw, xh,
				0, 0, xw, xh, null);
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void windowActivated(WindowEvent __e)
		{
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void windowClosed(WindowEvent __e)
		{
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void windowClosing(WindowEvent __e)
		{
			todo.DEBUG.note("Window is closing!");
			
			// Post close event
			NativeDisplayAccess.postEvent(
				EventType.EXIT_REQUEST.ordinal(),
				0, -1, -1, -1, -1);
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void windowDeactivated(WindowEvent __e)
		{
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void windowDeiconified(WindowEvent __e)
		{
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void windowIconified(WindowEvent __e)
		{
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/11/18
		 */
		@Override
		public void windowOpened(WindowEvent __e)
		{
		}
	}
}

