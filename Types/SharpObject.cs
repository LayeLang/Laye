using System.Collections.Generic;
using System.Reflection;

namespace Laye
{
    using static Laye;

    public sealed class SharpObject : LayeObject
    {
        public readonly object instance;

        internal readonly Dictionary<string, FieldInfo> instanceFieldInfos = new Dictionary<string, FieldInfo>();
        internal readonly Dictionary<string, FunctionCallback> instanceMethodDelegates = new Dictionary<string, FunctionCallback>();
        internal FunctionCallback indexGetDelegate;

        public override LayeObject this[LayeState state, string key, bool raiseExceptions = true]
        {
            get
            {
                if (instanceFieldInfos.ContainsKey(key))
                    return (LayeObject)instanceFieldInfos[key].GetValue(instance);
                return base[state, key, raiseExceptions];
            }
            set
            {
                if (instanceFieldInfos.ContainsKey(key))
                    instanceFieldInfos[key].SetValue(instance, value);
                else base[state, key, raiseExceptions] = value;
            }
        }

        public override LayeObject this[LayeState state, params LayeObject[] args]
        {
            get
            {
                if (indexGetDelegate != null)
                    return indexGetDelegate.Invoke(state, args);
                return base[state, args];
            }
            set
            {
                base[state, args] = value;
            }
        }

        internal SharpObject(SharpTypeDef type, object instance)
            : base(type)
        {
            this.instance = instance;
        }

        public override LayeObject MethodInvoke(LayeState state, string methodName, params LayeObject[] args)
        {
            if (!instanceMethodDelegates.ContainsKey(methodName))
            {
                state.RaiseException("No such method {1} in {0}.", TypeName, methodName);
                return NULL;
            }
            return instanceMethodDelegates[methodName](state, args);
        }
    }
}
