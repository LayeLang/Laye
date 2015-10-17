using System;

namespace Laye
{
#if LAYE64
    using lint = System.Int64;
    using lfloat = System.Double;
#else
    using lint = System.Int32;
    using lfloat = System.Single;
#endif

    using static Laye;

    internal sealed class FloatTypeDef : ObjectTypeDef
    {
        public FloatTypeDef(LayeTypeDef type)
            : base(type)
        {
            type.PutAsCast(LayeInt.TYPE, (LayeCallback)As__Int);
        }

        protected override LayeObject IPropertyGet__hashCode(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf((ths as LayeFloat).value.GetHashCode());
        }

        protected override LayeObject As__Bool(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return (LayeBool)((ths as LayeFloat).value != 0);
        }

        private LayeObject As__Int(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf((lint)(ths as LayeFloat).value);
        }

        protected override LayeObject IMethod__toString(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return new LayeString((ths as LayeFloat).value.ToString());
        }

        protected override LayeObject Infix__equalTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (ReferenceEquals(ths, args[0]))
                return TRUE;
            var arg = args[0] as LayeFloat;
            if (arg == null)
                return FALSE;
            return (LayeBool)((ths as LayeFloat).value == arg.value);
        }

        protected override LayeObject Infix__notEqualTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (ReferenceEquals(ths, args[0]))
                return FALSE;
            var arg = args[0] as LayeFloat;
            if (arg == null)
                return TRUE;
            return (LayeBool)((ths as LayeFloat).value != arg.value);
        }
    }

    public sealed class LayeFloat : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("Float", true);

        static LayeFloat()
        {
            new FloatTypeDef(TYPE);
        }

        public override bool IsNumeric { get { return true; } }

        public readonly lfloat value;

        public LayeFloat(lfloat value)
            : base(TYPE)
        {
            this.value = value;
        }

        public override LayeObject Infix(LayeState state, string op, LayeObject that)
        {
            if (that.IsNumeric)
            {
                lfloat thatValue = that is LayeInt ? (that as LayeInt).value : (that as LayeFloat).value;
                switch (op)
                {
                    case "+": return new LayeFloat(value + thatValue);
                    case "-": return new LayeFloat(value - thatValue);
                    case "*": return new LayeFloat(value * thatValue);
                    case "/":
                        if (thatValue == 0)
                        {
                            state.RaiseException("Attempt to divide by zero.");
                            return NULL;
                        }
                        return new LayeFloat(value / thatValue);
                    case "//":
                        if (thatValue == 0)
                        {
                            state.RaiseException("Attempt to divide by zero.");
                            return NULL;
                        }
                        return LayeInt.ValueOf((lint)(value / thatValue));
                    case "%":
                        if (thatValue == 0)
                        {
                            state.RaiseException("Attempt to divide by zero.");
                            return NULL;
                        }
                        return new LayeFloat(value % thatValue);
                    case "^": return new LayeFloat((lfloat)Math.Pow(value, thatValue));
                    case "==": return value == thatValue ? TRUE : FALSE;
                    case "!=": return value != thatValue ? TRUE : FALSE;
                    case "<": return value < thatValue ? TRUE : FALSE;
                    case "<=": return value <= thatValue ? TRUE : FALSE;
                    case ">": return value > thatValue ? TRUE : FALSE;
                    case ">=": return value >= thatValue ? TRUE : FALSE;
                    default: break;
                }
            }
            return base.Infix(state, op, that);
        }
    }
}
