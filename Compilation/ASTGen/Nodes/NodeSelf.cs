using System;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeSelf : Node
    {
        internal NodeSelf(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
