/* -*- Mode: C; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// -------------------------------------------------------------------------*/

/**
 * Memory chunks.
 * 
 * @since 2021/10/17
 */

#ifndef SQUIRRELJME_MEMCHUNK_H
#define SQUIRRELJME_MEMCHUNK_H

#include "sjmerc.h"
#include "error.h"

/* Anti-C++. */
#ifdef __cplusplus
#ifndef SJME_CXX_IS_EXTERNED
#define SJME_CXX_IS_EXTERNED
#define SJME_CXX_SQUIRRELJME_MEMCHUNK_H
extern "C"
{
#endif /* #ifdef SJME_CXX_IS_EXTERNED */
#endif /* #ifdef __cplusplus */

/*--------------------------------------------------------------------------*/

/**
 * Represents a single memory chunk.
 * 
 * @since 2021/10/17
 */
typedef struct sjme_memChunk
{
	/** The data block. */
	const void* data;
	
	/** The size of the data block. */
	sjme_jint size;
} sjme_memChunk;

/**
 * Checks if a given read or write would be within the chunk bounds.
 * 
 * @param chunk The chunk to check.
 * @param off The offset within the chunk.
 * @param len The number of bytes to access.
 * @param error The possible error state.
 * @return If the chunk is within bounds or not.
 * @since 2021/10/17 
 */
sjme_jboolean sjme_chunkCheckBound(sjme_memChunk* chunk, sjme_jint off,
	sjme_jint len, sjme_error* error);

/**
 * Reads an unsigned short from the memory chunk. 
 * 
 * @param chunk The chunk to read from.
 * @param off The offset from within the chunk.
 * @param value The output value.
 * @param error The potential error.
 * @return If this was a successful read or not.
 * @since 2021/10/17
 */
sjme_jboolean sjme_chunkReadBigShort(sjme_memChunk* chunk, sjme_jint off,
	sjme_jshort* value, sjme_error* error);

/*--------------------------------------------------------------------------*/

/* Anti-C++. */
#ifdef __cplusplus
#ifdef SJME_CXX_SQUIRRELJME_MEMCHUNK_H
}
#undef SJME_CXX_SQUIRRELJME_MEMCHUNK_H
#undef SJME_CXX_IS_EXTERNED
#endif /* #ifdef SJME_CXX_SQUIRRELJME_MEMCHUNK_H */
#endif /* #ifdef __cplusplus */

#endif /* SQUIRRELJME_MEMCHUNK_H */
