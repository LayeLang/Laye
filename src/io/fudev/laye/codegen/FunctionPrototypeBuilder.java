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
package io.fudev.laye.codegen;

import io.fudev.laye.file.ScriptFile;
import io.fudev.laye.struct.FunctionPrototype;
import io.fudev.laye.struct.LocalValueInfo;
import io.fudev.laye.struct.OuterValueInfo;
import static io.fudev.laye.vm.Instruction.*;
import lombok.RequiredArgsConstructor;
import net.fudev.faxlib.collections.List;

/**
 * @author Sekai Kyoretsuna
 */
public @RequiredArgsConstructor
class FunctionPrototypeBuilder
{
   private final class Scope
   {
      public final Scope previous;
      public int initialLocalsSize;
      
      // TODO use numUpValues
      // public int numUpValues;
      
      public Scope(final Scope previous)
      {
         this.previous = previous;
         // this.numUpValues = FunctionBuilder.this.getNumUpVals();
         initialLocalsSize = locals;
      }
   }
   
   private final class Block
   {
      public final Block previous;
      
      public int startPosition;
      
      public Block(final Block previous)
      {
         this.previous = previous;
         startPosition = currentInsnPos();
      }
   }
   
   public final FunctionPrototypeBuilder parent;
   public final ScriptFile file;
   
   public int numParams = 0;
   public boolean vargs = false;
   
   private int locals = 0;
   private int maxLocals = 0;
   
   private int stackSize = 0;
   private int maxStackSize = 0;
   
   private final List<Integer> code = new List<>();
   private final List<Object> consts = new List<>();
   private final List<OuterValueInfo> outerValues = new List<>();
   private final List<LocalValueInfo> localValues = new List<>();
   private final List<FunctionPrototype> nested = new List<>();
   // TODO(sekai): add jump tables for match expressions
   
   public FunctionPrototype build()
   {
      int[] code = new int[this.code.size()];
      for (int i = 0; i < code.length; i++)
      {
         code[i] = this.code.get(i);
      }
      Object[] consts = this.consts.toArray();
      OuterValueInfo[] outerValues = this.outerValues.toArray();
      FunctionPrototype[] nested = this.nested.toArray();
      
      FunctionPrototype result = new FunctionPrototype();
      result.numParams = numParams;
      result.vargs = vargs;
      result.maxLocals = maxLocals;
      result.maxStackSize = maxStackSize;
      result.code = code;
      result.consts = consts;
      result.outerValues = outerValues;
      result.nestedClosures = nested;
      
      return(result);
   }
   
   public int currentInsnPos()
   {
      return code.size() - 1;
   }
   
   private void appendOp(int op)
   {
      code.append(op);
   }
   
   private void appendOp_A(int op, int a)
   {
      code.append((op & MAX_OP) | ((a & MAX_A) << POS_A));
   }
   
   private void appendOp_B(int op, int b)
   {
      code.append((op & MAX_OP) | ((b & MAX_B) << POS_B));
   }
   
   private void appendOp_C(int op, int c)
   {
      code.append((op & MAX_OP) | ((c & MAX_C) << POS_C));
   }
   
   private void appendOp_AB(int op, int a, int b)
   {
      code.append((op & MAX_OP) | ((a & MAX_A) << POS_A) |
            ((b & MAX_B) << POS_B));
   }
   
   // ===== Add instructions, woo
   
   public int opNop()
   {
      appendOp(OP_NOP);
      return currentInsnPos();
   }
   
   public int opPop()
   {
      appendOp(OP_POP);
      return currentInsnPos();
   }
   
   public int opDup()
   {
      appendOp(OP_DUP);
      return currentInsnPos();
   }
   
   public int opLoadLocal()
   {
      appendOp(OP_LOAD_LOCAL);
      return currentInsnPos();
   }
   
   public int opStoreLocal()
   {
      appendOp(OP_STORE_LOCAL);
      return currentInsnPos();
   }
   
   public int opLoadOuter()
   {
      appendOp(OP_LOAD_OUTER);
      return currentInsnPos();
   }
   
   public int opStoreOuter()
   {
      appendOp(OP_STORE_OUTER);
      return currentInsnPos();
   }
   
   public int opLoadGlobal()
   {
      appendOp(OP_LOAD_GLOBAL);
      return currentInsnPos();
   }
   
   public int opStoreGlobal()
   {
      appendOp(OP_STORE_GLOBAL);
      return currentInsnPos();
   }
   
   public int opLoadIndex()
   {
      appendOp(OP_LOAD_INDEX);
      return currentInsnPos();
   }
   
   public int opStoreIndex()
   {
      appendOp(OP_STORE_INDEX);
      return currentInsnPos();
   }
}
