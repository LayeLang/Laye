using System.Collections.Generic;

namespace Laye.Library
{
    public sealed class LayeLibrary
    {
        public readonly string name;
        internal readonly Dictionary<string, LayeObject> entries = new Dictionary<string, LayeObject>();

        public LayeObject this[string key]
        {
            get { return entries[key]; }
            set { entries[key] = value; }
        }

        public LayeLibrary(string name)
        {
            this.name = name;
        }
    }
}
