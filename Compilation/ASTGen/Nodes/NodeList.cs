using System.Collections.Generic;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeList : Node
    {
        internal readonly List<Node> values;

        internal NodeList(Location location, List<Node> values)
            : base(location)
        {
            this.values = values;
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
