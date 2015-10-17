using System.Collections.Generic;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class AST : Node
    {
        internal List<Node> nodes = new List<Node>();

        internal IEnumerable<Node> Nodes
        {
            get
            {
                foreach (var node in nodes)
                    yield return node;
                yield break;
            }
        }

        internal override uint EndLine { get { return nodes[nodes.Count - 1].EndLine; } }

        internal AST()
            : base(null)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
