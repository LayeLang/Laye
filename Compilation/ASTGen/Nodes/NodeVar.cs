using System.Collections.Generic;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeVar : Node
    {
        internal List<KeyValuePair<string, Node>> vars = new List<KeyValuePair<string, Node>>();

        internal NodeVar(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
