namespace Laye.Compilation.ASTGen.Nodes
{
    internal abstract class Node
    {
        internal readonly Location location;

        /// <summary>
        /// Used to determine if this node should leave it's result on the stack.
        /// 
        /// For example:
        /// 
        /// <code>
        /// fn main(args..)
        /// {
        ///     println(args)
        /// }
        /// </code>
        /// 
        /// The method 'main' is void (meaning it returns null on exit), so the call
        /// to 'println' needn't have it's return value saved, as we don't aim to return
        /// a value. That call would have an isResultRequired value of false.
        /// </summary>
        internal bool isResultRequired = true;

        /// <summary>
        /// Used to determine if this expression is the last one in a function.
        /// 
        /// This may always be set to true for return statements.
        /// </summary>
        internal bool inTailPosition = false;

        /// <summary>
        /// Gets the line this node starts on.
        /// </summary>
        internal virtual uint StartLine { get { return location.line; } }

        private uint endLine = 0;
        /// <summary>
        /// Gets the line this node ends on.
        /// </summary>
        internal virtual uint EndLine { get { return endLine == 0 ? location.endLine : endLine; } set { endLine = value; } }

        internal Node(Location location)
        {
            this.location = location;
        }

        internal abstract void Visit(ASTVisitor visitor);
    }
}
