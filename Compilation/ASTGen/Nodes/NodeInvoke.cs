using System.Collections.Generic;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeInvoke : Node
    {
        internal readonly Node target;
        internal readonly List<Node> args;

        internal NodeInvoke(Node target, List<Node> args)
            : base(target.location)
        {
            this.target = target;
            this.args = args;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
