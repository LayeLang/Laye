using System;

namespace Laye
{
    public sealed class NumberFormatException : Exception
    {
        internal NumberFormatException(string format, params object[] args)
            : base(string.Format(format, args))
        {
        }

        internal NumberFormatException(string message)
            : base(message)
        {
        }
    }
}
