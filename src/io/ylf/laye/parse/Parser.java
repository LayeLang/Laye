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
package io.ylf.laye.parse;

import static io.ylf.laye.LogMessageID.ERROR_UNEXPECTED_TOKEN;

import io.ylf.laye.ast.AST;
import io.ylf.laye.ast.ASTNode;
import io.ylf.laye.lexical.Token;
import io.ylf.laye.lexical.TokenStream;
import io.ylf.laye.log.DetailLogger;
import io.ylf.laye.struct.Identifier;
import io.ylf.laye.struct.Keyword;

/**
 * @author Sekai Kyoretsuna
 */
public class Parser
{
   private final DetailLogger logger;
   
   private TokenStream tokens = null;
   private Token token = null;
   
   public Parser(DetailLogger logger)
   {
      this.logger = logger;
   }
   
   public AST getSyntaxTree(TokenStream tokens)
   {
      assert(tokens != null);
      this.tokens = tokens;
      
      AST result = new AST();
      
      next();
      while (token != null)
      {
         
      }
      
      return result;
   }
   
   // ===== Manage the TokenStream and current token
   
   private void next()
   {
      assert(tokens != null);
      tokens.next();
      token = tokens.current();
   }
   
   private Token peek(int offset)
   {
      assert(tokens != null);
      return tokens.peek(offset);
   }
   
   private boolean check(Token.Type type)
   {
      assert(token != null);
      assert(type != null);
      return token.type == type;
   }
   
   private boolean peekCheck(int offset, Token.Type type)
   {
      assert(type != null);
      Token peeked = peek(offset);
      if (peeked == null)
      {
         return false;
      }
      return peeked.type == type;
   }
   
   private boolean expect(Token.Type type)
   {
      if (!check(type))
      {
         logger.logErrorf(token.location, ERROR_UNEXPECTED_TOKEN,
               "Unexpected token '%s', expected '%s'.", token.type.toString(), type.toString());
         return false;
      }
      return true;
   }
   
   private Identifier expectIdentifier()
   {
      Token last = token;
      if (!expect(Token.Type.IDENTIFIER))
      {
         return null;
      }
      return (Identifier)last.data;
   }
   
   private boolean expectKeyword(Keyword keyword)
   {
      Token last = token;
      if (!expect(Token.Type.KEYWORD))
      {
         return false;
      }
      return last.data == keyword;
   }
   
   // ===== Actual parser stuff
   
   private ASTNode parseTopLevel()
   {
      switch (token.type)
      {
         // TODO(sekai): Statements
         default:
         {
            // TODO(sekai): Expression parsing
            return null;
         }
      }
   }
}
