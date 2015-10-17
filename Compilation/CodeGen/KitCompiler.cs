namespace Laye.Compilation.CodeGen
{
    using ASTGen;
    using ASTGen.Nodes;

    internal sealed class KitCompiler : FunctionCompiler, ASTVisitor
    {
        internal KitCompiler(DetailLogger log, string fileName = null)
            : base(log, null, fileName)
        {
        }

        internal override void Accept(AST ast)
        {
            ast.nodes.ForEach(node =>
            {
                node.isResultRequired = false;
                node.Visit(this);
            });
        }

        internal override void Accept(NodeFnDef fn)
        {
            // Leaves the function on the stack
            Accept(fn as NodeFnExpr);

            // Assign it to a thing
            if (fn.isGlobal)
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
                    builder.currentLineNumber = fn.target.EndLine;
                    builder.OpKStore((fn.target as NodeIdentifier).image);
                }
                // TODO check all the things plz
                else log.Error(fn.target.location, "Invalid function target.");
            }

            // We'll always have a value on the stack here, pop the value if we don't need it
            if (!fn.isResultRequired)
                builder.OpPop();
        }

        internal override void Accept(NodeVar var)
        {
            builder.currentLineNumber = var.StartLine;
            var.vars.ForEach(def =>
            {
                if (def.Value == null)
                    builder.OpNull();
                else def.Value.Visit(this);
                builder.OpKStore(def.Key);
                if (!var.isResultRequired)
                    builder.OpPop();
            });
        }
    }
}
