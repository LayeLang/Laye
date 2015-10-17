using System;
using System.IO;
using System.Text;

namespace Laye.Kits
{
    using static Laye;

    public sealed class KitStd : LayeKit
    {
        private TextWriter stdout = Console.Out;
        public TextWriter StdOut
        {
            get { return stdout; }
            set { if (value == null) throw new ArgumentNullException(); stdout = value; }
        }

        internal KitStd(LayeState state)
        {
            // std types
            LayeTypeDef[] types = { LayeBool.TYPE, LayeClosure.TYPE, LayeFloat.TYPE, LayeGenerator.TYPE, LayeInt.TYPE, LayeKit.TYPE,
                LayeList.TYPE, LayeObject.TYPE, LayeString.TYPE, LayeSymbol.TYPE, LayeTuple.TYPE, LayeTypeDef.TYPE };
            foreach (var type in types)
                this[state, type.name] = type;

            this[state, "print"] = (LayeCallback)Callback__print;
            this[state, "println"] = (LayeCallback)Callback__println;
            this[state, "require"] = (LayeCallback)Callback__require;
        }

        private LayeObject Callback__print(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            var builder = new StringBuilder();
            for (var i = 0; i < args.Length; i++)
            {
                if (i > 0)
                    builder.Append(" ");
                var value = args[i];
                builder.Append(value.ToString(state));
            }
            stdout.Write(builder.ToString());
            return NULL;
        }

        private LayeObject Callback__println(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            var builder = new StringBuilder();
            for (var i = 0; i < args.Length; i++)
            {
                if (i > 0)
                    builder.Append(" ");
                var value = args[i];
                builder.Append(value.ToString(state));
            }
            stdout.WriteLine(builder.ToString());
            return NULL;
        }

        private LayeObject Callback__require(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            // Vars we need:
            LayeKit thisKit = null;
            string originalRequirePath = null;

            // Try to get these:
            if (args.Length < 2)
                throw new ArgumentException("args"); // TODO throw an exception in the state.

            thisKit = args[0] as LayeKit;
            originalRequirePath = (args[1] as LayeString)?.value;

            // Check if we succeeded:
            if (thisKit == null || originalRequirePath == null)
                throw new ArgumentException("kit || requirePath"); // TODO throw an exception in the state.

            // Get the name of the kit:

            // Load up the kit, or error if no kit can be found:
            LayeKit kit;
            switch (originalRequirePath)
            {
                case "std": kit = state.std; break;
                default:
                    // Determine the actual path to require:
                    var requirePath = originalRequirePath.Replace('.', '\\') + ".laye";
                    var newRequirePath = Path.Combine(thisKit.fileLocation, requirePath);
                    if (File.Exists(newRequirePath))
                    {
                        // Compile the kit and run it:
                        try
                        {
                            Compile(newRequirePath, null, out kit);
                        }
                        catch (CompilerException e)
                        {
                            e.log.Print();
                            throw e;
                        }
                        kit.Run(state);
                    }
                    else
                    {
                        state.RaiseException("Failed to load kit '{0}'.", originalRequirePath);
                        return NULL;
                    }
                    break;
            }

            // Load that kit into this kits global state:
            thisKit.Use(state, kit, originalRequirePath);

            // This is void.
            return NULL;
        }
    }
}
