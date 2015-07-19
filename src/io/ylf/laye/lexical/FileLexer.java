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
package io.ylf.laye.lexical;

import java.io.IOException;
import java.io.InputStream;

import static io.ylf.laye.LogMessageID.*;

import io.ylf.laye.file.ScriptFile;
import io.ylf.laye.log.DetailLogger;
import io.ylf.laye.struct.Identifier;
import io.ylf.laye.struct.Keyword;
import io.ylf.laye.struct.Operator;
import io.ylf.laye.vm.LayeFloat;
import io.ylf.laye.vm.LayeInt;
import io.ylf.laye.vm.LayeString;

/**
 * @author Sekai Kyoretsuna
 */
public class FileLexer
{
   /**
    * @author Sekai Kyoretsuna
    */
   private static interface CharacterMatcher
   {
      boolean apply(char c);
   }
   
   private static boolean isHexadecimalCharacter(char c)
   {
      return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
   }
   
   private static boolean isBinaryCharacter(char c)
   {
      return c == '0' || c == '1';
   }
   
   private static boolean isOctalCharacter(char c)
   {
      return c >= '0' && c <= '7';
   }

   private final DetailLogger logger;
   
   private InputStream input = null;
   private ScriptFile file = null;
   
   private StringBuilder builder = new StringBuilder();
   private char currentChar = '\u0000';
   
   private int line = 1, column = 0;
   private boolean eof;
   
   public FileLexer(DetailLogger logger)
   {
      this.logger = logger;
   }
   
   public TokenStream getTokens(ScriptFile file) throws IOException
   {
      this.file = file;
      this.input = file.read();
      
      TokenStream result = new TokenStream(logger);
      
      readChar();
      
      Token token;
      while ((token = lex()) != null)
      {
         result.append(token);
      }
      
      input.close();
      
      return result;
   }
   
   private Location getLocation()
   {
      return new Location(file, line, column);
   }
   
   private String getTempString()
   {
      String result = builder.toString();
      builder.setLength(0);
      builder.trimToSize();
      return result;
   }
   
   private boolean putChar()
   {
      builder.append(currentChar);
      return readChar();
   }
   
   private void putChar(char c)
   {
      builder.append(c);
   }
   
   private boolean readChar()
   {
      int next;
      try
      {
         next = input.read();
      }
      catch (IOException e)
      {
         eof = true;
         currentChar = '\u0000';
         return false;
      }
      if (next == -1)
      {
         eof = true;
         currentChar = '\u0000';
         return false;
      }
      currentChar = (char) next;
      if (currentChar == '\n')
      {
         line++;
         column = 0;
      }
      else
      {
         // FIXME(sekai): check other characters that don't have a width
         switch (currentChar)
         {
            case '\r':
               break;
            default:
               column++;
         }
      }
      return true;
   }
   
   private Token lex()
   {
      while (!eof)
      {
         if (Character.isWhitespace(currentChar))
         {
            readChar();
            continue;
         }
         final Location location = getLocation();
         switch (currentChar)
         {
            case '#':
               lexOutLineComment();
               return lex();
            case '(':
               readChar();
               return new Token(Token.Type.OPEN_BRACE, location);
            case ')':
               readChar();
               return new Token(Token.Type.CLOSE_BRACE, location);
            case '[':
               readChar();
               return new Token(Token.Type.OPEN_SQUARE_BRACE, location);
            case ']':
               readChar();
               return new Token(Token.Type.CLOSE_SQUARE_BRACE, location);
            case '{':
               readChar();
               return new Token(Token.Type.OPEN_CURLY_BRACE, location);
            case '}':
               readChar();
               return new Token(Token.Type.CLOSE_CURLY_BRACE, location);
            case ';':
               readChar();
               return new Token(Token.Type.SEMI_COLON, location);
            case ':':
               readChar();
               return new Token(Token.Type.COLON, location);
            case ',':
               readChar();
               return new Token(Token.Type.COMMA, location);
            case '.':
               readChar();
               return new Token(Token.Type.DOT, location);
            case '\'':
            case '"':
               return lexStringLiteral();
            default:
               if (Operator.isOperatorChar(currentChar))
               {
                  return lexOperatorToken();
               }
               if (Character.isDigit(currentChar))
               {
                  return lexNumericToken();
               }
               return lexOtherTokens();
         }
      }
      
      return null;
   }
   
   private void lexOutLineComment()
   {
      // Nom all the chars until the end of a line.
      do
      {
         readChar();
      }
      while (currentChar != '\n');
   }
   
   private Token lexStringLiteral()
   {
      final Location location = getLocation();
      final char quoteChar = currentChar;
      // Read quote
      readChar();
      while (currentChar != quoteChar && !eof)
      {
         if (currentChar == '\\')
         {
            putChar(lexEscapedCharacter());
         }
         else
         {
            putChar();
         }
      }
      if (currentChar != quoteChar)
      {
         logger.logError(location, ERROR_UNFINISHED_STRING, "Unfinished string.");
      }
      else
      {
         // Read quote
         readChar();
      }
      String result = getTempString();
      return new Token(Token.Type.STRING_LITERAL, new LayeString(result), location);
   }
   
   private char lexEscapedCharacter()
   {
      final Location location = getLocation();
      // Read '\'
      readChar();
      switch (currentChar)
      {
         case 'u':
         {
            readChar();
            final StringBuilder sb = new StringBuilder();
            int idx = 0;
            for (; !eof && idx < 4 && (Character.isDigit(currentChar) ||
                           (currentChar >= 'a' && currentChar <= 'f') ||
                           (currentChar >= 'A' && currentChar <= 'F')); idx++)
            {
               sb.append(currentChar);
               readChar();
            }
            if (idx != 4)
            {
               logger.logErrorf(location, ERROR_ESCAPED_UNICODE_SIZE,
                                "4 hexadecimal digits are expected when "
                                + "defining a unicode char, %d given.\n",
                                idx);
               return '\u0000';
            }
            return (char) Integer.parseInt(sb.toString(), 16);
         }
         case 'r':
            readChar();
            return '\r';
         case 'n':
            readChar();
            return '\n';
         case 't':
            readChar();
            return '\t';
         case '0':
            readChar();
            return '\0';
         case '"':
            readChar();
            return '\"';
         case '\'':
            readChar();
            return '\'';
         case '\\':
            readChar();
            return '\\';
         default:
            logger.logErrorf(location, ERROR_ILLEGAL_ESCAPE,
                             "escape character '%c' not recognized.\n", currentChar);
            return '\u0000';
      }
   }
   
   private Token lexOperatorToken()
   {
      final Location location = getLocation();
      do
      {
         putChar();
      }
      while (Operator.isOperatorChar(currentChar));
      String image = getTempString();
      if (image.equals("="))
      {
         return new Token(Token.Type.ASSIGN, location);
      }
      return new Token(Token.Type.OPERATOR, Operator.get(image), location);
   }
   
   private char lexIntegerDigits(CharacterMatcher matcher)
   {
      char lastChar = currentChar;
      while ((matcher.apply(currentChar) || currentChar == '_') && !eof)
      {
         lastChar = currentChar;
         if (currentChar != '_')
         {
            putChar();
         }
         else
         {
            readChar();
         }
      }
      return lastChar;
   }
   
   private Token lexNumericToken()
   {
      final Location location = getLocation();
      // TODO(sekai): this only handles ints. Needs fp and sci-note.

      char lastChar = currentChar;
      
      boolean isInteger = true;
      int iRadix = 10;
      
      // FIXME(sekai): Needs more '_' error checking.
      
      if (currentChar == '0')
      {
         // read '0'
         readChar();
         switch (currentChar)
         {
            case 'x': case 'X': // hexadecimal
            {
               iRadix = 16;
               readChar();
               lastChar = lexIntegerDigits(FileLexer::isHexadecimalCharacter);
            } break;
            case 'b': case 'B': // binary
            {
               iRadix = 2;
               readChar();
               lastChar = lexIntegerDigits(FileLexer::isBinaryCharacter);
            } break;
            default: // octal
            {
               iRadix = 8;
               lastChar = lexIntegerDigits(FileLexer::isOctalCharacter);
            } break;
         }
      }
      else
      {
         while ((Character.isDigit(currentChar) || currentChar == '_' ||
                currentChar == '.' || currentChar == 'e' || currentChar == 'E') && !eof)
         {
            lastChar = currentChar;
            // TODO(sekai): This looks like it can be put into a switch/case.
            if (currentChar == '.')
            {
               isInteger = false; // now it's floating point
            }
            else if (currentChar == 'e' || currentChar == 'E')
            {
               putChar();
               if (currentChar == '-')
               {
                  putChar();
               }
               continue;
            }
            else if (currentChar == '_')
            {
               readChar();
               continue;
            }
            putChar();
         }
      }
      
      if (lastChar == '_')
      {
         logger.logError(location, ERROR_UNDERSCORE_IN_NUMBER, "Numbers cannot end with '_'.");
      }
      
      if (currentChar == 'f' || currentChar == 'F')
      {
         if (!isInteger)
         {
            logger.logWarningf(location, WARNING_FLOAT_DECOR,
                  "'%c' was used on an already floating-point value, "
                  + "this is unnecessary.", currentChar);
         }
         readChar();
         isInteger = false;
      }

      String result = getTempString();
      
      // FIXME(sekai): Here, identifiers are now valid. (1.0fIDENT will lex), fix plz.
      
      boolean hadTrailingCharacters = false;
      while (!eof && Character.isLetterOrDigit(currentChar))
      {
         hadTrailingCharacters = true;
         putChar();
      }
      
      if (hadTrailingCharacters)
      {
         logger.logErrorf(location, ERROR_TRAILING_CHARS_IN_NUMBER,
               "Unexpected characters \"%s\" at the end of a number.\n",
               getTempString());
      }
      
      if (lastChar == '.' || currentChar == '.')
      {
         logger.logErrorf(location, ERROR_DOT_AFTER_NUMBER,
               "Illegal '.' at end of a number.\n",
               getTempString());
         if (currentChar == '.')
         {
            readChar();
         }
      }
      
      try
      {
         if (isInteger)
         {
            return new Token(Token.Type.INT_LITERAL, 
                             LayeInt.valueOf(Long.parseLong(result, iRadix)), 
                             location);
         }
         else
         {
            return new Token(Token.Type.FLOAT_LITERAL,
                             new LayeFloat(Double.parseDouble(result)), 
                             location);
         }
      }
      catch (NumberFormatException e)
      {
         logger.logError(location, ERROR_NUMBER_FORMAT,
                         "Number format: " + e.getMessage());
         if (isInteger)
         {
            return new Token(Token.Type.INT_LITERAL, LayeInt.valueOf(0L), location);
         }
         else
         {
            return new Token(Token.Type.FLOAT_LITERAL, new LayeFloat(0D), location);
         }
      }
   }
   
   private Token lexOtherTokens()
   {
      final Location location = getLocation();
      if (!Identifier.isIdentifierStart(currentChar))
      {
         logger.logErrorf(location, ERROR_INVALID_IDENTIFIER_START,
               "token '%c' is not a valid identifier start.\n", currentChar);
         return null;
      }
      do
      {
         putChar();
      }
      while (!eof && Identifier.isIdentifierPart(currentChar));
      String image = getTempString();
      if (image.equals("_"))
      {
         return new Token(Token.Type.WILDCARD, location);
      }
      else if (Keyword.exists(image))
      {
         return new Token(Token.Type.KEYWORD, Keyword.get(image), location);
      }
      return new Token(Token.Type.IDENTIFIER, Identifier.get(image), location);
   }
}
