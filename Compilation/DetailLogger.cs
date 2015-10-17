using System;
using System.Collections.Generic;
using System.Text;

namespace Laye.Compilation
{
    internal sealed class Detail
    {
        /// <summary>
        /// Used to determine the type of a detail.
        /// </summary>
        internal enum Type
        {
            INFO,
            WARNING,
            ERROR,
        }

        internal readonly Type type;
        /// <summary>
        /// Where this detail occurs.
        /// </summary>
        internal readonly Location location;
        /// <summary>
        /// The message given for this detail.
        /// </summary>
        internal readonly string message;

        internal Detail(Type type, Location location, string message)
        {
            this.type = type;
            this.location = location;
            this.message = message;
        }
    }

    public sealed class DetailLogger
    {
        /// <summary>
        /// Stores all the details logged in the order they were logged.
        /// </summary>
        private readonly List<Detail> details = new List<Detail>();

        public uint ErrorCount { get; private set; } = 0;
        public uint WarningCount { get; private set; } = 0;

        internal DetailLogger()
        {
        }

        // TODO accept an output target
        public void Print()
        {
            details.ForEach(detail =>
            {
                // TODO make this way better plz
                var builder = new StringBuilder();
                builder.Append("[");
                builder.Append(detail.type.ToString());
                builder.Append("] ");
                builder.Append(detail.location.ToString());
                builder.Append(": ");
                builder.Append(detail.message);
                Console.WriteLine(builder.ToString());
            });
        }

        /// <summary>
        /// Logs an info message at the given location.
        /// </summary>
        /// <param name="location"></param>
        /// <param name="message"></param>
        internal void Log(Location location, string message)
        {
            details.Add(new Detail(Detail.Type.INFO, location, message));
        }

        /// <summary>
        /// Logs an info message, after formatting, at the given location.
        /// </summary>
        /// <param name="location"></param>
        /// <param name="message"></param>
        internal void Log(Location location, string format, params object[] args)
        {
            details.Add(new Detail(Detail.Type.INFO, location, string.Format(format, args)));
        }

        /// <summary>
        /// Logs a warning mesage at the given location.
        /// </summary>
        /// <param name="location"></param>
        /// <param name="message"></param>
        internal void Warn(Location location, string message)
        {
            WarningCount++;
            details.Add(new Detail(Detail.Type.WARNING, location, message));
        }

        /// <summary>
        /// Logs a warning message, after formatting, at the given location.
        /// </summary>
        /// <param name="location"></param>
        /// <param name="message"></param>
        internal void Warn(Location location, string format, params object[] args)
        {
            WarningCount++;
            details.Add(new Detail(Detail.Type.WARNING, location, string.Format(format, args)));
        }

        /// <summary>
        /// Logs an error message at the given location.
        /// </summary>
        /// <param name="location"></param>
        /// <param name="message"></param>
        internal void Error(Location location, string message)
        {
            ErrorCount++;
            details.Add(new Detail(Detail.Type.ERROR, location, message));
        }

        /// <summary>
        /// Logs an error message, after formatting, at the given location.
        /// </summary>
        /// <param name="location"></param>
        /// <param name="message"></param>
        internal void Error(Location location, string format, params object[] args)
        {
            ErrorCount++;
            details.Add(new Detail(Detail.Type.ERROR, location, string.Format(format, args)));
        }
    }
}
