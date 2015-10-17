namespace Laye
{
    internal abstract class LayeReference : LayeObject
    {
        public abstract void Store(LayeState state, LayeObject value);
    }
}
