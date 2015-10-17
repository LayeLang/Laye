namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeFieldIndex : Node
    {
        internal readonly Node value;
        internal readonly string fieldName;

        internal NodeFieldIndex(Node value, string fieldName)
            : base(value.location)
        {
            this.value = value;
            this.fieldName = fieldName;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
