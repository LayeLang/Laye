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
package io.fudev.laye.kit;

import java.io.PrintStream;

import io.fudev.laye.vm.LayeKit;
import io.fudev.laye.vm.LayeObject;
import io.fudev.laye.vm.LayeVM;

/**
 * @author Sekai Kyoretsuna
 */
public
class KitLaye
   extends LayeKit
{
   public PrintStream out = System.out;
   
   public KitLaye(LayeVM vm)
   {
      setField(vm, "println", this::println);
   }
   
   public LayeObject println(LayeVM vm, LayeObject thisObject, LayeObject[] args)
   {
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < args.length; i++)
      {
         if (i > 0)
         {
            result.append(' ');
         }
         result.append(args[i]);
      }
      out.println(result.toString());
      return(LayeObject.NULL);
   }
}
