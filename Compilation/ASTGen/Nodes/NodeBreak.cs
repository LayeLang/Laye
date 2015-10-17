using System;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeBreak : Node
    {
        internal readonly string label;

        internal NodeBreak(Location location, string label)
            : base(location)
        {
            this.label = label;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
