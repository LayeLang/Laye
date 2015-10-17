using System;
using System.Collections.Generic;

namespace Laye.Compilation.TokenGen
{
    internal sealed class TokenStream
    {
        private readonly List<Token> tokens;

        // Current index into the token list
        private int curTokenIndex = 0;

        /// <summary>
        /// Gets the token at the current index.
        /// </summary>
        internal Token Current { get { return tokens[curTokenIndex]; } }
        /// <summary>
        /// Gets the next token.
        /// </summary>
        internal Token Next { get { return tokens[curTokenIndex + 1]; } }
        /// <summary>
        /// Gets a token relative to the current index.
        /// This is basically a peek, but with arbitrary direction and magnitude.
        /// Null is returned if the index is out of bounds.
        /// </summary>
        /// <param name="offset"></param>
        /// <returns></returns>
        internal Token this[int offset]
        {
            get
            {
                var newIndex = curTokenIndex + offset;
                if (newIndex < 0 || newIndex >= tokens.Count)
                    return null;
                return tokens[newIndex];
            }
        }

        /// <summary>
        /// Returns true if there are no tokens left to read, false otherwise.
        /// </summary>
        internal bool IsOver { get { return curTokenIndex >= tokens.Count; } }
        /// <summary>
        /// Returns true if there is at least one token after the current one.
        /// </summary>
        internal bool HasNext { get { return curTokenIndex < tokens.Count - 1; } }

        /// <summary>
        /// The DetailLogger this TokenStream uses.
        /// </summary>
        internal readonly DetailLogger log;

        /// <summary>
        /// Create a new stream of tokens.
        /// This instance assumes control over the given list.
        /// </summary>
        /// <param name="tokens"></param>
        internal TokenStream(DetailLogger log, List<Token> tokens)
        {
            this.log = log;
            this.tokens = tokens;
        }

        /// <summary>
        /// Sets the index of this stream back to 0.
        /// </summary>
        internal void Reset()
        {
            curTokenIndex = 0;
        }

        private void LogError(string message)
        {
            log.Error(IsOver ? this[-1].location : Current.location, message);
        }

        /// <summary>
        /// Increments the steam index.
        /// </summary>
        internal void Advance()
        {
            curTokenIndex++;
        }

        /// <summary>
        /// Checks if the current token is of the given type.
        /// If it is, the stream advances and this method returns true.
        /// Otherwise a message is added to the error list if one was given, then this method returns false.
        /// </summary>
        /// <param name="type"></param>
        /// <param name="failMessage"></param>
        /// <returns></returns>
        internal bool Expect(Token.Type type, string failMessage)
        {
            if (failMessage == null)
                throw new ArgumentNullException("failMessage");
            if (!IsOver && Current.type == type)
            {
                Advance();
                return true;
            }
            // We make this optional because the parser could provide much more detailed information
            // on its own after some thought, we don't HAVE to expect that error message up front.
            if (failMessage != null)
                LogError(failMessage);
            return false;
        }

        /// <summary>
        /// Checks if the current token is an identifier.
        /// If it is, the stream advances and this method returns true.
        /// Otherwise a message is added to the error list if one was given, then this method returns false.
        /// </summary>
        /// <param name="type"></param>
        /// <param name="failMessage"></param>
        /// <returns></returns>
        internal bool ExpectIdentifier(out string ident, string failMessage = null)
        {
            // Save this token for later
            var token = Current;
            var result = Expect(Token.Type.IDENTIFIER, failMessage);
            // If it was an identifier, save the image.
            if (result)
                ident = token.image;
            // otherwise, set to null, oops.
            else ident = null;
            return result;
        }
    }
}
