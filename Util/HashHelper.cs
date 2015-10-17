using System.Collections.Generic;

namespace Laye.Util
{
    internal static class HashHelper
    {
        internal static int GetHashCode(params object[] objects)
        {
            unchecked
            {
                int hash = 0;
                foreach (var item in objects)
                    if (item != null)
                        hash = 31 * hash + item.GetHashCode();
                return hash;
            }
        }

        internal static int GetHashCodeUnordered(params object[] objects)
        {
            unchecked
            {
                int hash = 0;
                foreach (var item in objects)
                    if (item != null)
                        hash += item.GetHashCode();
                return 31 * hash + objects.Length.GetHashCode();
            }
        }

        internal static int GetHashCode<T>(IEnumerable<T> list)
        {
            unchecked
            {
                int hash = 0;
                foreach (var item in list)
                    if (item != null)
                        hash = 31 * hash + item.GetHashCode();
                return hash;
            }
        }

        internal static int GetHashCodeUnordered<T>(IEnumerable<T> list)
        {
            unchecked
            {
                int hash = 0, count = 0;
                foreach (var item in list)
                {
                    if (item != null)
                        hash += item.GetHashCode();
                    count++;
                }
                return 31 * hash + count.GetHashCode();
            }
        }
    }
}
