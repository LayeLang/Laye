namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeEndL : Node
    {
        internal NodeEndL(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
