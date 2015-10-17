using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Laye.Compilation.ASTGen.Nodes
{
    internal class NodeTry : Node
    {
        internal Node body, handler;
        internal string exceptionName;

        public NodeTry(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
