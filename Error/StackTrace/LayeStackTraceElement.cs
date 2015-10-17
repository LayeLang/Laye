namespace Laye.StackTrace
{
    public sealed class LayeStackTraceElement
    {
        public readonly string file;
        public readonly uint line;
        public readonly LayeStackTraceElement causedBy;

        internal LayeStackTraceElement(string file, uint line, LayeStackTraceElement causedBy = null)
        {
            this.file = file;
            this.line = line;
            this.causedBy = causedBy;
        }
    }
}
