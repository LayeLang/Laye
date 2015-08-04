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

import java.util.HashMap;

import io.fudev.laye.LayeException;
import io.fudev.laye.struct.Identifier;
import io.fudev.laye.struct.Operator;
import lombok.EqualsAndHashCode;

/**
 * @author Sekai Kyoretsuna
 */
public @EqualsAndHashCode
class LayeObject
{
   public static final LayeNull NULL = LayeNull.INSTANCE;

   public static final LayeBool TRUE  = LayeBool.BOOL_TRUE;
   public static final LayeBool FALSE = LayeBool.BOOL_FALSE;
   
   final LayeTypeDef typedef;
   final HashMap<Identifier, LayeObject> fields = new HashMap<>();
   
   public LayeObject()
   {
      typedef = new LayeTypeDef();
   }
   
   public LayeObject(LayeTypeDef typedef)
   {
      this.typedef = typedef;
   }
   
   public String toString()
   {
      return "LayeObject:TODO"; // TODO(sekai): toString() for LayeObject
   }
   
   public boolean isNumeric(LayeVM vm)
   {
      return(false);
   }
   
   public boolean isInt(LayeVM vm)
   {
      return(false);
   }
   
   public boolean isFloat(LayeVM vm)
   {
      return(false);
   }
   
   public boolean isFunction(LayeVM vm)
   {
      return(false);
   }
   
   public int intValue(LayeVM vm)
   {
      return((int)longValue(vm));
   }
   
   public long longValue(LayeVM vm)
   {
      return(0L);
   }
   
   public float floatValue(LayeVM vm)
   {
      return((float)doubleValue(vm));
   }
   
   public double doubleValue(LayeVM vm)
   {
      return(0.0);
   }
   
   public boolean toBool(LayeVM vm)
   {
      return(true);
   }
   
   public boolean compareEquals(LayeVM vm, LayeObject that)
   {
      // FIXME(sekai): call operator == overloads
      return(this.equals(that)); 
   }
   
   public LayeObject load(LayeVM vm, LayeObject key)
   {
      if (!(key instanceof LayeString))
      {
         // FIXME(sekai): add type name
         throw new  LayeException(vm, "Attempt to index with type.");
      }
      Identifier name = Identifier.get(((LayeString)key).value);
      LayeObject result = fields.get(name);
      if (result == null)
      {
         if ((result = typedef.methods.get(name)) == null)
         {
            throw new  LayeException(vm, "Field '" + name + "' does not exist.");
         }
      }
      return(result);
   }
   
   public void store(LayeVM vm, LayeObject key, LayeObject object)
   {
      if (!(key instanceof LayeString))
      {
         // FIXME(sekai): add type name
         throw new  LayeException(vm, "Attempt to index with %s.",
               object.getClass().getSimpleName());
      }
      Identifier name = Identifier.get(((LayeString)key).value);
      fields.put(name, object);
   }
   
   public LayeObject invoke(LayeVM vm, LayeObject thisObject, LayeObject... args)
   {
      throw new LayeException(vm, "Attempt to call %s.", getClass().getSimpleName());
   }
   
   public LayeObject invokeMethod(LayeVM vm, LayeObject methodIndex, LayeObject... args)
   {
      return vm.invoke(load(vm, methodIndex), this, args);
   }
   
   public LayeObject deref(LayeVM vm)
   {
      return(this);
   }

   public LayeObject prefix(LayeVM vm, Operator op)
   {
      LayeObject prefix = typedef.prefix.get(op);
      if (prefix == null)
      {
         // FIXME(sekai): add type name
         throw new LayeException(vm, "Attempt to perform prefix operation '%s' on type.", op.image);
      }
      return(vm.invoke(prefix, this));
   }

   public LayeObject infix(LayeVM vm, Operator op, LayeObject that)
   {
      LayeObject infix = typedef.infix.get(op);
      if (infix == null)
      {
         // FIXME(sekai): add type name
         throw new LayeException(vm, "Attempt to perform infix operation '%s' on type.", op.image);
      }
      return(vm.invoke(infix, this, that));
   }
}
