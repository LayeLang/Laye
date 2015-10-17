using Laye.Compilation.CodeGen;
using System;
using System.IO;
using System.Text;

namespace Laye
{
#if LAYE64
    using lint = System.Int64;
    using lfloat = System.Double;
#else
    using lint = System.Int32;
    using lfloat = System.Single;
#endif

    public class LayeASM
    {
        #region Checks
        static void CheckEmpty(string line)
        {
            if (line.Length != 0)
                throw new ArgumentException(line);
        }

        static void CheckNotEmpty(string line)
        {
            if (line.Length == 0)
                throw new ArgumentException(line);
        }

        static void CheckSymbol(string line)
        {
            if (line.Length == 0)
                throw new ArgumentException(line);
            // TODO more checks
        }

        static void CheckOperator(string line)
        {
            if (line.Length == 0)
                throw new ArgumentException(line);
            // TODO more checks
        }
        #endregion Checks

        public readonly string filePath;

        public LayeASM(string filePath)
        {
            this.filePath = filePath;
        }

        public void Run()
        {
            var builder = new FunctionBuilder();
            builder.fileName = Path.GetFileName(filePath);

            var lines = File.ReadAllLines(filePath);
            for (uint lineNum = 0; lineNum < lines.Length; lineNum++)
            {
                var line = lines[lineNum].Trim();
                if (line.Length == 0 || line.StartsWith(";"))
                    continue;

                // make sure the builder knows what line we're on, woo.
                builder.currentLineNumber = lineNum + 1;

                var command = (line.Contains(" ") ? line.Substring(0, line.IndexOf(' ')) : line).ToLower();
                var rest = line.Substring(command.Length).Trim();

                if (command.StartsWith("."))
                {
                    switch (command.Substring(1))
                    {
                        case "fn":
                            var paramNames = rest.Split(' ');
                            builder = new FunctionBuilder(builder);
                            builder.currentLineNumber = lineNum + 1;
                            for (int i = 0; i < paramNames.Length; i++)
                                builder.AddParameter(paramNames[i]);
                            continue;
                        case "endfn":
                            var proto = builder.Build();
                            builder = builder.parent;
                            builder.OpClosure(proto);
                            continue;
                        default: Console.WriteLine(command); throw new ArgumentException();
                    }
                }
                else
                {
                    switch (command)
                    {
                        case "nop": CheckEmpty(rest); builder.OpNop(); continue;
                        case "pop": CheckEmpty(rest); builder.OpPop(); continue;
                        case "dup": CheckEmpty(rest); builder.OpDup(); continue;

                        case "jump": builder.OpJump((uint)(builder.InsnCount + int.Parse(rest) + 1)); continue;
                        case "jumpeq": builder.OpJumpEq((uint)(builder.InsnCount + int.Parse(rest) + 1)); continue;
                        case "jumpneq": builder.OpJumpNeq((uint)(builder.InsnCount + int.Parse(rest) + 1)); continue;
                        case "jumpt": builder.OpJumpT((uint)(builder.InsnCount + int.Parse(rest) + 1)); continue;
                        case "jumpf": builder.OpJumpF((uint)(builder.InsnCount + int.Parse(rest) + 1)); continue;

                        case "lload":
                        {
                            CheckSymbol(rest);
                            uint location;
                            if (builder.GetLocalLocation(rest, out location))
                                builder.OpLLoad(location);
                            else throw new ArgumentException();
                        } continue;
                        case "lstore":
                        {
                            CheckSymbol(rest);
                            uint location;
                            if (builder.GetLocalLocation(rest, out location))
                                builder.OpLStore(location);
                            else throw new ArgumentException();
                        } continue;
                        case "oload":
                        {
                            CheckSymbol(rest);
                            uint location;
                            if (builder.GetOuterLocation(rest, out location))
                                builder.OpOLoad(location);
                            else throw new ArgumentException();
                        } continue;
                        case "ostore":
                        {
                            CheckSymbol(rest);
                            uint location;
                            if (builder.GetOuterLocation(rest, out location))
                                builder.OpOStore(location);
                            else throw new ArgumentException();
                        } continue;
                        case "kload": CheckSymbol(rest); builder.OpKLoad(rest); continue;
                        case "kstore": CheckSymbol(rest); builder.OpKStore(rest); continue;
                        case "gload": CheckSymbol(rest); builder.OpGLoad(rest); continue;
                        case "gstore": CheckSymbol(rest); builder.OpGStore(rest); continue;
                        case "iload": CheckEmpty(rest); builder.OpILoad(uint.Parse(rest)); continue;
                        case "istore": CheckEmpty(rest); builder.OpIStore(uint.Parse(rest)); continue;
                        case "fload": CheckSymbol(rest); builder.OpFLoad(rest); continue;
                        case "fstore": CheckSymbol(rest); builder.OpFStore(rest); continue;
                        case "load": CheckSymbol(rest); builder.OpLoad(rest); continue;
                        case "store": CheckSymbol(rest); builder.OpStore(rest); continue;

                        case "null": CheckEmpty(rest); builder.OpNull(); continue;
                        case "bconst": builder.OpBConst(bool.Parse(rest)); continue;

                        case "iconst": builder.OpIConst(Laye.ConvertToInt(rest)); continue;
                        case "fconst": builder.OpFConst(Laye.ConvertToFloat(rest)); continue;

                        case "invoke": builder.OpInvoke(uint.Parse(rest)); continue;

                        case "prefix": CheckOperator(rest); builder.OpPrefix(rest); continue;
                        case "infix": CheckOperator(rest); builder.OpInfix(rest); continue;

                        case "compis": CheckEmpty(rest); builder.OpCompIs(); continue;
                        case "compnotis": CheckEmpty(rest); builder.OpCompNotIs(); continue;
                        case "comptypeof": CheckEmpty(rest); builder.OpCompTypeOf(); continue;
                        case "compnottypeof": CheckEmpty(rest); builder.OpCompNotTypeOf(); continue;
                        case "typeof": CheckEmpty(rest); builder.OpTypeOf(); continue;

                        default: Console.WriteLine(command); throw new ArgumentException();
                    }
                }
            }

            var kit = new LayeKit();
            var closure = new LayeClosure(kit, builder.Build());
            var lstate = new LayeState();
            /*
            lstate["println"] = (LayeCallback)((state, args) =>
            {
                var sbuilder = new StringBuilder();
                for (int i = 0; i < args.Length; i++)
                {
                    if (i > 0)
                        sbuilder.Append(" ");
                    sbuilder.Append(args[i].ToString());
                }
                Console.WriteLine(sbuilder);
                return LayeObject.NULL;
            }); */
            closure.Invoke(lstate);
        }
    }
}
