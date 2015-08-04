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
package io.fudev.laye.jobject;

import javax.swing.JFrame;

import io.fudev.laye.vm.LayeInt;
import io.fudev.laye.vm.LayeObject;
import io.fudev.laye.vm.LayeTuple;
import io.fudev.laye.vm.LayeVM;

/**
 * @author Sekai Kyoretsuna
 */
public final
class LayeWindow extends JFrame
{
   private static final long serialVersionUID = -7218160507649582736L;
   
   private LayeTuple sizeCached;
   
   public LayeWindow()
   {
   }
   
   //@LayeCtor()
   public void ctor(LayeVM vm, LayeObject... args)
   {
   }
   
   //@LayeSetter(name = "size")
   public void setSize(LayeVM vm, LayeObject arg)
   {
      // TODO(sekai): error checks
      LayeObject widthObj = arg.load(vm, LayeInt.I0), heightObj = arg.load(vm, LayeInt.I1);
      int width = widthObj.intValue(vm), height = heightObj.intValue(vm);
      super.setSize(width, height);
      sizeCached = new LayeTuple(LayeInt.valueOf(width), LayeInt.valueOf(height));
   }
   
   //@LayeGetter(name = "size")
   public LayeObject getSize(LayeVM vm)
   {
      return(sizeCached);
   }
   
   //@LayeMethod(name = "Center")
   public LayeObject center(LayeVM vm, LayeObject... args)
   {
      setLocationRelativeTo(null);
      pack();
      return(LayeObject.NULL);
   }
}
