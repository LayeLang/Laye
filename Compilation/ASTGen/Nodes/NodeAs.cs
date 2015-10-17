using System;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeAs : Node
    {
        internal readonly Node value, type;

        internal NodeAs(Node value, Node type)
            : base(value.location)
        {
            this.value = value;
            this.type = type;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
