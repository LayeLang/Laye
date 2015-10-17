using System;
using System.IO;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Text;

namespace Laye
{
    using static Laye;

    using Compilation;
    using Compilation.TokenGen;
    using Compilation.ASTGen;
    using Compilation.CodeGen;
    using Proto;

    internal sealed class KitTypeDef : ObjectTypeDef
    {
        public KitTypeDef(LayeTypeDef type)
            : base(type)
        {
        }
    }

    public class LayeKit : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("Kit", true);

        private static readonly Dictionary<string, LayeKit> compiledKits = new Dictionary<string, LayeKit>();

        static LayeKit()
        {
            new KitTypeDef(TYPE);
        }

        internal static DetailLogger Compile(string filePath, Encoding encoding, out LayeKit kit)
        {
            filePath = Path.GetFullPath(filePath);
            if (!File.Exists(filePath))
                throw new ArgumentException("filePath");

            if (compiledKits.ContainsKey(filePath))
            {
                kit = compiledKits[filePath];
                return null;
            }

            var log = new DetailLogger();

            var lexer = new Lexer(log);
            var tokens = lexer.GetTokens(filePath, encoding);

            if (log.ErrorCount != 0)
                throw new CompilerException(log);

            var parser = new Parser(log);
            var ast = parser.GenerateAST(tokens);

            if (log.ErrorCount != 0)
                throw new CompilerException(log);

            var compiler = new KitCompiler(log, Path.GetFileName(filePath));
            ast.Visit(compiler);

            if (log.ErrorCount != 0)
                throw new CompilerException(log);

            var proto = compiler.GetPrototype();
            kit = new LayeKit(Directory.GetParent(filePath).FullName, proto);

            compiledKits[filePath] = kit;

            return log;
        }

        public bool Sealed { get; private set; }

        /// <summary>
        /// Access the fields of this object.
        /// If a field does not exist, an exception is thrown.
        /// </summary>
        /// <param name="key"></param>
        /// <returns></returns>
        public override LayeObject this[LayeState state, string key, bool raiseExceptions = true]
        {
            get
            {
                if (fields.ContainsKey(key))
                    return fields[key];
                if (raiseExceptions)
                    state.RaiseException("No such field {0} in kit.", key);
                return NULL;
            }
            set
            {
                if (Sealed)
                {
                    if (fields.ContainsKey(key))
                        fields[key] = value;
                    else if (raiseExceptions)
                        state.RaiseException("No such field {0} in kit.", key);
                }
                else fields[key] = value;
            }
        }

        private readonly List<string> usedKits = new List<string>();
        private readonly Dictionary<string, LayeObject> globals = new Dictionary<string, LayeObject>();

        internal readonly string fileLocation;
        internal readonly LayeClosure body;

        public LayeKit()
            : base(TYPE)
        {
            body = null;
        }

        internal LayeKit(string fileLocation, FunctionPrototype proto)
            : base(TYPE)
        {
            this.fileLocation = fileLocation;
            body = new LayeClosure(this, proto);
        }

        internal void Use(LayeState state, LayeKit kit, string kitPath, string useAs = null)
        {
            if (usedKits.Contains(kitPath))
                return;
            if (useAs == null)
            {
                if (kitPath.Contains("."))
                    useAs = kitPath.Substring(kitPath.LastIndexOf(".") + 1);
                else useAs = kitPath;
            }
            SetGlobal(state, useAs, kit);
            usedKits.Add(kitPath);
        }

        internal void UseAllFrom(LayeState state, LayeKit kit)
        {
            foreach (var field in kit.fields)
                SetGlobal(state, field.Key, field.Value);
        }

        internal void UseFrom(LayeState state, LayeKit kit, params string[] fields)
        {
            foreach (var field in fields)
            {
                if (!kit.IsDefined(field))
                    ; // TODO error
                else SetGlobal(state, field, kit[state, field]);
            }
        }

        internal void Run(LayeState state)
        {
            // Sealed means we've run this kit.
            if (Sealed)
                return;
            // add the std kit:
            Use(state, state.std, "std");
            UseAllFrom(state, state.std);
            // Run!
            body.Invoke(state);
            // We're done here.
            Seal();
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal LayeObject GetGlobal(LayeState state, string key, bool raiseException = true)
        {
            if (!globals.ContainsKey(key))
            {
                if (raiseException)
                    state.RaiseException("Undefined symbol {0}.", key);
                return NULL;
            }
            return globals[key];
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        internal void SetGlobal(LayeState state, string key, LayeObject value)
        {
            globals[key] = value;
        }

        /// <summary>
        /// Seals this kit so that no new fields may be added to it.
        /// </summary>
        internal void Seal()
        {
            Sealed = true;
        }

        public bool IsDefined(string key)
        {
            return fields.ContainsKey(key);
        }

        /// <summary>
        /// Does not actually invoke a method.
        /// It finds a field with the given name and invokes it normally.
        /// </summary>
        /// <param name="state"></param>
        /// <param name="methodName"></param>
        /// <param name="args"></param>
        /// <returns></returns>
        public override LayeObject MethodInvoke(LayeState state, string methodName, params LayeObject[] args)
        {
            return this[state, methodName].Invoke(state, args);
        }
    }
}
