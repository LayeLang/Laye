namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodePrefix : Node
    {
        internal readonly Node value;
        internal readonly string op;

        internal NodePrefix(Location location, string op, Node value)
            : base(location)
        {
            this.value = value;
            this.op = op;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
