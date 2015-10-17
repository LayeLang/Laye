using System.IO;

namespace Laye.Compilation
{
    internal sealed class Location
    {
        /// <summary>
        /// The name of the file this location is in.
        /// </summary>
        internal readonly string file;
        /// <summary>
        /// The line this location starts at, starting from 1.
        /// </summary>
        internal readonly uint line;
        /// <summary>
        /// The column this location starts at, starting from 1.
        /// </summary>
        internal readonly uint col;
        /// <summary>
        /// The line this location ends at, starting from 1.
        /// </summary>
        internal readonly uint endLine;
        /// <summary>
        /// The column this location ends at, starting from 1.
        /// </summary>
        internal readonly uint endCol;

        /// <summary>
        /// Create a new location in the given file at the line and column.
        /// </summary>
        /// <param name="file"></param>
        /// <param name="line"></param>
        /// <param name="col"></param>
        internal Location(string file, uint line, uint col, uint endLine = 0, uint endCol = 0)
        {
            this.file = file;
            this.line = line;
            this.col = col;
            this.endLine = endLine == 0 ? line : endLine;
            this.endCol = endCol == 0 ? col + 1 : endCol;
        }

        public override string ToString()
        {
            return string.Format("{0} (line {1}, column {2})", Path.GetFileName(file), line, col);
        }
    }
}
