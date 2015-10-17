using System;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeYield : Node
    {
        internal readonly Node value;

        internal NodeYield(Location location, Node value)
            : base(location)
        {
            this.value = value;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
