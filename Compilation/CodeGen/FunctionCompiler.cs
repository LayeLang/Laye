using System;

namespace Laye.Compilation.CodeGen
{
    using ASTGen;
    using ASTGen.Nodes;
    using Proto;

    internal class FunctionCompiler : ASTVisitor
    {
        internal readonly DetailLogger log;
        internal readonly FunctionBuilder builder;

        internal FunctionCompiler(DetailLogger log, FunctionBuilder parent = null, string fileName = null)
        {
            this.log = log;
            builder = new FunctionBuilder(parent, fileName);
        }

        /// <summary>
        /// Get the function prototype this compiler generates.
        /// </summary>
        /// <returns></returns>
        internal FunctionPrototype GetPrototype()
        {
            return builder.Build();
        }

        void ASTVisitor.Accept(AST value) { Accept(value); }
        internal virtual void Accept(AST ast)
        {
            throw new NotImplementedException();
        }

        void ASTVisitor.Accept(NodeAnd value) { Accept(value); }
        internal void Accept(NodeAnd and)
        {
            and.left.Visit(this);
            uint fail = builder.OpAnd(0);
            and.right.Visit(this);
            builder.SetOpC(fail, builder.InsnCount);
        }

        void ASTVisitor.Accept(NodeAs value) { Accept(value); }
        internal void Accept(NodeAs a)
        {
            a.value.Visit(this);
            a.type.Visit(this);
            builder.OpAs();
        }

        void ASTVisitor.Accept(NodeAssign value) { Accept(value); }
        internal void Accept(NodeAssign assign)
        {
            // There are only a few ways to assign, sooo
            var target = assign.target;
            if (target is NodeIdentifier)
            {
                assign.value.Visit(this);
                builder.currentLineNumber = target.EndLine;
                builder.Store((target as NodeIdentifier).image, false);
            }
            else if (target is NodeIndex)
            {
                var index = target as NodeIndex;
                index.target.Visit(this);
                index.args.ForEach(arg => arg.Visit(this));
                assign.value.Visit(this);
                builder.currentLineNumber = index.EndLine;
                builder.OpIStore((uint)index.args.Count);
            }
            else if (target is NodeOperatorIndex)
            {
                var opIndex = target as NodeOperatorIndex;
                opIndex.target.Visit(this);
                assign.value.Visit(this);
                builder.currentLineNumber = opIndex.target.EndLine;
                builder.OpOIStore(opIndex.op);
            }
            // TODO other things when I add them
            if (!assign.isResultRequired)
                builder.OpPop();
        }

        void ASTVisitor.Accept(NodeBlock value) { Accept(value); }
        internal void Accept(NodeBlock block)
        {
            if (block.createScope)
                builder.StartScope();
            for (int i = 0; i < block.body.Count; i++)
            {
                var expr = block.body[i];
                expr.isResultRequired = i == block.body.Count - 1 ? block.isResultRequired : false;
                expr.inTailPosition = block.inTailPosition && i == block.body.Count - 1;
                expr.Visit(this);
            }
            if (block.createScope)
                builder.EndScope();
        }

        void ASTVisitor.Accept(NodeBool value) { Accept(value); }
        internal void Accept(NodeBool b)
        {
            builder.currentLineNumber = b.EndLine;
            builder.OpBConst(b.value);
        }

        void ASTVisitor.Accept(NodeBreak value) { Accept(value); }
        internal void Accept(NodeBreak b)
        {
            builder.currentLineNumber = b.EndLine;
            builder.AddBreak(log, b.location, b.label);
            // Push null for now, since this is technically an expression we must add an element to the stack.
            builder.OpNull();
        }

        void ASTVisitor.Accept(NodeCont value) { Accept(value); }
        internal void Accept(NodeCont c)
        {
            builder.currentLineNumber = c.EndLine;
            builder.AddCont(log, c.location, c.label);
            // Push null for now, since this is technically an expression we must add an element to the stack.
            builder.OpNull();
        }

        void ASTVisitor.Accept(NodeEach value) { Accept(value); }
        internal void Accept(NodeEach each)
        {
            var isResultRequired = each.isResultRequired;

            if (each.body is NodeBlock)
                (each.body as NodeBlock).createScope = false;
            each.body.isResultRequired = isResultRequired;
            each.body.inTailPosition = each.inTailPosition;

            builder.StartScope();

            uint eachVarLocal;
            if (!builder.AddLocal(each.eachVarName, out eachVarLocal))
                log.Error(each.location, "Duplicate local variable '{0}'.", each.eachVarName);
            uint genLocal = builder.ReseveLocal();

            each.value.Visit(this);
            builder.OpEachPrep(eachVarLocal);

            if (isResultRequired)
                builder.OpList(0);

            // Handle cont
            if (isResultRequired)
                builder.OpJump(builder.InsnCount + 2);
            builder.StartFlowBlock(each.label);
            if (isResultRequired)
                builder.OpPop(false);
            // END Handle cont

            uint loopPos = builder.OpEachLoop(eachVarLocal, 0);
            if (isResultRequired)
                builder.OpDup();
            each.body.Visit(this);
            if (isResultRequired)
            {
                builder.OpOIStore("+");
                builder.OpPop();
            }
            builder.OpJump(loopPos);
            builder.SetOpB(loopPos, builder.InsnCount);

            // Handle break
            if (isResultRequired)
                builder.OpJump(builder.InsnCount + 2);
            builder.EndFlowBlock();
            if (isResultRequired)
                builder.OpPop(false);
            // END Handle break
            builder.EndScope();
        }

        void ASTVisitor.Accept(NodeEndL value) { Accept(value); }
        internal void Accept(NodeEndL endl)
        {
            builder.currentLineNumber = endl.EndLine;
            builder.OpEndL();
        }

        void ASTVisitor.Accept(NodeFieldIndex value) { Accept(value); }
        internal void Accept(NodeFieldIndex index)
        {
            index.value.Visit(this);
            builder.currentLineNumber = index.EndLine;
            builder.OpFLoad(index.fieldName);
        }

        void ASTVisitor.Accept(NodeFloat value) { Accept(value); }
        internal void Accept(NodeFloat f)
        {
            builder.currentLineNumber = f.EndLine;
            builder.OpFConst(f.value);
        }

        void ASTVisitor.Accept(NodeFnDef value) { Accept(value); }
        internal virtual void Accept(NodeFnDef fn)
        {
            // Leaves the function on the stack
            Accept(fn as NodeFnExpr);

            // Assign it to a thing
            if (!fn.isGlobal)
            { // I hate nesting ifs without brackets...
                // The only valid node here will be an identifier
                if (fn.target is NodeIdentifier)
                {
                    builder.currentLineNumber = fn.target.EndLine;
                    builder.OpGStore((fn.target as NodeIdentifier).image);
                }
                else log.Error(fn.target.location, "Function target must be an identifier when assigning to the global namespace.");
            }
            else
            { // I really hate nesting ifs without brackets...
                if (fn.target is NodeIdentifier)
                {
                    uint local;
                    builder.AddLocal((fn.target as NodeIdentifier).image, out local);
                    builder.currentLineNumber = fn.target.EndLine;
                    builder.OpLStore(local);
                    if (!fn.isResultRequired)
                        builder.OpPop();
                }
                // TODO check all the things plz
                else log.Error(fn.target.location, "Invalid function target.");
            }

            // We'll always have a value on the stack here, pop the value if we don't need it
            if (!fn.isResultRequired)
                builder.OpPop();
        }

        void ASTVisitor.Accept(NodeFnExpr value) { Accept(value); }
        internal void Accept(NodeFnExpr fn)
        {
            // COMPILERRRRRRRR! DUDUDUDU DUN DUNNNNN DU DU-UNNN DU DU-UNNN DU DU-UNN (guitar noises) (Dope - Nothing For Me Here (listening to music is fun (just wait till Reol comes on (you won't regret it (れをる <3 (Japanese input is too fun (I'm done now)))))))
            var compiler = new FunctionCompiler(log, builder);

            compiler.builder.trulyAnon = fn.fullAnon;
            compiler.builder.hasVargs = fn.hasVargs;
            compiler.builder.isGenerator = fn.isGenerator;

            // params/vargs
            if (fn.fullAnon)
                fn.paramNames.ForEach(param => compiler.builder.ReserveParameter());
            else fn.paramNames.ForEach(param => compiler.builder.AddParameter(param));

            // Now do codeee
            fn.body.isResultRequired = !fn.isVoid;
            fn.body.inTailPosition = true;
            fn.body.Visit(compiler);

            // Now, we should have a completely compiled function!
            var proto = compiler.GetPrototype();
            // Load the default param values
            fn.defaultParams.ForEach(def => def.Visit(this));
            // Push that to the stack plz
            builder.OpClosure(proto, (uint)fn.defaultParams.Count);
            // If this is a generator, make it so.
            if (fn.isGenerator)
                builder.OpGenerator();
        }

        void ASTVisitor.Accept(NodeIf value) { Accept(value); }
        internal void Accept(NodeIf expr)
        {
            var condition = expr.condition;

            expr.pass.isResultRequired = expr.isResultRequired;
            expr.pass.inTailPosition = expr.inTailPosition;
            if (expr.fail != null)
            {
                expr.fail.isResultRequired = expr.isResultRequired;
                expr.fail.inTailPosition = expr.inTailPosition;
            }

            // Disable scopes, we'll do them ourselves
            if (expr.pass is NodeBlock)
                (expr.pass as NodeBlock).createScope = false;
            if (expr.fail != null && expr.fail is NodeBlock)
                (expr.fail as NodeBlock).createScope = false;

            builder.StartScope();
            uint failLoc;
            /*
            if (condition is NodeInfix && (condition as NodeInfix).op == "==")
            {

            }
            else if (condition is NodeInfix && (condition as NodeInfix).op == "!=")
            {

            }
            else // */
            {
                condition.Visit(this);
                failLoc = builder.InsnCount;
                if (expr.not)
                    builder.OpJumpT(0);
                else builder.OpJumpF(0);
            }

            expr.pass.Visit(this);
            builder.EndScope();

            if (expr.fail != null)
            {
                uint passLoc = builder.OpJump(0);
                builder.SetOpC(failLoc, builder.InsnCount);
                builder.StartScope();
                expr.fail.Visit(this);
                builder.EndScope();
                builder.SetOpC(passLoc, builder.InsnCount);
            }
            else
            {
                builder.SetOpC(failLoc, builder.InsnCount);
                if (expr.isResultRequired)
                    builder.OpNull();
            }
        }

        void ASTVisitor.Accept(NodeIdentifier value) { Accept(value); }
        internal void Accept(NodeIdentifier ident)
        {
            builder.currentLineNumber = ident.EndLine;
            builder.Load(ident.image);
        }

        void ASTVisitor.Accept(NodeIEach value) { Accept(value); }
        internal void Accept(NodeIEach ieach)
        {
            var isResultRequired = ieach.isResultRequired;

            if (ieach.body is NodeBlock)
                (ieach.body as NodeBlock).createScope = false;
            ieach.body.isResultRequired = isResultRequired;
            ieach.body.inTailPosition = ieach.inTailPosition;

            builder.StartScope();

            uint eachIndexLocal;
            if (!builder.AddLocal(ieach.eachIndexName, out eachIndexLocal))
                log.Error(ieach.location, "Duplicate local variable '{0}'.", ieach.eachVarName);
            uint tempIndexLocal = builder.ReseveLocal();
            uint eachVarLocal;
            if (!builder.AddLocal(ieach.eachVarName, out eachVarLocal))
                log.Error(ieach.location, "Duplicate local variable '{0}'.", ieach.eachVarName);
            uint genLocal = builder.ReseveLocal();

            ieach.value.Visit(this);
            builder.OpIEachPrep(eachIndexLocal);

            if (isResultRequired)
                builder.OpList(0);

            // Handle cont
            if (isResultRequired)
                builder.OpJump(builder.InsnCount + 2);
            builder.StartFlowBlock(ieach.label);
            if (isResultRequired)
                builder.OpPop(false);
            // END Handle cont

            uint loopPos = builder.OpIEachLoop(eachIndexLocal, 0);
            if (isResultRequired)
                builder.OpDup();
            ieach.body.Visit(this);
            if (isResultRequired)
            {
                builder.OpOIStore("+");
                builder.OpPop();
            }
            builder.OpJump(loopPos);
            builder.SetOpB(loopPos, builder.InsnCount);

            // Handle break
            if (isResultRequired)
                builder.OpJump(builder.InsnCount + 2);
            builder.EndFlowBlock();
            if (isResultRequired)
                builder.OpPop(false);
            // END Handle break
            builder.EndScope();
        }

        void ASTVisitor.Accept(NodeIndex value) { Accept(value); }
        internal void Accept(NodeIndex index)
        {
            index.target.Visit(this);
            index.args.ForEach(arg => arg.Visit(this));
            builder.currentLineNumber = index.target.EndLine;
            builder.OpILoad((uint)index.args.Count);
        }

        void ASTVisitor.Accept(NodeInfix value) { Accept(value); }
        internal void Accept(NodeInfix infix)
        {
            infix.left.Visit(this);
            infix.right.Visit(this);
            builder.currentLineNumber = infix.left.EndLine;
            builder.OpInfix(infix.op);
        }

        void ASTVisitor.Accept(NodeInt value) { Accept(value); }
        internal void Accept(NodeInt i)
        {
            builder.currentLineNumber = i.EndLine;
            builder.OpIConst(i.value);
        }

        void ASTVisitor.Accept(NodeInvoke value) { Accept(value); }
        internal void Accept(NodeInvoke invoke)
        {
            // TODO break this up to support invokeMethod, thisInvoke, and baseInvoke
            var target = invoke.target;
            if (target is NodeFieldIndex)
            {
                var index = target as NodeFieldIndex;
                index.value.Visit(this);
                invoke.args.ForEach(arg => arg.Visit(this));
                builder.currentLineNumber = index.value.EndLine;
                builder.OpMInvoke(index.fieldName, (uint)invoke.args.Count);
            }
            else
            {
                target.Visit(this);
                invoke.args.ForEach(arg => arg.Visit(this));
                builder.currentLineNumber = target.EndLine;
                builder.OpInvoke((uint)invoke.args.Count);
            }
            if (!invoke.isResultRequired)
                builder.OpPop();
        }

        void ASTVisitor.Accept(NodeIter value) { Accept(value); }
        internal void Accept(NodeIter iter)
        {
            var isResultRequired = iter.isResultRequired;
            bool hasStep = iter.step != null;

            if (iter.body is NodeBlock)
                (iter.body as NodeBlock).createScope = false;
            iter.body.isResultRequired = isResultRequired;
            iter.body.inTailPosition = iter.inTailPosition;

            builder.StartScope();

            uint iterVarLocal;
            if (!builder.AddLocal(iter.iterVarName, out iterVarLocal))
                log.Error(iter.location, "Duplicate local variable '{0}'.", iter.iterVarName);
            uint indexLocal = builder.ReseveLocal();
            uint limitLocal = builder.ReseveLocal();
            uint stepLocal  = builder.ReseveLocal();

            iter.init.Visit(this);
            iter.limit.Visit(this);
            if (hasStep)
                iter.step.Visit(this);
            builder.OpIterPrep(iterVarLocal, hasStep);

            if (isResultRequired)
                builder.OpList(0);

            // Handle cont
            if (isResultRequired)
                builder.OpJump(builder.InsnCount + 2);
            builder.StartFlowBlock(iter.label);
            if (isResultRequired)
                builder.OpPop(false);
            // END Handle cont

            uint loopPos = builder.OpIterLoop(iterVarLocal, 0);
            if (isResultRequired)
                builder.OpDup();
            iter.body.Visit(this);
            if (isResultRequired)
            {
                builder.OpOIStore("+");
                builder.OpPop();
            }
            builder.OpJump(loopPos);
            builder.SetOpB(loopPos, builder.InsnCount);

            // Handle break
            if (isResultRequired)
                builder.OpJump(builder.InsnCount + 2);
            builder.EndFlowBlock();
            if (isResultRequired)
                builder.OpPop(false);
            // END Handle break
            builder.EndScope();
        }

        void ASTVisitor.Accept(NodeList value) { Accept(value); }
        internal void Accept(NodeList list)
        {
            list.values.ForEach(value => value.Visit(this));
            builder.currentLineNumber = list.EndLine;
            builder.OpList((uint)list.values.Count);
        }

        void ASTVisitor.Accept(NodeNot value) { Accept(value); }
        internal void Accept(NodeNot not)
        {
            not.value.Visit(this);
            builder.OpNot();
        }

        void ASTVisitor.Accept(NodeNull value) { Accept(value); }
        internal void Accept(NodeNull n)
        {
            builder.currentLineNumber = n.EndLine;
            builder.OpNull();
        }

        void ASTVisitor.Accept(NodeOperatorIndex value) { Accept(value); }
        internal void Accept(NodeOperatorIndex opIndex)
        {
            opIndex.target.Visit(this);
            builder.currentLineNumber = opIndex.target.EndLine;
            builder.OpOILoad(opIndex.op);
        }

        void ASTVisitor.Accept(NodeOr value) { Accept(value); }
        internal void Accept(NodeOr or)
        {
            or.left.Visit(this);
            uint pass = builder.OpOr(0);
            or.right.Visit(this);
            builder.SetOpC(pass, builder.InsnCount);
        }

        void ASTVisitor.Accept(NodeParamIndex value) { Accept(value); }
        internal virtual void Accept(NodeParamIndex param)
        {
            // The underscore ALWAYS refers to the first argument, but only if the function
            // is anonymous was declared without parameters.
            if (!builder.trulyAnon)
                log.Error(param.location, "A parameter index may only be used to reference a parameter in a fully anonymous function.");
            builder.OpLLoad(param.index);
        }

        void ASTVisitor.Accept(NodePrefix value) { Accept(value); }
        internal void Accept(NodePrefix prefix)
        {
            prefix.value.Visit(this);
            builder.currentLineNumber = prefix.value.EndLine;
            builder.OpPrefix(prefix.op);
        }

        void ASTVisitor.Accept(NodeRes value) { Accept(value); }
        internal void Accept(NodeRes res)
        {
            res.value.Visit(this);
            builder.OpRes();
        }

        void ASTVisitor.Accept(NodeSelf value) { Accept(value); }
        internal void Accept(NodeSelf self)
        {
            builder.currentLineNumber = self.EndLine;
            builder.OpSelf();
        }

        void ASTVisitor.Accept(NodeString value) { Accept(value); }
        internal void Accept(NodeString s)
        {
            builder.currentLineNumber = s.EndLine;
            builder.OpSConst(s.value);
        }

        void ASTVisitor.Accept(NodeSymbol value) { Accept(value); }
        internal void Accept(NodeSymbol s)
        {
            builder.currentLineNumber = s.EndLine;
            builder.OpSConst(s.value, true);
        }

        void ASTVisitor.Accept(NodeTailRec value) { Accept(value); }
        internal void Accept(NodeTailRec tailRec)
        {
            if (!tailRec.inTailPosition)
                log.Error(tailRec.location, "tailrec call is not in tail position of function call, cannot optimize.");
            tailRec.args.ForEach(arg => arg.Visit(this));
            builder.currentLineNumber = tailRec.EndLine;
            builder.OpTailInvoke((uint)tailRec.args.Count);
        }

        void ASTVisitor.Accept(NodeThis value) { Accept(value); }
        internal void Accept(NodeThis ths)
        {
            builder.currentLineNumber = ths.EndLine;
            builder.OpThis();
        }

        void ASTVisitor.Accept(NodeThrow value) { Accept(value); }
        internal void Accept(NodeThrow t)
        {
            builder.currentLineNumber = t.EndLine;
            t.value.Visit(this);
            builder.OpThrow();
        }

        void ASTVisitor.Accept(NodeTry value) { Accept(value); }
        internal void Accept(NodeTry t)
        {
            // Disable scopes, we'll do them ourselves
            if (t.body is NodeBlock)
                (t.body as NodeBlock).createScope = false;
            if (t.handler is NodeBlock)
                (t.handler as NodeBlock).createScope = false;

            uint stackSize = builder.StackCount;
            // Start the exception handler
            uint handler = builder.OpPushExH(0);
            // Do things!
            t.body.isResultRequired = t.isResultRequired;
            t.body.inTailPosition = t.inTailPosition;
            builder.StartScope();
            t.body.Visit(this);
            builder.EndScope();
            // When we're done, skip the exception handling code
            uint pass = builder.OpJump(0);
            // This is where we'll go if we fail
            builder.SetOpC(handler, builder.InsnCount);
            // Begin our exception handling, which clears the stack to a certain point for us
            builder.OpBeginExH(stackSize);
            // Here, we have an exception! If we want it, assign it to our local var
            builder.StartScope();
            if (t.exceptionName != null)
            {
                uint local;
                if (!builder.AddLocal(t.exceptionName, out local))
                    log.Error(t.location, "Duplicate local variable '{0}'.", t.exceptionName);
                builder.OpStoreEx(local);
            }
            // Start the exception handling code
            t.handler.isResultRequired = t.isResultRequired;
            t.handler.inTailPosition = t.inTailPosition;
            t.handler.Visit(this);
            builder.EndScope();
            // Here's where we jump if we pass everything
            builder.SetOpC(pass, builder.InsnCount);
        }

        void ASTVisitor.Accept(NodeTuple value) { Accept(value); }
        internal void Accept(NodeTuple tuple)
        {
            tuple.values.ForEach(value => value.Visit(this));
            builder.currentLineNumber = tuple.EndLine;
            builder.OpTuple((uint)tuple.values.Count);
        }

        void ASTVisitor.Accept(NodeUse value) { Accept(value); }
        internal void Accept(NodeUse use)
        {
            // Not the best solution, but it works.
            builder.currentLineNumber = use.EndLine;
            builder.OpGLoad("std");
            builder.OpFLoad("require");
            builder.OpKit();
            builder.OpSConst(use.requirePath);
            builder.OpInvoke(2);
            // TODO builder.OpPop(); // ??
        }

        void ASTVisitor.Accept(NodeVar value) { Accept(value); }
        internal virtual void Accept(NodeVar var)
        {
            builder.currentLineNumber = var.StartLine;
            var.vars.ForEach(def =>
            {
                if (def.Value != null)
                    def.Value.Visit(this);
                else builder.OpNull();
                uint local;
                builder.AddLocal(def.Key, out local);
                builder.OpLStore(local);
                if (!var.isResultRequired)
                    builder.OpPop();
            });
        }

        void ASTVisitor.Accept(NodeWhen value) { Accept(value); }
        internal void Accept(NodeWhen when)
        {
            var condition = when.condition;

            when.pass.isResultRequired = when.isResultRequired;
            when.pass.inTailPosition = when.inTailPosition;
            when.fail.isResultRequired = when.isResultRequired;
            when.fail.inTailPosition = when.inTailPosition;

            // Disable scopes, we'll do them ourselves
            if (when.pass is NodeBlock)
                (when.pass as NodeBlock).createScope = false;
            if (when.fail is NodeBlock)
                (when.fail as NodeBlock).createScope = false;

            builder.StartScope();

            uint failLoc;
            /*
            if (condition is NodeInfix && (condition as NodeInfix).op == "==")
            {

            }
            else if (condition is NodeInfix && (condition as NodeInfix).op == "!=")
            {

            }
            else // */
            {
                condition.Visit(this);
                failLoc = builder.OpJumpF(0);
            }

            when.pass.Visit(this);

            uint passLoc = builder.OpJump(0);
            builder.SetOpC(failLoc, builder.InsnCount);
            when.fail.Visit(this);
            builder.SetOpC(passLoc, builder.InsnCount);

            builder.EndScope();
        }

        void ASTVisitor.Accept(NodeWhile value) { Accept(value); }
        internal void Accept(NodeWhile expr)
        {
            var isResultRequired = expr.isResultRequired;
            var hasEl = expr.fail != null;

            var condition = expr.condition;

            uint initFailLoc = 0, initPassLoc = 0, start, failLoc;

            // Disable scopes, we'll do them ourselves
            if (expr.pass is NodeBlock)
                (expr.pass as NodeBlock).createScope = false;
            if (expr.fail != null && expr.fail is NodeBlock)
                (expr.fail as NodeBlock).createScope = false;

            expr.pass.isResultRequired = isResultRequired;
            expr.pass.inTailPosition = expr.inTailPosition;

            builder.StartScope();
            if (hasEl)
            {
                expr.fail.isResultRequired = isResultRequired;
                expr.fail.inTailPosition = expr.inTailPosition;

                // initial if
                condition.Visit(this);
                initFailLoc = builder.InsnCount;
                if (expr.not)
                    builder.OpJumpT(0);
                else builder.OpJumpF(0);
            }

            // while loop
            if (isResultRequired)
                builder.OpList(0);

            if (hasEl)
                initPassLoc = builder.OpJump(0);

            //*/ Handle cont
            if (isResultRequired)
                builder.OpJump(builder.InsnCount + 2);
            builder.StartFlowBlock(expr.label);
            if (isResultRequired)
                builder.OpPop(false);
            // END Handle cont */

            start = builder.InsnCount;
            {
                condition.Visit(this);
                failLoc = builder.InsnCount;
                if (expr.not)
                    builder.OpJumpT(0);
                else builder.OpJumpF(0);
            }

            if (hasEl)
                builder.SetOpC(initPassLoc, builder.InsnCount);
            if (isResultRequired)
                builder.OpDup();
            expr.pass.Visit(this);
            if (isResultRequired)
            {
                builder.OpOIStore("+");
                builder.OpPop();
            }
            builder.OpJump(start);

            //*/ Handle break
            if (isResultRequired)
                builder.OpJump(builder.InsnCount + 2);
            builder.EndFlowBlock();
            if (isResultRequired)
                builder.OpPop(false);
            // END Handle break */
            builder.EndScope();
            builder.SetOpC(failLoc, builder.InsnCount);
            uint finalFailLoc = builder.OpJump(0);

            // el
            if (hasEl)
            {
                builder.SetOpC(initFailLoc, builder.InsnCount);
                builder.StartScope();
                expr.fail.Visit(this);
                builder.EndScope();
            }

            builder.SetOpC(finalFailLoc, builder.InsnCount);
        }

        void ASTVisitor.Accept(NodeWildcard value) { Accept(value); }
        internal void Accept(NodeWildcard wild)
        {
            // The underscore ALWAYS refers to the first argument, but only if the function
            // is anonymous was declared without parameters.
            if (!builder.trulyAnon)
                log.Error(wild.location, "The wildcard token may only be used to reference a parameter in a fully anonymous function.");
            builder.OpLLoad(0);
        }

        void ASTVisitor.Accept(NodeXor value) { Accept(value); }
        internal void Accept(NodeXor xor)
        {
            xor.left.Visit(this);
            xor.right.Visit(this);
            builder.OpXor();
        }

        void ASTVisitor.Accept(NodeYield value) { Accept(value); }
        internal void Accept(NodeYield yield)
        {
            if (!builder.isGenerator)
                log.Error(yield.location, "Can only yield inside a generator.");
            yield.value.Visit(this);
            builder.OpYield();
        }
    }
}
