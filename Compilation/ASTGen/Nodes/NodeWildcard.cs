using System;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeWildcard : Node
    {
        internal NodeWildcard(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
