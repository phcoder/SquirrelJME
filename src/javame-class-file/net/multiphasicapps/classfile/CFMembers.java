// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.classfile;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.multiphasicapps.collections.MissingCollections;
import net.multiphasicapps.descriptors.IdentifierSymbol;
import net.multiphasicapps.descriptors.MemberTypeSymbol;

/**
 * This is the base class for the set of members and such that a class may
 * have.
 *
 * @param <S> The type of symbol the member type uses.
 * @param <F> The flag type the member uses.
 * @param <E> The entry type.
 * @since 2016/03/19
 */
public abstract class CFMembers<S extends MemberTypeSymbol,
	F extends CFMemberFlags, E extends CFMember<S, F>>
	extends AbstractMap<CFMemberKey<S>, E>
{
	/** The owning class. */
	protected final CFClass owner;	
	
	/** The type the sub-class must be. */
	protected final Class<E> cast;
	
	/** The member mappings. */
	protected final Map<CFMemberKey<S>, E> store;
	
	/**
	 * Initializes the member list.
	 *
	 * @param __own The owning class to use.
	 * @param __cl The type the added elements must be.
	 * @param __s The mapping to source values from.
	 * @throws IllegalArgumentException If the mapping has a key which does not
	 * match its value.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/03/20
	 */
	public CFMembers(CFClass __own, Class<E> __cl, Map<CFMemberKey<S>, E> __s)
		throws IllegalArgumentException, NullPointerException
	{
		// Check
		if (__own == null || __cl == null || __s == null)
			throw new NullPointerException("NARG");
		
		// Set
		owner = __own;
		cast = __cl;
		
		// Setup storage
		Map<CFMemberKey<S>, E> ss = new LinkedHashMap<>();
		for (Map.Entry<CFMemberKey<S>, E> e : __s.entrySet())
		{
			// Read values
			CFMemberKey<S> k = e.getKey();
			E v = e.getValue();
			
			// {@squirreljme.error CF0v Attempt to add a member to the mapping
			// where the specified key does not match the mapping of the
			// member. (The mapping key; The value key)}
			if (!v.nameAndType().equals(k))
				throw new IllegalArgumentException(String.format("CF0v %s %s",
					k, v.nameAndType()));
			
			// Put it in
			ss.put(k, v);
		}
		
		// Lock
		store = MissingCollections.<CFMemberKey<S>, E>unmodifiableMap(ss);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/03/20
	 */
	@Override
	public final E get(Object __k)
	{
		return store.get(__k);
	}
	
	/**
	 * Obtains a member using the given name and type.
	 *
	 * @param __n The name of the member.
	 * @param __t The type of the member.
	 * @return The member by the given name and type or {@code null} if not
	 * found.
	 * @since 2016/03/20
	 */
	public final E get(IdentifierSymbol __n, S __t)
	{
		return get(new CFMemberKey<>(__n, __t));
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/03/20
	 */
	@Override
	public final Set<Map.Entry<CFMemberKey<S>, E>> entrySet()
	{
		return store.entrySet();
	}
}

