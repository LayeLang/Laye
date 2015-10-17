namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeBool : Node
    {
        internal readonly bool value;

        internal NodeBool(Location location, bool value)
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
