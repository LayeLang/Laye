using System.Collections.Generic;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeBlock : Node
    {
        internal bool createScope = true;
        internal readonly List<Node> body;

        internal NodeBlock(Location location, List<Node> body)
            : base(location)
        {
            this.body = body;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
