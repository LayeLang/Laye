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

import static io.fudev.laye.log.LogMessageID.*;

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
               case Keyword.STR_VAR:
               {
                  return(parseVariableDefinition());
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
   
   /**
    * DON'T call this directly, use {@link #factor()}
    * @return
    */
   private NodeExpression parsePrimaryExpression()
   {
      Location location = getLocation();
      switch (token.type)
      {
         case OPERATOR:
         {
            Operator op = (Operator)token.data;
            // nom operator
            next();
            NodeExpression expression = parsePrimaryExpression();
            return(new NodePrefixExpression(location, expression, op));
         }
         case KEYWORD:
         {
            switch (((Keyword)token.data).image)
            {
               case Keyword.STR_NULL:
               {
                  // nom 'null'
                  next();
                  return(new NodeNullLiteral(location));
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
            return(new NodeStringLiteral(location, value));
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
            return(scope);
         }
         case IDENTIFIER:
         {
            Identifier ident = (Identifier)token.data;
            // nom ident
            next();
            return(new NodeIdentifier(location, ident));
         }
         default:
         {
         } break;
      }
      logger.logErrorf(location, ERROR_UNEXPECTED_TOKEN,
            "Failed to parse expression on token '%s'\n", token.toString());
      return(null);
   }
   
   private NodeExpression factor()
   {
       final NodeExpression left;
       if ((left = parsePrimaryExpression()) == null)
       {
          return(null);
       }
       return(factorRHS(left, 0));
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
   
   private NodeFunctionDef parseFunctionDefinition()
   {
      NodeFunctionDef def = new NodeFunctionDef(getLocation());
      
      // nom 'fn'
      next();
      
      def.name = expectIdentifier();

      // nom '('
      expect(Token.Type.OPEN_BRACE);
      
      while (!check(Token.Type.CLOSE_BRACE))
      {
         Identifier param = expectIdentifier();
         // TODO(sekai): check defaults and vargs
         
         def.data.params.append(param);
         def.data.defaults.append(null);
         
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
      
      // nom ')'
      expect(Token.Type.CLOSE_BRACE);
      
      def.data.body = factor();
      
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
