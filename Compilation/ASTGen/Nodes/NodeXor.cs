using System;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeXor : Node
    {
        internal readonly Node left, right;

        internal NodeXor(Node left, Node right)
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
