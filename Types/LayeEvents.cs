using System.Collections.Generic;

namespace Laye
{
    public sealed class LayeEvent : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("Event");

        static LayeEvent()
        {
            TYPE.SetInvoke((LayeCallback)((state, ths, args) =>
            {
                return (ths as LayeEvent).Fire(state, args) ? Laye.TRUE : Laye.FALSE;
            }));
            TYPE.PutInstanceMethod("connect", (LayeCallback)((state, ths, args) =>
            {
                if (args.Length == 0 || args[0] == Laye.NULL)
                {
                    state.RaiseException("bad argument #0 to connect, event handler cannot be null.");
                    return Laye.NULL;
                }
                var arg = args[0];
                var e = ths as LayeEvent;
                var handles = e.handles;
                foreach (var h in handles)
                    if (h.handler == arg)
                        return h;
                var handle = new LayeEventHandle(e, arg);
                return Laye.NULL;
            }));
        }

        private List<LayeEventHandle> handles = new List<LayeEventHandle>();

        public LayeEvent()
            : base(TYPE)
        {
        }

        public bool Fire(LayeState state, params LayeObject[] args)
        {
            for (int i = handles.Count - 1; i >= 0; i--)
                if (handles[i].handler.Invoke(state, args).ToBool(state))
                    return true;
            return false;
        }

        internal void DisconnectHandle(LayeEventHandle handle)
        {
            handles.Remove(handle);
        }
    }

    public sealed class LayeEventHandle : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("EventHandle");

        static LayeEventHandle()
        {
            TYPE.PutInstanceMethod("disconnect", (LayeCallback)((state, ths, args) =>
            {
                var e = ths as LayeEventHandle;
                if (e.parent == null)
                    return Laye.NULL;
                e.parent.DisconnectHandle(e);
                e.parent = null;
                return Laye.NULL;
            }));
        }

        private LayeEvent parent;
        internal readonly LayeObject handler;

        public LayeEventHandle(LayeEvent parent, LayeObject handler)
        {
            this.parent = parent;
            this.handler = handler;
        }
    }
}
