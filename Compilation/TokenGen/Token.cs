using System;

namespace Laye.Compilation.TokenGen
{
#if LAYE64
    using lint = System.Int64;
    using lfloat = System.Double;
#else
    using lint = System.Int32;
    using lfloat = System.Single;
#endif

    internal sealed class Token
    {
        internal enum Type
        {
            INT,
            FLOAT,
            STRING,

            NULL,
            TRUE,
            FALSE,
            ENDL,

            SYMBOL,
            WILDCARD,
            PARAM_INDEX,
            VARGS,

            OPEN_BRACE,
            CLOSE_BRACE,
            OPEN_SQUARE_BRACE,
            CLOSE_SQUARE_BRACE,
            OPEN_CURLY_BRACE,
            CLOSE_CURLY_BRACE,

            DOT,
            COMMA,
            COLON,

            OPERATOR,
            ASSIGN,

            VAR,
            LAZY,
            TAKE,
            PRIVATE,
            STATIC,
            READONLY,

            USE,
            FROM,
            KIT,
            GLOBAL,

            SELF,
            TAILREC,
            FN,
            GEN,
            CTOR,
            INVOKE,

            THIS,
            BASE,
            TYPE,
            ENUM,
            SEALED,
            PARTIAL,
            GET,
            SET,
            NEW,

            IF,
            EL,
            WHEN,
            ITER,
            TO,
            BY,
            EACH,
            IN,
            WHILE,
            MATCH,
            TRY,
            CATCH,
            RET,
            BREAK,
            CONT,
            RES,
            YIELD,
            THROW,

            AND,
            OR,
            XOR,
            NOT,
            TYPEOF,
            IS,
            AS,
            REF,
            DEREF,

            IDENTIFIER,
        }

        /// <summary>
        /// The type of this token.
        /// </summary>
        internal readonly Type type;
        /// <summary>
        /// The location of this token in the source file.
        /// </summary>
        internal readonly Location location;

        /// <summary>
        /// A string describing the whitespace that came before this token.
        /// This breaks at and doesn't include newline characters.
        /// 
        /// This is useful when reformatting source code, since the column
        /// in locations is character based and whitespace can include tabs.
        /// This allows you to accureately regurgitate the source when needed.
        /// </summary>
        internal string PreWhitespace { get; set; } = null;
        /// <summary>
        /// A string describing the whitespace that came after this token.
        /// This breaks at and doesn't include newline characters.
        /// 
        /// This is a lot less usefull than the preWhitespace field.
        /// This should only be used on the last token in the file, since
        /// this overlaps with the preWhitespace of the next token otherwise.
        /// If whitespace at the end of a line is important to you, use this there as well.
        /// </summary>
        internal string PostWhitespace { get; set; } = null;

        /// <summary>
        /// The representation of this token.
        /// Used especially for strings, keywords and identifiers.
        /// </summary>
        internal readonly string image;

        /// <summary>
        /// The integer value of this token, if it represents Type.INT
        /// </summary>
        internal readonly lint intValue;

        /// <summary>
        /// The floating point value of this token, if it represents Type.FLOAT
        /// </summary>
        internal readonly lfloat floatValue;

        internal Token(Type type, Location location, string image = null)
        {
            this.type = type;
            this.location = location;
            if (image == null)
                switch (type)
                {
                    case Type.DOT: this.image = "."; break;
                    case Type.VARGS: this.image = ".."; break;
                    case Type.COMMA: this.image = ","; break;
                    case Type.COLON: this.image = ":"; break;

                    case Type.OPEN_BRACE: this.image = "("; break;
                    case Type.CLOSE_BRACE: this.image = ")"; break;
                    case Type.OPEN_SQUARE_BRACE: this.image = "["; break;
                    case Type.CLOSE_SQUARE_BRACE: this.image = "]"; break;
                    case Type.OPEN_CURLY_BRACE: this.image = "{"; break;
                    case Type.CLOSE_CURLY_BRACE: this.image = "}"; break;

                    case Type.ASSIGN: this.image = "="; break;

                    case Type.WILDCARD: this.image = "_"; break;

                    default: throw new ArgumentException("Could not create token image.");
                }
            else this.image = image;
        }

        internal Token(Location location, uint index)
        {
            this.location = location;
            type = Type.PARAM_INDEX;
            image = index.ToString();
            intValue = (lint)index;
        }

        internal Token(Location location, lint ival, string image)
        {
            this.type = Type.INT;
            this.location = location;
            this.image = image;
            intValue = ival;
        }

        internal Token(Location location, lfloat fval, string image)
        {
            this.type = Type.FLOAT;
            this.location = location;
            this.image = image;
            floatValue = fval;
        }

        public override string ToString()
        {
            if (type == Type.INT)
                return image + " (" + intValue + ")";
            else if (type == Type.FLOAT)
                return image + " (" + floatValue + ")";
            return image;
        }
    }
}
