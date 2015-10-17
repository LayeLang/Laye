using System.Text;

namespace Laye
{
    using Library;

    /// <summary>
    /// Represents a Laye script.
    /// </summary>
    public class LayeScript
    {
        public static LayeScript Compile(string fileName, Encoding encoding = null)
        {
            LayeKit kit;
            LayeKit.Compile(fileName, encoding, out kit);
            return new LayeScript(kit, new LayeState());
        }

        private readonly LayeKit kit;
        public readonly LayeState state;

        public LayeObject this[string key]
        {
            get
            {
                if (kit.IsDefined(key))
                    return kit[state, key];
                return kit.GetGlobal(state, key, false);
            }
            set
            {
                if (kit.IsDefined(key))
                    kit[state, key] = value;
                else kit.SetGlobal(state, key, value);
            }
        }

        private LayeScript(LayeKit kit, LayeState state)
        {
            this.kit = kit;
            this.state = state;
        }

        /// <summary>
        /// Execute this script's kit.
        /// </summary>
        public void Run()
        {
            kit.Run(state);
        }

        public void Open(LayeLibrary lib)
        {
            var libKit = new LayeKit();
            foreach (var entry in lib.entries)
                libKit[state, entry.Key] = entry.Value;
            kit.Use(state, libKit, lib.name);
        }

        /// <summary>
        /// Puts a kit in the global namespace if a kit with the same name doesn't already exist.
        /// </summary>
        /// <param name="name"></param>
        /// <param name="kit"></param>
        public void UseKit(string name, LayeKit kit)
        {
            this.kit.Use(state, kit, name);
        }

        /// <summary>
        /// Puts a value into the global namespace, overriding any other global with the same name.
        /// </summary>
        /// <param name="name"></param>
        /// <param name="obj"></param>
        public void UseValue(string name, LayeObject obj)
        {
            kit.SetGlobal(state, name, obj);
        }
    }
}
