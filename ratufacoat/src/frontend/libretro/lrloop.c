/* -*- Mode: C; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// -------------------------------------------------------------------------*/

#include "debug.h"
#include "frontend/libretro/lrfreeze.h"
#include "frontend/libretro/lrjar.h"
#include "frontend/libretro/lrlocal.h"
#include "frontend/libretro/lrloop.h"
#include "frontend/libretro/lrscreen.h"
#include "memory.h"

sjme_libRetroCallbacks g_libRetroCallbacks =
{
	NULL, NULL, NULL, NULL
};

sjme_libRetroState* g_libRetroState = NULL;

/**
 * Destroys the given instance instance.
 * 
 * @param state The state to destroy.
 * @since 2022/01/02
 */
void sjme_libRetro_deinit(sjme_libRetroState* state)
{
	/* Destroy Screen. */
	if (state->video.pixels != NULL)
	{
		sjme_free(state->video.pixels, NULL);
		state->video.pixels = NULL;
	}
	
	/* De-allocate the VM state. */
	sjme_free(state, NULL);
}

/**
 * Destroys the library instance.
 * 
 * @since 2022/01/02
 */
SJME_GCC_USED void retro_deinit(void)
{
	sjme_libRetroState* oldState;
	
	/* Nothing to destroy? */
	oldState = g_libRetroState;
	if (oldState == NULL)
		return;
	
	/* Notice */
	sjme_libRetro_message(0, "Destroying engine.");
	
	/* Forward destruction. */
	g_libRetroState = NULL;
	sjme_libRetro_deinit(oldState);
	
	/* Notice. */
	sjme_libRetro_message(100, "Engine destroyed.");
}

/**
 * Initializes the base library.
 * 
 * @since 2022/01/02
 */
SJME_GCC_USED void retro_init(void)
{
	/* Does the same action as resetting the system. */
	retro_reset();
}

SJME_GCC_USED

void retro_reset(void)
{
	sjme_libRetroState* newState;
	sjme_jboolean okayInit;
	
	/* If we have a pre-existing state, destroy it so we have just one
	 * instance ever total. */
	if (g_libRetroState != NULL)
		retro_deinit();
	
	/* Notice. */
	sjme_libRetro_message(0, "Initializing engine.");
	
	/* Initialize a blank state. */
	newState = sjme_malloc(sizeof(*newState), NULL);
	memset(newState, 0, sizeof(*newState));
	
	/* Setup engine configuration from the settings. */
	okayInit = sjme_true;
	okayInit &= sjme_libRetro_selectRom(&newState->config);
	okayInit &= sjme_libRetro_screenConfig(&newState->config);
	okayInit &= sjme_libRetro_loopConfig(&newState->config);
	
	/* Did initialization fail? */
	if (!okayInit)
	{
		/* Nope, de-init. */
		sjme_libRetro_deinit(newState);
		
		/* Emit a failure message. */
		sjme_libRetro_message(-1,
			"Could not configure engine.");
		
		/* Fail. */
		return;
	}
	
	/* Notice. */
	sjme_libRetro_message(50, "Configuration complete.");
	
	/* Notice. */
	sjme_libRetro_message(100, "Initialization complete.");
	
	/* Use this global state now that it is fully up. */
	g_libRetroState = newState;
}

/** Runs single frame. */
SJME_GCC_USED void retro_run(void)
{
	sjme_libRetroState* currentState;
	sjme_jubyte badScreen[4];
	
	/* Poll for input because otherwise it prevents the user from accessing */
	/* the RetroArch menu. */
	g_libRetroCallbacks.inputPollFunc();
	
	/* Do nothing if there is no state. */
	currentState = g_libRetroState;
	if (sjme_true || currentState == NULL)
	{
		/* Nothing is running, so we cannot fast-forward. */
		sjme_libRetro_inhibitFastForward(sjme_true);
		
		/* Draw a completely blank display since we need to show something,
		 * otherwise RetroArch will freeze and not respond at all. */
		memset(badScreen, 0, sizeof(badScreen));
		g_libRetroCallbacks.videoFunc(badScreen,
			1, 1, sizeof(badScreen));
		
		return;
	}
	
	sjme_todo("Run single frame?");
}

sjme_jboolean sjme_libRetro_loopConfig(sjme_engineConfig* config)
{
	return sjme_true;
}
