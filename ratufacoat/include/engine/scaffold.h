/* -*- Mode: C; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// -------------------------------------------------------------------------*/

/**
 * Engine scaffolding.
 * 
 * @since 2021/09/06
 */

#ifndef SQUIRRELJME_SCAFFOLD_H
#define SQUIRRELJME_SCAFFOLD_H

#include "sjmerc.h"

/* Anti-C++. */
#ifdef __cplusplus
#ifndef SJME_CXX_IS_EXTERNED
#define SJME_CXX_IS_EXTERNED
#define SJME_CXX_SQUIRRELJME_SCAFFOLD_H
extern "C"
{
#endif /* #ifdef SJME_CXX_IS_EXTERNED */
#endif /* #ifdef __cplusplus */

/*--------------------------------------------------------------------------*/

/**
 * This is the scaffold for an engine between the common engine layer and the
 * specific engine implementation.
 * 
 * @since 2021/12/19
 */
typedef struct sjme_engineScaffold
{
	/** The name of the engine. */
	const char* const name;
} sjme_engineScaffold;

/** Scaffolds which are available for use. */
extern const sjme_engineScaffold* const sjme_engineScaffolds[];

/** SpringCoat engine. */
extern const sjme_engineScaffold sjme_engineScaffoldSpringCoat;

/*--------------------------------------------------------------------------*/

/* Anti-C++. */
#ifdef __cplusplus
#ifdef SJME_CXX_SQUIRRELJME_SCAFFOLD_H
}
#undef SJME_CXX_SQUIRRELJME_SCAFFOLD_H
#undef SJME_CXX_IS_EXTERNED
#endif /* #ifdef SJME_CXX_SQUIRRELJME_SCAFFOLD_H */
#endif /* #ifdef __cplusplus */

#endif /* SQUIRRELJME_SCAFFOLD_H */
