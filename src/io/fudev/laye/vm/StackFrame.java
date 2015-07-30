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

/**
 * @author Sekai Kyoretsuna
 */
public
class StackFrame
{
   public final StackFrame previous;
   
   public final LayeClosure closure;
   public final LayeObject thisObject;
   
   private final LayeObject[] locals;
   private final LayeObject[] stack;
   
   private int stackPointer = 0;
   
   public int ip = 0;
   
   public StackFrame(StackFrame previous, LayeClosure closure, LayeObject thisObject)
   {
      this.previous = previous;
      this.closure = closure;
      this.thisObject = thisObject;
      this.locals = new LayeObject[closure.proto.maxLocals];
      this.stack = new LayeObject[closure.proto.maxStackSize];
   }
   
   public LayeObject[] getLocals()
   {
      return(locals);
   }
   
   public boolean hasValue()
   {
      return(stackPointer > 0);
   }
   
   // ===== Local Operations
   
   public void store(int index, LayeObject value)
   {
      locals[index] = value;
   }
   
   public LayeObject load(int index)
   {
      return(locals[index]);
   }
   
   // ===== Stack Operations
   
   public void push(LayeObject value)
   {
      stack[stackPointer++] = value;
   }
   
   public void dup()
   {
      stack[stackPointer++] = stack[stackPointer - 2];
   }
   
   public LayeObject pop()
   {
      return(stack[--stackPointer]);
   }
   
   public LayeObject[] popCount(int count)
   {
      LayeObject[] result = new LayeObject[count];
      while (--count >= 0)
      {
         result[count] = pop();
      }
      return(result);
   }
   
   public LayeObject top()
   {
      return(stack[stackPointer - 1]);
   }
}
