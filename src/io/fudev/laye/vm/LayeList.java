/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Sekai Kyoretsuna
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.fudev.laye.vm;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.fudev.laye.LayeException;
import lombok.EqualsAndHashCode;
import net.fudev.faxlib.collections.List;

/**
 * @author Sekai Kyoretsuna
 */
public @EqualsAndHashCode(callSuper = false)
class LayeList extends LayeObject implements Iterable<LayeObject>
{
   private final List<LayeObject> list = new List<>();
   
   public LayeList()
   {
   }
   
   public LayeList(LayeObject... values)
   {
      list.appendAll(values);
   }

   public int length()
   {
      return list.size();
   }
   
   public void setLength(int length)
   {
      int dif = list.size() - length;
      if (dif < 0)
      {
         while (dif++ < 0)
         {
            list.removeAt(list.size() - 1);
         }
      }
      else if (dif > 0)
      {
         while (dif-- > 0)
         {
            list.append(NULL);
         }
      }
   }
   
   @Override
   public String toString()
   {
      return(list.toString());
   }

   public void append(LayeObject value)
   {
      list.append(value);
   }

   public void appendAll(LayeObject[] values)
   {
      list.appendAll(values);
   }

   public void clear()
   {
      list.clear();
   }

   public boolean contains(LayeObject value)
   {
      return list.contains(value);
   }

   @Override
   public void forEach(Consumer<? super LayeObject> action)
   {
      list.forEach(action);
   }
   
   @Override
   public LayeObject load(LayeVM vm, LayeObject key)
   {
      if (key instanceof LayeInt)
      {
         long index =  ((LayeInt)key).value;
         if (index < 1 || index > list.size())
         {
            throw new LayeException(vm, "Index %d out of bounds.", index);
         }
         return(list.get((int)index - 1));
      }
      return(super.load(vm, key));
   }

   public LayeObject get(int index)
   {
      return list.get(index);
   }

   public int indexOf(LayeObject value)
   {
      return list.indexOf(value);
   }

   public void insert(LayeObject value, int index)
   {
      list.insert(value, index);
   }

   public boolean isEmpty()
   {
      return list.isEmpty();
   }

   @Override
   public Iterator<LayeObject> iterator()
   {
      return list.iterator();
   }

   public int lastIndexOf(LayeObject value)
   {
      return list.lastIndexOf(value);
   }

   public void prepend(LayeObject value)
   {
      list.prepend(value);
   }

   public void prependAll(LayeObject[] values)
   {
      list.prependAll(values);
   }

   public boolean remove(LayeObject value)
   {
      return list.remove(value);
   }

   public int removeAll(LayeObject value)
   {
      return list.removeAll(value);
   }

   public int removeAll(LayeObject[] values)
   {
      return list.removeAll(values);
   }

   public int removeAll(Predicate<LayeObject> predicate)
   {
      return list.removeAll(predicate);
   }

   public LayeObject removeAt(int index)
   {
      return list.removeAt(index);
   }

   public int retainAll(LayeObject value)
   {
      return list.retainAll(value);
   }

   public int retainAll(LayeObject[] values)
   {
      return list.retainAll(values);
   }

   public int retainAll(Predicate<LayeObject> predicate)
   {
      return list.retainAll(predicate);
   }
   
   @Override
   public void store(LayeVM vm, LayeObject key, LayeObject value)
   {
      if (key instanceof LayeInt)
      {
         long index =  ((LayeInt)key).value;
         if (index < 1 || index > list.size())
         {
            throw new LayeException(vm, "Index %d out of bounds.", index);
         }
         list.set((int)index - 1, value);
      }
      else
      {
         super.store(vm, key, value);
      }
   }

   public LayeObject set(int index, LayeObject value)
   {
      return list.set(index, value);
   }

   public void sort()
   {
      list.sort();
   }

   public void sort(Comparator<? super LayeObject> comparator)
   {
      list.sort(comparator);
   }

   @Override
   public Spliterator<LayeObject> spliterator()
   {
      return list.spliterator();
   }

   public LayeObject[] toArray()
   {
      return list.toArray();
   }
}
