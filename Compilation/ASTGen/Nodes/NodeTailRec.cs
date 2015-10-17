using System.Collections.Generic;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeTailRec : Node
    {
        internal readonly List<Node> args;

        internal NodeTailRec(Location location, List<Node> args)
            : base(location)
        {
            this.args = args;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
