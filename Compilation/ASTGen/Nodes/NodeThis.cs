using System;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeThis : Node
    {
        internal NodeThis(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
