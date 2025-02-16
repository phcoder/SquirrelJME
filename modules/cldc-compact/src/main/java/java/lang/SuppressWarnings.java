// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package java.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Suppresses warnings that are generated by the compiler so that they do not
 * appear for the given method. Duplicates are permitted and values which are
 * unknown are ignored.
 *
 * The following warnings may have support to be disabled, although it is not
 * required: {@code all}, {@code boxing}, {@code cast}, {@code dep-ann},
 * {@code deprecation}, {@code fallthrough}, {@code finally}, {@code hiding},
 * {@code incomplete-switch}, {@code nls}, {@code null}, {@code rawtypes},
 * {@code restriction}, {@code serial}, {@code static-access},
 * {@code synthetic-access}, {@code unchecked},
 * {@code unqualified-field-access}, and {@code unused}.
 *
 * @since 2018/09/19
 */
@Target(value={ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,
	ElementType.PARAMETER, ElementType.CONSTRUCTOR,
	ElementType.LOCAL_VARIABLE})
@Retention(value=RetentionPolicy.SOURCE)
public @interface SuppressWarnings
{
	/** @return The warnings that should be suppressed. */
	String[] value();
}

