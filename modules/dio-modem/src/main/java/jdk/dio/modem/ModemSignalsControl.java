// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package jdk.dio.modem;

import java.io.IOException;
import jdk.dio.ClosedDeviceException;
import jdk.dio.Device;
import jdk.dio.UnavailableDeviceException;

public interface ModemSignalsControl<P extends Device<? super P>>
{
	int CTS_SIGNAL =
		32;
	
	int DCD_SIGNAL =
		2;
	
	int DSR_SIGNAL =
		4;
	
	int DTR_SIGNAL =
		1;
	
	int RI_SIGNAL =
		8;
	
	int RTS_SIGNAL =
		16;
	
	boolean getSignalState(int __a)
		throws IOException, UnavailableDeviceException, 
			ClosedDeviceException;
	
	void setSignalChangeListener(ModemSignalListener<P> __a, int __b)
		throws IOException, UnavailableDeviceException, 
			ClosedDeviceException;
	
	void setSignalState(int __a, boolean __b)
		throws IOException, UnavailableDeviceException, 
			ClosedDeviceException;
}


