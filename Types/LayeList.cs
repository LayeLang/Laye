using System.Collections;
using System.Collections.Generic;
using System.Text;

namespace Laye
{
    using static Laye;

    internal sealed class ListEnumerator : LayeGenerator
    {
        private IEnumerator<LayeObject> iter;

        internal ListEnumerator(IEnumerator<LayeObject> iter)
        {
            this.iter = iter;
        }

        public override bool Resume(LayeState state, out LayeObject result)
        {
            if (!TryResume(state))
            {
                result = null;
                return false;
            }
            if (!iter.MoveNext())
                result = null;
            else result = iter.Current;
            Suspend();
            return true;
        }
    }

    internal sealed class ListTypeDef : ObjectTypeDef
    {
        public ListTypeDef(LayeTypeDef type)
            : base(type)
        {
            type.PutPrefix("#", (LayeCallback)Prefix__count);
            type.PutInstanceProperty("enumerator", new LayeProperty((LayeCallback)((state, ths, args) => new ListEnumerator((ths as LayeList).values.GetEnumerator())), null));
            type.PutInstanceOperatorIndex("+", new LayeProperty(null, (LayeCallback)IOperatorIndex__push));
            type.PutInstanceOperatorIndex("-", new LayeProperty((LayeCallback)IOperatorIndex__pop, null));
            type.PutInstanceMethod("forEach", (LayeCallback)IMethod__forEach);
            type.PutInstanceMethod("forIEach", (LayeCallback)IMethod__forIEach);
        }

        protected override LayeObject IPropertyGet__hashCode(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf((ths as LayeList).values.GetHashCode());
        }

        protected override LayeObject IMethod__toString(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            var values = (ths as LayeList).values;
            var builder = new StringBuilder();
            builder.Append('[');
            for (int i = 0; i < values.Count; i++)
            {
                if (i > 0)
                    builder.Append(", ");
                builder.Append(values[i].ToString(state));
            }
            return new LayeString(builder.Append(']').ToString());
        }

        private LayeObject Prefix__count(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf((ths as LayeList).Count);
        }

        private LayeObject IOperatorIndex__push(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (args.Length == 0)
                return NULL;
            var list = ths as LayeList;
            foreach (var arg in args)
                list.Add(state, arg);
            return NULL;
        }

        private LayeObject IOperatorIndex__pop(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return (ths as LayeList).Pop(state);
        }

        private LayeObject IMethod__forEach(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (args.Length == 0)
                return NULL;
            var each = args[0];
            foreach (var value in (ths as LayeList))
                each.Invoke(state, value);
            return NULL;
        }

        private LayeObject IMethod__forIEach(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (args.Length == 0)
                return NULL;
            var i = 0;
            var each = args[0];
            foreach (var value in (ths as LayeList))
                each.Invoke(state, LayeInt.ValueOf(i++), value);
            return NULL;
        }

        protected override LayeObject Infix__equalTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (ReferenceEquals(ths, args[0]))
                return TRUE;
            var arg = args[0] as LayeList;
            if (arg == null)
                return FALSE;
            var list = ths as LayeList;
            if (list.Count != arg.Count)
                return FALSE;
            var count = list.Count;
            for (var i = 0; i < count; i++)
                if (list.values[i].NotEqualTo(state, arg.values[i]))
                    return FALSE;
            return TRUE;
        }

        protected override LayeObject Infix__notEqualTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            if (ReferenceEquals(ths, args[0]))
                return FALSE;
            var arg = args[0] as LayeList;
            if (arg == null)
                return TRUE;
            var list = ths as LayeList;
            if (list.Count != arg.Count)
                return TRUE;
            var count = list.Count;
            for (var i = 0; i < count; i++)
                if (list.values[i].NotEqualTo(state, arg.values[i]))
                    return TRUE;
            return FALSE;
        }
    }

    public class LayeList : LayeObject, IEnumerable<LayeObject>
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("List", false);

        static LayeList()
        {
            new ListTypeDef(TYPE);
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
                    state.RaiseException("Attempt to index List with {0}.", args[0].TypeName);
                    return NULL;
                }
                return values[(int)index.value];
            }
            set
            {
                if (args.Length == 0)
                {
                    state.RaiseException("At least one argument is required when indexing an object.");
                    return;
                }
                var index = args[0] as LayeInt;
                if (index == null)
                {
                    state.RaiseException("Attempt to index List with {0}.", args[0].TypeName);
                    return;
                }
                values[(int)index.value] = value;
            }
        }

        public LayeList(params LayeObject[] values)
            : base(TYPE)
        {
            this.values = new List<LayeObject>();
            foreach (var value in values)
                this.values.Add(value);
        }

        public void Add(LayeState state, LayeObject obj)
        {
            if (obj == null)
                state.RaiseException("No object given to add.");
            else values.Add(obj);
        }

        public LayeObject Pop(LayeState state)
        {
            var result = values[values.Count - 1];
            values.RemoveAt(values.Count - 1);
            return result;
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
