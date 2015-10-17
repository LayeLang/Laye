namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeWhile : Node
    {
        internal bool not = false; // while not (condition) vs while (condition)
        internal Node condition = null, pass = null, fail = null;
        internal string label;

        internal NodeWhile(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
