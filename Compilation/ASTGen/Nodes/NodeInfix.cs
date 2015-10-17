namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeInfix : Node
    {
        internal readonly Node left, right;
        internal readonly string op;

        internal NodeInfix(Node left, Node right, string op)
            : base(left.location)
        {
            this.left = left;
            this.right = right;
            this.op = op;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
