using System.IO;
using System.Collections.Generic;
using System.Text;

namespace Laye.Compilation.TokenGen
{
#if LAYE64
    using lint = System.Int64;
    using lfloat = System.Double;
#else
    using lint = System.Int32;
    using lfloat = System.Single;
#endif

    internal sealed class Lexer
    {
        internal readonly DetailLogger log;

        private StreamReader reader;
        private int currentChar;

        private string file;
        private uint line = 1, col = 1;
        private bool eof = false;

        /// <summary>
        /// Backing field for IsFirstToken.
        /// </summary>
        private bool isFirstTokenBacking;
        /// <summary>
        /// Used to determine if the token being read is the first token in the stream.
        /// </summary>
        private bool IsFirstToken
        {
            get
            {
                if (isFirstTokenBacking)
                {
                    isFirstTokenBacking = false;
                    return true;
                }
                return false;
            }
            set
            {
                isFirstTokenBacking = value;
            }
        }

        private StringBuilder whitespace = new StringBuilder();

        internal Lexer(DetailLogger log)
        {
            this.log = log;
        }

        /// <summary>
        /// Reads a file and generates a list of tokens.
        /// The input stream will attempt to determine the encoding based on byte order marks.
        /// If it cannot, it falls back on the specified encoding.
        /// If not encoding is passed, it defaults to UTF-8.
        /// </summary>
        /// <param name="fileName"></param>
        /// <returns></returns>
        internal TokenStream GetTokens(string fileName, Encoding encoding = null)
        {
            file = fileName;

            if (encoding == null)
                encoding = Encoding.UTF8;

            // Init variables
            IsFirstToken = true;
            currentChar = -1;

            // Get reader
            reader = new StreamReader(File.OpenRead(fileName), encoding, true);
            Read();

            // Get all tokens woooo
            var tokens = new List<Token>();
            while (!eof)
            {
                var token = GetNextToken();
                // Will be EOF anyway
                if (token == null)
                    continue;
                tokens.Add(token);
            }

            reader.Close();

            return new TokenStream(log, tokens);
        }

        private void Read()
        {
            if (currentChar != -1)
                if (currentChar == '\t')
                    col += Laye.tabWidth;
                else col++;
            currentChar = reader.Read();
            if (currentChar == -1)
                eof = true;
        }

        private void EatWhitespace()
        {
            while (!eof)
            {
                if (char.IsWhiteSpace((char)currentChar))
                {
                    if (currentChar == '\n')
                    {
                        whitespace.Clear();
                        col = 1;
                        line++;
                    }
                    else whitespace.Append((char)currentChar);
                    Read();
                }
                else return;
            }
        }

        private void EatWhitespaceSansNewline()
        {
            while (!eof)
            {
                if (char.IsWhiteSpace((char)currentChar))
                {
                    if (currentChar == '\n')
                        return;
                    whitespace.Append((char)currentChar);
                    Read();
                }
                else return;
            }
        }

        private Token GetNextToken()
        {
            // First, we handle the whitespace.

            // We first make sure the initial whitespace is eaten if it exists:
            if (IsFirstToken)
                EatWhitespace();
            // Now save this whitespace and clear the buffer, we'll check for more whitespace after the token.
            string preWhitespace = whitespace.ToString();
            whitespace.Clear();

            // Get the token
            Token result = null;
            switch (currentChar)
            {
                case '.':
                    Read();
                    if (currentChar == '.')
                    {
                        Read();
                        result = new Token(Token.Type.VARGS, new Location(file, line, col - 2, line, col));
                    }
                    else result = new Token(Token.Type.DOT, new Location(file, line, col - 1, line, col));
                    break;
                case ',': Read(); result = new Token(Token.Type.COMMA, new Location(file, line, col - 1, line, col)); break;
                case ':': Read(); result = new Token(Token.Type.COLON, new Location(file, line, col - 1, line, col)); break;

                case '(': Read(); result = new Token(Token.Type.OPEN_BRACE, new Location(file, line, col - 1, line, col)); break;
                case ')': Read(); result = new Token(Token.Type.CLOSE_BRACE, new Location(file, line, col - 1, line, col)); break;
                case '[': Read(); result = new Token(Token.Type.OPEN_SQUARE_BRACE, new Location(file, line, col - 1, line, col)); break;
                case ']': Read(); result = new Token(Token.Type.CLOSE_SQUARE_BRACE, new Location(file, line, col - 1, line, col)); break;
                case '{': Read(); result = new Token(Token.Type.OPEN_CURLY_BRACE, new Location(file, line, col - 1, line, col)); break;
                case '}': Read(); result = new Token(Token.Type.CLOSE_CURLY_BRACE, new Location(file, line, col - 1, line, col)); break;

                case '`': ReadLineComment(); result = null; break;

                case '~': case '!': case '@': case '#':
                case '%': case '^': case '&': case '*':
                case '-': case '=': case '+': case '\\':
                case '|': case '<': case '>': case '/':
                case '?': case ';': result = ReadOperator(); break;

                case '"':  result = ReadString(); break;

                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    result = ReadNumber(); break;

                case '$': result = ReadParamIndex(); break;
                case '\'':
                    var loc = new Location(file, line, col);
                    Read();
                    var sym = ReadOthers();
                    if (sym.type == Token.Type.WILDCARD)
                        log.Error(sym.location, "The wildcard character cannot be used as a symbol literal.");
                    else if (sym.type != Token.Type.IDENTIFIER)
                        log.Error(sym.location, "Keyword '{0}' cannot be used as a symbol literal.", sym.image);
                    result = new Token(Token.Type.SYMBOL, loc, sym.image);
                    break;

                default:
                    if (currentChar == '_' || char.IsLetter((char)currentChar))
                        result = ReadOthers();
                    else
                    {
                        // TODO error, skip the character in attempt to recover.
                        log.Error(new Location(file, line, col),
                            "Unexpected character '{0}'. This character should only exist in a string, are you missing quotes?",
                            char.ConvertFromUtf32(currentChar));
                        Read();
                        return GetNextToken();
                    }
                    break;
            }

            EatWhitespaceSansNewline();
            if (result != null)
            {
            // Add the whitespace to the token.
                result.PreWhitespace = preWhitespace;
                result.PostWhitespace = whitespace.ToString();
            }
            // Once the token has its whitespace, make sure we've handled newlines now.
            // If the tokens are on the same line, this does nothing so that's good.
            EatWhitespace();

            // That's it!
            return result;
        }

        private void ReadLineComment()
        {
            Read(); // `

            // the newline is nom'd after the token is returned.
            while (!eof && currentChar != '\n')
                Read();
        }

        private Token ReadOperator()
        {
            var builder = new StringBuilder();
            var initCol = col;

            do
            {
                builder.Append((char)currentChar);
                Read();
            }
            while (!eof && Laye.IsOperatorChar(currentChar));

            var op = builder.ToString();
            if (op == "=")
                return new Token(Token.Type.ASSIGN, new Location(file, line, initCol, line, col));
            return new Token(Token.Type.OPERATOR, new Location(file, line, initCol, line, col), op);
        }

        private Token ReadString()
        {
            var builder = new StringBuilder();
            var initLine = line;
            var initCol = col;

            Read(); // quote char

            // the newline is nom'd after the token is returned.
            while (!eof && currentChar != '"')
            {
                builder.Append(char.ConvertFromUtf32(currentChar));
                Read();
            }

            if (eof)
                // The location of this should be the begining of the string, so we can mark the whole
                // string as having an issue.
                log.Error(new Location(file, initLine, initCol, line, col),
                    "This string is unfinished. The end of the file was reached before a matching quote ({0}) was found. Check that your string terminates where it should, you may have forgotten or lost a quote (\")!");
            else Read(); // quote char

            return new Token(Token.Type.STRING, new Location(file, initLine, initCol, line, col), builder.ToString());
        }

        private static bool DoesCharDefineNumericBase(int c)
        {
            switch (c)
            {
                case 'x': case 'X':
                case 'b': case 'B':
                case 'c': case 'C':
                    return true;
                default: return false;
            }
        }

        private delegate bool BaseTest(int c);

        private Token ReadNumber()
        {
            var initCol = col;
            var firstChar = currentChar;
            Read();

            string resultImage = null;
            bool isInt = true;

            if (firstChar == '0' && DoesCharDefineNumericBase(currentChar))
            {
                var baseChar = currentChar;
                Read();

                BaseTest test;
                if (baseChar == 'x' || baseChar == 'X')
                    test = c => (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
                else if (baseChar == 'c' || baseChar == 'C')
                    test = c => (c >= '0' && c <= '7');
                else test = c => (c == '0' || c == '1');

                var builder = new StringBuilder();
                while (!eof && test(currentChar))
                {
                    builder.Append((char)currentChar);
                    Read();
                }

                resultImage = string.Format("0{0}{1}", (char)baseChar, builder.ToString());
            }
            else
            {
                var builder = new StringBuilder();
                builder.Append((char)firstChar);
                while (!eof && (char.IsDigit((char)currentChar) || currentChar == '.'))
                {
                    if (currentChar == '.')
                    {
                        if (!isInt)
                            break;
                        isInt = false;
                    }
                    builder.Append((char)currentChar);
                    Read();
                }
                resultImage = builder.ToString();
                if (resultImage.EndsWith("."))
                    log.Error(new Location(file, line, initCol, line, col),
                        "Floats cannot end with the decimal point marker. Change the number to '{0}0' to fix the issue.",
                        resultImage);
            }

            if (isInt)
            {
                var result = Laye.ConvertToInt(resultImage);
                return new Token(new Location(file, line, initCol, line, col), result, resultImage);
            }
            else
            {
                var result = Laye.ConvertToFloat(resultImage);
                return new Token(new Location(file, line, initCol, line, col), result, resultImage);
            }
        }

        private Token ReadParamIndex()
        {
            var initCol = col;
            var loc = new Location(file, line, col, line, col + 2);

            Read();
            if (eof)
            {
                log.Error(loc, "Expected digit as parameter index value.");
                return new Token(loc, 0);
            }
            else if (!char.IsDigit((char)currentChar))
            {
                log.Error(loc, "Expected digit as parameter index value.");
                return new Token(loc, 0);
            }

            var index = (uint)(currentChar - '0');
            Read();
            return new Token(loc, index);
        }

        private Token ReadOthers()
        {
            var builder = new StringBuilder();
            var initCol = col;

            do
            {
                builder.Append((char)currentChar);
                Read();
            }
            while (!eof && (char.IsLetterOrDigit((char)currentChar) || currentChar == '_'));

            var word = builder.ToString();
            var loc = new Location(file, line, initCol, line, col);

            if (word == "_")
                return new Token(Token.Type.WILDCARD, loc, "_");
            else
            {
                switch (word)
                {
                    case "null": return new Token(Token.Type.NULL, loc, word);
                    case "true": return new Token(Token.Type.TRUE, loc, word);
                    case "false": return new Token(Token.Type.FALSE, loc, word);
                    case "endl": return new Token(Token.Type.ENDL, loc, word);

                    case "var": return new Token(Token.Type.VAR, loc, word);
                    case "lazy": return new Token(Token.Type.LAZY, loc, word);
                    case "take": return new Token(Token.Type.TAKE, loc, word);
                    case "private": return new Token(Token.Type.PRIVATE, loc, word);
                    case "static": return new Token(Token.Type.STATIC, loc, word);
                    case "readonly": return new Token(Token.Type.READONLY, loc, word);

                    case "use": return new Token(Token.Type.USE, loc, word);
                    case "from": return new Token(Token.Type.FROM, loc, word);
                    case "kit": return new Token(Token.Type.KIT, loc, word);
                    case "global": return new Token(Token.Type.GLOBAL, loc, word);

                    case "self": return new Token(Token.Type.SELF, loc, word);
                    case "tailrec": return new Token(Token.Type.TAILREC, loc, word);
                    case "fn": return new Token(Token.Type.FN, loc, word);
                    case "gen": return new Token(Token.Type.GEN, loc, word);
                    case "ctor": return new Token(Token.Type.CTOR, loc, word);
                    case "invoke": return new Token(Token.Type.INVOKE, loc, word);

                    case "this": return new Token(Token.Type.THIS, loc, word);
                    case "base": return new Token(Token.Type.BASE, loc, word);
                    case "type": return new Token(Token.Type.TYPE, loc, word);
                    case "enum": return new Token(Token.Type.ENUM, loc, word);
                    case "sealed": return new Token(Token.Type.SEALED, loc, word);
                    case "partial": return new Token(Token.Type.PARTIAL, loc, word);
                    case "get": return new Token(Token.Type.GET, loc, word);
                    case "set": return new Token(Token.Type.SET, loc, word);
                    case "new": return new Token(Token.Type.NEW, loc, word);

                    case "if": return new Token(Token.Type.IF, loc, word);
                    case "el": return new Token(Token.Type.EL, loc, word);
                    case "when": return new Token(Token.Type.WHEN, loc, word);
                    case "iter": return new Token(Token.Type.ITER, loc, word);
                    case "in": return new Token(Token.Type.IN, loc, word);
                    case "to": return new Token(Token.Type.TO, loc, word);
                    case "each": return new Token(Token.Type.EACH, loc, word);
                    case "by": return new Token(Token.Type.BY, loc, word);
                    case "while": return new Token(Token.Type.WHILE, loc, word);
                    case "match": return new Token(Token.Type.MATCH, loc, word);
                    case "try": return new Token(Token.Type.TRY, loc, word);
                    case "catch": return new Token(Token.Type.CATCH, loc, word);
                    case "ret": return new Token(Token.Type.RET, loc, word);
                    case "break": return new Token(Token.Type.BREAK, loc, word);
                    case "cont": return new Token(Token.Type.CONT, loc, word);
                    case "res": return new Token(Token.Type.RES, loc, word);
                    case "yield": return new Token(Token.Type.YIELD, loc, word);
                    case "throw": return new Token(Token.Type.THROW, loc, word);

                    case "and": return new Token(Token.Type.AND, loc, word);
                    case "or": return new Token(Token.Type.OR, loc, word);
                    case "xor": return new Token(Token.Type.XOR, loc, word);
                    case "not": return new Token(Token.Type.NOT, loc, word);
                    case "typeof": return new Token(Token.Type.TYPEOF, loc, word);
                    case "is": return new Token(Token.Type.IS, loc, word);
                    case "as": return new Token(Token.Type.AS, loc, word);
                    case "ref": return new Token(Token.Type.REF, loc, word);
                    case "deref": return new Token(Token.Type.DEREF, loc, word);

                    default: return new Token(Token.Type.IDENTIFIER, loc, word);
                }
            } 
        }
    }
}
