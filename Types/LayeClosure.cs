using System;

namespace Laye
{
    using Proto;

    using static Laye;

    internal sealed class FunctionTypeDef : ObjectTypeDef
    {
        public FunctionTypeDef(LayeTypeDef type)
            : base(type)
        {
        }
    }

    public sealed class LayeClosure : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("Function", true);

        static LayeClosure()
        {
            new FunctionTypeDef(TYPE);
        }

        /// <summary>
        /// The kit this closure was defined in.
        /// </summary>
        public readonly LayeKit kit;

        internal readonly FunctionPrototype proto;
        internal readonly OuterValue[] outers;
        
        /// <summary>
        /// The type this closure is defined it, if any.
        /// This is used with the 'static' keyword in Laye.
        /// </summary>
        internal readonly LayeObject definedType;

        internal readonly LayeObject[] defaults;

        internal LayeClosure(LayeKit kit, FunctionPrototype proto, LayeTypeDef definedType = null, LayeObject[] defaults = null)
            : base(TYPE)
        {
            if (kit == null)
                throw new ArgumentNullException("kit");
            this.kit = kit;
            this.proto = proto;
            outers = new OuterValue[proto.outers.Length];
            this.definedType = definedType == null ? NULL : definedType;

            var numParams = proto.hasVargs ? proto.numParams - 1 : proto.numParams;
            this.defaults = new LayeObject[numParams];
            if (defaults == null || defaults.Length == 0)
                for (int i = 0; i < numParams; i++)
                    this.defaults[i] = NULL;
            else
            {
                for (uint i = 0; i < numParams - defaults.Length; i++)
                    this.defaults[i] = NULL;
                for (uint i = numParams - (uint)defaults.Length, j = 0; i < numParams; i++, j++)
                    this.defaults[i] = defaults[j];
            }
        }

        public override LayeObject Invoke(LayeState state, params LayeObject[] args)
        {
            return state.Execute(null, this, NULL, args);
        }

        public override LayeObject InvokeAsMethod(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return state.Execute(null, this, ths, args);
        }
    }
}
