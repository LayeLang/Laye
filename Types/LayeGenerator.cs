namespace Laye
{
    public abstract class LayeGeneratorSpawner : LayeObject
    {
        public override LayeObject Invoke(LayeState state, params LayeObject[] args)
        {
            return InvokeAsMethod(state, null, args);
        }

        public abstract override LayeObject InvokeAsMethod(LayeState state, LayeObject ths, params LayeObject[] args);
    }
    
    public abstract class LayeGenerator : LayeObject
    {
        new public static readonly LayeTypeDef TYPE = new LayeTypeDef("Generator");

        internal enum State
        {
            RUNNING, SUSPENDED, DEAD,
        }

        internal LayeGenerator()
            : base(TYPE)
        {
        }

        internal State state = State.SUSPENDED;

        /// <summary>
        /// Attempts to resume this generator.
        /// All this does is check that the state is valid (not already running
        /// or dead) and raises exceptions if in an invalid state.
        /// The state is set to <c>State.RUNNING</c> on success.
        /// Returns false if exceptions were thrown, true otherwise.
        /// </summary>
        /// <param name="state"></param>
        /// <returns></returns>
        protected bool TryResume(LayeState state)
        {
            if (this.state == State.RUNNING)
            {
                state.RaiseException("Attempt to resume a running generator.");
                return false;
            }
            if (this.state == State.DEAD)
            {
                state.RaiseException("Attempt to resume a dead generator.");
                return false;
            }
            this.state = State.RUNNING;
            return true;
        }

        /// <summary>
        /// Sets the state of this generator to <c>State.SUSPENDED</c>
        /// </summary>
        protected void Suspend()
        {
            state = State.SUSPENDED;
        }

        /// <summary>
        /// Sets the state of this generator to <c>State.DEAD</c>
        /// </summary>
        protected void Kill()
        {
            state = State.DEAD;
        }

        /// <summary>
        /// Returns <c>true</c> if this generator resumed, even if the function returned, false if the resume failed.
        /// If result is <c>null</c>, either resuming failed or the generator died this cycle.
        /// </summary>
        /// <param name="state"></param>
        /// <param name="result"></param>
        /// <returns></returns>
        public abstract bool Resume(LayeState state, out LayeObject result);
    }

    internal sealed class LayeClosureGeneratorSpawner : LayeGeneratorSpawner
    {
        private readonly LayeClosure closure;

        public LayeClosureGeneratorSpawner(LayeClosure closure)
        {
            this.closure = closure;
        }

        public override LayeObject InvokeAsMethod(LayeState state, LayeObject ths, params LayeObject[] args)
        {
            var frame = state.stack.NewFrame(closure, ths, args);
            return new LayeClosureGenerator(frame, closure, ths);
        }
    }

    internal sealed class LayeClosureGenerator : LayeGenerator
    {
        private readonly StackFrame frame;
        private readonly LayeClosure closure;
        private readonly LayeObject ths;

        public LayeClosureGenerator(StackFrame frame, LayeClosure closure, LayeObject ths)
        {
            frame.gtor = this;
            this.frame = frame;
            this.closure = closure;
            this.ths = ths;
        }

        public override bool Resume(LayeState state, out LayeObject result)
        {
            if (!TryResume(state))
            {
                result = null;
                return false;
            }
            frame.yielded = false;
            state.Execute(frame, closure, ths);
            if (frame.yielded)
            {
                Suspend();
                result = frame.HasValue() ? frame.Pop() : Laye.NULL;
                return true;
            }
            Kill();
            result = null;
            return true;
        }
    }
}
