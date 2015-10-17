using System.Collections.Generic;

namespace Laye.Compilation.ASTGen
{
    using static TokenGen.Token.Type;

    using TokenGen;
    using Nodes;

    internal sealed class FunctionHolder
    {
        internal readonly FunctionHolder previous;
        internal readonly NodeFnExpr current;

        internal FunctionHolder(FunctionHolder previous, NodeFnExpr current)
        {
            this.previous = previous;
            this.current = current;
        }
    }

    internal sealed class Parser
    {
        internal readonly DetailLogger log;

        private TokenStream tokens;
        private FunctionHolder holder;

        internal Parser(DetailLogger log)
        {
            this.log = log;
        }

        private void SetCurrentFunction(NodeFnExpr fn)
        {
            holder = new FunctionHolder(holder, fn);
        }

        private void ReleaseCurrentFunction()
        {
            holder = holder.previous;
        }

        private NodeFnExpr CurrentFunction { get { return holder == null ? null : holder.current; } }

        internal AST GenerateAST(TokenStream tokens)
        {
            this.tokens = tokens;

            AST ast = new AST();

            while (TokensRemain)
            {
                switch (Current.type)
                {
                    case USE:
                        var loc = Location;
                        Advance();
                        var requirePath = "";
                        while (TokensRemain)
                        {
                            string ident;
                            ExpectIdentifier(out ident, "Identifier expected for kit path.");
                            requirePath += ident;
                            if (Check(DOT))
                            {
                                requirePath += ".";
                                Advance();
                            }
                            else break;
                        }
                        var use = new NodeUse(loc, requirePath);
                        ast.nodes.Add(use);
                        continue;
                    default:
                        var expr = Expression();
                        // We hit a huge problem, probably nothing left...
                        if (expr == null)
                            break;
                        ast.nodes.Add(expr);
                        continue;
                }
            }

            return ast;
        }

        #region Tokens
        private bool TokensRemain
        {
            get { return !tokens.IsOver; }
        }

        private bool HasNextToken
        {
            get { return tokens.HasNext; }
        }

        private Location Location
        {
            get { return Current.location; }
        }

        private Token Current
        {
            get { return tokens.Current; }
        }

        private Token Next
        {
            get { return tokens.Next; }
        }

        private void Advance()
        {
            tokens.Advance();
        }

        private bool Expect(Token.Type type, string failMessage)
        {
            return tokens.Expect(type, failMessage);
        }

        private bool ExpectIdentifier(out string ident, string failMessage)
        {
            return tokens.ExpectIdentifier(out ident, failMessage);
        }

        private bool Check(Token.Type type)
        {
            return TokensRemain && Current.type == type;
        }
        #endregion Tokens

        private Node Expression()
        {
            Node expr = PrimaryExpression();
            if (expr == null)
                return null;
            if (TokensRemain)
            {
                Node right;
                switch (Current.type)
                {
                    case WHEN: return ParseWhen(expr);
                    case AND:
                        Advance();
                        right = Expression();
                        if (right == null)
                            log.Error(expr.location, "Expression expected for right of 'and'.");
                        return new NodeAnd(expr, right);
                    case OR:
                        Advance();
                        right = Expression();
                        if (right == null)
                            log.Error(expr.location, "Expression expected for right of 'or'.");
                        return new NodeOr(expr, right);
                    case XOR:
                        Advance();
                        right = Expression();
                        if (right == null)
                            log.Error(expr.location, "Expression expected for right of 'xor'.");
                        return new NodeXor(expr, right);
                    default: break;
                }
            }
            return Factor(expr);
        }

        private List<Node> CommaExpressions(bool allowTrailingComma = false)
        {
            var result = new List<Node>();
            bool first = true;
            while (true)
            {
                var expr = Expression();
                if (expr == null)
                {
                    if (first || allowTrailingComma)
                        break;
                    else log.Error(tokens[-1].location, "Expected an expression, got nothing.");
                }
                first = false;
                result.Add(expr);
                if (Check(COMMA))
                    Advance();
                else break;
            }
            return result;
        }

        private Node PrimaryExpression(bool allowPostfixInvocation = true)
        {
            if (!TokensRemain)
                return null;
            var loc = Location;
            Node node;
            switch (Current.type)
            {
                case WILDCARD:
                    // If we aren't in a truly anon function, we ignore this
                    if (CurrentFunction != null && CurrentFunction.fullAnon && CurrentFunction.paramNames.Count == 0)
                        CurrentFunction.paramNames.Add(null);
                    node = new NodeWildcard(loc);
                    Advance();
                    break;
                case PARAM_INDEX:
                    var index = (uint)Current.intValue;
                    // If we aren't in a truly anon function, we ignore this
                    if (CurrentFunction != null && CurrentFunction.fullAnon)
                        while (CurrentFunction.paramNames.Count <= index)
                            CurrentFunction.paramNames.Add(null);
                    node = new NodeParamIndex(loc, index);
                    Advance();
                    break;
                case IDENTIFIER:
                    node = new NodeIdentifier(loc, Current.image);
                    Advance();
                    break;
                case SYMBOL:
                    node = new NodeSymbol(loc, Current.image);
                    Advance();
                    break;
                case NULL:
                    node = new NodeNull(loc);
                    Advance();
                    break;
                case TRUE:
                case FALSE:
                    node = new NodeBool(loc, Check(TRUE));
                    Advance();
                    break;
                case ENDL:
                    node = new NodeEndL(loc);
                    Advance();
                    break;
                case THIS:
                    node = new NodeThis(loc);
                    Advance();
                    break;
                case SELF:
                    node = new NodeSelf(loc);
                    Advance();
                    break;
                case INT:
                    node = new NodeInt(loc, Current.intValue);
                    Advance();
                    break;
                case FLOAT:
                    node = new NodeFloat(loc, Current.floatValue);
                    Advance();
                    break;
                case STRING:
                    node = new NodeString(loc, Current.image);
                    Advance();
                    break;
                case OPEN_BRACE:
                    Advance();
                    if (Check(CLOSE_BRACE))
                    {
                        // 0-length tuple:
                        node = new NodeTuple(loc, new List<Node>());
                        node.EndLine = Current.location.line;
                        Advance();
                    }
                    else
                    {
                        var values = CommaExpressions();
                        if (values.Count != 1)
                        {
                            // This is a tuple :D
                            node = new NodeTuple(loc, values);
                            Expect(CLOSE_BRACE, "Closing brace (')') expected to end tuple expression.");
                            node.EndLine = tokens[-1].location.line;
                        }
                        else
                        {
                            Expect(CLOSE_BRACE, "Closing brace (')') expected to surround expression.");
                            // Just an expression, carry on:
                            node = values[0];
                        }
                    }
                    break;
                case OPEN_SQUARE_BRACE:
                    // This is a list :D
                    Advance();
                    node = new NodeList(loc, (TokensRemain && Current.type != CLOSE_SQUARE_BRACE) ? CommaExpressions() : new List<Node>());
                    Expect(CLOSE_SQUARE_BRACE, "Closing square brace (']') expected to end list expression.");
                    node.EndLine = tokens[-1].location.line;
                    break;
                case OPEN_CURLY_BRACE:
                    // This is a code block :D
                    Advance();
                    var exprs = new List<Node>();
                    while (!Check(CLOSE_CURLY_BRACE))
                    {
                        if (!TokensRemain)
                        {
                            log.Error(tokens[-1].location, "Unfinished block. A closing curly brace ('}') should be used to end blocks.");
                            break;
                        }
                        var expr = Expression();
                        if (expr != null)
                            exprs.Add(expr);
                    }
                    Expect(CLOSE_CURLY_BRACE, "Closing curly brace ('}') expected to end block.");
                    node = new NodeBlock(loc, exprs);
                    node.EndLine = tokens[-1].location.line;
                    break;
                case TAILREC:
                    Advance();
                    Expect(OPEN_BRACE, "Expected an open brace ('(') to begin the tailrec argument list.");
                    List<Node> args;
                    if (!Check(CLOSE_BRACE))
                        args = CommaExpressions();
                    else args = new List<Node>();
                    Expect(CLOSE_BRACE, "Expected a close brace (')') to end the tailrec argument list.");
                    node = new NodeTailRec(loc, args);
                    break;
                // These can't be postfix'd, so we don't allow it.
                case OPERATOR:
                    var prefix = Current.image;
                    Advance();
                    if (!TokensRemain)
                        log.Error(loc, "An expression is expected after prefix operator, but the end of the file was reached.");
                    var prefixValue = PrimaryExpression();
                    return new NodePrefix(loc, prefix, prefixValue);
                case NOT:
                    Advance();
                    if (!TokensRemain)
                        log.Error(loc, "An expression is expected after the 'not' keyword, but the end of the file was reached.");
                    return new NodeNot(loc, Expression());
                case THROW:
                    Advance();
                    if (!TokensRemain)
                        log.Error(loc, "An expression is expected after the 'throw' keyword, but the end of the file was reached.");
                    return new NodeThrow(loc, Expression());
                case YIELD:
                    Advance();
                    if (!TokensRemain)
                        log.Error(loc, "An expression is expected after the 'yield' keyword, but the end of the file was reached.");
                    return new NodeYield(loc, Expression());
                case RES:
                    Advance();
                    if (!TokensRemain)
                        log.Error(loc, "An expression is expected after the 'res' keyword, but the end of the file was reached.");
                    return new NodeRes(loc, Expression());
                case BREAK:
                    Advance();
                    string bLabel = null;
                    if (Check(IDENTIFIER) && Current.location.line == loc.line)
                    {
                        bLabel = Current.image;
                        Advance();
                    }
                    return new NodeBreak(loc, bLabel);
                case CONT:
                    Advance();
                    string cLabel = null;
                    if (Check(IDENTIFIER) && Current.location.line == loc.line)
                    {
                        cLabel = Current.image;
                        Advance();
                    }
                    return new NodeCont(loc, cLabel);
                case FN: return ParseFn();
                case GEN: return ParseFn(true);
                case VAR: return ParseVar();
                case IF: return ParseIf();
                case WHILE: return ParseWhile();
                case TRY: return ParseTry();
                case ITER: return ParseIter();
                case EACH: return ParseEach();
                case IEACH: return ParseIEach();
                // And here, we don't know what they want... oops.
                default:
                    log.Error(Location, "Unexpected token '{0}', skipping...", Current.ToString());
                    // We don't know what to do with this, let's skip and try to recover.
                    Advance();
                    // Error saying we couldn't understand the token:
                    // return a new primary expression.
                    return PrimaryExpression();
            }
            // TODO postfix
            return Postfix(node, allowPostfixInvocation);
        }

        private Node Postfix(Node node, bool allowPostfixInvocation)
        {
            if (node == null || !TokensRemain)
                return node;
            // handle { i+ = 1 } assign operators
            if (HasNextToken && Current.type == OPERATOR && Next.type == ASSIGN)
            {
                string op = Current.image;
                Advance(); Advance();
                var right = Expression();
                if (right == null)
                {
                    // TODO error
                }
                return new NodeAssign(node, new NodeInfix(node, right, op));
            }
            switch (Current.type)
            {
                case ASSIGN: Advance(); return new NodeAssign(node, Expression());
                case OPEN_BRACE:
                    if (Location.line != node.EndLine || !allowPostfixInvocation)
                        break;
                    Advance();
                    List<Node> args;
                    if (!Check(CLOSE_BRACE))
                        args = CommaExpressions();
                    else args = new List<Node>();
                    Expect(CLOSE_BRACE, "Expected a close brace (')') to end the function argument list.");
                    return Postfix(new NodeInvoke(node, args), allowPostfixInvocation);
                case OPEN_SQUARE_BRACE:
                    if (Location.line != node.EndLine)
                        break;
                    Advance();
                    if (Check(OPERATOR) && HasNextToken && Next.type == CLOSE_SQUARE_BRACE)
                    {
                        node = new NodeOperatorIndex(node, Current.image);
                        Advance();
                    }
                    else if (Check(CLOSE_SQUARE_BRACE))
                    {
                        Advance();
                        log.Error(Location, "At least one argument is needed to index an object.");
                        return node;
                    }
                    else node = new NodeIndex(node, CommaExpressions());
                    Expect(CLOSE_SQUARE_BRACE, "Expected a close brace (']') to end the operator index.");
                    return Postfix(node, allowPostfixInvocation);
                case DOT:
                    Advance();
                    // TODO do other checks, such as infix/prefix/as
                    // for now, just check identifier indexing.
                    string ident;
                    ExpectIdentifier(out ident, "Identifier expected for field indexing.");
                    return Postfix(new NodeFieldIndex(node, ident), allowPostfixInvocation);
                case AS:
                    Advance();
                    if (!TokensRemain)
                    {
                        log.Error(tokens[-1].location, "Expected expression for 'as' cast, but the end of the file was reached.");
                        break;
                    }
                    var type = PrimaryExpression();
                    if (type == null)
                    {
                        log.Error(tokens[-1].location, "Expected expression for 'as' cast.");
                        break;
                    }
                    var a = new NodeAs(node, type);
                    return a;
            }
            return node;
        }

        private Node Factor(Node left, uint minp = 0)
        {
            // Here we have two options:
            // - There's an operator, and it's precedence is >= the current precedence.
            // - There's an identifier, and the current precedence >= the default precedence.
            // In either of these cases, the token must be on the same line as the expression.
            while (TokensRemain &&
                  ((Check(OPERATOR) && Laye.GetOperatorPrecedence(Current.image) >= minp) ||
                  ( Check(IDENTIFIER) && Laye.defaultOperatorPrecedence >= minp)) &&
                    Current.location.line == left.EndLine)
            {
                // We know it's either an operator OR a method name, determine which:
                var isOp = Check(OPERATOR);
                // Save the token and location, as well.
                var token = Current;
                // Skip past this token, time to finish this infix expression.
                Advance();
                // Get an expression without checking infix.
                Node right = PrimaryExpression();
                // End of file?
                if (right == null)
                {
                    // TODO error
                }
                // Make sure we don't have a semi colon.
                else
                {
                    // get our precedence
                    var thisPrec = isOp ? Laye.GetOperatorPrecedence(token.image) :
                        Laye.defaultOperatorPrecedence;
                    // Basically we do the same as above
                    // This time the precedence must be GREATER, but not equal.
                    while (TokensRemain && 
                          ((Check(OPERATOR) && Laye.GetOperatorPrecedence(Current.image) > thisPrec) ||
                          ( Check(IDENTIFIER) && Laye.defaultOperatorPrecedence > thisPrec)) &&
                            Current.location.line == right.EndLine)
                    {
                        // Our right side is now the result of the infix operation.
                        right = Factor(right, Current.type == OPERATOR ?
                            Laye.GetOperatorPrecedence(Current.image) : Laye.defaultOperatorPrecedence);
                    }
                }
                // Even if we hit an error, let's return some valid data!
                if (isOp)
                    left = new NodeInfix(left, right, token.image);
                else left = new NodeInvoke(new NodeFieldIndex(left, token.image), new List<Node>() { right });
            }
            return left;
        }

        private Node ParseFn(bool isGenerator = false)
        {
            // Use the location of 'fn' as the location of this function.
            // The end of the function can be determined by the end of
            // it's body, so it's only expected to rely on the
            // line/col fields for functions.
            var loc = Location;
            // The first token we'll have is 'fn', so skip it.
            Advance();

            // We'll use this later, after checking for a name
            NodeFnExpr fn;

            // Next, check if there's an open brace.
            // If an open brace follows 'fn', it must be a lambda,
            // otherwise it's a definition, we need a location.
            if (Check(ASSIGN) || Check(OPEN_CURLY_BRACE))
            {
                fn = new NodeFnExpr(loc);
                fn.fullAnon = true;
            }
            else if (!Check(OPEN_BRACE))
            {
                fn = new NodeFnDef(loc);
                (fn as NodeFnDef).target = PrimaryExpression(false);
            }
            else fn = new NodeFnExpr(loc);
            fn.isGenerator = isGenerator;

            if (!fn.fullAnon)
            {
                // Hope that this is the open brace we seek.
                // TODO If not, maybe I should try to recover better.
                Expect(OPEN_BRACE,
                    "Expected an open brace ('(') to start the function parameter list.");

                // Now, we need to get the argument list.
                while (true)
                {
                    if (!TokensRemain)
                    {
                        log.Error(tokens[-1].location, "End of file reached, expected a parameter declaration.");
                        break;
                    }

                    // Break out early if we see a close brace.
                    // Even if it's not at the right time, it's
                    // a safe place to break.
                    if (Check(CLOSE_BRACE))
                        break;
                    // Next check for a parameter.
                    string paramName;
                    // If there's an identifier, it's a param
                    if (ExpectIdentifier(out paramName, "Expected identifier to name function parameter."))
                        fn.paramNames.Add(paramName);
                    // Otherwise, let's try to get the next param immediately
                    // rather than skipping or fumbling with more tokens.
                    else continue;
                    if (Check(ASSIGN))
                    {
                        Advance();
                        if (!TokensRemain)
                        {
                            log.Error(tokens[-1].location, "Expected expression for default parameter value, but the end of the file was reached.");
                            return fn;
                        }
                        fn.defaultParams.Add(Expression());
                    }
                    else if (fn.defaultParams.Count > 0)
                        fn.defaultParams.Add(new NodeNull(null));
                    // TODO check for vargs.
                    // If there's a comma, we expect there to be more parameters.
                    // Simply skip the comma and prepare for more.
                    if (Check(COMMA))
                        Advance();
                    // No more parameters, break out.
                    else break;
                }

                // Param lists end with ')', so look for it.
                Expect(CLOSE_BRACE,
                    "Expected a close brace (')') to end the function parameter list.");
            }

            // If the function is declared with an assign operator, it shouldn't be void.
            if (TokensRemain && Current.type == ASSIGN)
            {
                if (fn.isGenerator)
                    log.Error(Location, "Generators must be declared void.");
                Advance();
                fn.isVoid = false;
            }
            else fn.isVoid = true;

            // Finally, we get the body of the function.
            // Whether or not a function is void, its body is always an expression.

            if (!TokensRemain)
            {
                log.Error(fn.location, "Expected expression for function body, but the end of the file was reached.");
                return fn;
            }

            SetCurrentFunction(fn);
            fn.body = Expression();
            ReleaseCurrentFunction();

            return fn;
        }

        private Node ParseVar()
        {
            // The first token we'll have is 'var', so skip it.
            var var = new NodeVar(Location);
            Advance();

            // Now, let's get us some variables!
            while (true)
            {
                if (!TokensRemain)
                {
                    log.Error(tokens[-1].location, "End of file reached, expected a variable declaration.");
                    break;
                }

                // Variables are all identifiers, so do that.
                string name;
                ExpectIdentifier(out name, "");

                Node value = null;
                // Vars don't have to be assigned to initially, they default to null.
                // Initialization?
                if (Check(ASSIGN))
                {
                    Advance();
                    value = Expression();
                }

                // Add it to our var list
                var.vars.Add(new KeyValuePair<string, Node>(name, value));

                // If there's a comma, we expect another definition:
                if (Check(COMMA))
                    Advance();
                else break;
            }

            return var;
        }

        private Node ParseIf()
        {
            var expr = new NodeIf(Location);
            Advance();

            // if not (condition) is allowed and handled specially.
            if (Check(NOT))
            {
                expr.not = true;
                Advance();
            }

            // Conditions must be in braces ()
            Expect(OPEN_BRACE, "An open brace ('(') is expected to start the condition of an 'if' expression.");
            if (!TokensRemain)
            {
                log.Error(expr.location, "Expected expression for 'if' condition, but the end of the file was reached.");
                return expr;
            }
            expr.condition = Expression();
            Expect(CLOSE_BRACE, "A close brace (')') is expected to end the condition of an 'if' expression.");

            // Get the body:
            if (!TokensRemain)
            {
                log.Error(expr.location, "Expected expression for 'if' body, but the end of the file was reached.");
                return expr;
            }
            expr.pass = Expression();

            // Check for an 'el' (fail) condition
            if (Check(EL))
            {
                Advance();
                if (!TokensRemain)
                {
                    log.Error(expr.location, "Expected expression for 'el' body, but the end of the file was reached.");
                    return expr;
                }
                expr.fail = Expression();
            }

            return expr;
        }

        private Node ParseWhen(Node pass)
        {
            var when = new NodeWhen(Location);
            when.pass = pass;

            Advance(); // 'when'

            if (!TokensRemain)
            {
                log.Error(when.location, "Expected expression for 'when' condition, but the end of the file was reached.");
                return when;
            }
            when.condition = Expression();
            Expect(EL, "Expected 'el' to complete when expression.");
            if (!TokensRemain)
            {
                log.Error(when.location, "Expected expression for 'when' fail expression, but the end of the file was reached.");
                return when;
            }
            when.fail = Expression();

            return when;
        }

        private Node ParseWhile()
        {
            var expr = new NodeWhile(Location);
            Advance();

            // while not (condition) is allowed and handled specially.
            if (Check(NOT))
            {
                expr.not = true;
                Advance();
            }

            if (Check(COLON))
            {
                Advance();
                ExpectIdentifier(out expr.label, "Expected an identifier for loop label.");
            }

            // Conditions must be in braces ()
            Expect(OPEN_BRACE, "An open brace ('(') is expected to start the condition of a 'while' expression.");
            if (!TokensRemain)
            {
                log.Error(expr.location, "Expected expression for 'while' condition, but the end of the file was reached.");
                return expr;
            }
            expr.condition = Expression();
            Expect(CLOSE_BRACE, "A close brace (')') is expected to end the condition of a 'while' expression.");

            // Get the body:
            if (!TokensRemain)
            {
                log.Error(expr.location, "Expected expression for 'while' block, but the end of the file was reached.");
                return expr;
            }
            expr.pass = Expression();

            // Check for an 'el' (fail) condition
            if (Check(EL))
            {
                Advance();
                expr.fail = Expression();
            }

            return expr;
        }

        private Node ParseTry()
        {
            var expr = new NodeTry(Location);
            Advance();

            // what we're trying
            if (!TokensRemain)
            {
                log.Error(expr.location, "Expected an expression for 'try' block, but the end of the file was reached.");
                return expr;
            }
            expr.body = Expression();

            Expect(CATCH, "Expected 'catch' block after try body.");
            if (Check(OPEN_BRACE))
            {
                Advance();
                string name;
                ExpectIdentifier(out name, "Identifier expected as exception variable name.");
                expr.exceptionName = name;
                Expect(CLOSE_BRACE, "A close brace (')') is expected to surround exception variable name.");
            }
            // what we're using to handle issues
            if (!TokensRemain)
            {
                log.Error(expr.location, "Expected an expression for 'catch' block, but the end of the file was reached.");
                return expr;
            }
            expr.handler = Expression();

            return expr;
        }

        private Node ParseIter()
        {
            var iter = new NodeIter(Location);
            Advance();

            if (Check(COLON))
            {
                Advance();
                ExpectIdentifier(out iter.label, "Expected an identifier for loop label.");
            }

            Expect(OPEN_BRACE, "Expected an open brace ('(') to start iter initialization.");
            ExpectIdentifier(out iter.iterVarName, "Expected an identifier for iter index variable name.");
            Expect(ASSIGN, "Expected initial assignmed to iter index variable.");
            if (TokensRemain)
                iter.init = Expression();
            else
            {
                log.Error(tokens[-1].location, "Expected expression to assign to initial iter index, but the end of the file was reached.");
                return iter;
            }
            Expect(TO, "Expected 'to' after iter index variable initialization to mark iter limit.");
            if (TokensRemain)
                iter.limit = Expression();
            else
            {
                log.Error(tokens[-1].location, "Expected expression for iter limit, but the end of the file was reached.");
                return iter;
            }
            if (Check(BY))
            {
                Advance();
                if (TokensRemain)
                    iter.step = Expression();
                else
                {
                    log.Error(tokens[-1].location, "Expected expression for iter step, but the end of the file was reached.");
                    return iter;
                }
            }
            Expect(CLOSE_BRACE, "Expected a close brace (')') to end iter initialization.");

            if (TokensRemain)
                iter.body = Expression();
            else log.Error(tokens[-1].location, "Expected expression for iter body, but the end of the file was reached.");

            return iter;
        }

        private Node ParseEach()
        {
            var each = new NodeEach(Location);
            Advance();

            if (Check(COLON))
            {
                Advance();
                ExpectIdentifier(out each.label, "Expected an identifier for loop label.");
            }

            Expect(OPEN_BRACE, "Expected an open brace ('(') to start each initialization.");
            ExpectIdentifier(out each.eachVarName, "Expected an identifier for each value variable name.");
            Expect(IN, "Expected 'in' after each variable declaration to define the enumerator target.");
            if (TokensRemain)
                each.value = Expression();
            else
            {
                log.Error(tokens[-1].location, "Expected expression for each enumerator target, but the end of the file was reached.");
                return each;
            }
            Expect(CLOSE_BRACE, "Expected a close brace (')') to end each initialization.");

            if (TokensRemain)
                each.body = Expression();
            else log.Error(tokens[-1].location, "Expected expression for each body, but the end of the file was reached.");

            return each;
        }

        private Node ParseIEach()
        {
            var ieach = new NodeIEach(Location);
            Advance();

            if (Check(COLON))
            {
                Advance();
                ExpectIdentifier(out ieach.label, "Expected an identifier for loop label.");
            }

            Expect(OPEN_BRACE, "Expected an open brace ('(') to start each initialization.");
            ExpectIdentifier(out ieach.eachIndexName, "Expected an identifier for each index variable name.");
            Expect(COMMA, "Comma expected to separate each variable names.");
            ExpectIdentifier(out ieach.eachVarName, "Expected an identifier for each value variable name.");
            Expect(IN, "Expected 'in' after each variable declaration to define the enumerator target.");
            if (TokensRemain)
                ieach.value = Expression();
            else
            {
                log.Error(tokens[-1].location, "Expected expression for each enumerator target, but the end of the file was reached.");
                return ieach;
            }
            Expect(CLOSE_BRACE, "Expected a close brace (')') to end each initialization.");

            if (TokensRemain)
                ieach.body = Expression();
            else log.Error(tokens[-1].location, "Expected expression for each body, but the end of the file was reached.");

            return ieach;
        }
    }
}
