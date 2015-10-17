using System.Collections.Generic;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal class NodeFnExpr : Node
    {
        internal List<string> paramNames = new List<string>();
        internal List<Node> defaultParams = new List<Node>();
        internal bool isVoid = true, hasVargs = false, fullAnon = false, isGenerator = false;
        internal Node body = null;

        internal override uint EndLine { get { return body.EndLine; } }

        internal NodeFnExpr(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
