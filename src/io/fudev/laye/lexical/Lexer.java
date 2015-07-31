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
package io.fudev.laye.lexical;

import static io.fudev.laye.log.LogMessageID.ERROR_DOT_AFTER_NUMBER;
import static io.fudev.laye.log.LogMessageID.ERROR_ESCAPED_UNICODE_SIZE;
import static io.fudev.laye.log.LogMessageID.ERROR_ILLEGAL_ESCAPE;
import static io.fudev.laye.log.LogMessageID.ERROR_INVALID_IDENTIFIER_START;
import static io.fudev.laye.log.LogMessageID.ERROR_NUMBER_FORMAT;
import static io.fudev.laye.log.LogMessageID.ERROR_TRAILING_CHARS_IN_NUMBER;
import static io.fudev.laye.log.LogMessageID.ERROR_UNDERSCORE_IN_NUMBER;
import static io.fudev.laye.log.LogMessageID.ERROR_UNFINISHED_STRING;
import static io.fudev.laye.log.LogMessageID.WARNING_FLOAT_DECOR;

import java.io.IOException;
import java.io.InputStreamReader;

import io.fudev.laye.file.ScriptFile;
import io.fudev.laye.log.DetailLogger;
import io.fudev.laye.struct.Identifier;
import io.fudev.laye.struct.Keyword;
import io.fudev.laye.struct.Operator;
import io.fudev.laye.vm.LayeFloat;
import io.fudev.laye.vm.LayeInt;
import io.fudev.laye.vm.LayeString;

/**
 * TODO(sekai): all unicode characters should eventually be supported in here, plz.
 * 
 * @author Sekai Kyoretsuna
 */
public class Lexer
{
   /**
    * @author Sekai Kyoretsuna
    */
   private static interface CharacterMatcher
   {
      boolean apply(int c);
   }
   
   private static boolean doesCharacterDefineInteger(int c)
   {
      return(c == 'x' || c == 'X' || c == 'b' || c == 'B' || c == 'c' || c == 'C');
   }
   
   private static boolean isHexadecimalCharacter(int c)
   {
      return((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'));
   }
   
   private static boolean isBinaryCharacter(int c)
   {
      return(c == '0' || c == '1');
   }
   
   private static boolean isOctalCharacter(int c)
   {
      return(c >= '0' && c <= '7');
   }

   private final DetailLogger logger;
   
   private InputStreamReader input = null;
   private ScriptFile file = null;
   
   private StringBuilder builder = new StringBuilder();
   private int currentChar = 0, lastChar = currentChar;
   
   private int line = 1, column = 0;
   private boolean eof;
   
   public Lexer(DetailLogger logger)
   {
      this.logger = logger;
   }
   
   public TokenStream getTokens(ScriptFile file) throws IOException
   {
      this.file = file;
      this.input = file.read();
      
      TokenStream result = new TokenStream();
      
      readChar();
      
      Token token;
      while ((token = lex()) != null)
      {
         result.append(token);
      }
      
      input.close();
      
      return(result);
   }
   
   private Location getLocation()
   {
      return(new Location(file, line, column));
   }
   
   private String getTempString()
   {
      String result = builder.toString();
      builder.setLength(0);
      builder.trimToSize();
      return(result);
   }
   
   private boolean putChar()
   {
      builder.appendCodePoint(currentChar);
      return(readChar());
   }
   
   private void putChar(int c)
   {
      builder.appendCodePoint(c);
   }
   
   private boolean readChar()
   {
      lastChar = currentChar;
      int next;
      try
      {
         next = input.read();
      }
      catch (IOException e)
      {
         eof = true;
         currentChar = '\u0000';
         return(false);
      }
      if (next == -1)
      {
         eof = true;
         currentChar = '\u0000';
         return(false);
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
      return(true);
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
               return(lex());
            case '(':
               readChar();
               return(new Token(Token.Type.OPEN_BRACE, location));
            case ')':
               readChar();
               return(new Token(Token.Type.CLOSE_BRACE, location));
            case '[':
               readChar();
               return(new Token(Token.Type.OPEN_SQUARE_BRACE, location));
            case ']':
               readChar();
               return(new Token(Token.Type.CLOSE_SQUARE_BRACE, location));
            case '{':
               readChar();
               return(new Token(Token.Type.OPEN_CURLY_BRACE, location));
            case '}':
               readChar();
               return(new Token(Token.Type.CLOSE_CURLY_BRACE, location));
            case ';':
               readChar();
               return(new Token(Token.Type.SEMI_COLON, location));
            case ':':
               readChar();
               return(new Token(Token.Type.COLON, location));
            case ',':
               readChar();
               return(new Token(Token.Type.COMMA, location));
            case '.':
               readChar();
               if (currentChar == '.')
               {
                  readChar();
                  return(new Token(Token.Type.VARGS, location));
               }
               return(new Token(Token.Type.DOT, location));
            case '\'':
            case '"':
               return(lexStringLiteral());
            default:
               if (Operator.isOperatorChar(currentChar))
               {
                  return(lexOperatorToken());
               }
               if (Character.isDigit(currentChar))
               {
                  return(lexNumericToken());
               }
               return(lexOtherTokens());
         }
      }
      
      return(null);
   }
   
   private void lexOutLineComment()
   {
      // Nom all the chars until the end of a line.
      do
      {
         readChar();
      }
      while (currentChar != '\n' && !eof);
   }
   
   private Token lexStringLiteral()
   {
      final Location location = getLocation();
      final int quoteChar = currentChar;
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
      return(new Token(Token.Type.STRING_LITERAL, new LayeString(result), location));
   }
   
   private char lexEscapedCharacter()
   {
      final Location location = getLocation();
      // Read '\'
      readChar();
      switch ((char)currentChar)
      {
         case 'u':
         {
            readChar();
            final StringBuilder sb = new StringBuilder();
            int idx = 0;
            for (; !eof && idx < 4 && isHexadecimalCharacter(currentChar); idx++)
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
               return('\u0000');
            }
            return((char) Integer.parseInt(sb.toString(), 16));
         }
         case 'r':
            readChar();
            return('\r');
         case 'n':
            readChar();
            return('\n');
         case 't':
            readChar();
            return('\t');
         case '0':
            readChar();
            return('\0');
         case '"':
            readChar();
            return('\"');
         case '\'':
            readChar();
            return('\'');
         case '\\':
            readChar();
            return('\\');
         default:
            logger.logErrorf(location, ERROR_ILLEGAL_ESCAPE,
                             "escape character '%c' not recognized.\n", currentChar);
            return('\u0000');
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
         return(new Token(Token.Type.ASSIGN, location));
      }
      return(new Token(Token.Type.OPERATOR, Operator.get(image), location));
   }
   
   private void lexIntegerDigits(CharacterMatcher matcher)
   {
      while ((matcher.apply(currentChar) || currentChar == '_') && !eof)
      {
         if (currentChar != '_')
         {
            putChar();
         }
         else
         {
            readChar();
         }
      }
   }
   
   private Token lexNumericToken()
   {
      final Location location = getLocation();

      readChar();
      
      boolean isInteger = true;
      int iRadix = 10;

      if (lastChar == '0' && doesCharacterDefineInteger(currentChar))
      {
         readChar();
         switch (lastChar)
         {
            case 'x': case 'X': // hexadecimal
            {
               iRadix = 16;
               lexIntegerDigits(Lexer::isHexadecimalCharacter);
            } break;
            case 'b': case 'B': // binary
            {
               iRadix = 2;
               lexIntegerDigits(Lexer::isBinaryCharacter);
            } break;
            case 'c': case 'C': // octal
            {
               iRadix = 8;
               lexIntegerDigits(Lexer::isOctalCharacter);
            } break;
            default:
            {
            } break;
         }
      }
      else
      {
         putChar(lastChar);
         // TODO(sekai): Make sure '_' is handled in all places.
         while ((Character.isDigit(currentChar) || currentChar == '_' ||
                currentChar == '.' || currentChar == 'e' || currentChar == 'E') && !eof)
         {
            // TODO(sekai): This looks like it can be put into a switch/case.
            if (currentChar == '.')
            {
               if (lastChar == '_')
               {
                  logger.logError(location, ERROR_UNDERSCORE_IN_NUMBER, 
                        "Numbers cannot contain '_' next to a decimal point.");
               }
               isInteger = false; // now it's floating point
            }
            else if (currentChar == 'e' || currentChar == 'E')
            {
               putChar();
               if (currentChar == '-')
               {
                  putChar();
               }
               isInteger = false;
               continue;
            }
            else if (currentChar == '_')
            {
               if (lastChar == '.')
               {
                  logger.logError(location, ERROR_UNDERSCORE_IN_NUMBER, 
                        "Numbers cannot contain '_' next to a decimal point.");
               }
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
                  + "this is unnecessary.\n", currentChar);
         }
         readChar();
         isInteger = false;
      }

      String result = getTempString();
      
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
            return(new Token(Token.Type.INT_LITERAL, 
                             LayeInt.valueOf(Long.parseLong(result, iRadix)), 
                             location));
         }
         else
         {
            return(new Token(Token.Type.FLOAT_LITERAL,
                             new LayeFloat(Double.parseDouble(result)), 
                             location));
         }
      }
      catch (NumberFormatException e)
      {
         logger.logError(location, ERROR_NUMBER_FORMAT,
                         (isInteger ? "Int" : "Float") + " format: " + e.getMessage());
         if (isInteger)
         {
            return(new Token(Token.Type.INT_LITERAL, LayeInt.valueOf(0L), location));
         }
         else
         {
            return(new Token(Token.Type.FLOAT_LITERAL, new LayeFloat(0D), location));
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
         return(null);
      }
      do
      {
         putChar();
      }
      while (!eof && Identifier.isIdentifierPart(currentChar));
      String image = getTempString();
      if (image.equals("_"))
      {
         return(new Token(Token.Type.WILDCARD, location));
      }
      else if (Keyword.exists(image))
      {
         return(new Token(Token.Type.KEYWORD, Keyword.get(image), location));
      }
      return(new Token(Token.Type.IDENTIFIER, Identifier.get(image), location));
   }
}
