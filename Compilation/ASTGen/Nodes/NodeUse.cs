namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeUse : Node
    {
        internal readonly string requirePath;

        internal NodeUse(Location location, string requirePath)
            : base(location)
        {
            this.requirePath = requirePath;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
