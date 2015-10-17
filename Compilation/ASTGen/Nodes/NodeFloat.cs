namespace Laye.Compilation.ASTGen.Nodes
{
#if LAYE64
    using lfloat = System.Double;
#else
    using lfloat = System.Single;
#endif

    internal sealed class NodeFloat : Node
    {
        internal readonly lfloat value;

        internal NodeFloat(Location location, lfloat value)
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
