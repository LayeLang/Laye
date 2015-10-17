using Laye.Util;

namespace Laye.Proto
{
    internal sealed class LocalValueInfo
    {
        internal readonly string name;
        internal readonly uint location;

        internal bool IsOuter { get; private set; }

        internal LocalValueInfo(string name, uint location)
        {
            this.name = name;
            this.location = location;
        }

        internal void MarkAsOuter()
        {
            IsOuter = true;
        }

        public override int GetHashCode()
        {
            return HashHelper.GetHashCode(name, location);
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(this, obj))
                return true;
            var that = obj as LocalValueInfo;
            if (that == null)
                return false;
            return that.name == name && that.location == location;
        }
    }
}
