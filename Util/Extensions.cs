using System;
using System.Collections.Generic;

namespace Laye.Util
{
    internal static class Extensions
    {
        internal static TValue GetOrCreateValue<TKey, TValue>(this IDictionary<TKey, TValue> dictionary, TKey key, TValue value)
        {
            TValue ret;
            if (!dictionary.TryGetValue(key, out ret))
                dictionary[key] = (ret = value);
            return ret;
        }

        internal static TValue GetOrCreateValue<TKey, TValue>(this IDictionary<TKey, TValue> dictionary, TKey key, Func<TValue> valueProvider)
        {
            TValue ret;
            if (!dictionary.TryGetValue(key, out ret))
                dictionary[key] = (ret = valueProvider());
            return ret;
        }
    }
}
