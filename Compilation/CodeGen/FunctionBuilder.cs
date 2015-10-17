using System.Collections.Generic;

namespace Laye.Compilation.CodeGen
{
#if LAYE64
    using lint = System.Int64;
    using lfloat = System.Double;
#else
    using lint = System.Int32;
    using lfloat = System.Single;
#endif

    using Proto;
    using System;

    internal class FunctionBuilder
    {
        private sealed class Scope
        {
            internal readonly Scope previous;
            internal readonly uint initialLocalCount;

            internal Scope(Scope previous, uint initialLocalCount)
            {
                this.previous = previous;
                this.initialLocalCount = initialLocalCount;
            }
        }

        private sealed class FlowBlock
        {
            internal readonly FlowBlock previous;
            internal uint contTarg;

            internal uint numScopes;
            private string label;

            internal readonly List<uint> breaks = new List<uint>();
            internal readonly List<uint> conts = new List<uint>();

            internal FlowBlock(FlowBlock previous, uint contTarg, string label)
            {
                this.previous = previous;
                this.contTarg = contTarg;
                this.label = label;
            }

            internal bool IsLabeled(string label)
            {
                if (label == null)
                    return true;
                return this.label == label;
            }
        }

        private uint numParams;
        internal bool hasVargs;
        internal bool trulyAnon;

        internal bool isGenerator;

        private uint stackCount, maxStackCount;
        private uint localCount, maxLocalCount;

        private uint outerValueCount;

        private uint LocalCount
        {
            get { return localCount; }
            set
            {
                if (value > int.MaxValue)
                    throw new ArgumentException("local count cannot be negative.");
                localCount = value;
                if (maxLocalCount < value)
                    maxLocalCount = value;
            }
        }

        internal uint StackCount
        {
            get { return stackCount; }
            private set
            {
                if (value > int.MaxValue)
                    throw new ArgumentException("stack count cannot be negative.");
                stackCount = value;
                if (maxStackCount < value)
                    maxStackCount = value;
            }
        }

        private readonly List<LocalValueInfo> localValues = new List<LocalValueInfo>();
        private readonly List<OuterValueInfo> outerValues = new List<OuterValueInfo>();

        private readonly List<uint> code = new List<uint>();
        private readonly List<uint> lineInfos = new List<uint>();
        private readonly List<FunctionPrototype> nested = new List<FunctionPrototype>();
        private readonly List<string> strings = new List<string>();
        private readonly List<LayeObject> numericConsts = new List<LayeObject>();

        public uint InsnCount { get { return (uint)code.Count; } }

        private Scope currentScope = null;
        private FlowBlock currentFlowBlock;

        public readonly FunctionBuilder parent;
        public uint currentLineNumber = 1;
        public string fileName = null;

        public FunctionBuilder(FunctionBuilder parent = null, string fileName = null)
        {
            this.parent = parent;
            if (parent != null)
                this.fileName = parent.fileName;
            else this.fileName = fileName;
        }

        public FunctionPrototype Build()
        {
            FunctionPrototype proto = new FunctionPrototype();
            proto.numParams = numParams;
            proto.hasVargs = hasVargs;
            proto.code = code.ToArray();
            proto.lineInfos = lineInfos.ToArray();
            proto.definedFile = fileName;
            proto.outers = outerValues.ToArray();
            proto.nested = nested.ToArray();
            proto.strings = strings.ToArray();
            proto.numericConsts = numericConsts.ToArray();
            proto.maxLocalCount = (int)maxLocalCount;
            proto.maxStackCount = (int)maxStackCount;
            return proto;
        }

        #region Scope and Flow
        public void StartScope()
        {
            currentScope = new Scope(currentScope, localCount);
            if (currentFlowBlock != null)
                currentFlowBlock.numScopes++;
        }

        public void EndScope()
        {
            var oldOuters = outerValueCount;
            if (localCount != currentScope.initialLocalCount)
            {
                SetLocalCount(currentScope.initialLocalCount);
                if (oldOuters != outerValueCount)
                    OpClose(currentScope.initialLocalCount);
            }
            currentScope = currentScope.previous;
            if (currentFlowBlock != null)
                currentFlowBlock.numScopes--;
        }

        internal void StartFlowBlock(string label)
        {
            currentFlowBlock = new FlowBlock(currentFlowBlock, InsnCount, label);
        }

        internal void EndFlowBlock()
        {
            uint contTarg = currentFlowBlock.contTarg, breakTarg = InsnCount;
            currentFlowBlock.breaks.ForEach(index => SetOpC(index, breakTarg));
            currentFlowBlock.conts.ForEach(index => SetOpC(index, contTarg));
            currentFlowBlock = currentFlowBlock.previous;
        }

        private FlowBlock AddBreakOrCont(DetailLogger log, Location loc, string label)
        {
            if (currentFlowBlock == null)
            {
                log.Error(loc, "There is no enclosing control flow block to break out of.");
                return null;
            }
            int stackAmt = 0;
            var block = currentFlowBlock;
            while (block != null && !block.IsLabeled(label))
            {
                stackAmt += (int)block.numScopes;
                block = block.previous;
            }
            if (block == null)
            {
                log.Error(loc, "There is no enclosing control flow block with a matching label to break out of.");
                return null;
            }
            // Close the scopes without actually closing the scopes
            { // EndScopes
                var scope = currentScope;
                var newLocalCount = LocalCount;
                var newOuterValueCount = outerValueCount;
                while (stackAmt > 0)
                {
                    stackAmt--;
                    var oldOuters = newOuterValueCount;
                    if (localCount != scope.initialLocalCount)
                    {
                        while (newLocalCount > scope.initialLocalCount)
                        {
                            newLocalCount--;
                            if (localValues[(int)newLocalCount].IsOuter)
                                newOuterValueCount--;
                        }
                        if (oldOuters != newOuterValueCount)
                            OpClose(scope.initialLocalCount);
                    }
                    scope = scope.previous;
                }
            }
            return block;
        }

        internal void AddBreak(DetailLogger log, Location loc, string label)
        {
            var block = AddBreakOrCont(log, loc, label);
            if (block != null)
                block.breaks.Add(OpJump(0));
        }

        internal void AddCont(DetailLogger log, Location loc, string label)
        {
            var block = AddBreakOrCont(log, loc, label);
            if (block != null)
                block.conts.Add(OpJump(0));
        }
        #endregion

        #region Locals
        internal bool AddLocal(string name, out uint location)
        {
            location = 0;
            if (name == null)
                throw new ArgumentNullException("name");
            foreach (var var in localValues)
                if (var.name == name)
                    return false;
            location = localCount;
            localValues.Add(new LocalValueInfo(name, location));
            LocalCount++;
            return true;
        }

        // Used for anonymous local values that the compiler needs
        internal uint ReseveLocal()
        {
            var location = localCount;
            localValues.Add(new LocalValueInfo(null, location));
            LocalCount++;
            return location;
        }

        internal uint ReserveParameter()
        {
            uint location = ReseveLocal();
            numParams++;
            return location;
        }

        internal uint AddParameter(string name)
        {
            uint param;
            AddLocal(name, out param);
            numParams++;
            return param;
        }

        internal bool GetLocalLocation(string name, out uint location)
        {
            foreach (var var in localValues)
                if (var.name == name)
                {
                    location = var.location;
                    return true;
                }
            location = 0;
            return false;
        }

        internal bool GetLocalName(uint location, out string name)
        {
            foreach (var var in localValues)
                if (var.location == location)
                {
                    name = var.name;
                    return true;
                }
            name = null;
            return false;
        }

        internal void SetLocalCount(uint n)
        {
            while (LocalCount > n)
            {
                LocalCount--;
                if (localValues[(int)LocalCount].IsOuter)
                    outerValueCount--;
                localValues.RemoveAt((int)LocalCount);
            }
        }

        internal void MarkLocalAsOuter(uint local)
        {
            localValues[(int)local].MarkAsOuter();
            outerValueCount++;
        }
        #endregion Locals

        #region Outers
        internal bool GetOuterLocation(string name, out uint location)
        {
            for (var i = 0; i < outerValues.Count; i++)
                if (outerValues[i].name == name)
                {
                    location = (uint)i;
                    return true;
                }
            if (parent != null)
            {
                uint pos;
                if (parent.GetLocalLocation(name, out pos))
                {
                    parent.MarkLocalAsOuter(pos);
                    location = (uint)outerValues.Count;
                    outerValues.Add(new OuterValueInfo(name, OuterValueType.LOCAL, pos));
#if PRINT_DEBUG
                    Console.WriteLine("Marked {0} as local-outer at location {1}.", name, location);
#endif
                    return true;
                }
                else
                {
                    if (parent.GetOuterLocation(name, out pos))
                    {
                        location = (uint)outerValues.Count;
                        outerValues.Add(new OuterValueInfo(name, OuterValueType.OUTER, pos));
#if PRINT_DEBUG
                        Console.WriteLine("Marked {0} as outer-outer at location {1}.", name, location);
#endif
                        return true;
                    }
                }
            }
            location = 0;
            return false;
        }

        internal bool GetOuterName(uint location, out string name)
        {
            foreach (var var in outerValues)
                if (var.location == location)
                {
                    name = var.name;
                    return true;
                }
            name = null;
            return false;
        }
#endregion

#region Constants and Nested
        private uint AddIfMissing<T>(List<T> list, T obj)
        {
            int index = list.IndexOf(obj);
            if (index == -1)
            {
                index = list.Count;
                list.Add(obj);
            }
            return (uint)index;
        }

        internal uint AddNested(FunctionPrototype proto)
        {
            return AddIfMissing(nested, proto);
        }

        internal uint AddString(string str)
        {
            return AddIfMissing(strings, str);
        }

        internal uint AddNumericConstant(LayeObject obj)
        {
            // Must be numeric!
            if (!(obj is LayeInt || obj is LayeFloat))
                throw new ArgumentException("obj");
            return AddIfMissing(numericConsts, obj);
        }
        #endregion Constants and Nested

        /// <summary>
        /// Adds a -Store op based on the location of the name.
        /// if storeKit is true, this will never assign to the global
        /// namespace, special checks should be done to handle that separately.
        /// </summary>
        /// <param name="name"></param>
        /// <param name="storeKit"></param>
        /// <returns></returns>
        internal uint Store(string name, bool storeKit = true)
        {
            uint index;
            if (GetLocalLocation(name, out index))
                return OpLStore(index);
            else if (GetOuterLocation(name, out index))
                return OpOStore(index);
            if (storeKit)
                return OpKStore(name);
            return OpStore(name);
        }

        /// <summary>
        /// Adds a -Load op based on the location of the name.
        /// </summary>
        /// <param name="name"></param>
        /// <returns></returns>
        internal uint Load(string name)
        {
            uint index;
            if (GetLocalLocation(name, out index))
                return OpLLoad(index);
            else if (GetOuterLocation(name, out index))
                return OpOLoad(index);
            // Handle both kit/global loading in the VM
            return OpLoad(name);
        }

#region Put OpCodes
        internal uint PutOp(OpCode op)
        {
            code.Add(Insn.BUILD(op));
            lineInfos.Add(currentLineNumber);
            return (uint)code.Count - 1;
        }

        internal uint PutOp(OpCode op, uint c)
        {
            code.Add(Insn.BUILD(op, c));
            lineInfos.Add(currentLineNumber);
            return (uint)code.Count - 1;
        }

        internal uint PutOp(OpCode op, uint a, uint b)
        {
            code.Add(Insn.BUILD(op, a, b));
            lineInfos.Add(currentLineNumber);
            return (uint)code.Count - 1;
        }

        internal void SetOpC(uint index, uint c)
        {
            var op = code[(int)index];
            code[(int)index] = Insn.BUILD(Insn.GET_OP(op), c);
        }

        internal void SetOpB(uint index, uint b)
        {
            var op = code[(int)index];
            code[(int)index] = Insn.BUILD(Insn.GET_OP(op), Insn.GET_A(op), b);
        }
        #endregion

        #region Generate OpCodes
        internal uint OpNop()
        {
            return PutOp(OpCode.NOP);
        }

        internal uint OpPop(bool changeStackCount = true)
        {
            if (changeStackCount)
                StackCount--;
            return PutOp(OpCode.POP);
        }

        internal uint OpDup()
        {
            StackCount++;
            return PutOp(OpCode.DUP);
        }

        internal uint OpClose(uint newTop)
        {
            return PutOp(OpCode.CLOSE, newTop);
        }

        internal uint OpJump(uint to)
        {
            return PutOp(OpCode.JUMP, to);
        }

        internal uint OpJumpEq(uint to)
        {
            StackCount -= 2;
            return PutOp(OpCode.JUMPEQ, to);
        }

        internal uint OpJumpNeq(uint to)
        {
            StackCount -= 2;
            return PutOp(OpCode.JUMPNEQ, to);
        }

        internal uint OpJumpT(uint to)
        {
            StackCount--;
            return PutOp(OpCode.JUMPT, to);
        }

        internal uint OpJumpF(uint to)
        {
            StackCount--;
            return PutOp(OpCode.JUMPF, to);
        }

        internal uint OpLLoad(uint localIndex)
        {
            StackCount++;
            return PutOp(OpCode.LLOAD, localIndex);
        }

        internal uint OpLStore(uint localIndex)
        {
            return PutOp(OpCode.LSTORE, localIndex);
        }

        internal uint OpOLoad(uint outerIndex)
        {
            StackCount++;
            return PutOp(OpCode.OLOAD, outerIndex);
        }

        internal uint OpOStore(uint outerIndex)
        {
            return PutOp(OpCode.OSTORE, outerIndex);
        }

        internal uint OpKLoad(string kitFieldName)
        {
            StackCount++;
            return PutOp(OpCode.KLOAD, AddString(kitFieldName));
        }

        internal uint OpKStore(string kitFieldName)
        {
            return PutOp(OpCode.KSTORE, AddString(kitFieldName));
        }

        internal uint OpGLoad(string globalName)
        {
            StackCount++;
            return PutOp(OpCode.GLOAD, AddString(globalName));
        }

        internal uint OpGStore(string globalName)
        {
            return PutOp(OpCode.GSTORE, AddString(globalName));
        }

        internal uint OpILoad(uint count)
        {
            StackCount -= count;
            return PutOp(OpCode.ILOAD, count);
        }

        internal uint OpIStore(uint count)
        {
            StackCount -= count + 1;
            return PutOp(OpCode.ISTORE, count);
        }

        internal uint OpOILoad(string op)
        {
            StackCount++;
            return PutOp(OpCode.OILOAD, AddString(op));
        }

        internal uint OpOIStore(string op)
        {
            return PutOp(OpCode.OISTORE, AddString(op));
        }

        internal uint OpFLoad(string fieldName)
        {
            StackCount++;
            return PutOp(OpCode.FLOAD, AddString(fieldName));
        }

        internal uint OpFStore(string fieldName)
        {
            return PutOp(OpCode.FSTORE, AddString(fieldName));
        }

        internal uint OpLoad(string name)
        {
            StackCount++;
            return PutOp(OpCode.LOAD, AddString(name));
        }

        internal uint OpStore(string name)
        {
            return PutOp(OpCode.STORE, AddString(name));
        }

        internal uint OpNull()
        {
            StackCount++;
            return PutOp(OpCode.NULL);
        }

        internal uint OpBConst(bool value)
        {
            StackCount++;
            return value ? PutOp(OpCode.TRUE) : PutOp(OpCode.FALSE);
        }

        internal uint OpEndL()
        {
            StackCount++;
            return PutOp(OpCode.ENDL);
        }

        internal uint OpNConst(LayeObject number)
        {
            StackCount++;
            return PutOp(OpCode.NCONST, AddNumericConstant(number));
        }

        internal uint OpSConst(string s, bool isSymbol = false)
        {
            StackCount++;
            return PutOp(OpCode.SCONST, AddString(s), isSymbol ? 1u : 0u);
        }

        internal uint OpIConst(lint value)
        {
            StackCount++;
            switch ((int)value)
            {
                case -1: return PutOp(OpCode.ICONSTM1);
                case 0: return PutOp(OpCode.ICONST0);
                case 1: return PutOp(OpCode.ICONST1);
                case 2: return PutOp(OpCode.ICONST2);
                case 3: return PutOp(OpCode.ICONST3);
                case 4: return PutOp(OpCode.ICONST4);
                case 5: return PutOp(OpCode.ICONST5);
                default: return PutOp(OpCode.NCONST, AddNumericConstant(LayeInt.ValueOf(value)));
            }
        }

        internal uint OpFConst(lfloat value)
        {
            StackCount++;
            if (value == -1)
                return PutOp(OpCode.FCONSTM1);
            else if (value == 0)
                return PutOp(OpCode.FCONST0);
            else if (value == 1)
                return PutOp(OpCode.FCONST1);
            else if (value == 2)
                return PutOp(OpCode.FCONST2);
            return PutOp(OpCode.NCONST, AddNumericConstant(new LayeFloat(value)));
        }

        internal uint OpList(uint count)
        {
            if (count == 0)
                StackCount++;
            else StackCount -= count - 1;
            return PutOp(OpCode.LIST, count);
        }

        internal uint OpTuple(uint count)
        {
            if (count == 0)
                StackCount++;
            else StackCount -= count - 1;
            return PutOp(OpCode.TUPLE, count);
        }

        internal uint OpClosure(FunctionPrototype proto, uint defaultParams = 0)
        {
            StackCount++;
            uint index = (uint)nested.Count;
            nested.Add(proto);
            return PutOp(OpCode.CLOSURE, index, defaultParams);
        }

        internal uint OpGenerator()
        {
            return PutOp(OpCode.GENERATOR);
        }

        internal uint OpInvoke(uint argc)
        {
            StackCount -= argc;
            return PutOp(OpCode.INVOKE, argc);
        }

        internal uint OpMInvoke(string methodName, uint argc)
        {
            StackCount -= argc;
            return PutOp(OpCode.MINVOKE, argc, AddString(methodName));
        }

        internal uint OpTailInvoke(uint argc)
        {
            StackCount -= argc;
            return PutOp(OpCode.TAILINVOKE, argc);
        }

        internal uint OpYield()
        {
            return PutOp(OpCode.YIELD);
        }

        internal uint OpRes()
        {
            return PutOp(OpCode.RES);
        }

        internal uint OpKit()
        {
            StackCount++;
            return PutOp(OpCode.KIT);
        }

        internal uint OpThis()
        {
            StackCount++;
            return PutOp(OpCode.THIS);
        }

        internal uint OpSelf()
        {
            StackCount++;
            return PutOp(OpCode.SELF);
        }

        internal uint OpPrefix(string op)
        {
            return PutOp(OpCode.PREFIX, AddString(op));
        }

        internal uint OpInfix(string op)
        {
            StackCount--;
            return PutOp(OpCode.INFIX, AddString(op));
        }

        internal uint OpAs()
        {
            StackCount--;
            return PutOp(OpCode.AS);
        }

        internal uint OpNot()
        {
            return PutOp(OpCode.NOT);
        }

        internal uint OpAnd(uint fail)
        {
            // a and b results in one value, but two were allocated.
            // We -- because we need to.
            StackCount--;
            return PutOp(OpCode.AND, fail);
        }

        internal uint OpOr(uint pass)
        {
            // a or b results in one value, but two were allocated.
            // We -- because we need to.
            StackCount--;
            return PutOp(OpCode.OR, pass);
        }

        internal uint OpXor()
        {
            StackCount--;
            return PutOp(OpCode.XOR);
        }

        internal uint OpCompIs()
        {
            StackCount--;
            return PutOp(OpCode.COMPIS);
        }

        internal uint OpCompNotIs()
        {
            StackCount--;
            return PutOp(OpCode.COMPNOTIS);
        }

        internal uint OpCompTypeOf()
        {
            StackCount--;
            return PutOp(OpCode.COMPTYPEOF);
        }

        internal uint OpCompNotTypeOf()
        {
            StackCount--;
            return PutOp(OpCode.COMPNOTTYPEOF);
        }

        internal uint OpTypeOf()
        {
            return PutOp(OpCode.TYPEOF);
        }

        internal uint OpThrow()
        {
            StackCount--;
            return PutOp(OpCode.THROW);
        }

        internal uint OpStoreEx(uint local)
        {
            return PutOp(OpCode.STOREEX, local);
        }

        internal uint OpBeginExH(uint stackBottom)
        {
            // NOTE we DON'T change the stack count here because this should
            // ONLY be called after try blocks to remove excess variables
            // (that we already account for!)
            return PutOp(OpCode.BEGINEXH, stackBottom);
        }

        internal uint OpPushExH(uint startsAt)
        {
            return PutOp(OpCode.PUSHEXH, startsAt);
        }

        internal uint OpPopExH()
        {
            return PutOp(OpCode.POPEXH);
        }

        internal uint OpIterPrep(uint iterVar, bool hasStep)
        {
            if (hasStep)
                StackCount -= 3;
            else StackCount -= 2;
            return PutOp(OpCode.ITERPREP, iterVar, hasStep ? 1u : 0u);
        }

        internal uint OpIterLoop(uint iterVar, uint jump)
        {
            return PutOp(OpCode.ITERLOOP, iterVar, jump);
        }

        internal uint OpEachPrep(uint eachVar)
        {
            StackCount--;
            return PutOp(OpCode.EACHPREP, eachVar);
        }

        internal uint OpEachLoop(uint eachVar, uint jump)
        {
            return PutOp(OpCode.EACHLOOP, eachVar, jump);
        }

        internal uint OpIEachPrep(uint eachVar)
        {
            StackCount--;
            return PutOp(OpCode.IEACHPREP, eachVar);
        }

        internal uint OpIEachLoop(uint eachVar, uint jump)
        {
            return PutOp(OpCode.IEACHLOOP, eachVar, jump);
        }

        // TODO the rest pls
        #endregion
    }
}
