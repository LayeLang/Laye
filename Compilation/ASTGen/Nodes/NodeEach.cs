namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeEach : Node
    {
        internal string eachIndexName, eachVarName;
        internal Node value, body;
        internal string label;

        internal NodeEach(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
