using System;
using System.Collections.Generic;

namespace Laye
{
    using Util;

    using static Laye;

    /// <summary>
    /// The data for the Object type in Laye. This is the root of the object hierarchy.
    /// </summary>
    internal class ObjectTypeDef
    {
        internal ObjectTypeDef(LayeTypeDef type)
        {
            type.PutInstanceProperty("hashCode", LayeProperty.FromGet((LayeCallback)IPropertyGet__hashCode));
            type.PutAsCast(LayeBool.TYPE, (LayeCallback)As__Bool);
            type.PutInstanceMethod("toString", (LayeCallback)IMethod__toString);
            type.PutInfix("==", (LayeCallback)Infix__equalTo);
            type.PutInfix("!=", (LayeCallback)Infix__notEqualTo);
            type.PutInfix("<>", (LayeCallback)Infix__Concat);
            type.PutInfix("<;", (LayeCallback)((state, ths, args) => ths));
            type.PutInfix(";>", (LayeCallback)((state, ths, args) => args[0]));
            type.PutInfix(";", (LayeCallback)((state, ths, args) => args[0]));
        }

        protected virtual LayeObject IPropertyGet__hashCode(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return LayeInt.ValueOf(HashHelper.GetHashCode(ths.TypeName));
        }

        protected virtual LayeObject As__Bool(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return TRUE;
        }

        protected virtual LayeObject IMethod__toString(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return new LayeString(ths.TypeDef.ToString());
        }

        protected virtual LayeObject Infix__equalTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return (LayeBool)ReferenceEquals(ths, args[0]);
        }

        protected virtual LayeObject Infix__notEqualTo(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return (LayeBool)ReferenceEquals(ths, args[0]);
        }

        protected virtual LayeObject Infix__Concat(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            return new LayeString(ths.ToString(state) + args[0].ToString(state));
        }
    }

    /// <summary>
    /// LayeObject is the base class for all objects in Laye.
    /// </summary>
    public class LayeObject
    {
        public static readonly LayeTypeDef TYPE = new LayeTypeDef("Object");

        static LayeObject()
        {
            new ObjectTypeDef(TYPE);
        }

        #region Fields
        protected readonly Dictionary<string, LayeObject> fields = new Dictionary<string, LayeObject>();

        /// <summary>
        /// Access the fields of this object.
        /// If a field does not exist, an exception is thrown.
        /// </summary>
        /// <param name="key"></param>
        /// <returns></returns>
        public virtual LayeObject this[LayeState state, string key, bool raiseExceptions = true]
        {
            get
            {
                LayeObject property;
                if (type.FindInstancePropertyGet(key, out property))
                    return property.InvokeAsMethod(state, this);
                else if (type.HasInstanceField(key))
                    return fields[key];
                if (raiseExceptions)
                    state.RaiseException("No such field {1} in {0}.", TypeName, key);
                return NULL;
            }
            set
            {
                LayeObject property;
                if (type.FindInstancePropertySet(key, out property))
                    property.InvokeAsMethod(state, this, value);
                else if (type.HasInstanceField(key))
                    fields[key] = value;
                else if (raiseExceptions)
                    state.RaiseException("No such field {1} in {0}.", TypeName, key);
            }
        }

        /// <summary>
        /// Allows iteration over each field in this object as key/value pairs.
        /// This does not include methods.
        /// </summary>
        public IEnumerable<KeyValuePair<string, LayeObject>> Fields
        {
            get
            {
                foreach (var entry in fields)
                    yield return entry;
                yield break;
            }
        }
        #endregion Fields

        #region Indexing
        /// <summary>
        /// Access an index of this object.
        /// </summary>
        /// <param name="key"></param>
        /// <returns></returns>
        public virtual LayeObject this[LayeState state, params LayeObject[] args]
        {
            get { state.RaiseException("Attempt to index {0}.", TypeName); return NULL; }
            set { state.RaiseException("Attempt to index {0}.", TypeName); }
        }
        #endregion Indexing

        #region TypeDef
        protected readonly LayeTypeDef type;

        public LayeObject TypeDef
        {
            get { return type == null ? NULL : type; }
        }

        /// <summary>
        /// The name of this object's type as a string.
        /// </summary>
        public string TypeName
        {
            get { return type == null ? null : type.name; }
        }
        #endregion TypeDef

        public virtual bool IsNumeric { get { return false; } }

        #region Constructors
        public LayeObject()
            : this(TYPE)
        {
        }

        public LayeObject(LayeTypeDef type)
        {
            this.type = type;
        }
        #endregion Constructors

        #region ToString, GetHashCode, EqualTo, NotEqualTo
        public string ToString(LayeState state)
        {
            if (type != null)
            {
                LayeObject methToString;
                if (type.FindInstanceMethod("toString", out methToString))
                {
                    var result = methToString.InvokeAsMethod(state, this);
                    var strval = result.As(state, LayeString.TYPE) as LayeString;
                    if (strval == null)
                    {
                        state.RaiseException("Attempt to convert {0} to String.", result.TypeName);
                        return null;
                    }
                    return strval.value;
                }
            }
            return base.ToString();
        }

        public int GetHashCode(LayeState state)
        {
            if (type != null)
            {
                LayeObject propHashCode;
                if (type.FindInstancePropertyGet("hashCode", out propHashCode))
                {
                    var result = propHashCode.InvokeAsMethod(state, this);
                    var ival = result.As(state, LayeInt.TYPE) as LayeInt;
                    if (ival == null)
                    {
                        state.RaiseException("Attempt to convert {0} to Int.", result.TypeName);
                        return 0;
                    }
                    return ival.value.GetHashCode();
                }
            }
            return base.GetHashCode();
        }

        public bool EqualTo(LayeState state, LayeObject that)
        {
            if (that == null)
                throw new ArgumentNullException("that");
            if (type != null)
            {
                LayeObject infixEqualTo;
                if (type.FindInfix("==", out infixEqualTo))
                {
                    var result = infixEqualTo.InvokeAsMethod(state, this, that);
                    var boolval = result.As(state, LayeBool.TYPE) as LayeBool;
                    if (boolval == null)
                    {
                        state.RaiseException("Attempt to convert {0} to Bool.", result.TypeName);
                        return false;
                    }
                    return boolval.value;
                }
            }
            return ReferenceEquals(this, that);
        }

        public bool NotEqualTo(LayeState state, LayeObject that)
        {
            if (that == null)
                throw new ArgumentNullException("that");
            if (type != null)
            {
                LayeObject infixEqualTo;
                if (type.FindInfix("!=", out infixEqualTo))
                {
                    var result = infixEqualTo.InvokeAsMethod(state, this, that);
                    var boolval = result.As(state, LayeBool.TYPE) as LayeBool;
                    if (boolval == null)
                    {
                        state.RaiseException("Attempt to convert {0} to Bool.", result.TypeName);
                        return true;
                    }
                    return boolval.value;
                }
            }
            return !ReferenceEquals(this, that);
        }
        #endregion ToString, GetHashCode, EqualTo, NotEqualTo

        public bool ToBool(LayeState state)
        {
            var value = As(state, LayeBool.TYPE);
            if (value == null)
                return false;
            return (value as LayeBool).value;
        }

        public bool TypeOf(LayeState state, LayeObject thatType)
        {
            if (type == null || thatType == null || ReferenceEquals(thatType, NULL) || !(thatType is LayeTypeDef))
                return false;
            return type.InheritsFromOrIs(thatType as LayeTypeDef);
        }

        public bool TypeOf(LayeState state, LayeTypeDef thatType)
        {
            if (type == null || thatType == null)
                return false;
            return type.InheritsFromOrIs(thatType);
        }

        #region Operations
        public LayeObject OperatorIndexGet(LayeState state, string op)
        {
            if (type != null)
            {
                LayeObject property;
                if (type.FindInstanceOperatorIndexGet(op, out property))
                    return property.InvokeAsMethod(state, this);
            }
            state.RaiseException("Attempt to index {0} with operator {1}.", TypeName);
            return NULL;
        }

        public LayeObject OperatorIndexSet(LayeState state, string op, LayeObject value)
        {
            if (type != null)
            {
                LayeObject property;
                if (type.FindInstanceOperatorIndexSet(op, out property))
                    return property.InvokeAsMethod(state, this, value);
            }
            state.RaiseException("Attempt to index {0} with operator {1}.", TypeName);
            return NULL;
        }

        public LayeObject As(LayeState state, LayeObject typeDef)
        {
            if (typeDef == null)
                throw new ArgumentException("typeDef");
            if (!(typeDef is LayeTypeDef))
            {
                state.RaiseException("Type expected in as expression, got {0}.", typeDef.TypeName);
                return NULL;
            }
            var type = typeDef as LayeTypeDef;
            if (this.type.InheritsFromOrIs(type))
                return this;
            LayeObject asCast;
            if (this.type.FindAsCast(type, out asCast))
            {
                LayeObject result = asCast.InvokeAsMethod(state, this);
                if (!result.TypeOf(state, type))
                {
                    state.RaiseException("Expected result of type {0} to be returned by cast method, but {1} was found.", type.name, result.TypeName);
                    return NULL;
                }
                return result;
            }
            return NULL;
        }

        /// <summary>
        /// Attempts to invoke this object.
        /// If this object cannot be invoked, a <code>LayeIllegalOperation</code> exception is thrown.
        /// Otherwise, the result of the invocation is returned.
        /// </summary>
        /// <param name="state"></param>
        /// <param name="ths"></param>
        /// <param name="args"></param>
        /// <returns></returns>
        public virtual LayeObject Invoke(LayeState state, params LayeObject[] args)
        {
            if (type != null)
            {
                LayeObject invoke;
                if (type.GetInvoke(out invoke))
                    return invoke.InvokeAsMethod(state, this, args);
            }
            state.RaiseException("Attempt to call {0}.", TypeName);
            return NULL;
        }

        /// <summary>
        /// Attempts to invoke this object as a method on another object.
        /// If this object cannot be invoked, a <code>LayeIllegalOperation</code> exception is thrown.
        /// Otherwise, the result of the invocation is returned.
        /// </summary>
        /// <param name="state"></param>
        /// <param name="ths"></param>
        /// <param name="args"></param>
        /// <returns></returns>
        public virtual LayeObject InvokeAsMethod(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            state.RaiseException("Attempt to call {0}.", TypeName);
            return NULL;
        }

        /// <summary>
        /// Attempts to invoke a method on this object.
        /// If a method cannot be found, a <code>LayeNoSuchMethod</code> exception is thrown.
        /// Otherwise, the result of the invocation is returned.
        /// </summary>
        /// <param name="state"></param>
        /// <param name="ths"></param>
        /// <param name="args"></param>
        /// <returns></returns>
        public virtual LayeObject MethodInvoke(LayeState state, string methodName, params LayeObject[] args)
        {
            if (type != null)
            {
                LayeObject method;
                if (type.FindInstanceMethod(methodName, out method))
                    return method.InvokeAsMethod(state, this, args);
            }
            state.RaiseException("No such method {1} in {0}.", TypeName, methodName);
            return NULL;
        }

        public virtual LayeObject Prefix(LayeState state, string op)
        {
            if (type != null)
            {
                LayeObject method;
                if (type.FindPrefix(op, out method))
                    return method.InvokeAsMethod(state, this);
            }
            state.RaiseException("Attempt to invoke prefix {0} on {1}.", op, TypeName);
            return NULL;
        }

        public virtual LayeObject Infix(LayeState state, string op, LayeObject that)
        {
            if (type != null)
            {
                LayeObject method;
                if (type.FindInfix(op, out method))
                    return method.InvokeAsMethod(state, this, that);
            }
            state.RaiseException("Attempt to invoke infix {0} on {1} with {2}.", op, TypeName, that.TypeName);
            return NULL;
        }
        #endregion Operations
    }
}
