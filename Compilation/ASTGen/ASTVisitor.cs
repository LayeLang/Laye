namespace Laye.Compilation.ASTGen
{
    using Nodes;

    internal interface ASTVisitor
    {
        void Accept(AST ast);
        void Accept(NodeAnd and);
        void Accept(NodeAs a);
        void Accept(NodeAssign assign);
        void Accept(NodeBlock block);
        void Accept(NodeBool b);
        void Accept(NodeBreak b);
        void Accept(NodeCont b);
        void Accept(NodeEach each);
        void Accept(NodeEndL endl);
        void Accept(NodeFieldIndex index);
        void Accept(NodeFloat f);
        void Accept(NodeFnDef fn);
        void Accept(NodeFnExpr fn);
        void Accept(NodeIdentifier ident);
        void Accept(NodeIf expr);
        void Accept(NodeIndex index);
        void Accept(NodeInfix infix);
        void Accept(NodeInt i);
        void Accept(NodeInvoke invoke);
        void Accept(NodeIter iter);
        void Accept(NodeList list);
        void Accept(NodeNot not);
        void Accept(NodeNull n);
        void Accept(NodeOperatorIndex opIndex);
        void Accept(NodeOr or);
        void Accept(NodeParamIndex param);
        void Accept(NodePrefix prefix);
        void Accept(NodeRes res);
        void Accept(NodeSelf self);
        void Accept(NodeString s);
        void Accept(NodeSymbol s);
        void Accept(NodeTailRec tailRec);
        void Accept(NodeThis ths);
        void Accept(NodeThrow t);
        void Accept(NodeTry t);
        void Accept(NodeTuple tuple);
        void Accept(NodeUse use);
        void Accept(NodeVar var);
        void Accept(NodeWhen when);
        void Accept(NodeWhile expr);
        void Accept(NodeWildcard wild);
        void Accept(NodeXor xor);
        void Accept(NodeYield yield);
    }
}
