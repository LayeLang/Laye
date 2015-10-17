namespace Laye.Proto
{
    internal sealed class FunctionPrototype
    {
        internal uint numParams;
        internal bool hasVargs;

        internal uint[] code, lineInfos;
        internal string definedFile;

        internal OuterValueInfo[] outers;
        internal FunctionPrototype[] nested;

        internal string[] strings;
        internal LayeObject[] numericConsts;

        internal int maxLocalCount;
        internal int maxStackCount;
    }
}
