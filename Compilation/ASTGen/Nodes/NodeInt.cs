namespace Laye.Compilation.ASTGen.Nodes
{
#if LAYE64
    using lint = System.Int64;
#else
    using lint = System.Int32;
#endif

    internal sealed class NodeInt : Node
    {
        internal readonly lint value;

        internal NodeInt(Location location, lint value)
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
