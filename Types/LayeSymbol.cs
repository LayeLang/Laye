using System;
using System.Collections.Generic;

namespace Laye
{
    using Util;

    using static Laye;

    internal sealed class SymbolTypeDef : ObjectTypeDef
    {
        public SymbolTypeDef(LayeTypeDef type)
            : base(type)
        {
            type.PutAsCast(LayeString.TYPE, (LayeCallback)((state, ths, args) => new LayeString((ths as LayeSymbol).value)));
        }

        protected override LayeObject IPropertyGet__hashCode(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf((ths as LayeSymbol).value.GetHashCode());
        }

        protected override LayeObject IMethod__toString(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return new LayeString(ths.ToString());
        }

        protected override LayeObject Infix__equalTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            var arg = args[0];
            if (ReferenceEquals(this, arg))
                return TRUE;
            if (!(arg is LayeSymbol))
                return FALSE;
            return (LayeBool)((ths as LayeSymbol).value == (arg as LayeSymbol).value);
        }

        protected override LayeObject Infix__notEqualTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            var arg = args[0];
            if (ReferenceEquals(this, arg))
                return FALSE;
            if (!(arg is LayeSymbol))
                return TRUE;
            return (LayeBool)((ths as LayeSymbol).value != (arg as LayeSymbol).value);
        }
    }

    public sealed class LayeSymbol : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("Symbol", true);

        private static readonly Dictionary<string, LayeSymbol> symbols = new Dictionary<string, LayeSymbol>();

        static LayeSymbol()
        {
            new SymbolTypeDef(TYPE);
        }

        public static implicit operator LayeSymbol(string value)
        {
            return get(value);
        }

        public static LayeSymbol get(string value)
        {
            if (!IsIdentifier(value))
                throw new ArgumentException(string.Format("'{0}' is not a valid Laye identifier.", value));
            return symbols.GetOrCreateValue(value, () => new LayeSymbol(value));
        }

        internal static LayeSymbol getUnsafe(string value)
        {
            return symbols.GetOrCreateValue(value, () => new LayeSymbol(value));
        }

        public readonly string value;
        
        private LayeSymbol(string value)
            : base(TYPE)
        {
            this.value = value;
        }

        #region ToString, GetHashCode, Equals
        public override string ToString()
        {
            return value;
        }

        public override int GetHashCode()
        {
            return value.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            return ReferenceEquals(this, obj) || (obj is LayeSymbol && (value == (obj as LayeSymbol).value));
        }
        #endregion ToString, GetHashCode, Equals
    }
}
