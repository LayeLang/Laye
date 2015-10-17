using System;

using System.Runtime.CompilerServices;

namespace Laye
{
#if LAYE64
    using lint = System.Int64;
    using ulint = System.UInt64;
    using lfloat = System.Double;
#else
    using lint = System.Int32;
    using ulint = System.UInt32;
    using lfloat = System.Single;
#endif

    using static Laye;

    internal sealed class IntTypeDef : ObjectTypeDef
    {
        public IntTypeDef(LayeTypeDef type)
            : base(type)
        {
            type.PutPrefix("#", (LayeCallback)Prefix__DigitCount);
        }

        protected override LayeObject IPropertyGet__hashCode(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return ths;
        }

        protected override LayeObject As__Bool(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return (LayeBool)((ths as LayeInt).value != 0);
        }

        protected override LayeObject IMethod__toString(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return new LayeString((ths as LayeInt).value.ToString());
        }

        protected override LayeObject Infix__equalTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (ReferenceEquals(ths, args[0]))
                return TRUE;
            var arg = args[0] as LayeInt;
            if (arg == null)
                return FALSE;
            return (LayeBool)((ths as LayeInt).value == arg.value);
        }

        protected override LayeObject Infix__notEqualTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (ReferenceEquals(ths, args[0]))
                return FALSE;
            var arg = args[0] as LayeInt;
            if (arg == null)
                return TRUE;
            return (LayeBool)((ths as LayeInt).value != arg.value);
        }

        private LayeObject Prefix__DigitCount(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf((ths as LayeInt).DigitCount);
        }
    }

    internal static class IntCache
    {
        internal const int MAX = 128, MIN = -127, SIZE = MAX - MIN;
        internal static readonly LayeInt[] CACHE = new LayeInt[SIZE];

        static IntCache()
        {
            for (int i = 0, v = MIN; i < SIZE; i++, v++)
                CACHE[i] = new LayeInt(v);
        }
    }

    public sealed class LayeInt : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("Int", true);

        static LayeInt()
        {
            new IntTypeDef(TYPE);
        }

#if SUGGEST_INLINING
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
#endif
        public static LayeInt ValueOf(lint value)
        {
#if LAYE64
            var index = (int)value;
#else
            var index = value;
#endif
            if (index >= IntCache.MIN && index < IntCache.MAX)
                return IntCache.CACHE[index - IntCache.MIN];
            return new LayeInt(value);
        }

        private int digitCount = 0;
        public int DigitCount
        {
            get
            {
                if (digitCount == 0)
                {
                    if (value == 0)
                        digitCount = 1;
                    else
                    {
                        var absValue = value < 0 ? 0 - value : value;
                        if (absValue < 10000)
                            do
                                digitCount++;
                            while ((absValue /= 10) >= 1);
                        else digitCount = (int)Math.Floor(Math.Log10(absValue) + 1);
                    }
                }
                return digitCount;
            }
        }

        public override bool IsNumeric { get { return true; } }

        public readonly lint value;

        public LayeInt(lint value)
            : base(TYPE)
        {
            this.value = value;
        }

        public override LayeObject Prefix(LayeState state, string op)
        {
            switch (op)
            {
                case "-": return ValueOf(-value);
            }
            return base.Prefix(state, op);
        }

        public override LayeObject Infix(LayeState state, string op, LayeObject that)
        {
            if (that.IsNumeric)
            {
                switch (op)
                {
                    case "&":
                        if (that is LayeInt)
                            return ValueOf(value & (that as LayeInt).value);
                        break;
                    case "|":
                        if (that is LayeInt)
                            return ValueOf(value | (that as LayeInt).value);
                        break;
                    case "~":
                        if (that is LayeInt)
                            return ValueOf(value ^ (that as LayeInt).value);
                        break;
                    case "<<":
                        if (that is LayeInt)
                            return ValueOf(value << (int)(that as LayeInt).value);
                        break;
                    case ">>":
                        if (that is LayeInt)
                            return ValueOf(value >> (int)(that as LayeInt).value);
                        break;
                    case ">>>":
                        if (that is LayeInt)
                            return ValueOf((lint)((ulint)value >> (int)(that as LayeInt).value));
                        break;
                    case "+":
                        if (that is LayeInt)
                            return ValueOf(value + (that as LayeInt).value);
                        else return new LayeFloat(value + (that as LayeFloat).value);
                    case "-":
                        if (that is LayeInt)
                            return ValueOf(value - (that as LayeInt).value);
                        else return new LayeFloat(value - (that as LayeFloat).value);
                    case "*":
                        if (that is LayeInt)
                            return ValueOf(value * (that as LayeInt).value);
                        else return new LayeFloat(value * (that as LayeFloat).value);
                    case "/":
                    case "//":
                        if (that is LayeInt)
                        {
                            var thatValue = (that as LayeInt).value;
                            if (thatValue == 0)
                            {
                                state.RaiseException("Attempt to divide by zero.");
                                return NULL;
                            }
                            return ValueOf(value / thatValue);
                        }
                        else
                        {
                            var thatValue = (that as LayeFloat).value;
                            if (thatValue == 0)
                            {
                                state.RaiseException("Attempt to divide by zero.");
                                return NULL;
                            }
                            return new LayeFloat(value / thatValue);
                        }
                    case "%":
                        if (that is LayeInt)
                        {
                            var thatValue = (that as LayeInt).value;
                            if (thatValue == 0)
                            {
                                state.RaiseException("Attempt to divide by zero.");
                                return NULL;
                            }
                            return ValueOf(value % thatValue);
                        }
                        else
                        {
                            var thatValue = (that as LayeFloat).value;
                            if (thatValue == 0)
                            {
                                state.RaiseException("Attempt to divide by zero.");
                                return NULL;
                            }
                            return new LayeFloat(value % thatValue);
                        }
                    case "^":
                        return new LayeFloat((lfloat)Math.Pow(value, (that as LayeFloat).value));
                    case "==":
                        if (that is LayeInt)
                            return value == (that as LayeInt).value ? TRUE : FALSE;
                        else return value == (that as LayeFloat).value ? TRUE : FALSE;
                    case "!=":
                        if (that is LayeInt)
                            return value != (that as LayeInt).value ? TRUE : FALSE;
                        else return value != (that as LayeFloat).value ? TRUE : FALSE;
                    case "<":
                        if (that is LayeInt)
                            return value < (that as LayeInt).value ? TRUE : FALSE;
                        else return value < (that as LayeFloat).value ? TRUE : FALSE;
                    case "<=":
                        if (that is LayeInt)
                            return value <= (that as LayeInt).value ? TRUE : FALSE;
                        else return value <= (that as LayeFloat).value ? TRUE : FALSE;
                    case ">":
                        if (that is LayeInt)
                            return value > (that as LayeInt).value ? TRUE : FALSE;
                        else return value > (that as LayeFloat).value ? TRUE : FALSE;
                    case ">=":
                        if (that is LayeInt)
                            return value >= (that as LayeInt).value ? TRUE : FALSE;
                        else return value >= (that as LayeFloat).value ? TRUE : FALSE;
                    default: break;
                }
            }
            return base.Infix(state, op, that);
        }
    }
}
