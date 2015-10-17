namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeParamIndex : Node
    {
        internal uint index;

        internal NodeParamIndex(Location location, uint index)
            : base(location)
        {
            this.index = index;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
