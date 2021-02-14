// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.vm.summercoat;

import cc.squirreljme.emulator.profiler.ProfiledThread;
import cc.squirreljme.emulator.profiler.ProfilerSnapshot;
import cc.squirreljme.emulator.vm.VMException;
import cc.squirreljme.jvm.SupervisorPropertyIndex;
import cc.squirreljme.jvm.SystemCallError;
import cc.squirreljme.jvm.SystemCallIndex;
import cc.squirreljme.jvm.summercoat.ld.mem.ReadableMemoryInputStream;
import cc.squirreljme.jvm.summercoat.ld.mem.WritableMemory;
import cc.squirreljme.runtime.cldc.debug.CallTraceElement;
import cc.squirreljme.runtime.cldc.debug.CallTraceUtils;
import cc.squirreljme.runtime.cldc.debug.Debugging;
import cc.squirreljme.runtime.cldc.util.IntegerArrayList;
import dev.shadowtail.classfile.nncc.ArgumentFormat;
import dev.shadowtail.classfile.nncc.InvalidInstructionException;
import dev.shadowtail.classfile.nncc.NativeCode;
import dev.shadowtail.classfile.nncc.NativeInstruction;
import dev.shadowtail.classfile.nncc.NativeInstructionType;
import dev.shadowtail.classfile.xlate.CompareType;
import dev.shadowtail.classfile.xlate.DataType;
import dev.shadowtail.classfile.xlate.MathType;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

/**
 * This represents a native CPU which may run within its own thread to
 * execute code that is running from within the virtual machine.
 *
 * @since 2019/04/21
 */
public final class NativeCPU
	implements Runnable
{
	/**
	 * {@squirreljme.property cc.squirreljme.summercoat.debug=boolean
	 * Should SummerCoat print lots of debugging information?}
	 */
	public static final boolean ENABLE_DEBUG =
		Boolean.getBoolean("cc.squirreljme.summercoat.debug");
	
	/** Maximum amount of CPU registers. */
	public static final int MAX_REGISTERS =
		64;
	
	/** The size of the method cache. */
	public static final int METHOD_CACHE =
		2048;
	
	/** Spill over protection for the cache. */
	public static final int METHOD_CACHE_SPILL =
		1024;
	
	/** The number of execution slices to store. */
	public static final int MAX_EXECUTION_SLICES =
		32;
	
	/** The maximum number of popped slices to store. */
	public static final int MAX_POPPED_SLICE_STORE =
		8;
	
	/** Limit the number of frames that can be entered. */
	private static final int _FRAME_LIMIT = 
		64;
	
	/** Threshhold for too many debug points */
	private static final int _POINT_THRESHOLD =
		65536;
	
	/** The machine state. */
	protected final MachineState state;
	
	/** The memory to read/write from. */
	protected final WritableMemory memory;
	
	/** The profiler to use. */
	protected final ProfiledThread profiler;
	
	/** Virtual CPU id. */
	protected final int vcpuid;
	
	/** The array base. */
	protected final int arrayBase;
	
	/** Virtual machine attributes handle. */
	protected final MemHandle vmAttribHandle;
	
	/** Stack frames. */
	private final LinkedList<CPUFrame> _frames =
		new LinkedList<>();
	
	/** System call error states for this CPU. */
	final int[] _sysCallErrors =
		new int[SystemCallIndex.NUM_SYSCALLS];
	
	/** Super visor properties. */
	private final int[] _supervisorproperties =
		new int[SupervisorPropertyIndex.NUM_PROPERTIES];
	
	/** Execution slices which came from the popped frame. */
	private final Deque<Deque<ExecutionSlice>> _sopf =
		(NativeCPU.ENABLE_DEBUG ? new LinkedList<Deque<ExecutionSlice>>() : null);
	
	/** IPC Exception register. */
	private int _ipcexception;
	
	/**
	 * Initializes the native CPU.
	 *
	 * @param __ms The machine state.
	 * @param __mem The memory space.
	 * @param __vcid Virtual CPU id.
	 * @param __ps The profiler to use.
	 * @param __arrayBase The array base size.
	 * @param __vmAttrHandle Virtual machine attributes handle.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/04/21
	 */
	public NativeCPU(MachineState __ms, WritableMemory __mem, int __vcid,
		ProfilerSnapshot __ps, int __arrayBase, MemHandle __vmAttrHandle)
		throws NullPointerException
	{
		if (__ms == null || __mem == null || __vmAttrHandle == null)
			throw new NullPointerException("NARG");
		
		this.state = __ms;
		this.memory = __mem;
		this.vcpuid = __vcid;
		this.profiler = (__ps == null ? null :
			__ps.measureThread("cpu-" + __vcid));
		this.arrayBase = __arrayBase;
		this.vmAttribHandle = __vmAttrHandle;
	}
	
	/**
	 * Enters the given frame for the given address.
	 *
	 * @param __movePool Move the pool register?
	 * @param __pc The address of the frame.
	 * @param __poolPointer The pool pointer to set, if not moving.
	 * @param __args Arguments to the frame
	 * @return The newly created frame.
	 * @since 2019/04/21
	 */
	public final CPUFrame enterFrame(boolean __movePool, int __pc,
		int __poolPointer, int... __args)
	{
		// Debug this
		if (NativeCPU.ENABLE_DEBUG)
			Debugging.debugNote(
				"SC::enterFrame(mP=%s, m/p=%#08x/%#08x, %s)",
				__movePool, __pc, __poolPointer,
				IntegerArrayList.asList(__args));
		
		// Old frame, to source globals from
		LinkedList<CPUFrame> frames = this._frames;
		
		// Do not go too deep
		if (frames.size() >= NativeCPU._FRAME_LIMIT)
			throw new VMException("Frame limit reached.");
		
		CPUFrame lastframe = frames.peekLast();
		
		// Setup new frame
		CPUFrame rv = new CPUFrame(this.state.memHandles, this.arrayBase);
		rv._pc = __pc;
		rv._entrypc = __pc;
		rv._lastpc = __pc;
		
		// Add to frame list
		frames.addLast(rv);
		
		// Seed initial registers, if valid
		int[] dest = rv.getRegisters();
		if (lastframe != null)
		{
			// Copy globals
			int[] src = lastframe.getRegisters();
			for (int i = 0; i < NativeCode.LOCAL_REGISTER_BASE; i++)
				dest[i] = src[i];
			
			// Set the pool register to the next pool register value (classic)
			if (__movePool)
				dest[NativeCode.POOL_REGISTER] =
					src[NativeCode.NEXT_POOL_REGISTER];
			
			// Copy task register.
			rv._taskid = lastframe._taskid;
		}
		
		// Copy the arguments to the argument slots
		for (int i = 0, o = NativeCode.ARGUMENT_REGISTER_BASE,
			n = __args.length; i < n; i++, o++)
			dest[o] = __args[i];
		
		// The pool pointer is explicitly set (modern)
		if (!__movePool)
			dest[NativeCode.POOL_REGISTER] = __poolPointer;
		
		// Clear zero
		dest[0] = 0;
		
		// Use this frame
		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/04/21
	 */
	@Override
	public final void run()
	{
		this.run(0);
	}
	
	/**
	 * Runs anything after this frame.
	 *
	 * @param __fl The frame limit.
	 * @since 2019/06/13
	 */
	public final void run(int __fl)
	{
		try
		{
			this.runWithoutCatch(0);
		}
		
		// Failed
		catch (VMException|InvalidInstructionException e)
		{
			// Spacer
			System.err.println("********************************************");
			
			// Only print execution slices if debugging is enabled
			if (NativeCPU.ENABLE_DEBUG)
			{
				// Each frame has its own slices
				for (CPUFrame l : this._frames)
				{
					// Traces for this frame
					System.err.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					System.err.printf(">>>>>>>>>>>> %s%n", this.trace(l));
					
					// Print all the various execution slices
					Deque<ExecutionSlice> execslices = l._execslices;
					System.err.printf("Printing the last %d instructions:%n",
						execslices.size());
					while (!execslices.isEmpty())
						execslices.removeFirst().print(System.err);
					
					// Spacer
					System.err.println();
				}
				
				// Spacer
				System.err.println();
				
				// If there were any execution slices that came from the
				// method we called, print them out.
				Deque<Deque<ExecutionSlice>> sopf = this._sopf;
				if (sopf != null)
				{
					// Traces for this frame
					System.err.print("++++++++++++++++++++++++++++++++++++");
					System.err.println("++++++++++++++++++++++++++++++++++++");
					
					// Print all of them
					while (!sopf.isEmpty())
					{
						// Spacer
						System.err.println(
							"++++++++++++++++++++++++++++++++++++++++++++");
						System.err.println("Slices of a popped frame:");
						
						// Print out
						Deque<ExecutionSlice> qq = sopf.removeFirst();
						while (!qq.isEmpty())
							qq.removeFirst().print(System.err);
						
						// Spacer
						System.err.println();
					}
				}
					
				// Spacer
				System.err.println(
					"--------------------------------------------");
			}
			
			// Print the call trace
			CallTraceUtils.printStackTrace(System.err,
				Objects.toString(e.getMessage(), "Fatal Exception"),
				this.trace(), null, null, 0);
			
			// Spacer
			System.err.println("********************************************");
			
			// {@squirreljme.error AE02 Virtual machine exception. (The failing
			// instruction)}
			throw new VMException(
				String.format("AE02 %s", this.traceTop()), e);
		}
	}
	
	/**
	 * Runs anything after this frame, no catching is performed.
	 *
	 * @param __fl The frame limit.
	 * @since 2019/04/21
	 */
	public final void runWithoutCatch(int __fl)
	{
		// Read the CPU stuff
		final WritableMemory memory = this.memory;
		boolean reload = true;
		ProfiledThread profiler = this.profiler;
		
		// Frame specific info
		CPUFrame nowframe = null;
		int[] lr = null;
		int pc = -1;
		
		// Per operation handling
		final int[] args = new int[6];
		
		// Method cache to reduce tons of method reads
		final byte[] icache = new byte[NativeCPU.METHOD_CACHE];
		int lasticache = -(NativeCPU.METHOD_CACHE_SPILL + 1);
		
		// Debug point counter
		int pointcounter = 0;
		
		// Execution is effectively an infinite loop
		LinkedList<CPUFrame> frames = this._frames;
		for (int frameat = frames.size(), lastframe = -1; frameat >= __fl;
			frameat = frames.size())
		{
			// Reload parameters?
			if ((reload |= (lastframe != frameat)))
			{
				// Before dumping this frame, store old info
				if (nowframe != null)
					nowframe._pc = pc;
				
				// Get current frame, stop execution if there is nothing
				// left to execute
				nowframe = frames.peekLast();
				if (nowframe == null)
					return;
				
				// Load stuff needed for execution
				lr = nowframe.getRegisters();
				pc = nowframe._pc;
				
				// Used to auto-detect frame change
				lastframe = frameat;
				
				// No longer reload information
				reload = false;
			}
			
			// For a bit faster execution of the method, cache a bunch of
			// the code that is being executed in memory. Constantly performing
			// the method calls to read single bytes of memory is a bit so, so
			// this should hopefully improve performance slightly.
			int pcdiff = pc - lasticache;
			if (pcdiff < 0 || pcdiff >= NativeCPU.METHOD_CACHE_SPILL)
			{
				memory.memReadBytes(pc, icache, 0, NativeCPU.METHOD_CACHE);
				lasticache = pc;
			}
			
			// Calculate last PC base address
			int bpc = pc - lasticache;
			
			// Always set PC address for debugging frames
			nowframe._pc = pc;
			
			// Read operation
			nowframe._lastpc = pc;
			int op = icache[bpc] & 0xFF;
			
			// Set the last native operation used
			nowframe._lastNativeOp = op;
			
			// Reset all input arguments
			Arrays.fill(args, 0);
			
			// Register list, just one is used everywhere
			int[] reglist = null;
			
			// Load arguments for this instruction
			ArgumentFormat[] af = NativeInstruction.argumentFormat(op);
			int rargp = bpc + 1;
			for (int i = 0, n = af.length; i < n; i++)
				switch (af[i])
				{
						// Variable sized entries, may be pool values
					case VUINT:
					case VUREG:
					case VPOOL:
					case VJUMP:
						{
							// Long value?
							int base = (icache[rargp++] & 0xFF);
							if ((base & 0x80) != 0)
							{
								base = ((base & 0x7F) << 8);
								base |= (icache[rargp++] & 0xFF);
							}
							
							// Set
							if (af[i] == ArgumentFormat.VJUMP)
								args[i] = (short)(base |
									((base & 0x4000) << 1));
							else
								args[i] = base;
							
							// {@squirreljme.error AE03 Reference to register
							// which is out of range of maximum number of
							// registers. (The operation; The register index;
							// The argument formats; The decoded arguments)}
							if (af[i] == ArgumentFormat.VUREG &&
								(base < 0 || base >= NativeCode.MAX_REGISTERS))
								throw new VMException(String.format(
									"AE03 %s %d %s %s",
									NativeInstruction.mnemonic(op), base,
									Arrays.asList(af),
									IntegerArrayList.asList(args)
										.subList(0, af.length)));
						}
						break;
					
						// Register list.
					case REGLIST:
						{
							// Wide
							int count = (icache[rargp++] & 0xFF);
							if ((count & 0x80) != 0)
							{
								count = ((count & 0x7F) << 8) |
									(icache[rargp++] & 0xFF);
								
								// Read values
								reglist = new int[count];
								for (int r = 0; r < count; r++)
									reglist[r] =
										((icache[rargp++] & 0xFF) << 8) |
										(icache[rargp++] & 0xFF);
							}
							// Narrow
							else
							{
								reglist = new int[count];
								
								// Read values
								for (int r = 0; r < count; r++)
									reglist[r] = (icache[rargp++] & 0xFF);
							}
						}
						break;
					
						// 32-bit integer/float
					case INT32:
					case FLOAT32:
						args[i] = ((icache[rargp++] & 0xFF) << 24) |
							((icache[rargp++] & 0xFF) << 16) |
							((icache[rargp++] & 0xFF) << 8) |
							((icache[rargp++] & 0xFF));
						break;
					
					default:
						throw new todo.OOPS(af[i].name());
				}
			
			// Determine the encoding
			int encoding = NativeInstruction.encoding(op);
			
			// Set first point flag
			if (encoding == NativeInstructionType.DEBUG_ENTRY)
				pointcounter = 0;
			
			// Only track slices if we are debugging
			if (NativeCPU.ENABLE_DEBUG)
			{
				// Get slice for this instruction
				ExecutionSlice el = ExecutionSlice.of(this.trace(nowframe),
					nowframe, op, args, af.length, reglist);
				
				// Add to previous instructions, do not exceed slice limits
				Deque<ExecutionSlice> execslices = nowframe._execslices;
				if (execslices.size() >= NativeCPU.MAX_EXECUTION_SLICES)
					execslices.removeFirst();
				execslices.addLast(el);
			
				// In debug points check to see if the execution seems to
				// be stuck in here (really long methods)
				if (encoding == NativeInstructionType.DEBUG_POINT)
				{
					// Seems to be stuck?
					boolean doprint = false;
					if (pointcounter++ >= NativeCPU._POINT_THRESHOLD)
					{
						doprint = true;
						pointcounter = 0;
					}
					
					// Print the point?
					if (doprint)
						el.print();
				}
				
				// Print current operation
				Debugging.debugNote("@#> %s", this.traceTop());
			}
			
			// By default the next instruction is the address after all
			// arguments have been read
			int nextpc = lasticache + rargp;
			
			// Handle the operation
			switch (encoding)
			{
					// CPU Ping
				case NativeInstructionType.PING:
					if (NativeCPU.ENABLE_DEBUG)
					{
						CallTraceUtils.printStackTrace(System.err,
							String.format("PING! %04Xh: %s",
								args[0], (args[1] == 0 ? "" :
								this.__loadUtfString(nowframe.pool(args[1])))),
							this.trace(),
							null, null, 0);
					}
					break;
					
					// CPU Breakpoint
				case NativeInstructionType.BREAKPOINT:
				case NativeInstructionType.BREAKPOINT_MARKED:
					// Marker for the breakpoint
					String mark = (op == NativeInstructionType.BREAKPOINT ?
						"Un" : Integer.toString(args[0], 16)
							.toUpperCase() + "h");
				
					// If profiling, immediately enter the frame to signal
					// a break point then exit it
					if (profiler != null)
					{
						String bit = "<breakpoint?" + mark + ">";
						profiler.enterFrame(bit, bit, bit);
						profiler.exitFrame();
					}
					
					// Is there a stored note?
					int noteId = (op == NativeInstructionType
						.BREAKPOINT_MARKED ? args[1] : 0);
					String note = (noteId == 0 ? "" :
						this.__loadUtfString(nowframe.pool(noteId)));
					
					throw new VMException(String.format(
						"Breakpoint Hit (%s: %s)!", mark, note));
				
					// Debug entry point of method
				case NativeInstructionType.DEBUG_ENTRY:
					this.__debugEntry(nowframe, args[0], args[1], args[2],
						args[3]);
					break;
					
					// Debug exit of method
				case NativeInstructionType.DEBUG_EXIT:
					this.__debugExit(nowframe);
					break;
					
					// Debug point in method.
				case NativeInstructionType.DEBUG_POINT:
					this.__debugPoint(nowframe, args[0], args[1], args[2]);
					break;
					
					// Atomic compare, get, and set
				case NativeInstructionType.ATOMIC_COMPARE_GET_AND_SET:
					synchronized (memory)
					{
						// Read parameters
						int check = lr[args[0]],
							set = lr[args[2]],
							addr = lr[args[3]],
							off = args[4];
						
						// Read value here
						int read = memory.memReadInt(addr + off);
						
						// Is the value the same?
						if (read == check)
							memory.memWriteInt(addr + off, set);
						
						// Set the read value before check
						lr[args[1]] = read;
						
						// Debug
						/*if (ENABLE_DEBUG)
							todo.DEBUG.note("%08x(%d) = %d ? %d = %d",
								addr, off, read, check, set);*/
					}
					break;
				
					// Atomic decrement and get
				case NativeInstructionType.ATOMIC_INT_DECREMENT_AND_GET:
					synchronized (memory)
					{
						// The address to load from/store to
						int addr = lr[args[1]],
							off = args[2];
						
						// Read, increment, and store
						int newval;
						memory.memWriteInt(addr + off,
							(newval = memory.memReadInt(addr + off) - 1));
						
						// Store the value after the decrement
						lr[args[0]] = newval;
					}
					break;
					
					// Atomic increment
				case NativeInstructionType.ATOMIC_INT_INCREMENT:
					synchronized (memory)
					{
						// The address to load from/store to
						int addr = lr[args[0]],
							off = args[1];
						
						// Read, increment, and store
						int oldv;
						memory.memWriteInt(addr + off,
							(oldv = memory.memReadInt(addr + off)) + 1);
						
						// Debug
						/*if (ENABLE_DEBUG)
							todo.DEBUG.note("%08x(%d) += %d + 1 = %d",
								addr, off, oldv, oldv + 1);*/
					}
					break;
				
					// Copy
				case NativeInstructionType.COPY:
					lr[args[1]] = lr[args[0]];
					break;
					
					// Compare integers and possibly jump
				case NativeInstructionType.IF_ICMP:
					{
						// Parts
						int a = lr[args[0]],
							b = lr[args[1]];
						
						// Compare
						boolean branch;
						CompareType ct;
						switch ((ct = CompareType.of(op & 0b111)))
						{
							case EQUALS:
								branch = (a == b); break;
							case NOT_EQUALS:
								branch = (a != b); break;
							case LESS_THAN:
								branch = (a < b); break;
							case LESS_THAN_OR_EQUALS:
								branch = (a <= b); break;
							case GREATER_THAN:
								branch = (a > b); break;
							case GREATER_THAN_OR_EQUALS:
								branch = (a >= b); break;
							case TRUE:
								branch = true; break;
							case FALSE:
								branch = false; break;
							
							default:
								throw new todo.OOPS();
						}
						
						// Branching?
						if (branch)
							nextpc = pc + args[2];
					}
					break;
					
					// If value equal to constant
				case NativeInstructionType.IFEQ_CONST:
					{
						// Branching? Remember that jumps are relative
						if (lr[args[0]] == args[1])
							nextpc = pc + args[2];
					}
					break;
					
					// Invoke a pointer
				case NativeInstructionType.INVOKE:
					{
						// Load values into the register list
						for (int i = 0, n = reglist.length; i < n; i++)
							reglist[i] = lr[reglist[i]];
						
						// Enter the frame
						this.enterFrame(true, lr[args[0]], 0, reglist);
						
						// Entering some other frame
						reload = true;
						
						// Clear point counter
						pointcounter = 0;
					}
					break;
					
					// Invoke with a ExecutionPointer+Pool
				case NativeInstructionType.INVOKE_POINTER_AND_POOL:
					{
						// Load values into the register list
						for (int i = 0, n = reglist.length; i < n; i++)
							reglist[i] = lr[reglist[i]];
						
						// Enter the frame
						this.enterFrame(false, lr[args[0]],
							lr[args[1]], reglist);
						
						// Entering some other frame
						reload = true;
						
						// Clear point counter
						pointcounter = 0;
					}
					break;
					
					// Load from constant pool
				case NativeInstructionType.LOAD_POOL:
					lr[args[1]] = nowframe.pool(args[0]);
					
					// Debug
					if (NativeCPU.ENABLE_DEBUG)
						Debugging.debugNote("Pool#%d %d/%#08x -> %d",
							args[0], lr[args[1]], lr[args[1]], args[1]);
					break;
					
					// Integer math
				case NativeInstructionType.MATH_CONST_INT:
				case NativeInstructionType.MATH_REG_INT:
					{
						// Parts
						int a = lr[args[0]],
							b = (((op & 0x80) != 0) ? args[1] : lr[args[1]]),
							c;
						
						// Operation to execute
						MathType mt;
						switch ((mt = MathType.of(op & 0xF)))
						{
							case ADD:		c = a + b; break;
							case SUB:		c = a - b; break;
							case MUL:		c = a * b; break;
							case DIV:		c = a / b; break;
							case REM:		c = a % b; break;
							case NEG:		c = -a; break;
							case SHL:		c = a << b; break;
							case SHR:		c = a >> b; break;
							case USHR:		c = a >>> b; break;
							case AND:		c = a & b; break;
							case OR:		c = a | b; break;
							case XOR:		c = a ^ b; break;
							case SIGNX8:	c = (byte)a; break;
							case SIGNX16:	c = (short)a; break;
							
							case CMPL:
							case CMPG:
								c = (a < b ? -1 : (a == b ? 0 : 1));
								break;
							
							default:
								throw new todo.OOPS();
						}
						
						// Set result
						lr[args[2]] = c;
					}
					break;
					
					// Read/write Memory Handles
				case NativeInstructionType.MEM_HANDLE_OFF_REG:
				case NativeInstructionType.MEM_HANDLE_OFF_ICONST:
					{
						// Is this a load operation?
						boolean load = ((op & 0b1000) != 0);
						
						// The handle to read from
						MemHandle handle =
							this.state.memHandles.get(lr[args[1]]);
						int off = lr[args[2]];
						
						// Potential load/store value
						int read = 0;
						int write = lr[args[0]];
						
						// Depends on the type
						DataType dt = DataType.of(op & 0b0111);
						switch (dt)
						{
							case BYTE:
								if (load)
									read = (byte)handle.memReadByte(off);
								else
									handle.memWriteByte(off, write);
								break;
							
							case CHARACTER:
								if (load)
									read = handle.memReadShort(off) & 0xFFF;
								else
									handle.memWriteShort(off, write);
								break;
							
							case SHORT:
								if (load)
									read = (short)handle.memReadShort(off);
								else
									handle.memWriteShort(off, write);
								break;
								
							case OBJECT:
							case INTEGER:
							case FLOAT:
								if (load)
									read = handle.memReadInt(off);
								else
									handle.memWriteInt(off, write);
								break;
							
							default:
								throw Debugging.oops(dt);
						}
						
						// Store in value
						if (load)
							lr[args[0]] = read;
					}
					break;
				
					// Read/Write raw memory
				case NativeInstructionType.MEMORY_OFF_REG:
				case NativeInstructionType.MEMORY_OFF_ICONST:
					{
						// Is this a load operation?
						boolean load = ((op & 0b1000) != 0);
						
						// The address to load from/store to
						long base = (lr[args[1]] & 0xFFFFFFFFL) |
							(((long)lr[args[2]]) << 32L);
						int offs = (((op & 0x80) != 0) ? args[2] :
							lr[args[2]]);
						long addr = base + offs;
						
						// Not currently valid!
						DataType dt = DataType.of(op & 0b0111);
						if (true)
							throw new VMException(String.format(
								"Invalid MemAccess: %s " +
									"@%#08x (%#08x + %d): %s %s",
								(load ? "LOAD" : "STORE"),
								addr, base, offs,
								NativeInstruction.mnemonic(op),
								this.traceTop()));
						
						// Loads
						if (load)
						{
							// Load value
							int v;
							switch (dt)
							{
								case BYTE:
									v = (byte)memory.memReadByte(addr);
									break;
									
								case SHORT:
									v = (short)memory.memReadShort(addr);
									break;
									
								case CHARACTER:
									v = memory.memReadShort(addr) & 0xFFFF;
									break;
									
								case OBJECT:
								case INTEGER:
								case FLOAT:
									v = memory.memReadInt(addr);
									break;
								
								case LONG:
								case DOUBLE:
									if (true)
										throw Debugging.todo();
									v = (int)memory.memReadLong(addr);
									break;
									
									// Unknown
								default:
									throw new todo.OOPS(dt.name());
							}
							
							// Set value
							lr[args[0]] = v;
							
							// Debug
							/*if (ENABLE_DEBUG)
								todo.DEBUG.note(
									"%c %08x+%d (%08x) -> %d (%08x)",
									(isjava ? 'J' : 'N'),
									base, offs, addr, v, v);*/
						}
						
						// Stores
						else
						{
							// Value to store
							int v = lr[args[0]];
							
							// Store
							switch (dt)
							{
								case BYTE:
									memory.memWriteByte(addr, v);
									break;
									
								case SHORT:
									memory.memWriteShort(addr, v);
									break;
									
								case CHARACTER:
									memory.memWriteShort(addr, v);
									break;
								
								case OBJECT:
								case INTEGER:
								case FLOAT:
									memory.memWriteInt(addr, v);
									break;
									
									// Unknown
								default:
									throw new todo.OOPS(dt.name());
							}
							
							// Debug
							/*if (ENABLE_DEBUG)
								todo.DEBUG.note(
									"%c %08x+%d (%08x) <- %d (%08x)",
									(isjava ? 'J' : 'N'),
									base, offs, addr, v, v);*/
						}
					}
					break;
					
					// Return from method call
				case NativeInstructionType.RETURN:
					{
						// Go up frame
						CPUFrame was = frames.removeLast(),
							now = frames.peekLast();
						
						// {@squirreljme.error AE0m Return from the main frame
						// without using a system call to exit.
						// (The return value pair; The exception register)}
						if (now == null)
							throw new VMException(String.format(
								"AE0m [%d, %d] @%08x",
								was.get(NativeCode.RETURN_REGISTER),
								was.get(NativeCode.RETURN_REGISTER + 1),
								was.get(
									NativeCode.EXCEPTION_REGISTER)));
						
						// Capture the execution slices of this popped frame
						Deque<Deque<ExecutionSlice>> sopf = this._sopf;
						if (sopf != null)
						{
							// Add these slices
							sopf.addLast(was._execslices);
							
							// If there are too many, remove them
							if (sopf.size() > NativeCPU.MAX_POPPED_SLICE_STORE)
								sopf.removeFirst();
						}
						
						// We are going back onto a frame so copy all
						// the globals which were set since they are meant to
						// be global!
						int[] wr = was.getRegisters(),
							nr = now.getRegisters();
						
						// Copy globals
						for (int i = 0; i < NativeCode.LOCAL_REGISTER_BASE;
							i++)
						{
							// Ignore the pool register because if it is
							// replaced then it will just explode and
							// cause issues for the parent method
							if (i == NativeCode.POOL_REGISTER)
								continue;
							
							// Reset the next pool register
							else if (i == NativeCode.NEXT_POOL_REGISTER)
							{
								nr[i] = 0;
								break;
							}
							
							// Copy otherwise
							else
								nr[i] = wr[i];
						}
						
						// A reload is done as the frame has changed
						reload = true;
						
						// Clear point counter
						pointcounter = 0;
						
						// Debug
						if (now != null && ENABLE_DEBUG)
							Debugging.debugNote(
								"<<<< Return: %08x: " +
									"[%#08x:%08x ex:%#08x] <<<<<<<<<<<<<<%n",
								now._pc,
								now.get(NativeCode.RETURN_REGISTER + 1),
								now.get(NativeCode.RETURN_REGISTER),
								now.get(NativeCode.EXCEPTION_REGISTER));
					}
					break;
				
					// System call
				case NativeInstructionType.SYSTEM_CALL:
					{
						// The arguments to the system calls are in registers
						// so they must be extracted first before they can be
						// known
						int nrl = reglist.length;
						int[] sargs = new int[nrl];
						for (int i = 0; i < nrl; i++)
							sargs[i] = lr[reglist[i]];
						
						// Get the system call ID
						short syscallid = (short)lr[args[0]];
						
						// Handle system call as is from the supervisor
						// IPC Exception load/store is not included
						// IPC Calls are always virtualized even in supervisor.
						CPUFrame was = frames.getLast();
						if ((was._taskid == 0 ||
							syscallid == SystemCallIndex.EXCEPTION_LOAD ||
							syscallid == SystemCallIndex.EXCEPTION_STORE))
						{
							// If profiling, profile the handling of the
							// system call in a sub-frame
							if (profiler != null)
								profiler.enterFrame("<syscall>",
									Integer.toString(syscallid),
									"(IIIIIIII)I");
							
							// Set the return register to whatever system call
							// was used
							try
							{
								long rv = this.__sysCall(syscallid, sargs);
								
								lr[NativeCode.RETURN_REGISTER] = (int)rv;
								lr[NativeCode.RETURN_REGISTER + 1] =
									(int)(rv >>> 32);
							}
							
							// If profiling, that frame needs to exit always!
							finally
							{
								if (profiler != null)
									profiler.exitFrame();
							}
						}
						
						// Otherwise jump into supervisor and handle the
						// system call on behalf of the task
						else
						{
							// Enter the frame
							int[] svp = this._supervisorproperties;
							CPUFrame f = this.enterFrame(true, svp[SupervisorPropertyIndex.
									TASK_SYSCALL_METHOD_HANDLER], 0);
							
							// Set frame's task ID to zero
							f._taskid = 0;
							
							// Set required registers
							f.set(NativeCode.POOL_REGISTER,
								svp[SupervisorPropertyIndex.
									TASK_SYSCALL_METHOD_POOL_POINTER]);
							f.set(NativeCode.STATIC_FIELD_REGISTER,
								svp[SupervisorPropertyIndex.
									TASK_SYSCALL_STATIC_FIELD_POINTER]);
							
							// Setup call: taskid + oldsfp + sysid + 8 syscall
							f.set(
								NativeCode.ARGUMENT_REGISTER_BASE + 0,
								was._taskid);
							f.set(
								NativeCode.ARGUMENT_REGISTER_BASE + 1,
								was.get(
									NativeCode.STATIC_FIELD_REGISTER));
							f.set(
								NativeCode.ARGUMENT_REGISTER_BASE + 2,
								syscallid);
							
							// Forward system call arguments
							for (int x = 0, xn = sargs.length; x < xn; x++)
								f.set(NativeCode.ARGUMENT_REGISTER_BASE
									+ 3 + x, sargs[x]);
							
							// Setup for frame enter
							reload = true;
							pointcounter = 0;
						}
					}
					break;
					
					// Count reference up
				case NativeInstructionType.MEM_HANDLE_COUNT_UP:
					this.state.memHandles.get(lr[args[0]]).count(true);
					break;
				
					// Count reference down
				case NativeInstructionType.MEM_HANDLE_COUNT_DOWN:
					lr[args[1]] = this.state.memHandles.get(lr[args[0]])
						.count(false);
					break;
					
					// {@squirreljme.error AE0n Invalid instruction.}
				default:
					throw new VMException("AE0n " +
						NativeInstruction.mnemonic(op));
			}
			
			// Set next PC address
			pc = nextpc;
		}
	}
	
	/**
	 * Returns the trace of the current execution.
	 *
	 * @return The current trace.
	 * @since 2019/04/22
	 */
	public final CallTraceElement[] trace()
	{
		LinkedList<CPUFrame> frames = this._frames;
		
		// Need to store all the frames
		int numframes = frames.size();
		CallTraceElement[] rv = new CallTraceElement[numframes];
		
		// Make the newest frame first!
		for (int i = numframes - 1, o = 0; i >= 0; i--, o++)
			rv[o] = this.trace(frames.get(i));
		
		return rv;
	}
	
	/**
	 * Returns the trace for the given frame.
	 *
	 * @param __f The frame to trace.
	 * @return The trace of this frame.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/04/22
	 */
	public final CallTraceElement trace(CPUFrame __f)
		throws NullPointerException
	{
		if (__f == null)
			throw new NullPointerException("NARG");
		
		// Build trace
		return new CallTraceElement(
			__f._inclass,
			__f._inmethodname,
			__f._inmethodtype,
			__f._pc, __f._insourcefile,
			__f._inline,
			__f._injop,
			__f._injpc,
			__f._taskid,
			__f._lastNativeOp);
	}
	
	/**
	 * Traces the top most frame.
	 *
	 * @return The top most frame.
	 * @since 2019/06/13
	 */
	public final CallTraceElement traceTop()
	{
		LinkedList<CPUFrame> frames = this._frames;
		
		// Only look at the top most frame
		CPUFrame top = frames.peekLast();
		if (top == null)
			return new CallTraceElement();
		return this.trace(top);
	}
	
	/**
	 * Sets the frame information string from the given pool entries.
	 *
	 * @param __f The frame.
	 * @param __pcl The class string from the pool.
	 * @param __pmn The method name from the pool.
	 * @param __pmt The method type from the pool.
	 * @param __psf The current source file.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/05/15
	 */
	private void __debugEntry(CPUFrame __f, int __pcl, int __pmn,
		int __pmt, int __psf)
		throws NullPointerException
	{
		if (__f == null)
			throw new NullPointerException("NARG");
		
		// Get the pool address
		int poolAddr = __f.get(NativeCode.POOL_REGISTER);
		
		int icl = __f.pool(__pcl),
			imn = __f.pool(__pmn),
			imt = __f.pool(__pmt),
			isf = __f.pool(__psf);
		
		// Store in state
		__f._inclassp = icl;
		__f._inmethodnamep = imn;
		__f._inmethodtypep = imt;
		__f._insourcefilep = isf;
		
		// Load strings
		String scl, smn, smt, ssf;
		WritableMemory memory = this.memory;
		__f._inclass = 
			(scl = (icl == 0 ? null : this.__loadUtfString(icl)));
		__f._inmethodname = 
			(smn = (imn == 0 ? null : this.__loadUtfString(imn)));
		__f._inmethodtype = 
			(smt = (imt == 0 ? null : this.__loadUtfString(imt)));
		__f._insourcefile = 
			(ssf = (isf == 0 ? null : this.__loadUtfString(isf)));
		
		// Enter it on the profiler
		ProfiledThread profiler = this.profiler;
		if (profiler != null)
			profiler.enterFrame(
				(scl == null ? "<AClass>" : scl),
				(smn == null ? "<AMethod>" : smn),
				(smt == null ? "<AType>" : smt));
		
		// Debug this
		if (NativeCPU.ENABLE_DEBUG)
			Debugging.debugNote("SC::enterFrame!(%s:%s %s)",
				(scl == null ? "<AClass>" : scl),
				(smn == null ? "<AMethod>" : smn),
				(smt == null ? "<AType>" : smt));
	}
	
	/**
	 * Debugs the exit of a frame.
	 *
	 * @param __f The frame to exit.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/06/30
	 */
	private final void __debugExit(CPUFrame __f)
		throws NullPointerException
	{
		if (__f == null)
			throw new NullPointerException("NARG");
		
		// Exit the frame, but if we extra exited then it is very possible
		// that the state can be messed up so do not absolutely destroy the
		// VM stuff
		ProfiledThread profiler = this.profiler;
		if (profiler != null)
			try
			{
				profiler.exitFrame();
			}
			catch (IllegalStateException e)
			{
			}
	}
	
	/**
	 * Sets the debug point in the frame.
	 *
	 * @param __f The frame.
	 * @param __sln The source line.
	 * @param __jop The Java operation.
	 * @param __jpc The Java address.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/05/15
	 */
	private final void __debugPoint(CPUFrame __f, int __sln, int __jop, int __jpc)
		throws NullPointerException
	{
		if (__f == null)
			throw new NullPointerException("NARG");
		
		__f._inline = __sln;
		__f._injop = __jop;
		__f._injpc = __jpc;
	}
	
	/**
	 * Attempts to load the given integer, without failing.
	 * 
	 * @param __addr The address to read from.
	 * @return The read value or {@code __addr}.
	 * @since 2021/01/17
	 */
	private int __loadDebugInt(int __addr)
	{
		WritableMemory memory = this.memory;
		try
		{
			return memory.memReadInt(__addr);
		}
		catch (VMMemoryAccessException e)
		{
			return __addr;
		}
	}
	
	/**
	 * Loads a UTF string from the given memory address.
	 *
	 * @param __addr The address to read from.
	 * @return The resulting string.
	 * @since 2019/05/15
	 */
	private final String __loadUtfString(int __addr)
	{
		// Read length to figure out how long the string is
		WritableMemory memory = this.memory;
		int strlen = -1;
		try
		{
			strlen = memory.memReadShort(__addr) & 0xFFFF;
			
			// Decode string data
			try (DataInputStream dis = new DataInputStream(
				new ReadableMemoryInputStream(memory, __addr,
					strlen + 2)))
			{
				return dis.readUTF();
			}
		}
		
		// Could not read string, use some other string form
		catch (IOException|VMMemoryAccessException e)
		{
			return String.format("@%08x/%d???", __addr, strlen);
		}
	}
	
	/**
	 * Internal system call handling.
	 *
	 * @param __si System call index.
	 * @param __args Arguments.
	 * @return The result.
	 * @since 2019/05/23
	 */
	private long __sysCall(short __si, int... __args)
	{
		// Error state for the last call of this type
		int[] errors = this._sysCallErrors;
		
		// Determine the error handling index of the call
		SystemCallHandler handler = SystemCallHandler.of(__si);
		int errorDx = (handler == null ? SystemCallIndex.QUERY_INDEX : __si);
		
		// The system call result
		long rv;
		int err;
		
		// Not a valid system call?
		if (handler == null)
		{
			rv = 0;
			err = SystemCallError.UNSUPPORTED_SYSTEM_CALL;
			
			if (true)
				throw Debugging.oops(__si);
		}
		
		// Use normal handling
		else
		{
			try
			{
				rv = handler.handle(this, __args);
				err = 0;
			}
			catch (VMSystemCallException e)
			{
				e.printStackTrace();
				
				rv = 0;
				err = e.error;
			}
		}
		
		// Set error state as needed
		synchronized (this)
		{
			errors[__si] = err;
		}
		
		// Debug result
		if (NativeCPU.ENABLE_DEBUG)
			Debugging.debugNote("SC::sysCall(%d, %s) -> %d (err: %d)",
				__si, IntegerArrayList.asList(__args),
				rv, err);
		
		// Use returning value
		return rv;
		/*
		// Return value with error value, to set if any
		long rv;
		int err;
		
		// Depends on the system call type
		switch (__si)
		{
				// Check if system call is supported
			case SystemCallIndex.QUERY_INDEX:
				{
					err = 0;
					switch (__args[0])
					{
						case SystemCallIndex.ARRAY_ALLOCATION_BASE:
						case SystemCallIndex.BYTE_ORDER_LITTLE:
						case SystemCallIndex.ERROR_GET:
						case SystemCallIndex.ERROR_SET:
						case SystemCallIndex.EXCEPTION_LOAD:
						case SystemCallIndex.EXCEPTION_STORE:
						case SystemCallIndex.FATAL_TODO:
						case SystemCallIndex.FRAME_TASK_ID_GET:
						case SystemCallIndex.FRAME_TASK_ID_SET:
						case SystemCallIndex.CALL_STACK_HEIGHT:
						case SystemCallIndex.CALL_STACK_ITEM:
						case SystemCallIndex.MEM_HANDLE_NEW:
						case SystemCallIndex.MEM_SET:
						case SystemCallIndex.PD_OF_STDERR:
						case SystemCallIndex.PD_OF_STDIN:
						case SystemCallIndex.PD_OF_STDOUT:
						case SystemCallIndex.PD_WRITE_BYTE:
						case SystemCallIndex.SLEEP:
						case SystemCallIndex.SUPERVISOR_BOOT_OKAY:
						case SystemCallIndex.SUPERVISOR_PROPERTY_GET:
						case SystemCallIndex.SUPERVISOR_PROPERTY_SET:
						case SystemCallIndex.TIME_MILLI_WALL:
						case SystemCallIndex.TIME_NANO_MONO:
						case SystemCallIndex.VMI_MEM_FREE:
						case SystemCallIndex.VMI_MEM_MAX:
						case SystemCallIndex.VMI_MEM_USED:
							rv = 1;
							break;
						
						default:
							rv = 0;
							break;
					}
				}
				break;
				
				// Allocation base for arrays
			case SystemCallIndex.ARRAY_ALLOCATION_BASE:
				rv = this.arrayBase;
				err = 0;
				break;
				
				// Is this little endian?
			case SystemCallIndex.BYTE_ORDER_LITTLE:
				rv = 0;
				err = 0;
				break;
			
				// Get the height of the call stack
			case SystemCallIndex.CALL_STACK_HEIGHT:
				{
					rv = this._frames.size();
					err = 0;
				}
				break;
				
				// Get item on the call stack
			case SystemCallIndex.CALL_STACK_ITEM:
				{
					// Locate frame
					int fr = __args[0];
					LinkedList<CPUFrame> frames = this._frames;
					int numframes = frames.size();
					CPUFrame frame = ((fr < 0 || fr >= numframes) ? null :
						frames.get((numframes - 1) - fr));
					
					// Depends on the ID
					int dx = (frame == null ? -1 : __args[1]);
					switch (dx)
					{
						case CallStackItem.CLASS_NAME:
							err = 0;
							rv = frame._inclassp;
							break;
							
						case CallStackItem.METHOD_NAME:
							err = 0;
							rv = frame._inmethodnamep;
							break;
							
						case CallStackItem.METHOD_TYPE:
							err = 0;
							rv = frame._inmethodtypep;
							break;
						
						case CallStackItem.SOURCE_FILE:
							err = 0;
							rv = frame._insourcefilep;
							break;
							
						case CallStackItem.SOURCE_LINE:
							err = 0;
							rv = frame._inline;
							break;
							
						case CallStackItem.PC_ADDRESS:
							err = 0;
							rv = frame._lastpc;
							break;
							
						case CallStackItem.JAVA_OPERATION:
							err = 0;
							rv = frame._injop;
							break;
							
						case CallStackItem.JAVA_PC_ADDRESS:
							err = 0;
							rv = frame._injpc;
							break;
						
						case CallStackItem.TASK_ID:
							err = 0;
							rv = frame._taskid;
						
							// Not valid
						default:
							rv = 0;
							err = SystemCallError.VALUE_OUT_OF_RANGE;
							break;
					}
				}
				break;
				
				// Get error
			case SystemCallIndex.ERROR_GET:
				{
					// If the ID is valid then a bad array access will be used
					int dx = __args[0];
					if (dx < 0 || dx >= SystemCallIndex.NUM_SYSCALLS)
						dx = SystemCallIndex.QUERY_INDEX;
					
					// Return the stored error code
					synchronized (errors)
					{
						rv = errors[dx];
					}
					
					// Always succeeds
					err = 0;
				}
				break;
				
				// Set error
			case SystemCallIndex.ERROR_SET:
				{
					// If the ID is valid then a bad array access will be used
					int dx = __args[0];
					if (dx < 0 || dx >= SystemCallIndex.NUM_SYSCALLS)
						dx = SystemCallIndex.QUERY_INDEX;
					
					// Return last error code, and set new one
					synchronized (errors)
					{
						rv = errors[dx];
						errors[dx] = __args[0];
					}
					
					// Always succeeds
					err = 0;
				}
				break;
				
				// IPC Exception load
			case SystemCallIndex.EXCEPTION_LOAD:
				rv = this._ipcexception;
				err = 0;
				break;
				
				// IPC Exception store
			case SystemCallIndex.EXCEPTION_STORE:
				rv = this._ipcexception;
				this._ipcexception = __args[0];
				err = 0;
				break;
				
				// Fatal exit because of incomplete code
			case SystemCallIndex.FATAL_TODO:
				// {@squirreljme.error AE0o Fatal ToDo system call executed.}
				throw new VMToDoException("AE0o");
				
				// Gets the frame task ID
			case SystemCallIndex.FRAME_TASK_ID_GET:
				{
					LinkedList<CPUFrame> frames = this._frames;
					CPUFrame frame = frames.getLast();
					
					// Is fine
					rv = frame._taskid;
					err = 0;
				}
				break;
				
				// Sets the frame task ID
			case SystemCallIndex.FRAME_TASK_ID_SET:
				{
					LinkedList<CPUFrame> frames = this._frames;
					CPUFrame frame = frames.getLast();
					
					// Set
					frame._taskid = __args[0];
					
					// Is fine
					rv = 1;
					err = 0;
				}
				break;
				
				// Allocates a new memory handle
			case SystemCallIndex.MEM_HANDLE_NEW:
				{
					int kind = __args[0];
					int size = __args[1];
					
					rv = 0;
					if (kind <= 0 || kind >= MemHandleKind.NUM_KINDS)
						err = SystemCallError.INVALID_MEMHANDLE_KIND;
					else if (size < 0)
						err = SystemCallError.VALUE_OUT_OF_RANGE;
					else
					{
						MemHandle handle = this.state.memHandles.alloc(kind,
							size);
						
						// Just use the ID of the handle
						rv = handle.id;
						err = 0;
					}
				}
				break;
				
				// Sets memory to byte value
			case SystemCallIndex.MEM_SET:
				{
					// Set memory
					WritableMemory memory = this.memory;
					
					// Get parameters
					int addr = __args[0],
						valu = __args[1],
						lens = __args[2];
					
					// Wipe
					for (int i = 0; i < lens; i++)
						memory.memWriteByte(addr + i, valu);
					
					// Is okay
					rv = 0;
					err = 0;
				}
				break;
				
				// Pipe descriptor of standard error
			case SystemCallIndex.PD_OF_STDERR:
				{
					rv = 2;
					err = 0;
				}
				break;
				
				// Pipe descriptor of standard input
			case SystemCallIndex.PD_OF_STDIN:
				{
					rv = 0;
					err = 0;
				}
				break;
				
				// Pipe descriptor of standard output
			case SystemCallIndex.PD_OF_STDOUT:
				{
					rv = 1;
					err = 0;
				}
				break;
				
				// Write single byte to PD
			case SystemCallIndex.PD_WRITE_BYTE:
				{
					// Depends on the stream
					int pd = __args[0];
					OutputStream os = (pd == 1 ? System.out :
						(pd == 2 ? System.err : null));
					
					// Write
					if (os != null)
					{
						try
						{
							os.write(__args[1]);
							
							// Okay
							rv = 1;
							err = 0;
						}
						
						// Failed
						catch (IOException e)
						{
							rv = -1;
							err = SystemCallError.PIPE_DESCRIPTOR_BAD_WRITE;
						}
					}
					
					// Failed
					else
					{
						rv = -1;
						err = SystemCallError.PIPE_DESCRIPTOR_INVALID;
					}
				}
				break;
				
				// Sleep
			case SystemCallIndex.SLEEP:
				try
				{
					// Sleep
					Thread.sleep(__args[0], __args[1]);
					
					rv = 0;
					err = SystemCallError.NO_ERROR;
				}
				catch (InterruptedException e)
				{
					rv = 1;
					err = SystemCallError.INTERRUPTED;
				}
				break;
				
				// Supervisor booted okay!
			case SystemCallIndex.SUPERVISOR_BOOT_OKAY:
				// Flag that this happened!
				this.state.flagSupervisorOkay();
				
				// Is fine
				rv = 0;
				err = 0;
				break;
				
				// Get supervisor property
			case SystemCallIndex.SUPERVISOR_PROPERTY_GET:
				{
					int dx = __args[0];
					
					// Out of range?
					if (dx < 0 || dx >= SupervisorPropertyIndex.NUM_PROPERTIES)
					{
						rv = 0;
						err = SystemCallError.VALUE_OUT_OF_RANGE;
					}
					
					// Valid
					else
					{
						rv = this._supervisorproperties[dx];
						err = SystemCallError.NO_ERROR;
					}
				}
				break;
				
				// Set supervisor property
			case SystemCallIndex.SUPERVISOR_PROPERTY_SET:
				{
					int dx = __args[0];
					
					// Out of range?
					if (dx < 0 || dx >= SupervisorPropertyIndex.NUM_PROPERTIES)
					{
						rv = 0;
						err = SystemCallError.VALUE_OUT_OF_RANGE;
					}
					
					// Valid
					else
					{
						this._supervisorproperties[dx] = __args[1];
						
						rv = 0;
						err = SystemCallError.NO_ERROR;
					}
				}
				break;

				// Current wall clock milliseconds.
			case SystemCallIndex.TIME_MILLI_WALL:
				{
					rv = System.currentTimeMillis();
					err = 0;
				}
				break;

				// Current monotonic clock nanoseconds.
			case SystemCallIndex.TIME_NANO_MONO:
				{
					rv = System.nanoTime();
					err = 0;
				}
				break;
				
				// VM information: Memory free bytes
			case SystemCallIndex.VMI_MEM_FREE:
				{
					rv = (int)Math.min(Integer.MAX_VALUE,
						Runtime.getRuntime().freeMemory());
					err = 0;
				}
				break;
			
				// VM information: Memory used bytes
			case SystemCallIndex.VMI_MEM_USED:
				{
					rv = (int)Math.min(Integer.MAX_VALUE,
						Runtime.getRuntime().totalMemory());
					err = 0;
				}
				break;
			
				// VM information: Memory max bytes
			case SystemCallIndex.VMI_MEM_MAX:
				{
					rv = (int)Math.min(Integer.MAX_VALUE,
						Runtime.getRuntime().maxMemory());
					err = 0;
				}
				break;
				
			default:
				// Returns no value but sets an error
				rv = 0;
				err = SystemCallError.UNSUPPORTED_SYSTEM_CALL;
				
				// If the ID is valid then a bad array access will be used
				if (__si < 0 || __si >= SystemCallIndex.NUM_SYSCALLS)
					__si = SystemCallIndex.QUERY_INDEX;
				break;
		}
		
		// Set error state as needed
		synchronized (errors)
		{
			errors[__si] = err;
		}
		
		// Debug result
		Debugging.debugNote("SC::sysCall(%d, %s) -> %d (err: %d)",
			__si, IntegerArrayList.asList(__args),
			rv, err);
		
		// Use returning value
		return rv;*/
	}
}
