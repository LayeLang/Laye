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

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

import io.fudev.collections.List;
import io.fudev.laye.LayeException;

/**
 * @author Sekai Kyoretsuna
 */
public
class LayeList
   extends LayeObject
   implements Iterable<LayeObject>
{
   private static final LayeTypeDef TYPEDEF_LIST = new LayeTypeDef();
   
   static
   {
      TYPEDEF_LIST.addMethod("ForEach", new LayeFunction((vm, thisObject, args) ->
      {
         // FIXME(kai): proper error checking plz
         List<LayeObject> list = ((LayeList)thisObject).list;
         LayeObject fn = args[0];
         list.forEach(value -> fn.invoke(vm, null, value));
         return(NULL);
      }));
      TYPEDEF_LIST.addMethod("Map", new LayeFunction((vm, thisObject, args) ->
      {
         // FIXME(kai): proper error checking plz
         List<LayeObject> list = ((LayeList)thisObject).list;
         LayeList result = new LayeList();
         LayeObject fn = args[0];
         for (int i = 0; i < list.size(); i++)
         {
            result.list.append(fn.invoke(vm, null, list.get(i)));
         }
         return(result);
      }));
      TYPEDEF_LIST.addMethod("Replace", new LayeFunction((vm, thisObject, args) ->
      {
         // FIXME(kai): proper error checking plz
         List<LayeObject> list = ((LayeList)thisObject).list;
         LayeObject fn = args[0];
         for (int i = 0; i < list.size(); i++)
         {
            list.set(i, fn.invoke(vm, null, list.get(i)));
         }
         return(thisObject);
      }));
      TYPEDEF_LIST.addMethod("Append", new LayeFunction((vm, thisObject, args) ->
      {
         // FIXME(kai): proper error checking plz
         LayeObject value = args[0];
         ((LayeList)thisObject).list.append(value);
         return(NULL);
      }));
   }
   
   private final List<LayeObject> list = new List<>();
   
   public LayeList()
   {
      super(TYPEDEF_LIST);
   }
   
   public LayeList(LayeObject... values)
   {
      this();
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

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((list == null) ? 0 : list.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof LayeList))
      {
         return false;
      }
      LayeList other = (LayeList) obj;
      if (list == null)
      {
         if (other.list != null)
         {
            return false;
         }
      }
      else if (!list.equals(other.list))
      {
         return false;
      }
      return true;
   }
   
   public void append(LayeObject object)
   {
      list.append(object);
   }
   
   @Override
   public void forEach(Consumer<? super LayeObject> action)
   {
      list.forEach(action);
   }

   public List<LayeObject> map(Function<? super LayeObject, ? extends LayeObject> action)
   {
      return list.map(action);
   }

   @Override
   public LayeObject load(LayeVM vm, LayeObject key)
   {
      if (key instanceof LayeInt)
      {
         long index =  ((LayeInt)key).value;
         if (index < 0 || index >= list.size())
         {
            throw new LayeException(vm, "Index %d out of bounds.", index);
         }
         return(list.get((int)index));
      }
      return(super.load(vm, key));
   }

   @Override
   public Iterator<LayeObject> iterator()
   {
      return list.iterator();
   }
   
   @Override
   public void store(LayeVM vm, LayeObject key, LayeObject value)
   {
      if (key instanceof LayeInt)
      {
         long index =  ((LayeInt)key).value;
         if (index < 0 || index >= list.size())
         {
            throw new LayeException(vm, "Index %d out of bounds.", index);
         }
         list.set((int)index, value);
      }
      else
      {
         super.store(vm, key, value);
      }
   }

   @Override
   public Spliterator<LayeObject> spliterator()
   {
      return list.spliterator();
   }
}
