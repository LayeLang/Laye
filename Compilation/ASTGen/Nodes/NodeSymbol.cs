namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeSymbol : Node
    {
        internal readonly string value;

        internal NodeSymbol(Location location, string value)
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
