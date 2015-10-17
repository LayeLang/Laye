using System;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeNot : Node
    {
        internal readonly Node value;

        internal NodeNot(Location location, Node value)
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
