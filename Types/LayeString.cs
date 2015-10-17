namespace Laye
{
    using static Laye;

    internal sealed class StringTypeDef : ObjectTypeDef
    {
        public StringTypeDef(LayeTypeDef type)
            : base(type)
        {
            type.PutPrefix("#", (LayeCallback)((state, ths, args) => new LayeInt((ths as LayeString).value.Length)));
            type.PutInfix("*", (LayeCallback)Infix__repeat);
        }

        protected override LayeObject IPropertyGet__hashCode(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf((ths as LayeString).value.GetHashCode());
        }

        protected override LayeObject IMethod__toString(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return ths;
        }

        protected override LayeObject Infix__equalTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            var arg = args[0];
            if (!(arg is LayeString))
                return FALSE;
            return (LayeBool)((ths as LayeString).value == (arg as LayeString).value);
        }

        protected override LayeObject Infix__notEqualTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            var arg = args[0];
            if (!(arg is LayeString))
                return TRUE;
            return (LayeBool)((ths as LayeString).value != (arg as LayeString).value);
        }

        private LayeObject Infix__repeat(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            var arg = args[0] as LayeInt;
            if (arg == null)
            {
                state.RaiseException("Can only multiply a string by an integer value, got a(n) {0}.", args[0].TypeName);
                return NULL;
            }
            var count = arg.value;
            switch (count)
            {
                case 0: return EMPTY_STRING;
                case 1: return ths;
                case 2: return new LayeString((ths as LayeString).value + (ths as LayeString).value);
                default:
                    var value = (ths as LayeString).value;
                    var builder = new System.Text.StringBuilder();
                    for (var i = 0; i < count; i++)
                        builder.Append(value);
                    return new LayeString(builder.ToString());
            }
        }
    }

    public sealed class LayeString : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("String", true);

        static LayeString()
        {
            new StringTypeDef(TYPE);
        }

        public readonly string value;
        
        public LayeString(string value = "")
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
            return obj is LayeString && (value == (obj as LayeString).value);
        }
        #endregion ToString, GetHashCode, Equals
    }
}
