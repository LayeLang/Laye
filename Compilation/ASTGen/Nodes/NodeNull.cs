namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeNull : Node
    {
        internal NodeNull(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
