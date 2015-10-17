namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeIf : Node
    {
        internal bool not = false; // if not (condition) vs if (condition)
        internal Node condition = null, pass = null, fail = null;

        internal NodeIf(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
