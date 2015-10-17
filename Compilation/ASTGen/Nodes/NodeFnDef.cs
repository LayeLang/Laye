namespace Laye.Compilation.ASTGen.Nodes
{
    internal sealed class NodeFnDef : NodeFnExpr
    {
        /// <summary>
        /// Where this function is to be defined.
        /// 
        /// For example:
        /// 
        /// <c>
        /// fn Int.infix !!()
        /// {
        /// }
        /// </c>
        /// 
        /// will be defined in Int as the !! infix operator.
        /// </summary>
        internal Node target = null;

        /// <summary>
        /// Should this function be defined in the global namespace?
        /// </summary>
        internal bool isGlobal = false;

        internal NodeFnDef(Location location)
            : base(location)
        {
        }

        internal override void Visit(ASTVisitor visitor)
        {
            visitor.Accept(this);
        }
    }
}
