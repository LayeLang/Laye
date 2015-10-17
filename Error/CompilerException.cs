using System;

namespace Laye
{
    using Compilation;

    public sealed class CompilerException : Exception
    {
        public readonly DetailLogger log;

        internal CompilerException(DetailLogger log)
            : base(string.Format("Compilation failed with {0} errors and {1} warnings.", log.ErrorCount, log.WarningCount))
        {
            this.log = log;
        }
    }
}
