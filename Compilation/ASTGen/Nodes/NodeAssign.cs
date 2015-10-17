namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeAssign : Node
    {
        internal readonly Node target, value;

        internal NodeAssign(Node target, Node value)
            : base(target.location)
        {
            this.target = target;
            this.value = value;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
