// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package jdk.dio.watchdog;

import java.io.IOException;
import jdk.dio.ClosedDeviceException;
import jdk.dio.Device;
import jdk.dio.UnavailableDeviceException;

public interface WatchdogTimer
	extends Device<WatchdogTimer>
{
	boolean causedLastReboot()
		throws IOException, UnavailableDeviceException, 
			ClosedDeviceException;
	
	long getMaxTimeout()
		throws IOException, UnavailableDeviceException, 
			ClosedDeviceException;
	
	long getTimeout()
		throws IOException, UnavailableDeviceException, 
			ClosedDeviceException;
	
	void refresh()
		throws IOException, UnavailableDeviceException, 
			ClosedDeviceException;
	
	void start(long __a)
		throws IOException, UnavailableDeviceException, 
			ClosedDeviceException;
	
	void stop()
		throws IOException, UnavailableDeviceException, 
			ClosedDeviceException;
}


