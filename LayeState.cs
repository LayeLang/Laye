//#define DEBUG_STACK

using Laye.Proto;
using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Text;

namespace Laye
{
    using static Laye;

    using StackTrace;

    internal class CallStack
    {
        internal StackFrame Top { get; private set; }
        internal uint FrameCount { get; private set; } = 0;

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal StackFrame PushFrame(LayeClosure closure, LayeObject ths, LayeObject[] args)
        {
            FrameCount++;
            return Top = new StackFrame(Top, closure, ths, args);
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal StackFrame PushFrame(StackFrame frame)
        {
            FrameCount++;
            frame.previous = Top;
            return Top = frame;
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal StackFrame NewFrame(LayeClosure closure, LayeObject ths, LayeObject[] args)
        {
            return new StackFrame(Top, closure, ths, args);
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal StackFrame PopFrame()
        {
            FrameCount--;
            var result = Top;
            if (result.gtor != null)
                result.gtor.state = LayeGenerator.State.DEAD;
            Top = Top.previous;
            return result;
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal void Unwind(uint count)
        {
            for (uint i = 0; i < count; i++)
                PopFrame().Abort();
        }
    }

    internal class StackFrame
    {
        internal StackFrame previous;

        internal readonly LayeClosure closure;
        internal readonly LayeObject ths;

        private readonly LayeObject[] locals;
        private readonly LayeObject[] stack;

        internal readonly OuterValue[] openOuters;

        /// <summary>
        /// Represents the size of this stack minus 1, pointing at the topmost value.
        /// </summary>
        internal int stackPointer = -1;
        internal uint ip = 0, activeExceptionHandlers = 0;

        internal LayeGenerator gtor = null;
        internal bool yielded = false;

        internal LayeObject[] Locals { get { return locals; } }
        internal IEnumerable<LayeObject> Stack
        {
            get
            {
                for (int i = 0; i < stackPointer; i++)
                    yield return stack[i];
                yield break;
            }
        }

        internal LayeObject this[uint local]
        {
            get { return locals[local]; }
            set { if (value == null) throw new ArgumentNullException("value"); locals[local] = value; }
        }

        internal LayeObject Top
        {
#if SUGGEST_INLINING
            [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
            get { return stack[stackPointer]; }
#if SUGGEST_INLINING
            [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
            set { stack[stackPointer] = value; }
        }

        internal bool Aborted { get; private set; } = false;

        internal StackFrame(StackFrame previous, LayeClosure closure, LayeObject ths, LayeObject[] args)
        {
            this.previous = previous;
            this.closure = closure;
            this.ths = ths;
            locals = new LayeObject[closure.proto.maxLocalCount];
            stack = new LayeObject[closure.proto.maxStackCount];

            openOuters = closure.proto.nested.Length != 0 ? new OuterValue[closure.proto.maxStackCount] : null;
            SetArgs(args);
        }

        internal void SetArgs(LayeObject[] args)
        {
            var paramc = closure.proto.numParams;
            var argc = args.Length;
            var vargs = closure.proto.hasVargs;

            for (int argi = 0; argi < paramc; argi++)
            {
                int index = argi;
                LayeObject arg;
                if (argi < argc)
                    if (argi == paramc - 1 && vargs)
                    {
                        var vargsArray = new LayeObject[argc - argi];
                        Array.Copy(args, argi, vargsArray, 0, argc - argi);
                        arg = new LayeList(vargsArray);
                        argi = argc;
                    }
                    else arg = args[argi];
                else arg = closure.defaults[argi];
                this[(uint)index] = arg;
            }
        }

        internal void Reset()
        {
            for (var i = 0; i < locals.Length; i++)
                locals[i] = null;
            for (var i = 0; i <= stackPointer; i++)
                stack[i] = null;
            stackPointer = -1;
            ip = 0;
            activeExceptionHandlers = 0;
        }

        internal void Abort()
        {
            Aborted = true;
        }

#if DEBUG_STACK
        internal void PrintLocals(LayeState state)
        {
            var builder = new StringBuilder();

            builder.Append("[");
            for (var i = 0; i < locals.Length; i++)
            {
                if (i > 0)
                    builder.Append(", ");
                builder.Append(locals[i] == null ? "null" : locals[i].ToString(state));
            }
            builder.Append("]");

            Console.WriteLine(builder.ToString());
        }

        internal void PrintStack(LayeState state)
        {
            var offset = stackPointer + 1;
            var builder = new StringBuilder();

            builder.AppendFormat("({0}/{1}) [", offset, stack.Length);
            for (var i = 0; i < offset; i++)
            {
                if (i > 0)
                    builder.Append(", ");
                builder.Append(stack[i].ToString(state));
            }
            builder.Append("]");

            Console.WriteLine(builder.ToString());
        }
#endif

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal bool HasValue()
        {
            return stackPointer >= 0;
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        private void CheckOverflow()
        {
            // [0, 1, 2, 3] sp = 2 (3/4), no overflow on next push
            // [0, 1, 2, 3] sp = 3 (4/4), WILL overflow on next push
            if (stackPointer == stack.Length - 1)
                throw new Exception("Stack overflow");
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        private void CheckUnderflow()
        {
            // [0, 1, 2, 3] sp = 0 (1/4), no underflow on next pop
            // [0, 1, 2, 3] sp = -1 (0/4), WILL underflow on next pop
            if (stackPointer == -1)
                throw new Exception("Stack underflow");
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal void Push(LayeObject value)
        {
            if (value == null)
                throw new ArgumentNullException("value");
            CheckOverflow();
            stack[++stackPointer] = value;
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal void Dup()
        {
            CheckOverflow();
            var value = Top;
            stackPointer++;
            Top = value;
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal LayeObject Pop()
        {
            CheckUnderflow();
            var result = Top;
            Top = null;
            stackPointer--;
            return result;
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal LayeObject[] PopCount(int count)
        {
            LayeObject[] result = new LayeObject[count];
            while (--count >= 0)
                result[count] = Pop();
            return result;
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal LayeObject SwapPop()
        {
            var lastStackPointer = stackPointer--;
            CheckUnderflow();
            var result = Top;
            Top = stack[lastStackPointer];
            stack[lastStackPointer] = null;
            return result;
        }
    }

    internal sealed class OuterValue
    {
        private LayeObject[] values;
        internal uint Index { get; private set; }

        internal LayeObject Value
        {
#if SUGGEST_INLINING
            [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
            get
            { return values[Index]; }
        }

        public OuterValue(LayeObject[] stack, uint index)
        {
            values = stack;
            Index = index;
        }

        public override string ToString()
        {
            var builder = new StringBuilder("[");
            builder.Append(Index).Append("/");
            builder.Append(values.Length).Append("] ");
            return builder.Append(values[Index].ToString()).ToString();
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal void Close()
        {
            LayeObject[] old = values;
            values = new LayeObject[] { old[Index] };
            old[Index] = null;
            Index = 0;
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal void Set(LayeState state, LayeObject value)
        {
            LayeObject temp;
            if ((temp = values[Index]) is LayeReference)
                (temp as LayeReference).Store(state, value);
            else values[Index] = value;
        }
    }

    internal sealed class ExceptionHandler
    {
        internal readonly uint frameIndex;
        internal readonly uint catchIP;

        internal ExceptionHandler(uint frameIndex, uint catchIP)
        {
            this.frameIndex = frameIndex;
            this.catchIP = catchIP;
        }
    }

    public sealed class LayeState
    {
        private static readonly LayeString ENDL = new LayeString(Environment.NewLine);

        private static readonly LayeInt ICONSTM1 = LayeInt.ValueOf(-1);
        private static readonly LayeInt ICONST0 = LayeInt.ValueOf(0);
        private static readonly LayeInt ICONST1 = LayeInt.ValueOf(1);
        private static readonly LayeInt ICONST2 = LayeInt.ValueOf(2);
        private static readonly LayeInt ICONST3 = LayeInt.ValueOf(3);
        private static readonly LayeInt ICONST4 = LayeInt.ValueOf(4);
        private static readonly LayeInt ICONST5 = LayeInt.ValueOf(5);

        private static readonly LayeFloat FCONSTM1 = new LayeFloat(-1);
        private static readonly LayeFloat FCONST0 = new LayeFloat(0);
        private static readonly LayeFloat FCONST1 = new LayeFloat(1);
        private static readonly LayeFloat FCONST2 = new LayeFloat(2);

        private static OuterValue FindOuterValue(LayeObject[] locals, uint idx, OuterValue[] openOuters)
        {
            int n = openOuters.Length;
            for (int i = 0; i < n; i++)
                if (openOuters[i] != null && openOuters[i].Index == idx)
                    return openOuters[i];
            for (int i = 0; i < n; i++)
                if (openOuters[i] == null)
                    return openOuters[i] = new OuterValue(locals, idx);
            throw new ArgumentException("no space for new outer value.");
        }

        internal readonly CallStack stack;
        public readonly Kits.KitStd std;

        private readonly Stack<ExceptionHandler> exceptionHandlers = new Stack<ExceptionHandler>();
        private LayeObject lastException = NULL;

        public LayeState()
        {
            stack = new CallStack();
            std = new Kits.KitStd(this);
        }

        private LayeStackTraceElement GetStackTrace()
        {
            LayeStackTraceElement elem = null;
            var frame = stack.Top;
            while (frame != null)
            {
                elem = new LayeStackTraceElement(frame.closure.proto.definedFile, frame.closure.proto.lineInfos[frame.ip], elem);
                frame = frame.previous;
            }
            return elem;
        }

        /// <summary>
        /// Raises an exception in this Laye state.
        /// The exception will propogate up the call stack until handled.
        /// If the exception propogates through all stacks, the exception is thrown
        /// as a <c>UnhandledLayeException</c> exception.
        /// 
        /// The given exception can be any Laye object.
        /// 
        /// A specific type is provided for detailed exceptions, the <c>Exception</c> type.
        /// It's not necessary to use this type, but it's provided for convenience.
        /// </summary>
        /// <param name="obj"></param>
        public void RaiseException(LayeObject obj)
        {
            if (exceptionHandlers.Count == 0)
                throw new UnhandledLayeException(GetStackTrace(), obj.ToString(this));
            else
            {
                lastException = obj;
                var handler = exceptionHandlers.Pop();
                stack.Top.activeExceptionHandlers--;
                stack.Unwind(stack.FrameCount - handler.frameIndex);
                stack.Top.ip = handler.catchIP - 1;
            }
        }

        public void RaiseException(string format, params object[] args)
        {
            RaiseException(new LayeString(string.Format(format, args)));
        }

        public void RaiseException(string message)
        {
            RaiseException(new LayeString(message));
        }

        internal LayeClosure BuildClosure(FunctionPrototype proto, OuterValue[] openOuters, LayeTypeDef definedType, LayeObject[] defaults)
        {
            var top = stack.Top;
            var closure = new LayeClosure(top.closure.kit, proto, definedType, defaults);
            var protoOuters = proto.outers;

            for (var i = 0; i < protoOuters.Length; i++)
                if (protoOuters[i].type == OuterValueType.LOCAL)
                    closure.outers[i] = FindOuterValue(top.Locals, protoOuters[i].location, openOuters);
                else closure.outers[i] = top.closure.outers[protoOuters[i].location];

            return closure;
        }

        internal LayeObject Execute(StackFrame frame, LayeClosure closure, LayeObject ths = null, params LayeObject[] args)
        {
            if (frame == null)
                frame = stack.PushFrame(closure, ths, args);
            else stack.PushFrame(frame);

        reentry:
            var kit = closure.kit;

            var outers = closure.outers;
            var nested = closure.proto.nested;
            var strings = closure.proto.strings;
            var numericConsts = closure.proto.numericConsts;

            var code = closure.proto.code;
            var codeLen = (uint)code.Length;

            var paramc = closure.proto.numParams;
            var argc = args.Length;
            var vargs = closure.proto.hasVargs;

            var openOuters = frame.openOuters;

            uint insn;
            OpCode op;

            for (; frame.ip < codeLen && !frame.Aborted && !frame.yielded; frame.ip++)
            {
                op = (OpCode)((insn = code[frame.ip]) & Insn.MAX_OP);
#if DEBUG_STACK
                Console.WriteLine(frame.ip + " " + op);
                frame.PrintLocals(this);
                frame.PrintStack(this);
#endif
                switch (op)
                {
                    default: RaiseException(string.Format("Unhandled op code {0}.", op)); return NULL;

                    case OpCode.NOP: continue;
                    case OpCode.POP: frame.Pop(); continue;
                    case OpCode.DUP: frame.Dup(); continue;

                    case OpCode.CLOSE: CloseOuters(openOuters, Insn.GET_C(insn)); continue;

                        // These all do n - 1 because frame.ip++ happens anyway. I hate it, but oh well.
                    case OpCode.JUMP: frame.ip = Insn.GET_C(insn) - 1; continue;
                    case OpCode.JUMPEQ: if (frame.SwapPop().Equals(frame.Pop())) frame.ip = Insn.GET_C(insn) - 1; continue;
                    case OpCode.JUMPNEQ: if (!frame.SwapPop().Equals(frame.Pop())) frame.ip = Insn.GET_C(insn) - 1; continue;
                    case OpCode.JUMPT: if (frame.Pop().ToBool(this)) frame.ip = Insn.GET_C(insn) - 1; continue;
                    case OpCode.JUMPF: if (!frame.Pop().ToBool(this)) frame.ip = Insn.GET_C(insn) - 1; continue;

                    case OpCode.LLOAD: frame.Push(frame[Insn.GET_C(insn)]); continue;
                    case OpCode.LSTORE: frame[Insn.GET_C(insn)] = frame.Top; continue;
                    case OpCode.OLOAD: frame.Push(outers[Insn.GET_C(insn)].Value); continue;
                    case OpCode.OSTORE: outers[Insn.GET_C(insn)].Set(this, frame.Top); continue;
                    case OpCode.KLOAD: frame.Push(kit[this, strings[Insn.GET_C(insn)]]); continue;
                    case OpCode.KSTORE: kit[this, strings[Insn.GET_C(insn)]] = frame.Top; continue;
                    case OpCode.GLOAD: frame.Push(kit.GetGlobal(this, strings[Insn.GET_C(insn)])); continue;
                    case OpCode.GSTORE: kit.SetGlobal(this, strings[Insn.GET_C(insn)], frame.Top); continue;
                    case OpCode.ILOAD: DoILoad(frame, insn); continue;
                    case OpCode.ISTORE: DoIStore(frame, insn); continue;
                    case OpCode.OILOAD: frame.Push(frame.Pop().OperatorIndexGet(this, strings[Insn.GET_C(insn)])); continue;
                    case OpCode.OISTORE: frame.SwapPop().OperatorIndexSet(this, strings[Insn.GET_C(insn)], frame.Top); continue;
                    case OpCode.FLOAD: frame.Push(frame.Pop()[this, strings[Insn.GET_C(insn)]]); continue;
                    case OpCode.FSTORE: frame.SwapPop()[this, strings[Insn.GET_C(insn)]] = frame.Top; continue;
                    case OpCode.LOAD: DoLoad(frame, kit, strings[Insn.GET_C(insn)]); continue;
                    case OpCode.STORE: DoStore(frame, kit, strings[Insn.GET_C(insn)]); continue;

                    case OpCode.NULL: frame.Push(NULL); continue;
                    case OpCode.TRUE: frame.Push(TRUE); continue;
                    case OpCode.FALSE: frame.Push(FALSE); continue;
                    case OpCode.ENDL: frame.Push(ENDL); continue;
                    case OpCode.NCONST: frame.Push(numericConsts[Insn.GET_C(insn)]); continue;
                    case OpCode.SCONST: frame.Push(Insn.GET_B(insn) == 0 ? new LayeString(strings[Insn.GET_A(insn)]) as LayeObject : LayeSymbol.getUnsafe(strings[Insn.GET_A(insn)])); continue;

                    case OpCode.ICONSTM1: frame.Push(ICONSTM1); continue;
                    case OpCode.ICONST0: frame.Push(ICONST0); continue;
                    case OpCode.ICONST1: frame.Push(ICONST1); continue;
                    case OpCode.ICONST2: frame.Push(ICONST2); continue;
                    case OpCode.ICONST3: frame.Push(ICONST3); continue;
                    case OpCode.ICONST4: frame.Push(ICONST4); continue;
                    case OpCode.ICONST5: frame.Push(ICONST5); continue;

                    case OpCode.FCONSTM1: frame.Push(FCONSTM1); continue;
                    case OpCode.FCONST0: frame.Push(FCONST0); continue;
                    case OpCode.FCONST1: frame.Push(FCONST1); continue;
                    case OpCode.FCONST2: frame.Push(FCONST2); continue;

                    case OpCode.LIST: frame.Push(new LayeList(frame.PopCount((int)Insn.GET_C(insn)))); continue;
                    case OpCode.TUPLE: frame.Push(new LayeTuple(frame.PopCount((int)Insn.GET_C(insn)))); continue;

                    case OpCode.CLOSURE: frame.Push(BuildClosure(nested[Insn.GET_A(insn)], openOuters, null, frame.PopCount((int)Insn.GET_B(insn)))); continue;
                    case OpCode.GENERATOR: frame.Push(new LayeClosureGeneratorSpawner(frame.Pop() as LayeClosure)); continue;

                    case OpCode.INVOKE: DoInvoke(frame, insn); continue;
                    case OpCode.MINVOKE: DoMethodInvoke(frame, strings[Insn.GET_B(insn)], insn); continue;
                    case OpCode.TINVOKE: DoThisInvoke(frame, ths, strings[Insn.GET_B(insn)], insn); continue;
                    case OpCode.TAILINVOKE:
                        var saveArgs = frame.PopCount((int)Insn.GET_C(insn));
                        EndCall(frame, openOuters);
                        frame.Reset();
                        frame.SetArgs(saveArgs);
                        goto reentry;

                    case OpCode.YIELD: frame.yielded = true; continue;
                    case OpCode.RES: DoRes(frame); continue;

                    case OpCode.KIT: frame.Push(kit); continue;
                    case OpCode.THIS: frame.Push(ths); continue;
                    case OpCode.SELF: frame.Push(closure); continue;
                    case OpCode.STATIC: frame.Push(closure.definedType); continue;

                    case OpCode.PREFIX: frame.Push(frame.Pop().Prefix(this, strings[Insn.GET_C(insn)])); continue;
                    case OpCode.INFIX: frame.Push(frame.SwapPop().Infix(this, strings[Insn.GET_C(insn)], frame.Pop())); continue;
                    case OpCode.AS: frame.Push(frame.SwapPop().As(this, frame.Pop())); continue;

                    case OpCode.NOT: frame.Push(frame.Pop().ToBool(this) ? FALSE : TRUE); continue;
                    case OpCode.AND: if (frame.Top.ToBool(this)) frame.Pop(); else frame.ip = Insn.GET_C(insn) - 1; continue;
                    case OpCode.OR: if (frame.Top.ToBool(this)) frame.ip = Insn.GET_C(insn) - 1; else frame.Pop(); continue;
                    case OpCode.XOR: frame.Push((frame.Pop().ToBool(this) != frame.Pop().ToBool(this)) ? TRUE : FALSE); continue;

                    case OpCode.COMPIS: frame.Push((LayeBool)ReferenceEquals(frame.Pop(), frame.Pop())); continue;
                    case OpCode.COMPNOTIS: frame.Push((LayeBool)!ReferenceEquals(frame.Pop(), frame.Pop())); continue;
                    case OpCode.COMPTYPEOF: frame.Push((LayeBool)frame.SwapPop().TypeOf(this, frame.Pop())); continue;
                    case OpCode.COMPNOTTYPEOF: frame.Push((LayeBool)!frame.SwapPop().TypeOf(this, frame.Pop())); continue;
                    case OpCode.TYPEOF: frame.Push(frame.Pop().TypeDef); continue;

                    case OpCode.THROW: RaiseException(frame.Pop()); continue;
                    case OpCode.STOREEX: frame[Insn.GET_C(insn)] = lastException; continue;
                    case OpCode.BEGINEXH: var c = Insn.GET_C(insn); while (frame.stackPointer >= c) frame.Pop(); continue;
                    case OpCode.PUSHEXH: frame.activeExceptionHandlers++; exceptionHandlers.Push(new ExceptionHandler(stack.FrameCount, Insn.GET_C(insn))); continue;
                    case OpCode.POPEXH: frame.activeExceptionHandlers--; exceptionHandlers.Pop(); continue;

                    case OpCode.ITERPREP: DoIterPrep(frame, Insn.GET_A(insn), Insn.GET_B(insn) != 0); continue;
                    case OpCode.ITERLOOP: DoIterLoop(frame, Insn.GET_A(insn), Insn.GET_B(insn)); continue;
                    case OpCode.EACHPREP: DoEachPrep(frame, Insn.GET_A(insn)); continue;
                    case OpCode.EACHLOOP: DoEachLoop(frame, Insn.GET_A(insn), Insn.GET_B(insn)); continue;
                    case OpCode.IEACHPREP: DoIEachPrep(frame, Insn.GET_A(insn)); continue;
                    case OpCode.IEACHLOOP: DoIEachLoop(frame, Insn.GET_A(insn), Insn.GET_B(insn)); continue;
                } // end switch
            } // end for

            EndCall(frame, openOuters);

#if DEBUG_STACK
            frame.PrintStack(this);
#endif
            if (frame.Aborted)
                return NULL;
            stack.PopFrame();

            return frame.HasValue() ? frame.Top : NULL;
        }

        private void EndCall(StackFrame frame, OuterValue[] openOuters)
        {
            // If we return early, make sure all things are gone plz
            // (this is me being lazy, we don't pop them automatically in the compiler)
            while (frame.activeExceptionHandlers > 0)
            {
                frame.activeExceptionHandlers--;
                exceptionHandlers.Pop();
            }

            if (openOuters != null)
                CloseOuters(openOuters, 0);
        }

        #region Instruction Impls
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void CloseOuters(OuterValue[] openOuters, uint newTop)
        {
            for (int i = openOuters.Length; --i >= newTop;)
                if (openOuters[i] != null)
                {
                    openOuters[i].Close();
                    openOuters[i] = null;
                }
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoILoad(StackFrame frame, uint insn)
        {
            var args = frame.PopCount((int)Insn.GET_C(insn));
            frame.Push(frame.Pop()[this, args]);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoIStore(StackFrame frame, uint insn)
        {
            var value = frame.Pop();
            var args = frame.PopCount((int)Insn.GET_C(insn));
            frame.Pop()[this, args] = value;
            frame.Push(value);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoLoad(StackFrame frame, LayeKit kit, string key)
        {
            if (kit.IsDefined(key))
                frame.Push(kit[this, key]);
            else frame.Push(kit.GetGlobal(this, key));
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoStore(StackFrame frame, LayeKit kit, string key)
        {
            if (kit.IsDefined(key))
                kit[this, key] = frame.Top;
            else kit.SetGlobal(this, key, frame.Top);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoInvoke(StackFrame frame, uint insn)
        {
            var args = frame.PopCount((int)Insn.GET_C(insn));
            frame.Push(frame.Pop().Invoke(this, args));
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoMethodInvoke(StackFrame frame, string methodName, uint insn)
        {
            var args = frame.PopCount((int)Insn.GET_A(insn));
            frame.Push(frame.Pop().MethodInvoke(this, methodName, args));
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoThisInvoke(StackFrame frame, LayeObject ths, string methodName, uint insn)
        {
            var args = frame.PopCount((int)Insn.GET_A(insn));
            frame.Push(ths.MethodInvoke(this, methodName, args));
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoRes(StackFrame frame)
        {
            var value = frame.Pop();
            var gen = value as LayeGenerator;
            if (gen == null)
            {
                RaiseException("Expected a generator to resume, got a(n) {0}.", value.TypeName);
                return;
            }
            LayeObject result;
            var success = gen.Resume(this, out result);
            if (success)
            {
                if (result == null)
                    frame.Push(NULL);
                else frame.Push(result);
            }
        }

        //[MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoIterPrep(StackFrame frame, uint iterVar, bool hasStep)
        {
            // step, limit, init -> iter (iterVar = init to limit by step)
            var step  = hasStep ? frame.Pop().As(this, LayeInt.TYPE) as LayeInt : null;
            var limit = frame.Pop().As(this, LayeInt.TYPE) as LayeInt;
            var init  = frame.Pop().As(this, LayeInt.TYPE) as LayeInt;
            if (!hasStep)
                step = init.value < limit.value ? ICONST1 : ICONSTM1;
            // Now check what we popped, make sure they're valid
            if (init == null)
            {
                RaiseException("iter initial value must be an Int or convertible to an Int.");
                return;
            }
            if (limit == null)
            {
                RaiseException("iter limit must be an Int or convertible to an Int.");
                return;
            }
            if (step == null)
            {
                RaiseException("iter step must be an Int or convertible to an Int.");
                return;
            }
            if (step.value == 0)
            {
                RaiseException("iter step cannot be zero.");
                return;
            }
            if (init.value < limit.value ? step.value < 0 : step.value > 0)
            {
                RaiseException("Invalid iter step. The given step will lead the index away from the limit, dooming the loop to never complete.");
                return;
            }
            // Prep the init value
            init = LayeInt.ValueOf(init.value - step.value);
            // Store each value where it should go:
            // We double the first one because + 0 is the visible value, + 1 is the internal value for safety.
            // We don't want the user messing with our internal counter, we reset it each iteration.
            frame[iterVar] = init;
            frame[iterVar + 1] = init;
            frame[iterVar + 2] = limit;
            frame[iterVar + 3] = step;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoIterLoop(StackFrame frame, uint iterVar, uint jump)
        {
            // Get the values we need
            var index = (frame[iterVar + 1] as LayeInt).value;
            var limit = (frame[iterVar + 2] as LayeInt).value;
            var step  = (frame[iterVar + 3] as LayeInt).value;
            // Do operations and check bounds
            index = index + step;
            if (step > 0 ? index < limit : index > limit)
                // Update the index
                frame[iterVar] = frame[iterVar + 1] = LayeInt.ValueOf(index);
            else frame.ip = jump - 1;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoEachPrep(StackFrame frame, uint eachVar)
        {
            LayeGenerator gen;
            var value = frame.Pop();
            if (!(value is LayeGenerator))
            {
                var genObj = value[this, "enumerator", false];
                gen = genObj as LayeGenerator;
                if (gen == null)
                {
                    RaiseException("Failed to get a generator from type {0} to enumerate. There should be an instance property called \"enumerator\" that returns a generator.", genObj.TypeName);
                    return;
                }
            }
            else gen = value as LayeGenerator;
            // eachVar is the var the user has access to, eachVar + 1 is our temp store, and eachVar + 2 is the generator
            frame[eachVar + 1] = gen;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoEachLoop(StackFrame frame, uint eachVar, uint jump)
        {
            LayeObject result;
            var gen = frame[eachVar + 1] as LayeGenerator;
            var success = gen.Resume(this, out result);
            if (!success)
                return; // there was an error, control flow is not ours.
            if (result == null)
                // This is dead generator lol
                frame.ip = jump - 1;
            else frame[eachVar] = result;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoIEachPrep(StackFrame frame, uint eachIndex)
        {
            LayeGenerator gen;
            var value = frame.Pop();
            if (!(value is LayeGenerator))
            {
                var genObj = value[this, "enumerator", false];
                gen = genObj as LayeGenerator;
                if (gen == null)
                {
                    RaiseException("Failed to get a generator from type {0} to enumerate. There should be an instance property called \"enumerator\" that returns a generator.", genObj.TypeName);
                    return;
                }
            }
            else gen = value as LayeGenerator;
            frame[eachIndex + 1] = ICONST0;
            frame[eachIndex + 3] = gen;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void DoIEachLoop(StackFrame frame, uint eachIndex, uint jump)
        {
            LayeObject result;
            var gen = frame[eachIndex + 3] as LayeGenerator;
            var success = gen.Resume(this, out result);
            if (!success)
                return; // there was an error, control flow is not ours.
            if (result == null)
                // This is dead generator lol
                frame.ip = jump - 1;
            else
            {
                frame[eachIndex] = frame[eachIndex + 1] = LayeInt.ValueOf((frame[eachIndex + 1] as LayeInt).value + 1);
                frame[eachIndex + 2] = result;
            }
        }
        #endregion Instruction Impls
    }

    internal static class Insn
    {
#region Constants
        public const uint SIZE_OP = 8;
        public const uint SIZE_A = 12;
        public const uint SIZE_B = 12;
        public const uint SIZE_C = SIZE_A + SIZE_B;

        public const uint POS_OP = 0;
        public const uint POS_A = SIZE_OP;
        public const uint POS_B = POS_A + SIZE_A;
        public const uint POS_C = SIZE_OP;

        public const uint MAX_OP = (1u << (int)SIZE_OP) - 1;
        public const uint MAX_A = (1u << (int)SIZE_A) - 1;
        public const uint MAX_B = (1u << (int)SIZE_B) - 1;
        public const uint MAX_C = (1u << (int)SIZE_C) - 1;
#endregion Constants

#region Builders
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static uint BUILD(OpCode op, uint c = 0u)
        {
            return (byte)op | ((c & MAX_C) << (int)POS_C);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static uint BUILD(OpCode op, uint a, uint b)
        {
            return (byte)op | ((a & MAX_A) << (int)POS_A) | ((b & MAX_B) << (int)POS_B);
        }
#endregion Builders

#region Getters
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static OpCode GET_OP(uint code)
        {
            return (OpCode)(code & MAX_OP);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static void GET_OP(uint code, out OpCode op)
        {
            op = (OpCode)(code & MAX_OP);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static uint GET_A(uint code)
        {
            return (code >> (int)POS_A) & MAX_A;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static void GET_A(uint code, out uint a)
        {
            a = (code >> (int)POS_A) & MAX_A;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static uint GET_B(uint code)
        {
            return (code >> (int)POS_B) & MAX_B;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static void GET_B(uint code, out uint b)
        {
            b = (code >> (int)POS_B) & MAX_B;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static uint GET_C(uint code)
        {
            return code >> (int)POS_C;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static void GET_C(uint code, out uint c)
        {
            c = code >> (int)POS_C;
        }
#endregion Getters
    }

    internal enum OpCode : byte
    {
        NOP,
        POP,
        DUP,

        CLOSE,

        JUMP,
        JUMPEQ,
        JUMPNEQ,
        JUMPT,
        JUMPF,

        LLOAD,
        LSTORE,
        OLOAD,
        OSTORE,
        KLOAD,
        KSTORE,
        GLOAD,
        GSTORE,
        ILOAD,
        ISTORE,
        OILOAD,
        OISTORE,
        FLOAD,
        FSTORE,
        LOAD,
        STORE,

        NULL,
        TRUE,
        FALSE,
        ENDL,
        NCONST,
        SCONST,

        ICONSTM1,
        ICONST0,
        ICONST1,
        ICONST2,
        ICONST3,
        ICONST4,
        ICONST5,

        FCONSTM1,
        FCONST0,
        FCONST1,
        FCONST2,

        LIST,
        TUPLE,

        CLOSURE,
        GENERATOR,
        TYPE,

        INVOKE,
        MINVOKE,
        TINVOKE,
        BINVOKE,
        TAILINVOKE,

        RET,
        YIELD,
        RES,

        KIT,
        THIS,
        SELF,
        STATIC,

        PREFIX,
        INFIX,
        AS,

        NOT,
        AND,
        OR,
        XOR,

        COMPIS,
        COMPNOTIS,
        COMPTYPEOF,
        COMPNOTTYPEOF,
        TYPEOF,

        THROW,
        STOREEX,
        BEGINEXH,
        PUSHEXH,
        POPEXH,

        ITERPREP,
        ITERLOOP,
        EACHPREP,
        EACHLOOP,
        IEACHPREP,
        IEACHLOOP,
    }
}
