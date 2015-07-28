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
package io.ylf.laye.vm;

import java.util.HashMap;

import io.ylf.laye.LayeException;
import lombok.val;

/**
 * @author Sekai Kyoretsuna
 */
public abstract
class LayeObject
{
   public static final LayeNull NULL = LayeNull.INSTANCE;

   public static final LayeBool TRUE  = LayeBool.BOOL_TRUE;
   public static final LayeBool FALSE = LayeBool.BOOL_FALSE;
   
   private final HashMap<LayeObject, LayeObject> fields = new HashMap<>();
   
   public LayeObject()
   {
   }
   
   public abstract String toString();

   public abstract int hashCode();

   public abstract boolean equals(Object obj);
   
   public boolean toBool()
   {
      return(true);
   }
   
   public boolean compareEquals(LayeObject that)
   {
      return(this == that);
   }
   
   public LayeObject load(LayeObject key)
   {
      val result = fields.get(key);
      if (result == null)
      {
         return(LayeNull.INSTANCE);
      }
      return(result);
   }
   
   public void store(LayeObject key, LayeObject object)
   {
      fields.put(key, object);
   }

   public LayeObject prefix(String op)
   {
      // FIXME(sekai): add type name
      throw new LayeException("Attempt to perform prefix operation '%s' on type.", op);
   }

   public LayeObject infix(String op, LayeObject that)
   {
      // FIXME(sekai): add type name
      throw new LayeException("Attempt to perform infix operation '%s' on type.", op);
   }
}
