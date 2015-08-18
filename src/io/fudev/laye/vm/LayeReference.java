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

import java.util.Arrays;

import io.fudev.laye.struct.Operator;

/**
 * @author Sekai Kyoretsuna
 */
public abstract
class LayeReference
   extends LayeObject
{
   public abstract void store(LayeVM vm, LayeObject value);

   @Override
   public String toString()
   {
      return(deref(null).toString());
   }

   @Override
   public long longValue(LayeVM vm)
   {
      return(deref(vm).longValue(vm));
   }

   @Override
   public double doubleValue(LayeVM vm)
   {
      return(deref(vm).doubleValue(vm));
   }

   @Override
   public boolean toBool(LayeVM vm)
   {
      return(deref(vm).toBool(vm));
   }

   @Override
   public boolean compareEquals(LayeVM vm, LayeObject that)
   {
      if (that instanceof LayeReference && this.equals(that))
      {
         return(true);
      }
      return(deref(vm).compareEquals(vm, that));
   }

   @Override
   public LayeObject getField(LayeVM vm, String key)
   {
      return(deref(vm).getField(vm, key));
   }

   @Override
   public void setField(LayeVM vm, String key, LayeObject object)
   {
      deref(vm).setField(vm, key, object);
   }

   @Override
   public LayeObject load(LayeVM vm, LayeObject key)
   {
      return(deref(vm).load(vm, key));
   }

   @Override
   public void store(LayeVM vm, LayeObject key, LayeObject object)
   {
      deref(vm).store(vm, key, object);
   }

   @Override
   public LayeObject invoke(LayeVM vm, LayeObject thisObject, LayeObject... args)
   {
      return(deref(vm).invoke(vm, thisObject, args));
   }

   @Override
   public LayeObject invokeMethod(LayeVM vm, String methodIndex, LayeObject... args)
   {
      return(deref(vm).invokeMethod(vm, methodIndex, args));
   }

   @Override
   public LayeObject prefix(LayeVM vm, Operator op)
   {
      return(deref(vm).prefix(vm, op));
   }

   @Override
   public LayeObject infix(LayeVM vm, Operator op, LayeObject that)
   {
      return(deref(vm).infix(vm, op, that));
   }
}

class LayeGlobalReference
   extends LayeReference
{
   private final SharedState state;
   private final String key;

   public LayeGlobalReference(SharedState state, String key)
   {
      this.state = state;
      this.key = key;
   }

   @Override
   public String toString()
   {
      return(state.load(key).toString());
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      result = prime * result + ((state == null) ? 0 : state.hashCode());
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
      if (!(obj instanceof LayeGlobalReference))
      {
         return false;
      }
      LayeGlobalReference other = (LayeGlobalReference) obj;
      if (key == null)
      {
         if (other.key != null)
         {
            return false;
         }
      }
      else if (!key.equals(other.key))
      {
         return false;
      }
      if (state == null)
      {
         if (other.state != null)
         {
            return false;
         }
      }
      else if (!state.equals(other.state))
      {
         return false;
      }
      return true;
   }

   @Override
   public LayeObject deref(LayeVM vm)
   {
      return(state.load(key));
   }
   
   @Override
   public void store(LayeVM vm, LayeObject value)
   {
      state.store(key, value);
   }
}

class LayeOuterReference
   extends LayeReference
{
   private final OuterValue[] captures;
   private final int index;

   public LayeOuterReference(OuterValue[] captures, int index)
   {
      this.captures = captures;
      this.index = index;
   }

   @Override
   public String toString()
   {
      return(captures[index].getValue().toString());
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(captures);
      result = prime * result + index;
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
      if (!(obj instanceof LayeOuterReference))
      {
         return false;
      }
      LayeOuterReference other = (LayeOuterReference) obj;
      if (!Arrays.equals(captures, other.captures))
      {
         return false;
      }
      if (index != other.index)
      {
         return false;
      }
      return true;
   }

   @Override
   public LayeObject deref(LayeVM vm)
   {
      return(captures[index].getValue());
   }
   
   @Override
   public void store(LayeVM vm, LayeObject value)
   {
      captures[index].setValue(vm, value);
   }
}

class LayeLocalReference
   extends LayeReference
{
   private final StackFrame frame;
   private final int index;

   public LayeLocalReference(StackFrame frame, int index)
   {
      this.frame = frame;
      this.index = index;
   }

   @Override
   public String toString()
   {
      return(frame.load(index).toString());
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((frame == null) ? 0 : frame.hashCode());
      result = prime * result + index;
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
      if (!(obj instanceof LayeLocalReference))
      {
         return false;
      }
      LayeLocalReference other = (LayeLocalReference) obj;
      if (frame == null)
      {
         if (other.frame != null)
         {
            return false;
         }
      }
      else if (!frame.equals(other.frame))
      {
         return false;
      }
      if (index != other.index)
      {
         return false;
      }
      return true;
   }

   @Override
   public LayeObject deref(LayeVM vm)
   {
      return(frame.load(index));
   }
   
   @Override
   public void store(LayeVM vm, LayeObject value)
   {
      frame.store(index, value);
   }
}

class LayeIndexReference
   extends LayeReference
{
   private final LayeVM vm;
   private final LayeObject object;
   private final LayeObject key;

   public LayeIndexReference(LayeVM vm, LayeObject object, LayeObject key)
   {
      this.vm = vm;
      this.object = object;
      this.key = key;
   }

   @Override
   public String toString()
   {
      return(object.load(vm, key).toString());
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      result = prime * result + ((object == null) ? 0 : object.hashCode());
      result = prime * result + ((vm == null) ? 0 : vm.hashCode());
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
      if (!(obj instanceof LayeIndexReference))
      {
         return false;
      }
      LayeIndexReference other = (LayeIndexReference) obj;
      if (key == null)
      {
         if (other.key != null)
         {
            return false;
         }
      }
      else if (!key.equals(other.key))
      {
         return false;
      }
      if (object == null)
      {
         if (other.object != null)
         {
            return false;
         }
      }
      else if (!object.equals(other.object))
      {
         return false;
      }
      if (vm == null)
      {
         if (other.vm != null)
         {
            return false;
         }
      }
      else if (!vm.equals(other.vm))
      {
         return false;
      }
      return true;
   }

   @Override
   public LayeObject deref(LayeVM vm)
   {
      return(object.load(vm, key));
   }
   
   @Override
   public void store(LayeVM vm, LayeObject value)
   {
      object.store(vm, key, value);
   }
}

class LayeFieldReference
   extends LayeReference
{
   private final LayeVM vm;
   private final LayeObject object;
   private final String key;

   public LayeFieldReference(LayeVM vm, LayeObject object, String key)
   {
      this.vm = vm;
      this.object = object;
      this.key = key;
   }

   @Override
   public String toString()
   {
      return(object.getField(vm, key).toString());
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      result = prime * result + ((object == null) ? 0 : object.hashCode());
      result = prime * result + ((vm == null) ? 0 : vm.hashCode());
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
      if (!(obj instanceof LayeFieldReference))
      {
         return false;
      }
      LayeFieldReference other = (LayeFieldReference) obj;
      if (key == null)
      {
         if (other.key != null)
         {
            return false;
         }
      }
      else if (!key.equals(other.key))
      {
         return false;
      }
      if (object == null)
      {
         if (other.object != null)
         {
            return false;
         }
      }
      else if (!object.equals(other.object))
      {
         return false;
      }
      if (vm == null)
      {
         if (other.vm != null)
         {
            return false;
         }
      }
      else if (!vm.equals(other.vm))
      {
         return false;
      }
      return true;
   }

   @Override
   public LayeObject deref(LayeVM vm)
   {
      return(object.getField(vm, key));
   }
   
   @Override
   public void store(LayeVM vm, LayeObject value)
   {
      object.setField(vm, key, value);
   }
}
