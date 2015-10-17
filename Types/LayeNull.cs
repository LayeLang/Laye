namespace Laye
{
    using static Laye;

    internal sealed class NullTypeDef : ObjectTypeDef
    {
        public NullTypeDef(LayeTypeDef type)
            : base(type)
        {
        }

        protected override LayeObject IPropertyGet__hashCode(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf(0);
        }

        protected override LayeObject As__Bool(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return FALSE;
        }

        protected override LayeObject IMethod__toString(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return new LayeString("null");
        }

        protected override LayeObject Infix__equalTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return ReferenceEquals(ths, args[0]) ? TRUE : FALSE;
        }

        protected override LayeObject Infix__notEqualTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return ReferenceEquals(ths, args[0]) ? FALSE : TRUE;
        }
    }

    /// <summary>
    /// Represents the 'null' literal in Laye.
    /// </summary>
    internal sealed class LayeNull : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("Null");

        static LayeNull()
        {
            new NullTypeDef(TYPE);
        }

        internal LayeNull()
            : base(TYPE)
        {
        }

        // TODO trigger null reference exceptions
    }
}
