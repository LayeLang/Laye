namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeWhen : Node
    {
        internal Node condition = null, pass = null, fail = null;

        internal NodeWhen(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
