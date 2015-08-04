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

import io.fudev.laye.struct.Identifier;
import io.fudev.laye.struct.Operator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * @author Sekai Kyoretsuna
 */
public abstract
class LayeReference extends LayeObject
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
      // FIXME(sekai): check references first, then dereference.
      return(deref(vm).compareEquals(vm, that));
   }

   @Override
   public LayeObject getField(LayeVM vm, Identifier key)
   {
      return(deref(vm).getField(vm, key));
   }

   @Override
   public void setField(LayeVM vm, Identifier key, LayeObject object)
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
   public LayeObject invokeMethod(LayeVM vm, Identifier methodIndex, LayeObject... args)
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

@EqualsAndHashCode(callSuper = true) @RequiredArgsConstructor
class LayeGlobalReference extends LayeReference
{
   private final SharedState state;
   private final String key;

   @Override
   public String toString()
   {
      return(state.load(key).toString());
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

@EqualsAndHashCode(callSuper = true) @RequiredArgsConstructor
class LayeOuterReference extends LayeReference
{
   private final OuterValue[] captures;
   private final int index;

   @Override
   public String toString()
   {
      return(captures[index].getValue().toString());
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

@EqualsAndHashCode(callSuper = true) @RequiredArgsConstructor
class LayeLocalReference extends LayeReference
{
   private final StackFrame frame;
   private final int index;

   @Override
   public String toString()
   {
      return(frame.load(index).toString());
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

@EqualsAndHashCode(callSuper = true) @RequiredArgsConstructor
class LayeIndexReference extends LayeReference
{
   private final LayeVM vm;
   private final LayeObject object;
   private final LayeObject key;

   @Override
   public String toString()
   {
      return(object.load(vm, key).toString());
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

@EqualsAndHashCode(callSuper = true) @RequiredArgsConstructor
class LayeFieldReference extends LayeReference
{
   private final LayeVM vm;
   private final LayeObject object;
   private final Identifier key;

   @Override
   public String toString()
   {
      return(object.getField(vm, key).toString());
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
