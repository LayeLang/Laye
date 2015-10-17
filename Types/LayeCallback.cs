using System;
using System.Runtime.CompilerServices;

namespace Laye
{
    using static Laye;

    public delegate LayeObject FunctionCallback(LayeState state, params LayeObject[] args);
    public delegate LayeObject MethodCallback(LayeState state, LayeObject ths, params LayeObject[] args);

    /// <summary>
    /// LayeCallback allows Laye code to call your C# code directly by using the <code>HostCallback</code> delegate.
    /// </summary>
    public sealed class LayeCallback : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = LayeClosure.TYPE;

        #region Implicit Conversions
        /// <summary>
        /// Converts from a FunctionCallback delegate to a LayeCallback object.
        /// This is the same as <code>new LayeCallback(callback)</code>.
        /// </summary>
        /// <param name="function"></param>
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static implicit operator LayeCallback(FunctionCallback function)
        {
            return new LayeCallback(function);
        }

        /// <summary>
        /// Converts from a MethodCallback delegate to a LayeCallback object.
        /// This is the same as <code>new LayeCallback(callback)</code>.
        /// </summary>
        /// <param name="method"></param>
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static implicit operator LayeCallback(MethodCallback method)
        {
            return new LayeCallback(method);
        }
        #endregion Implicit Conversions

        // This gets invoked when this object does.
        private readonly MethodCallback callback;

        /// <summary>
        /// Creates a new LayeCallback using the given function.
        /// </summary>
        /// <param name="callback"></param>
        public LayeCallback(FunctionCallback function)
            : base(TYPE)
        {
            callback = (state, ths, args) => function(state, args);
        }

        /// <summary>
        /// Creates a new LayeCallback using the given method.
        /// </summary>
        /// <param name="callback"></param>
        public LayeCallback(MethodCallback method)
            : base(TYPE)
        {
            callback = method;
        }

        #region Operations
        public override LayeObject Invoke(LayeState state, params LayeObject[] args)
        {
            return InvokeAsMethod(state, NULL, args);
        }

        public override LayeObject InvokeAsMethod(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            try
            {
                return callback(state, ths, args);
            }
            catch (UnhandledLayeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                state.RaiseException(e.GetType().FullName + ": " + e.Message + Environment.NewLine + e.StackTrace);
                return NULL;
            }
        }
        #endregion Operations
    }
}
