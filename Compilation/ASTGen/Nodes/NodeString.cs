namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeString : Node
    {
        internal readonly string value;

        internal NodeString(Location location, string value)
            : base(location)
        {
            this.value = value;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
