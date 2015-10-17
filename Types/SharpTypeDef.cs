using System;
using System.Collections.Generic;
using System.Reflection;
using System.Linq;

namespace Laye
{
    using Util;

    using static Laye;

    public sealed class SharpTypeDef : LayeTypeDef
    {
        private static readonly Dictionary<Type, SharpTypeDef> types = new Dictionary<Type, SharpTypeDef>();

        public static SharpTypeDef Get(Type type, string name = null, bool isSealed = false)
        {
            return types.GetOrCreateValue(type, () => new SharpTypeDef(type, name, isSealed));
        }

        new private readonly Type type;

        private readonly List<MethodInfo> instanceMethodInfos = new List<MethodInfo>();
        private readonly List<FieldInfo> instanceFieldInfos = new List<FieldInfo>();
        private readonly Dictionary<string, MethodInfo> ctorInfos = new Dictionary<string, MethodInfo>();
        private MethodInfo instanceIndexGetInfo;

        internal SharpTypeDef(Type type, string name = null, bool isSealed = false)
            : base(TYPE, name == null ? type.Name : name, isSealed)
        {
            this.type = type;

            var instanceFields = type.GetFields(BindingFlags.Public | BindingFlags.Instance)
                .Where(fi => typeof(LayeObject).IsAssignableFrom(fi.FieldType))
                .ToList();

            var instanceMethods = type.GetMethods(BindingFlags.Public | BindingFlags.Instance)
                .Where(mi => typeof(LayeObject).IsAssignableFrom(mi.ReturnType)
                    && mi.GetParameters().Select(pi => pi.ParameterType)
                        .SequenceEqual(new Type[] { typeof(LayeState), typeof(LayeObject[]) }))
                .ToList();

            instanceFieldInfos.AddRange(instanceFields);
            // TODO assign to different areas based on name
            instanceMethods.ForEach(method =>
            {
                var n = method.Name;
                if (n == "IndexGet" && !method.IsStatic)
                    instanceIndexGetInfo = method;
                else instanceMethodInfos.Add(method);
            });
        }

        public SharpObject Bind(object instance)
        {
            var result = new SharpObject(this, instance);
            instanceFieldInfos.ForEach(fi => result.instanceFieldInfos[fi.Name] = fi);
            instanceMethodInfos.ForEach(mi => result.instanceMethodDelegates[mi.Name] = (FunctionCallback)mi.CreateDelegate(typeof(FunctionCallback), instance));
            if (instanceIndexGetInfo != null)
                result.indexGetDelegate = (FunctionCallback)instanceIndexGetInfo.CreateDelegate(typeof(FunctionCallback), instance);
            return result;
        }

        public LayeObject Instantiate(LayeState state, string ctorName, params LayeObject[] args)
        {
            if (!ctorInfos.ContainsKey(ctorName))
            {
                if (ctorName != null)
                    state.RaiseException("{0} does not have a constructor named {1}.", name, ctorName);
                else state.RaiseException("{0} does not have a default constructor.", name);
                return NULL;
            }
            var ctor = ctorInfos[ctorName];
            // TODO check contructors
            var instance = Activator.CreateInstance(type);
            return Bind(instance);
        }
    }
}
