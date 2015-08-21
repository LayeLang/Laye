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
import io.fudev.laye.struct.TypePrototype;

/**
 * @author Sekai Kyoretsuna
 */
public
class LayeTypeDef
   extends LayeObject
{
   HashMap<String, LayeObject> methods = new HashMap<>();
   HashMap<String, LayeObject> prefix = new HashMap<>(), infix = new HashMap<>();
   
   LayeObject invoke = null;
   
   private LayeObject initCtor = null;
   private HashMap<String, LayeObject> ctors = new HashMap<>();
   
   public LayeTypeDef()
   {
      super(null);
   }
   
   LayeTypeDef(LayeVM vm, TypePrototype proto, OuterValue[] openOuters)
   {
      this();
      
      // TODO(kai): separate public/private ctor access.
      proto.publicCtors.forEach((name, ctor) ->
      {
         ctors.put(name, vm.buildClosure(ctor, openOuters));
      });

      proto.publicMethods.forEach((name, method) ->
      {
         methods.put(name, vm.buildClosure(method, openOuters));
      });
      
      if (proto.invoke != null)
      {
         invoke = vm.buildClosure(proto.invoke, openOuters);
      }
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((ctors == null) ? 0 : ctors.hashCode());
      result = prime * result + ((infix == null) ? 0 : infix.hashCode());
      result = prime * result + ((initCtor == null) ? 0 : initCtor.hashCode());
      result = prime * result + ((methods == null) ? 0 : methods.hashCode());
      result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (!super.equals(obj))
      {
         return false;
      }
      if (!(obj instanceof LayeTypeDef))
      {
         return false;
      }
      LayeTypeDef other = (LayeTypeDef) obj;
      if (ctors == null)
      {
         if (other.ctors != null)
         {
            return false;
         }
      }
      else if (!ctors.equals(other.ctors))
      {
         return false;
      }
      if (infix == null)
      {
         if (other.infix != null)
         {
            return false;
         }
      }
      else if (!infix.equals(other.infix))
      {
         return false;
      }
      if (initCtor == null)
      {
         if (other.initCtor != null)
         {
            return false;
         }
      }
      else if (!initCtor.equals(other.initCtor))
      {
         return false;
      }
      if (methods == null)
      {
         if (other.methods != null)
         {
            return false;
         }
      }
      else if (!methods.equals(other.methods))
      {
         return false;
      }
      if (prefix == null)
      {
         if (other.prefix != null)
         {
            return false;
         }
      }
      else if (!prefix.equals(other.prefix))
      {
         return false;
      }
      return true;
   }

   // TODO(kai): Move this into LayeObject
   private boolean isInvokable(LayeObject object)
   {
      return(object instanceof LayeComposable ||
            (object.typedef != null && object.typedef.invoke != null));
   }
   
   @Override
   public LayeObject getField(LayeVM vm, String key)
   {
      if (methods.containsKey(key))
      {
         return(methods.get(key));
      }
      return(super.getField(vm, key));
   }

   @Override
   public void setField(LayeVM vm, String key, LayeObject object)
   {
      if (isInvokable(object))
      {
         methods.put(key, object);
      }
      else
      {
         // TODO(kai): more clarity?
         throw new LayeException(vm,
               "TypeDefs may only contain methods for objects to reference, cannot assign %s.",
               object.getClass().getSimpleName());
      }
   }

   @Override
   public LayeObject getPrefix(LayeVM vm, String operator)
   {
      throw new LayeException(vm, "Prefix %s does not exist.", operator);
   }

   @Override
   public void setPrefix(LayeVM vm, String operator, LayeObject object)
   {
      if (isInvokable(object))
      {
         prefix.put(operator, object);
      }
      else
      {
         // TODO(kai): more clarity?
         throw new LayeException(vm,
               "TypeDefs may only contain methods for objects to reference, cannot assign %s.",
               object.getClass().getSimpleName());
      }
   }

   @Override
   public LayeObject getInfix(LayeVM vm, String operator)
   {
      throw new LayeException(vm, "Infix %s does not exist.", operator);
   }

   @Override
   public void setInfix(LayeVM vm, String operator, LayeObject object)
   {
      if (isInvokable(object))
      {
         infix.put(operator, object);
      }
      else
      {
         // TODO(kai): more clarity?
         throw new LayeException(vm,
               "TypeDefs may only contain methods for objects to reference, cannot assign %s.",
               object.getClass().getSimpleName());
      }
   }

   @Override
   public LayeObject instantiate(LayeVM vm, String ctorName, LayeObject... args)
   {
      LayeObject result = new LayeObject(this);
      if (initCtor != null)
      {
         vm.invoke(initCtor, result);
      }
      LayeObject ctor = ctors.get(ctorName);
      if (ctor == null)
      {
         if (ctorName != null)
         {
            throw new LayeException(vm, "ctor '%s' does not exist.", ctorName);
         }
         throw new LayeException(vm, "default ctor does not exist.");
      }
      vm.invoke(ctor, result, args);
      return(result);
   }
   
   public void addMethod(String name, LayeObject value)
   {
      if (methods.get(name) != null)
      {
         return;
      }
      methods.put(name, value);
   }
   
   public void addCtor(String name, LayeObject value)
   {
      if (ctors.get(name) != null)
      {
         return;
      }
      ctors.put(name, value);
   }
}
