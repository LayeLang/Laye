namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeIdentifier : Node
    {
        internal readonly string image;

        internal NodeIdentifier(Location location, string image)
            : base(location)
        {
            this.image = image;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
