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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import io.fudev.collections.List;
import io.fudev.laye.struct.Identifier;

/**
 * @author Sekai Kyoretsuna
 */
public
class LayeJTypeDef
   extends LayeTypeDef
{
   public static LayeJTypeDef get(Class<?> _class)
   {
      LayeJTypeDef result = new LayeJTypeDef(_class);
      // All instance methods
      List<Method> methods = new List<>(_class.getDeclaredMethods())
            .filter(value -> (value.getModifiers() & Modifier.STATIC) == 0);
      methods.get(0);
      return(result);
   }
   
   Class<?> _class;
   
   private final HashMap<Identifier, Method> ctors = new HashMap<>();
   
   private LayeJTypeDef(Class<?> _class)
   {
      this._class = _class;
   }
   
   @Override
   public LayeJObject instantiate(LayeVM vm, Identifier ctorName, LayeObject... args)
   {
      LayeJObject result = new LayeJObject(this);
      Method ctor = ctors.get(ctorName);
      if (ctor == null)
      {
         // TODO(sekai): throw exception.
      }
      try
      {
         Object[] jargs = new Object[args.length + 1];
         System.arraycopy(args, 0, jargs, 1, args.length);
         jargs[0] = vm;
         ctor.invoke(result.instance, jargs);
      }
      catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
      {
         e.printStackTrace();
      }
      return(result);
   }
}
