using System;
using System.Collections.Generic;

namespace Laye
{
    using Util;

    public sealed class LayeProperty
    {
        #region Builders
        internal static LayeProperty FromGet(LayeObject get)
        {
            return new LayeProperty(get, null);
        }

        public static LayeProperty FromSet(LayeObject set)
        {
            return new LayeProperty(null, set);
        }
        #endregion Builders

        internal readonly LayeObject get, set;

        public LayeProperty(LayeObject get, LayeObject set)
        {
            this.get = get;
            this.set = set;
        }
    }

    public class LayeTypeDef : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef();

        #region DANGER PLS
        /// <summary>
        /// DO NOT USE THIS ANYWHERE EXCEPT THE <code>TYPE</code> CONSTANT.
        /// THIS IS FOR THE TYPE OF TYPEDEFS.
        /// </summary>
        /// <param name="type"></param>
        private LayeTypeDef()
            : base(null)
        {
            name = "Type";
            isSealed = true;
            PutInstanceMethod("toString", (LayeCallback)((state, ths, args) => new LayeString(ToString())));
        }
        #endregion DANGER PLS

        public readonly string name;
        public readonly bool isSealed;

        #region Instance Data
        private LayeObject invoke = null;
        private readonly List<string> instanceFields = new List<string>();
        private readonly Dictionary<string, LayeObject> ctors = new Dictionary<string, LayeObject>();
        //private LayeProperty instanceIndex = null;
        private readonly Dictionary<string, LayeProperty> instanceProperties = new Dictionary<string, LayeProperty>();
        private readonly Dictionary<string, LayeProperty> instanceOperatorIndex = new Dictionary<string, LayeProperty>();
        private readonly Dictionary<LayeTypeDef, LayeObject> asCasts = new Dictionary<LayeTypeDef, LayeObject>();
        private readonly Dictionary<string, LayeObject> instanceMethods = new Dictionary<string, LayeObject>();
        private readonly Dictionary<string, LayeObject> prefix = new Dictionary<string, LayeObject>();
        private readonly Dictionary<string, LayeObject> infix = new Dictionary<string, LayeObject>();
        #endregion Instance Data

        internal LayeTypeDef(LayeTypeDef type, string name, bool isSealed = false)
            : base(type)
        {
            if (name == null)
                throw new ArgumentNullException("name");
            this.name = name;
            this.isSealed = isSealed;
            PutInstanceMethod("toString", (LayeCallback)((state, ths, args) => new LayeString(ToString())));
        }

        public LayeTypeDef(string name, bool isSealed = false)
            : this(TYPE, name, isSealed)
        {
        }

        #region ToString, GetHashCode, Equals
        public override string ToString()
        {
            return name;
        }

        public override int GetHashCode()
        {
            return HashHelper.GetHashCode(name);
        }

        public override bool Equals(object obj)
        {
            return ReferenceEquals(this, obj);
        }
        #endregion ToString, GetHashCode, Equals

        public bool InheritsFromOrIs(LayeTypeDef that)
        {
            if (ReferenceEquals(this, that))
                return true;
            // TODO inheritance
            return false;
        }

        #region Instance Accessing
        public void SetInvoke(LayeObject invoke)
        {
            this.invoke = invoke;
        }

        public bool GetInvoke(out LayeObject invoke)
        {
            invoke = this.invoke;
            return invoke != null;
        }

        public void PutInstanceField(string name)
        {
            if (name == null)
                throw new ArgumentNullException();
            if (instanceFields.Contains(name) || instanceProperties.ContainsKey(name))
                return;
            instanceFields.Add(name);
        }

        public bool HasInstanceField(string name)
        {
            return instanceFields.Contains(name);
        }

        public void PutCtor(string name, LayeObject ctor)
        {
            if (name == null)
                throw new ArgumentNullException();
            else if (ctor == null)
                throw new ArgumentNullException();
            ctors[name] = ctor;
        }

        public bool FindCtor(string name, out LayeObject ctor)
        {
            if (name == null)
                throw new ArgumentNullException();
            ctors.TryGetValue(name, out ctor);
            return ctor != null;
        }

        public void PutInstanceProperty(string name, LayeProperty property)
        {
            if (name == null)
                throw new ArgumentNullException();
            else if (property == null)
                throw new ArgumentNullException();
            if (instanceFields.Contains(name))
                return;
            instanceProperties[name] = property;
        }

        public bool FindInstancePropertyGet(string name, out LayeObject get)
        {
            if (name == null)
                throw new ArgumentNullException();
            LayeProperty property;
            if (instanceProperties.TryGetValue(name, out property))
                get = property.get;
            else get = null;
            return get != null;
        }

        public bool FindInstancePropertySet(string name, out LayeObject set)
        {
            if (name == null)
                throw new ArgumentNullException();
            LayeProperty property;
            if (instanceProperties.TryGetValue(name, out property))
                set = property.set;
            else set = null;
            return set != null;
        }

        public void PutAsCast(LayeTypeDef to, LayeObject cast)
        {
            if (to == null)
                throw new ArgumentNullException();
            else if (cast == null)
                throw new ArgumentNullException();
            asCasts[to] = cast;
        }

        public bool FindAsCast(LayeTypeDef to, out LayeObject cast)
        {
            if (to == null)
                throw new ArgumentNullException();
            asCasts.TryGetValue(to, out cast);
            return cast != null;
        }

        public void PutInstanceMethod(string name, LayeObject method)
        {
            if (name == null)
                throw new ArgumentNullException();
            else if (method == null)
                throw new ArgumentNullException();
            instanceMethods[name] = method;
        }

        public bool FindInstanceMethod(string name, out LayeObject method)
        {
            if (name == null)
                throw new ArgumentNullException();
            instanceMethods.TryGetValue(name, out method);
            return method != null;
        }

        public void PutPrefix(string op, LayeObject method)
        {
            if (op == null)
                throw new ArgumentNullException();
            else if (method == null)
                throw new ArgumentNullException();
            prefix[op] = method;
        }

        public bool FindPrefix(string op, out LayeObject method)
        {
            if (op == null)
                throw new ArgumentNullException();
            prefix.TryGetValue(op, out method);
            return method != null;
        }

        public void PutInfix(string op, LayeObject method)
        {
            if (op == null)
                throw new ArgumentNullException();
            else if (method == null)
                throw new ArgumentNullException();
            infix[op] = method;
        }

        public bool FindInfix(string op, out LayeObject method)
        {
            if (op == null)
                throw new ArgumentNullException();
            infix.TryGetValue(op, out method);
            return method != null;
        }

        public void PutInstanceOperatorIndex(string op, LayeProperty property)
        {
            if (op == null)
                throw new ArgumentNullException();
            else if (property == null)
                throw new ArgumentNullException();
            instanceOperatorIndex[op] = property;
        }

        public bool FindInstanceOperatorIndexGet(string op, out LayeObject get)
        {
            if (op == null)
                throw new ArgumentNullException();
            LayeProperty property;
            if (instanceOperatorIndex.TryGetValue(op, out property))
                get = property.get;
            else get = null;
            return get != null;
        }

        public bool FindInstanceOperatorIndexSet(string op, out LayeObject set)
        {
            if (op == null)
                throw new ArgumentNullException();
            LayeProperty property;
            if (instanceOperatorIndex.TryGetValue(op, out property))
                set = property.set;
            else set = null;
            return set != null;
        }
        #endregion Instance Accessing
    }
}
