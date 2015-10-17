using System;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeOr : Node
    {
        internal readonly Node left, right;

        internal NodeOr(Node left, Node right)
            : base(left.location)
        {
            this.left = left;
            this.right = right;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
