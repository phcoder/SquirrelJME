// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.vm.springcoat;

import cc.squirreljme.jvm.mle.TypeShelf;
import cc.squirreljme.jvm.mle.brackets.TypeBracket;
import cc.squirreljme.vm.springcoat.brackets.JarPackageObject;
import cc.squirreljme.vm.springcoat.brackets.TypeObject;
import cc.squirreljme.vm.springcoat.exceptions.SpringClassNotFoundException;
import cc.squirreljme.vm.springcoat.exceptions.SpringMLECallError;
import java.util.Objects;
import net.multiphasicapps.classfile.ClassName;
import net.multiphasicapps.classfile.MethodNameAndType;
import net.multiphasicapps.classfile.PrimitiveType;

/**
 * Functions for {@link TypeShelf}.
 *
 * @since 2020/06/18
 */
public enum MLEType
	implements MLEFunction
{
	/** {@link TypeShelf#binaryName(TypeBracket)}. */
	BINARY_NAME("binaryName:(Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;)Ljava/lang/String;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/27
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return __thread.asVMObject(MLEType.__type(__args[0])
				.getSpringClass().name().binaryName().toString());
		}
	}, 
	
	/** {@link TypeShelf#binaryPackageName(TypeBracket)}. */
	BINARY_PACKAGE_NAME("binaryPackageName:(Lcc/squirreljme/jvm/" +
		"mle/brackets/TypeBracket;)Ljava/lang/String;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return __thread.asVMObject(MLEType.__type(__args[0])
				.getSpringClass().name().binaryName().inPackage()
				.toString());
		}
	},
	
	/** {@link TypeShelf#classToType(Class)}. */
	CLASS_TO_TYPE("classToType:(Ljava/lang/Class;)" +
		"Lcc/squirreljme/jvm/mle/brackets/TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return MLEType.__simple(__args[0]).fieldByField(
				__thread.resolveClass(new ClassName("java/lang/Class"))
				.lookupField(false, "_type",
				"Lcc/squirreljme/jvm/mle/brackets/TypeBracket;")).get();
		}
	},
	
	/** {@link TypeShelf#component(TypeBracket)}. */
	COMPONENT("component:(Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;)Lcc/squirreljme/jvm/mle/brackets/TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/28
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			SpringClass type = MLEType.__type(__args[0]).getSpringClass();
			
			if (!type.isArray())
				throw new SpringMLECallError("Not an array type.");
			
			return new TypeObject(__thread.machine, type.componentType());
		}
	},
	
	/** {@link TypeShelf#componentRoot(TypeBracket)}. */
	COMPONENT_ROOT("componentRoot:(Lcc/squirreljme/jvm/mle/" +
		"brackets/TypeBracket;)Lcc/squirreljme/jvm/mle/brackets/TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/28
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			SpringClass type = MLEType.__type(__args[0]).getSpringClass();
			
			if (!type.isArray())
				throw new SpringMLECallError("Not an array type.");
			
			// Find the root component
			while (type.isArray())
				type = type.componentType();
			return new TypeObject(__thread.machine, type);
		}
	},
	
	/** {@link TypeShelf#enumValues(TypeBracket)}. */
	ENUM_VALUES("enumValues:(Lcc/squirreljme/jvm/mle/" +
		"brackets/TypeBracket;)[Ljava/lang/Enum;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/28
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			SpringClass type = MLEType.__type(__args[0]).getSpringClass();
			
			// Must be an enumeration
			if (!type.isEnum())
				throw new SpringMLECallError("Not an enumeration type");
			
			// Call the static values() method in the given class, it is
			// automatically generated by the Java compiler (it is synthetic)
			return __thread.invokeMethod(true, type.name(),
				new MethodNameAndType("values", 
					"()" + type.name().field().addDimensions(1)));
		}
	}, 
	
	/** {@link TypeShelf#equals(TypeBracket, TypeBracket)}. */
	EQUALS("equals:(Lcc/squirreljme/jvm/mle/brackets/TypeBracket;" +
		"Lcc/squirreljme/jvm/mle/brackets/TypeBracket;)Z")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return Objects.equals(
				MLEType.__type(__args[0]).getSpringClass(),
				MLEType.__type(__args[1]).getSpringClass());
		}
	},
	
	/** {@link TypeShelf#findType(String)}. */
	FIND_TYPE("findType:(Ljava/lang/String;)" +
		"Lcc/squirreljme/jvm/mle/brackets/TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			SpringObject name = MLEType.__notNullObject(__args[0]);
			
			try
			{
				return new TypeObject(__thread.machine, __thread.loadClass(
					__thread.<String>asNativeObject(String.class, name)));
			}
			
			// Since the method returns null when not found, we want to return
			// this here
			catch (SpringClassNotFoundException e)
			{
				return null;
			}
		}
	},
	
	/** {@link TypeShelf#inJar(TypeBracket)}. */
	IN_JAR("inJar:(Lcc/squirreljme/jvm/mle/brackets/TypeBracket;)" +
		"Lcc/squirreljme/jvm/mle/brackets/JarPackageBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new JarPackageObject(__thread.machine,
				MLEType.__type(__args[0]).getSpringClass().inJar());
		}
	},
	
	/** {@link TypeShelf#interfaces(TypeBracket)}. */
	INTERFACES("interfaces:(Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;)[Lcc/squirreljme/jvm/mle/brackets/TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/29
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			SpringClass type = MLEType.__type(__args[0]).getSpringClass();
			
			SpringClass[] interfaces = type.interfaceClasses();
			int n = interfaces.length;
			
			SpringObject[] rv = new SpringObject[n];
			for (int i = 0; i < n; i++)
				rv[i] = new TypeObject(__thread.machine, interfaces[i]);
			
			return __thread.asVMObjectArray(__thread.resolveClass(
				"[Lcc/squirreljme/jvm/mle/brackets/TypeBracket;"), rv);
		}
	}, 
	
	/** {@link TypeShelf#isArray(TypeBracket)}. */
	IS_ARRAY("isArray:(Lcc/squirreljme/jvm/mle/brackets/TypeBracket;)Z")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return MLEType.__type(__args[0]).getSpringClass().isArray();
		}
	},
	
	/** {@link TypeShelf#isAssignableFrom(TypeBracket, TypeBracket)}. */
	IS_ASSIGNABLE_FROM("isAssignableFrom:(Lcc/squirreljme/jvm/mle/" +
		"brackets/TypeBracket;Lcc/squirreljme/jvm/mle/brackets/TypeBracket;)Z")
	{
		/**
		 * {@inheritDoc}
		 * @since 2021/02/07
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			if (MLEType.__type(__args[0]).getSpringClass().isAssignableFrom(
				MLEType.__type(__args[1]).getSpringClass()))
				return 1;
			return 0;
		}
	},
	
	/** {@link TypeShelf#isEnum(TypeBracket)}. */
	IS_ENUM("isEnum:(Lcc/squirreljme/jvm/mle/brackets/TypeBracket;)Z")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/28
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return MLEType.__type(__args[0]).getSpringClass().isEnum();
		}
	},
	
	/** {@link TypeShelf#isInterface(TypeBracket)}. */
	IS_INTERFACE("isInterface:(Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;)Z")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return MLEType.__type(__args[0]).getSpringClass().flags()
				.isInterface();
		}
	},
	
	/** {@link TypeShelf#isPrimitive(TypeBracket)}. */
	IS_PRIMITIVE("isPrimitive:(Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;)Z")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return MLEType.__type(__args[0]).getSpringClass().name()
				.isPrimitive();
		}
	},
	
	/** {@link TypeShelf#objectType(Object)}. */
	OBJECT_TYPE("objectType:(Ljava/lang/Object;)" +
		"Lcc/squirreljme/jvm/mle/brackets/TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new TypeObject(__thread.machine, __thread.loadClass(
				MLEType.__notNullObject(__args[0]).type().name().toString()));
		}
	},
	
	/** {@link TypeShelf#runtimeName(TypeBracket)}. */
	RUNTIME_NAME("runtimeName:(Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;)Ljava/lang/String;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return MLEType.__type(__args[0]).getSpringClass()
				.name().toRuntimeString();
		}
	},
	
	/** {@link TypeShelf#superClass(TypeBracket)}. */
	SUPER_CLASS("superClass:(Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;)Lcc/squirreljme/jvm/mle/brackets/TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			SpringClass superClass = MLEType.__type(__args[0]).getSpringClass()
				.superClass();
			
			if (superClass == null)
				return SpringNullObject.NULL;
			return new TypeObject(__thread.machine, superClass);
		}
	},
	
	/** {@link TypeShelf#typeOfBoolean()}. */
	TYPE_OF_BOOLEAN("typeOfBoolean:()Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new TypeObject(__thread.machine, __thread.loadClass(
				ClassName.fromPrimitiveType(PrimitiveType.BOOLEAN)));
		}
	},
	
	/** {@link TypeShelf#typeOfByte()}. */
	TYPE_OF_BYTE("typeOfByte:()Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new TypeObject(__thread.machine, __thread.loadClass(
				ClassName.fromPrimitiveType(PrimitiveType.BYTE)));
		}
	},
	
	/** {@link TypeShelf#typeOfCharacter()}. */
	TYPE_OF_CHARACTER("typeOfCharacter:()Lcc/squirreljme/jvm/mle/" +
		"brackets/TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new TypeObject(__thread.machine, __thread.loadClass(
				ClassName.fromPrimitiveType(PrimitiveType.CHARACTER)));
		}
	},
	
	/** {@link TypeShelf#typeOfDouble()}. */
	TYPE_OF_DOUBLE("typeOfDouble:()Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new TypeObject(__thread.machine, __thread.loadClass(
				ClassName.fromPrimitiveType(PrimitiveType.DOUBLE)));
		}
	},
	
	/** {@link TypeShelf#typeOfFloat()}. */
	TYPE_OF_FLOAT("typeOfFloat:()Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new TypeObject(__thread.machine, __thread.loadClass(
				ClassName.fromPrimitiveType(PrimitiveType.FLOAT)));
		}
	},
	
	/** {@link TypeShelf#typeOfInteger()}. */
	TYPE_OF_INTEGER("typeOfInteger:()Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new TypeObject(__thread.machine, __thread.loadClass(
				ClassName.fromPrimitiveType(PrimitiveType.INTEGER)));
		}
	},
	
	/** {@link TypeShelf#typeOfLong()}. */
	TYPE_OF_LONG("typeOfLong:()Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new TypeObject(__thread.machine, __thread.loadClass(
				ClassName.fromPrimitiveType(PrimitiveType.LONG)));
		}
	},
	
	/** {@link TypeShelf#typeOfShort()}. */
	TYPE_OF_SHORT("typeOfShort:()Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new TypeObject(__thread.machine, __thread.loadClass(
				ClassName.fromPrimitiveType(PrimitiveType.SHORT)));
		}
	},
	
	/** {@link TypeShelf#typeToClass(TypeBracket)}. */
	TYPE_TO_CLASS("typeToClass:(Lcc/squirreljme/jvm/mle/brackets/" +
		"TypeBracket;)Ljava/lang/Class;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return __thread.asVMObject(MLEType.__type(__args[0])
				.getSpringClass());
		}
	},
	
	/* End. */
	;
	
	/** The dispatch key. */
	protected final String key;
	
	/**
	 * Initializes the dispatcher info.
	 *
	 * @param __key The key.
	 * @throws NullPointerException On null arguments.
	 * @since 2020/06/18
	 */
	MLEType(String __key)
		throws NullPointerException
	{
		if (__key == null)
			throw new NullPointerException("NARG");
		
		this.key = __key;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2020/06/18
	 */
	@Override
	public String key()
	{
		return this.key;
	}
	
	/**
	 * Checks if the object is {@code null}.
	 * 
	 * @param __object The object to check.
	 * @return The object.
	 * @throws SpringMLECallError If is null.
	 * @since 2020/06/22
	 */
	static SpringObject __notNullObject(Object __object)
		throws SpringMLECallError
	{
		if (__object == null || SpringNullObject.NULL == __object)
			throw new SpringMLECallError("Null object.");
		
		return (SpringObject)__object;
	}
	
	/**
	 * Checks if this is a simple object.
	 * 
	 * @param __object The object to check.
	 * @return The simple object.
	 * @throws SpringMLECallError If not a simple object.
	 * @since 2020/06/22
	 */
	static SpringSimpleObject __simple(Object __object)
	{
		if (!(__object instanceof SpringSimpleObject))
			throw new SpringMLECallError("Not a SpringSimpleObject.");
		
		return (SpringSimpleObject)__object; 
	}
	
	/**
	 * Checks if this is a {@link TypeObject}.
	 * 
	 * @param __object The object to check.
	 * @return As a {@link TypeObject} if this is one.
	 * @throws SpringMLECallError If this is not a {@link TypeObject}.
	 * @since 2020/06/22
	 */
	static TypeObject __type(Object __object)
		throws SpringMLECallError
	{
		if (!(__object instanceof TypeObject))
			throw new SpringMLECallError("Not a TypeObject.");
		
		return (TypeObject)__object; 
	}
}
