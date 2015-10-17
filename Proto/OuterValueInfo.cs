using Laye.Util;

namespace Laye.Proto
{
    internal enum OuterValueType
    {
        LOCAL, OUTER
    }

    internal sealed class OuterValueInfo
    {
        internal readonly string name;
        internal readonly OuterValueType type;
        internal uint location;

        internal OuterValueInfo(string name, OuterValueType type, uint location)
        {
            this.name = name;
            this.type = type;
            this.location = location;
        }

        public override int GetHashCode()
        {
            return HashHelper.GetHashCode(name, type, location);
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(this, obj))
                return true;
            var that = obj as OuterValueInfo;
            if (that == null)
                return false;
            return that.name == name && that.type == type && that.location == location;
        }
    }
}
