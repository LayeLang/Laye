using System;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeThrow : Node
    {
        internal readonly Node value;

        internal NodeThrow(Location location, Node value)
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
