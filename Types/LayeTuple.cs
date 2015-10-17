using System.Collections;
using System.Collections.Generic;
using System.Text;

namespace Laye
{
    using static Laye;

    internal sealed class TupleTypeDef : ObjectTypeDef
    {
        public TupleTypeDef(LayeTypeDef type)
            : base(type)
        {
            type.PutPrefix("#", (LayeCallback)Prefix__count);
            // This uses the same enumerator as List, so we just steal it.
            type.PutInstanceProperty("enumerator", new LayeProperty((LayeCallback)((state, ths, args) => new ListEnumerator((ths as LayeTuple).values.GetEnumerator())), null));
            type.PutInstanceMethod("forEach", (LayeCallback)IMethod__forEach);
        }

        protected override LayeObject IPropertyGet__hashCode(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf((ths as LayeList).values.GetHashCode());
        }

        protected override LayeObject IMethod__toString(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            var values = (ths as LayeList).values;
            var builder = new StringBuilder();
            builder.Append('(');
            for (int i = 0; i < values.Count; i++)
            {
                if (i > 0)
                    builder.Append(", ");
                builder.Append(values[i].ToString(state));
            }
            return new LayeString(builder.Append(')').ToString());
        }

        private LayeObject Prefix__count(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf((ths as LayeList).Count);
        }

        private LayeObject IMethod__forEach(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (args.Length == 0)
                return NULL;
            var i = 0;
            var each = args[0];
            foreach (var value in (ths as LayeTuple))
                each.Invoke(state, value, LayeInt.ValueOf(i++));
            return NULL;
        }

        protected override LayeObject Infix__equalTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (ReferenceEquals(ths, args[0]))
                return TRUE;
            var arg = args[0] as LayeTuple;
            if (arg == null)
                return FALSE;
            var tuple = ths as LayeTuple;
            if (tuple.Count != arg.Count)
                return FALSE;
            var count = tuple.Count;
            for (var i = 0; i < count; i++)
                if (tuple.values[i].NotEqualTo(state, arg.values[i]))
                    return FALSE;
            return TRUE;
        }

        protected override LayeObject Infix__notEqualTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (ReferenceEquals(ths, args[0]))
                return FALSE;
            var arg = args[0] as LayeTuple;
            if (arg == null)
                return TRUE;
            var tuple = ths as LayeTuple;
            if (tuple.Count != arg.Count)
                return TRUE;
            var count = tuple.Count;
            for (var i = 0; i < count; i++)
                if (tuple.values[i].NotEqualTo(state, arg.values[i]))
                    return TRUE;
            return FALSE;
        }
    }

    public class LayeTuple : LayeObject, IEnumerable<LayeObject>
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("Tuple", false);

        static LayeTuple()
        {
            new TupleTypeDef(TYPE);
        }

        internal readonly List<LayeObject> values;
        public int Count { get { return values.Count; } }

        public override LayeObject this[LayeState state, params LayeObject[] args]
        {
            get
            {
                if (args.Length == 0)
                {
                    state.RaiseException("At least one argument is required when indexing an object.");
                    return NULL;
                }
                var index = args[0] as LayeInt;
                if (index == null)
                {
                    state.RaiseException("Attempt to index Tuple with {0}.", args[0].TypeName);
                    return NULL;
                }
                return values[(int)index.value];
            }
            set { state.RaiseException("Cannot modify the values of a Tuple. You might want to use a List."); }
        }

        public LayeTuple(params LayeObject[] values)
            : base(TYPE)
        {
            this.values = new List<LayeObject>();
            foreach (var value in values)
                this.values.Add(value);
        }

        public IEnumerator<LayeObject> GetEnumerator()
        {
            return ((IEnumerable<LayeObject>)values).GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return ((IEnumerable<LayeObject>)values).GetEnumerator();
        }
    }
}
