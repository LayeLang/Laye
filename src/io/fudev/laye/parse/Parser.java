/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Sekai Kyoretsuna
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.fudev.laye.parse;

import static io.fudev.laye.log.LogMessageID.ERROR_UNEXPECTED_TOKEN;
import static io.fudev.laye.log.LogMessageID.ERROR_UNFINISHED_INFIX;
import static io.fudev.laye.log.LogMessageID.ERROR_UNFINISHED_SCOPE;

import io.fudev.laye.ast.*;
import io.fudev.laye.lexical.Location;
import io.fudev.laye.lexical.Token;
import io.fudev.laye.lexical.TokenStream;
import io.fudev.laye.log.DetailLogger;
import io.fudev.laye.struct.Identifier;
import io.fudev.laye.struct.Keyword;
import io.fudev.laye.struct.Operator;
import io.fudev.laye.vm.LayeFloat;
import io.fudev.laye.vm.LayeInt;
import io.fudev.laye.vm.LayeString;
import net.fudev.faxlib.collections.List;

/**
 * @author Sekai Kyoretsuna
 */
public
class Parser
{
   private final DetailLogger logger;
   
   private TokenStream tokens = null;
   private Token token = null;
   
   public Parser(DetailLogger logger)
   {
      this.logger = logger;
   }
   
   public AST getAST(TokenStream tokens)
   {
      if (tokens == null)
      {
         throw new NullPointerException("tokens");
      }
      this.tokens = tokens;
      
      AST result = new AST();
      
      token = tokens.current();
      while (token != null)
      {
         ASTNode node = parseTopLevel();
         if (node != null)
         {
            result.children.append(node);
         }
      }
      
      return(result);
   }
   
   // ===== Manage the TokenStream and current token
   
   private Location getLocation()
   {
      return(token.location);
   }
   
   private void next()
   {
      assert(tokens != null) : "no token stream to read from.";
      tokens.next();
      token = tokens.current();
   }
   
   private Token peek(int offset)
   {
      assert(tokens != null) : "no token stream to peek into.";
      return(tokens.peek(offset));
   }
   
   private boolean check(Token.Type type)
   {
      if (token == null)
      {
         return(false);
      }
      return(token.type == type);
   }
   
   private boolean checkKeyword(Keyword keyword)
   {
      return(check(Token.Type.KEYWORD) && token.data.equals(keyword));
   }
   
   private boolean peekCheck(int offset, Token.Type type)
   {
      Token peeked = peek(offset);
      if (peeked == null)
      {
         return(false);
      }
      return(peeked.type == type);
   }
   
   private boolean expect(Token.Type type)
   {
      if (!check(type))
      {
         next();
         logger.logErrorf(token.location, ERROR_UNEXPECTED_TOKEN,
               "Unexpected token '%s', expected '%s'.\n", token.type.toString(), type.toString());
         return(false);
      }
      next();
      return(true);
   }
   
   private Identifier expectIdentifier()
   {
      Token last = token;
      if (!expect(Token.Type.IDENTIFIER))
      {
         return(null);
      }
      return((Identifier)last.data);
   }
   
   private boolean expectKeyword(Keyword keyword)
   {
      Token last = token;
      if (!expect(Token.Type.KEYWORD))
      {
         return(false);
      }
      return(last.data == keyword);
   }
   
   // ===== Actual parser stuff
   
   private ASTNode parseTopLevel()
   {
      switch (token.type)
      {
         // TODO(sekai): Statements
         case KEYWORD:
         {
            switch (((Keyword)token.data).image)
            {
               case Keyword.STR_FN:
               {
                  return(parseFunctionDefinition());
               }
               default:
               {
               } break;
            }
         } break;
         default:
         {
         } break;
      }
      NodeExpression result = factor();
      if (result == null)
      {
         // Attempt to fix the situation, should already have errored.
         next();
      }
      return(result);
   }
   
   private List<NodeExpression> commaFactor()
   {
      List<NodeExpression> exprs = new List<>();
      NodeExpression expr;
      while ((expr = factor()) != null)
      {
         exprs.append(expr);
         if (!check(Token.Type.COMMA))
         {
            break;
         }
         // nom ','
         next();
      }
      return exprs;
   }
   
   /**
    * DON'T call this directly, use {@link #factor()}
    * @return
    */
   private NodeExpression parsePrimaryExpression()
   {
      Location location = getLocation();
      switch (token.type)
      {
         case OPEN_BRACE:
         {
            // nom '('
            next();
            List<NodeExpression> values = commaFactor();
            expect(Token.Type.CLOSE_BRACE);
            if (values.size() == 1)
            {
               return(postfix(values.get(0)));
            }
            else
            {
               return(postfix(new NodeTuple(location, values)));
            }
         }
         case OPEN_SQUARE_BRACE:
         {
            // nom '['
            next();
            List<NodeExpression> values = commaFactor();
            expect(Token.Type.CLOSE_SQUARE_BRACE);
            return postfix(new NodeList(location, values));
         }
         case OPERATOR:
         {
            Operator op = (Operator)token.data;
            // nom operator
            next();
            return(new NodePrefixExpression(location, parsePrimaryExpression(), op));
         }
         case KEYWORD:
         {
            switch (((Keyword)token.data).image)
            {
               case Keyword.STR_VAR:
               {
                  return(parseVariableDefinition());
               }
               case Keyword.STR_FN:
               {
                  return(parseLambdaFunction());
               }
               case Keyword.STR_NULL:
               {
                  // nom 'null'
                  next();
                  return(postfix(new NodeNullLiteral(location)));
               }
               case Keyword.STR_TRUE:
               {
                  // nom 'null'
                  next();
                  return(postfix(new NodeBoolLiteral(location, true)));
               }
               case Keyword.STR_FALSE:
               {
                  // nom 'null'
                  next();
                  return(postfix(new NodeBoolLiteral(location, false)));
               }
               case Keyword.STR_NOT:
               {
                  // nom 'not'
                  next();
                  return(new NodeNot(location, parsePrimaryExpression()));
               }
               case Keyword.STR_IF:
               {
                  // nom 'if'
                  next();
                  NodeIf result = new NodeIf(location);
                  result.condition = factor();
                  result.pass = factor();
                  if (checkKeyword(Keyword.EL))
                  {
                     // nom 'el'
                     next();
                     result.fail = factor();
                  }
                  return(result);
               }
               default:
               {
               } break;
            }
         } break;
         case INT_LITERAL:
         {
            LayeInt value = (LayeInt)token.data;
            // nom int
            next();
            return(new NodeIntLiteral(location, value));
         }
         case FLOAT_LITERAL:
         {
            LayeFloat value = (LayeFloat)token.data;
            // nom float
            next();
            return(new NodeFloatLiteral(location, value));
         }
         case STRING_LITERAL:
         {
            LayeString value = (LayeString)token.data;
            // nom string
            next();
            return(postfix(new NodeStringLiteral(location, value)));
         }
         case OPEN_CURLY_BRACE:
         {
            NodeScope scope = new NodeScope(getLocation());
            // nom '{'
            next();
            while (!check(Token.Type.CLOSE_CURLY_BRACE))
            {
               if (tokens.isOver())
               {
                  logger.logError(location, ERROR_UNFINISHED_SCOPE, "Unfinished scope.");
                  break;
               }
               scope.body.append(parseTopLevel());
            }
            // nom '}'
            expect(Token.Type.CLOSE_CURLY_BRACE);
            return(postfix(scope));
         }
         case IDENTIFIER:
         {
            Identifier ident = (Identifier)token.data;
            // nom ident
            next();
            return(postfix(new NodeIdentifier(location, ident)));
         }
         default:
         {
         } break;
      }
      // TODO(sekai): is this needed? I think callers should error check!
//      logger.logErrorf(location, ERROR_UNEXPECTED_TOKEN,
//            "Failed to parse expression on token '%s'\n", token.toString());
      return(null);
   }
   
   private NodeExpression postfix(NodeExpression node)
   {
      return(postfix(node, true));
   }
   
   private NodeExpression postfix(NodeExpression node, boolean allowCall)
   {
      if (token == null)
      {
         return node;
      }
      switch (token.type)
      {
         case OPEN_BRACE:
         {
            // '(' must be on the same line
            if (node.location.line != token.location.line)
            {
               break;
            }
            // nom '('
            next();
            List<NodeExpression> args = commaFactor();
            expect(Token.Type.CLOSE_BRACE);
            node = postfix(new NodeInvoke(node.location, node, args));
         } break;
         case OPEN_SQUARE_BRACE:
         {
            // nom '['
            next();
            NodeExpression index = factor();
            expect(Token.Type.CLOSE_SQUARE_BRACE);
            node = postfix(new NodeLoadIndex(node.location, node, index));
         } break;
         case DOT:
         {
            // nom '.'
            next();
            Location location = getLocation();
            Identifier ident = expectIdentifier();
            node = postfix(new NodeLoadIndex(node.location, node,
                  new NodeStringLiteral(location, new LayeString(ident.image))));
         } break;
         default:
         {
         } break;
      }
      return(node);
   }
   
   private NodeExpression factor()
   {
       NodeExpression expr;
       if ((expr = parsePrimaryExpression()) == null)
       {
          return(null);
       }
       expr = factorRHS(expr, 0);
       if (token != null)
       {
          switch (token.type)
          {
             case ASSIGN:
             {
                // nom '='
                next();
                NodeExpression value = factor();
                // NOTE: Many left-expressions will fail, the code generator will check for us
                expr = new NodeAssignment(expr.location, expr, value);
             } break;
             case KEYWORD:
             {
                switch (((Keyword)token.data).image)
                {
                   case Keyword.STR_AND:
                   {
                      // nom 'and'
                      next();
                      NodeExpression value = factor();
                      expr = new NodeAnd(expr.location, expr, value);
                   } break;
                   case Keyword.STR_OR:
                   {
                      // nom 'or'
                      next();
                      NodeExpression value = factor();
                      expr = new NodeOr(expr.location, expr, value);
                   } break;
                   default:
                   {
                   } break;
                }
             }
             default:
             {
             } break;
          }
       }
       return(expr);
   }
   
   // TODO add precedence
   private NodeExpression factorRHS(NodeExpression left, int minp)
   {
      while (check(Token.Type.OPERATOR) && ((Operator)token.data).precedence >= minp)
      {
         final Location location = token.location;
         final Operator op = (Operator)token.data;
         // nom Operator
         next();
         // first, is this an assignment operator?
         NodeExpression right;
         if (check(Token.Type.ASSIGN))
         {
            // nom '='
            next();
            right = factor();
            return(new NodeAssignment(left.location, left,
                  new NodeInfixExpression(left.location, left, right, op)));
         }
         else
         {
            // Load up the right hand side, if one exists
            if ((right = parsePrimaryExpression()) != null)
            {
               while (check(Token.Type.OPERATOR) && ((Operator)token.data).precedence > op.precedence)
               {
                  right = factorRHS(right, ((Operator)token.data).precedence);
               }
               left = new NodeInfixExpression(left.location, left, right, op);
            }
            else
            {
               logger.logError(location, ERROR_UNFINISHED_INFIX,
                     "expected expression to complete infix expression");
               return(null);
            }
         }
      }
      return(left);
   }
   
   private FunctionData getFunctionData()
   {
      FunctionData data = new FunctionData();

      // nom '('
      expect(Token.Type.OPEN_BRACE);
      
      while (!check(Token.Type.CLOSE_BRACE))
      {
         // TODO(sekai): smarter error handling, check for args after a varg.
         Identifier param = expectIdentifier();
         if (check(Token.Type.VARGS))
         {
            // nom '..'
            next();
            data.vargs = true;
         }
         
         // TODO(sekai): check defaults
         
         data.params.append(param);
         data.defaults.append(null);
         
         if (!data.vargs && check(Token.Type.COMMA))
         {
            // nom ','
            next();
         }
         else
         {
            break;
         }
      }
      
      // nom ')'
      expect(Token.Type.CLOSE_BRACE);
      
      data.body = factor();
      
      return(data);
   }
   
   private NodeFunctionDef parseFunctionDefinition()
   {
      NodeFunctionDef def = new NodeFunctionDef(getLocation());
      
      // nom 'fn'
      next();
      
      def.name = expectIdentifier();
      def.data = getFunctionData();
      
      return(def);
   }
   
   private NodeFunction parseLambdaFunction()
   {
      NodeFunction def = new NodeFunction(getLocation());
      
      // nom 'fn'
      next();
      
      def.data = getFunctionData();
      
      return(def);
   }
   
   private NodeVariableDef parseVariableDefinition()
   {
      NodeVariableDef def = new NodeVariableDef(getLocation());
      
      // nom 'var'
      next();
      
      do
      {
         Identifier varName = expectIdentifier();
         NodeExpression varValue;
         
         if (check(Token.Type.ASSIGN))
         {
            // nom '='
            next();
            
            varValue = factor();
            if (varValue == null)
            {
               // Should already have errored, just try to fix the issue.
               next();
            }
         }
         else
         {
            varValue = new NodeNullLiteral(null);
         }
         
         // TODO(sekai): Do something with null values? expect() calls already error.
         def.addDefinition(varName, varValue);
         
         if (check(Token.Type.COMMA))
         {
            // nom ','
            next();
         }
         else
         {
            break;
         }
      }
      while (true);
      
      return(def);
   }
}
