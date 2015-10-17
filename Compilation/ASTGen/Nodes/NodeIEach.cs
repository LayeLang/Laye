namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeIEach : Node
    {
        internal string eachIndexName, eachVarName;
        internal Node value, body;
        internal string label;

        internal NodeIEach(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
