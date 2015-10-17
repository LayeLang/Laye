using System.Runtime.CompilerServices;

namespace Laye
{
    using static Laye;

    internal sealed class BoolTypeDef : ObjectTypeDef
    {
        public BoolTypeDef(LayeTypeDef type)
            : base(type)
        {
        }

        protected override LayeObject IPropertyGet__hashCode(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf((ths as LayeBool).value.GetHashCode());
        }

        protected override LayeObject As__Bool(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return ths;
        }

        protected override LayeObject IMethod__toString(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return new LayeString((ths as LayeBool).value ? "true" : "false");
        }

        protected override LayeObject Infix__equalTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (ReferenceEquals(ths, args[0]))
                return TRUE;
            var arg = args[0] as LayeBool;
            if (arg == null)
                return FALSE;
            return (LayeBool)((ths as LayeBool).value == arg.value);
        }

        protected override LayeObject Infix__notEqualTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (ReferenceEquals(ths, args[0]))
                return FALSE;
            var arg = args[0] as LayeBool;
            if (arg == null)
                return TRUE;
            return (LayeBool)((ths as LayeBool).value != arg.value);
        }
    }

    public sealed class LayeBool : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("Bool", true);

        static LayeBool()
        {
            new BoolTypeDef(TYPE);
        }

        #region Implicit Conversions
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static implicit operator LayeBool(bool value)
        {
            return value ? (LayeBool)TRUE : (LayeBool)FALSE;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static implicit operator bool(LayeBool value)
        {
            return value.value;
        }
        #endregion Implicit Conversions

        public readonly bool value;

        internal LayeBool(bool value)
            : base(TYPE)
        {
            this.value = value;
        }
    }
}
