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

import io.fudev.collections.List;
import io.fudev.laye.LayeException;
import io.fudev.laye.struct.Operator;

/**
 * @author Sekai Kyoretsuna
 */
public
class LayeObject
{
   public static final LayeNull NULL = LayeNull.INSTANCE;

   public static final LayeBool TRUE  = LayeBool.BOOL_TRUE;
   public static final LayeBool FALSE = LayeBool.BOOL_FALSE;
   
   final LayeTypeDef typedef;
   
   final HashMap<String, LayeObject> fields = new HashMap<>();
   final List<String> privateFields = new List<String>();
   
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
      return "LayeObject:TODO"; // TODO(kai): toString() for LayeObject
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((fields == null) ? 0 : fields.hashCode());
      result = prime * result + ((typedef == null) ? 0 : typedef.hashCode());
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
      if (!(obj instanceof LayeObject))
      {
         return false;
      }
      LayeObject other = (LayeObject) obj;
      if (fields == null)
      {
         if (other.fields != null)
         {
            return false;
         }
      }
      else if (!fields.equals(other.fields))
      {
         return false;
      }
      if (typedef == null)
      {
         if (other.typedef != null)
         {
            return false;
         }
      }
      else if (!typedef.equals(other.typedef))
      {
         return false;
      }
      return true;
   }

   public boolean isNumeric(LayeVM vm)
   {
      return(false);
   }
   
   public boolean isInt(LayeVM vm)
   {
      return(false);
   }
   
   public int checkInt(LayeVM vm)
   {
      throw new LayeException(vm, "could not convert " + getClass().getSimpleName() + " to int.");
   }
   
   public long checkLong(LayeVM vm)
   {
      throw new LayeException(vm, "could not convert " + getClass().getSimpleName() + " to long.");
   }
   
   public boolean isFloat(LayeVM vm)
   {
      return(false);
   }
   
   public boolean isString(LayeVM vm)
   {
      return(false);
   }
   
   public String checkString(LayeVM vm)
   {
      throw new LayeException(vm, "could not convert " + getClass().getSimpleName() + " to string.");
   }
   
   public boolean isFunction(LayeVM vm)
   {
      return(false);
   }
   
   public boolean isList(LayeVM vm)
   {
      return(false);
   }
   
   public List<LayeObject> checkList(LayeVM vm)
   {
      throw new LayeException(vm, "could not convert " + getClass().getSimpleName() + " to list.");
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
      // FIXME(kai): call operator == overloads
      return(this.equals(that)); 
   }
   
   public boolean hasField(LayeVM vm, String key)
   {
      return(fields.containsKey(key) || (typedef != null ? typedef.methods.containsKey(key) : false));
   }
   
   public LayeObject getField(LayeVM vm, String key)
   {
      LayeObject result = fields.get(key);
      if (result == null)
      {
         if ((result = typedef.methods.get(key)) == null)
         {
            throw new  LayeException(vm, "Field '" + key + "' does not exist.");
         }
      }
      return(result);
   }
   
   public void setField(LayeVM vm, String key, LayeObject object)
   {
      fields.put(key, object);
   }
   
   public LayeObject load(LayeVM vm, LayeObject index)
   {
      throw new LayeException(vm, "Attempt to index %s.", getClass().getSimpleName());
   }
   
   public void store(LayeVM vm, LayeObject index, LayeObject value)
   {
      throw new LayeException(vm, "Attempt to index %s.", getClass().getSimpleName());
   }
   
   public LayeObject invoke(LayeVM vm, LayeObject thisObject, LayeObject... args)
   {
      if (typedef != null && typedef.invoke != null)
      {
         return(vm.invoke(typedef.invoke, this, args));
      }
      throw new LayeException(vm, "Attempt to call %s.", getClass().getSimpleName());
   }
   
   public LayeObject invokeMethod(LayeVM vm, String methodIndex, LayeObject... args)
   {
      return vm.invoke(getField(vm, methodIndex), this, args);
   }

   public LayeObject instantiate(LayeVM vm, String ctorName, LayeObject... args)
   {
      throw new LayeException(vm, "Attempt to instantiate %s.", getClass().getSimpleName());
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
         throw new LayeException(vm, "Attempt to perform prefix operation '%s' on %s.",
               op.image, getClass().getSimpleName());
      }
      return(vm.invoke(prefix, this));
   }

   public LayeObject infix(LayeVM vm, Operator op, LayeObject that)
   {
      LayeObject infix = typedef.infix.get(op);
      if (infix == null)
      {
         throw new LayeException(vm, "Attempt to perform infix operation '%s' on %s.",
               op.image, getClass().getSimpleName());
      }
      return(vm.invoke(infix, this, that));
   }
}
