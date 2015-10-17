using System;
using System.Text;

namespace Laye
{
    using StackTrace;

    public sealed class UnhandledLayeException : Exception
    {
        private readonly LayeStackTraceElement cause;

        private string layeStackTrace = null;
        public string LayeStackTrace
        {
            get
            {
                if (layeStackTrace != null)
                    return layeStackTrace;

                var builder = new StringBuilder();
                var cause = this.cause;

                builder.AppendLine(Message);
                while (cause != null)
                {
                    builder.Append("\tcaused by ").Append(cause.file).Append(" on line ").AppendLine(cause.line.ToString());
                    cause = cause.causedBy;
                }

                return layeStackTrace = builder.ToString();
            }
        }

        internal UnhandledLayeException(LayeStackTraceElement cause, string format, params object[] args)
            : base(string.Format(format, args))
        {
            this.cause = cause;
        }

        internal UnhandledLayeException(LayeStackTraceElement cause, string message)
            : base(message)
        {
            this.cause = cause;
        }
    }
}
