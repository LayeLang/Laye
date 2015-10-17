namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeIter : Node
    {
        internal string iterVarName;
        internal Node init, limit, step, body;
        internal string label;

        public NodeIter(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
