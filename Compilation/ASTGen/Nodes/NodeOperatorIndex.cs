namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeOperatorIndex : Node
    {
        internal readonly Node target;
        internal readonly string op;

        internal NodeOperatorIndex(Node target, string op)
            : base(target.location)
        {
            this.target = target;
            this.op = op;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
