using System;

namespace Laye
{
#if LAYE64
    using lint = Int64;
    using lfloat = Double;
#else
    using lint = Int32;
    using lfloat = Single;
#endif

    public static class Laye
    {
        #region Constants
        /// <summary>
        /// The 'null' literal in Laye.
        /// </summary>
        public static readonly LayeObject NULL = new LayeNull();

        /// <summary>
        /// The 'true' literal in Laye.
        /// </summary>
        public static readonly LayeObject TRUE = new LayeBool(true);

        /// <summary>
        /// The 'false' literal in Laye.
        /// </summary>
        public static readonly LayeObject FALSE = new LayeBool(false);

        internal static readonly LayeObject EMPTY_STRING = new LayeString();
        #endregion Constants

        /// <summary>
        /// This should be set (and remain constant while lexers are running) to the
        /// expected tab width of the user's editor.
        /// </summary>
        public static uint tabWidth = 4;

        /// <summary>
        /// The default precedence of operators. This is equal to the precedence
        /// of the additive operators.
        /// </summary>
        public const uint defaultOperatorPrecedence = 7;

        public static SharpObject ToSharpObject<T>(T obj)
        {
            return SharpTypeDef.Get(typeof(T)).Bind(obj);
        }

        public static bool IsIdentifier(string image)
        {
            if (image == "_" || !IsIdentifierStart(image[0]))
                return false;
            for (int i = 1; i < image.Length; i++)
                if (!IsIdentifierPart(image[i]))
                    return false;
            return !IsKeyword(image);
        }

        public static bool IsIdentifierStart(char c)
        {
            return char.IsLetter(c) || c == '_';
        }

        public static bool IsIdentifierPart(char c)
        {
            return char.IsLetterOrDigit(c) || c == '_';
        }

        public static bool IsOperatorChar(char c)
        {
            switch (c)
            {
                case '~': case '!': case '@': case '#':
                case '%': case '^': case '&': case '*':
                case '-': case '=': case '+': case '\\':
                case '|': case '<': case '>': case '/':
                case '?': case ';': return true;
                default: return false;
            }
        }
        
        public static bool IsOperatorChar(int codepoint)
        {
            switch (codepoint)
            {
                case '~': case '!': case '@': case '#':
                case '%': case '^': case '&': case '*':
                case '-': case '=': case '+': case '\\':
                case '|': case '<': case '>': case '/':
                case '?': case ';': return true;
                default: return false;
            }
        }

        public static bool IsKeyword(string word)
        {
            switch (word)
            {
                case "private":
                case "static":
                case "readonly":
                case "sealed":
                case "var":
                case "global":
                case "lazy":
                case "new":

                case "fn":
                case "gen":
                case "ctor":
                case "invoke":
                case "type":
                case "partial":
                case "enum":
                case "use":
                case "from":

                case "and":
                case "or":
                case "xor":
                case "not":
                case "typeof":
                case "is":
                case "as":
                case "ref":
                case "deref":

                case "if":
                case "el":
                case "when":
                case "iter":
                case "each":
                case "ieach":
                case "in":
                case "to":
                case "by":
                case "while":
                case "take":
                case "match":
                case "throw":
                case "try":
                case "catch":

                case "ret":
                case "break":
                case "cont":
                case "res":
                case "yield":

                case "get":
                case "set":
                case "this":
                case "self":
                case "base":
                case "kit":

                case "true":
                case "false":
                case "null":
                case "endl":

                case "tailrec": return true;

                default: return false;
            }
        }

        public static uint GetOperatorPrecedence(string op)
        {
            switch (op)
            {
                case "+": return defaultOperatorPrecedence;
                case "-": return defaultOperatorPrecedence;
                case "*": return defaultOperatorPrecedence + 1;
                case "/": return defaultOperatorPrecedence + 1;
                case "//": return defaultOperatorPrecedence + 1;
                case "%": return defaultOperatorPrecedence + 1;
                case "^": return defaultOperatorPrecedence + 2;
                case "&": return defaultOperatorPrecedence - 4;
                case "|": return defaultOperatorPrecedence - 6;
                case "~": return defaultOperatorPrecedence - 5;
                case "<<": return defaultOperatorPrecedence - 1;
                case ">>": return defaultOperatorPrecedence - 1;
                case ">>>": return defaultOperatorPrecedence - 1;
                case "==": return defaultOperatorPrecedence - 3;
                case "!=": return defaultOperatorPrecedence - 3;
                case "<": return defaultOperatorPrecedence - 2;
                case "<=": return defaultOperatorPrecedence - 2;
                case ">": return defaultOperatorPrecedence - 2;
                case ">=": return defaultOperatorPrecedence - 2;
                case "<=>": return defaultOperatorPrecedence - 2;
                case "<>": return defaultOperatorPrecedence;
                default: return defaultOperatorPrecedence;
            }
        }

        public static bool TryConvertToInt(string input, out lint value)
        {
            try
            {
                value = ConvertToInt(input);
                return true;
            }
            catch (Exception)
            {
                value = 0;
                return false;
            }
        }

        public static lint ConvertToInt(string input)
        {
            input = input.ToLower();
            if (input.StartsWith("0x"))
                return ParseInt(input.Substring(2), 16);
            else if (input.StartsWith("0c"))
                return ParseInt(input.Substring(2), 8);
            else if (input.StartsWith("0b"))
                return ParseInt(input.Substring(2), 2);
            return ParseInt(input);
        }

        public static bool TryConvertToFloat(string input, out lfloat result)
        {
            return lfloat.TryParse(input, out result);
        }

        public static lfloat ConvertToFloat(string input)
        {
            return lfloat.Parse(input);
        }

        /// <summary>
        /// Not that this implementation comes from Java's parse methods.
        /// TODO accept _'s.
        /// </summary>
        /// <param name="s"></param>
        /// <param name="radix"></param>
        /// <returns></returns>
        public static lint ParseInt(string s, int radix = 10)
        {
            if (s == null)
                throw new NumberFormatException("null");
            if (radix < 2)
                throw new NumberFormatException("radix {0} is less than 2.", radix);
            if (radix > 36)
                throw new NumberFormatException("radix {0} is greater than 36.", radix);

            lint result = 0;
            bool isNegative = false;
            int i = 0, len = s.Length;
            lint limit = -lint.MaxValue, multmin;
            int digit;

            if (len > 0)
            {
                var first = s[0];
                if (first < '0')
                {
                    if (first == '-')
                    {
                        isNegative = true;
                        limit = lint.MinValue;
                    }
                    else if (first != '+')
                        throw new NumberFormatException("Illegal initial character.");
                    if (len == 1)
                        throw new NumberFormatException("Cannot have lone {0}.", s[0]);
                    i++;
                }
                multmin = limit / radix;
                while (i < len)
                {
                    if (!CharToDigit(s[i], radix, out digit))
                        throw new NumberFormatException("Illegal character {0} in {1}.", s[i], s);
                    i++;
                    if (result < multmin)
                        throw new NumberFormatException("Number out of rage: " + s);
                    result *= radix;
                    if (result < limit + digit)
                        throw new NumberFormatException(s);
                    result -= digit;
                }
            }
            else throw new NumberFormatException("empty string.");
            return isNegative ? result : -result;
        }

        private static char[] digits = new char[]
        {
            '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        };

        public static bool CharToDigit(char c, int radix, out int digit)
        {
            if (radix < 2 || radix > 36)
                throw new ArgumentException("radix");
            c = char.ToLower(c);
            for (int i = 0; i < radix; i++)
                if (digits[i] == c)
                {
                    digit = i;
                    return true;
                }
            digit = -1;
            return false;
        }
    }
}
