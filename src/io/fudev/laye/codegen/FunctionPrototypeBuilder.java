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

import static io.fudev.laye.vm.Instruction.*;

import io.fudev.laye.file.ScriptFile;
import io.fudev.laye.struct.FunctionPrototype;
import io.fudev.laye.struct.Identifier;
import io.fudev.laye.struct.LocalValueInfo;
import io.fudev.laye.struct.Operator;
import io.fudev.laye.struct.OuterValueInfo;
import io.fudev.laye.vm.LayeFloat;
import io.fudev.laye.vm.LayeInt;
import lombok.RequiredArgsConstructor;
import net.fudev.faxlib.collections.List;

/**
 * @author Sekai Kyoretsuna
 */
public @RequiredArgsConstructor
class FunctionPrototypeBuilder
{
   // TODO(sekai): Maybe consolidate these?
   
   private final @RequiredArgsConstructor
   class Scope
   {
      public final Scope previous;
      public int initialLocalsSize = locals;
   }
   
   private final @RequiredArgsConstructor
   class Block
   {
      public final Block previous;
      // public int startPosition = currentInsnPos();
   }
   
   public final FunctionPrototypeBuilder parent;
   public final ScriptFile file;
   
   public int numParams = 0;
   public boolean vargs = false;
   
   private int locals = 0;
   private int maxLocals = 0;
   
   private int stackSize = 0;
   private int maxStackSize = 0;
   
   private int outerValueCount = 0;
   
   private final List<Integer> code = new List<>();
   private final List<Object> consts = new List<>();
   private final List<OuterValueInfo> outerValues = new List<>();
   private final List<LocalValueInfo> localValues = new List<>();
   private final List<FunctionPrototype> nested = new List<>();
   // TODO(sekai): add jump tables for match expressions
   
   private Scope scope = null;
   private Block block = null;
   
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
   
   // ===== Scopes/Blocks
   
   public void startScope()
   {
      scope = new Scope(scope);
   }
   
   public void endScope()
   {
      int oldOuters = outerValueCount;
      if (locals != scope.initialLocalsSize)
      {
         setLocalCount(scope.initialLocalsSize);
         if (oldOuters != outerValueCount)
         {
            opCloseOuters(scope.initialLocalsSize);
         }
      }
      scope = scope.previous;
   }
   
   public void startBlock()
   {
      block = new Block(block);
   }
   
   public void endBlock()
   {
      // TODO(sekai): Old implementations changed return codes here, check?
      block = block.previous;
   }
   
   // ===== Locals
   
   public int addParameter(Identifier name)
   {
      numParams++;
      return(addLocal(name));
   }
   
   public int addLocal(Identifier name)
   {
      final int local = allocateLocal(name);
      if (local == -1)
      {
         throw new IllegalArgumentException(
               "local variable '" + name + "' already defined in function.");
      }
      return(local);
   }
   
   public int getLocalLocation(Identifier name)
   {
      for (final LocalValueInfo var : localValues)
      {
         if (name.equals(var.name))
         {
            return(var.location);
         }
      }
      return(-1);
   }
   
   public Identifier getLocalName(int local)
   {
      for (final LocalValueInfo var : localValues)
      {
         if (var.location == local)
         {
            return(var.name);
         }
      }
      return(null);
   }
   
   public void setLocalCount(int n)
   {
      while (locals > n)
      {
         locals--;
         final LocalValueInfo var = localValues.removeAt(locals);
         if (var.isOuterValue())
         {
            outerValueCount--;
         }
         var.endOp = currentInsnPos();
      }
   }
   
   public void markLocalAsOuter(int local)
   {
      localValues.get(local).markAsOuterValue();
      outerValueCount++;
   }
   
   private int allocateLocal(Identifier name)
   {
      if (name == null)
      {
         throw new IllegalArgumentException("name cannot be null");
      }
      for (LocalValueInfo var : localValues)
      {
         if (name.equals(var.name))
         {
            return(-1);
         }
      }
      int pos = locals;
      localValues.append(new LocalValueInfo(name, pos));
      if ((locals++) > maxLocals)
      {
         maxLocals = locals;
      }
      return(pos);
   }
   
   // ===== Outers
   
   public int getOuterLocation(Identifier name)
   {
      final int outerSize = outerValues.size();
      for (int i = 0; i < outerSize; i++)
      {
         if (outerValues.get(i).name.equals(name))
         {
            return(i);
         }
      }
      int pos = -1;
      if (parent != null)
      {
         pos = parent.getLocalLocation(name);
         if (pos == -1)
         {
            pos = parent.getOuterLocation(name);
            if (pos != -1)
            {
               outerValues.append(new OuterValueInfo(name, pos, OuterValueInfo.Type.OUTER));
               return(outerValues.size() - 1);
            }
         }
         else
         {
            parent.markLocalAsOuter(pos);
            outerValues.append(new OuterValueInfo(name, pos, OuterValueInfo.Type.LOCAL));
            return(outerValues.size() - 1);
         }
      }
      return(-1);
   }
   
   public Identifier getOuterName(int outer)
   {
      for (final OuterValueInfo var : outerValues)
      {
         if (var.pos == outer)
         {
            return var.name;
         }
      }
      return(null);
   }
   
   // ===== Stack Manipulation
   
   public void changeStackSize(final int amt)
   {
      stackSize += amt;
      if (stackSize > maxStackSize)
      {
         maxStackSize = stackSize;
      }
      else if (stackSize < 0)
      {
         throw new IllegalStateException("stackSize cannot be negative");
      }
   }
   
   public void increaseStackSize()
   {
      changeStackSize(1);
   }
   
   public void decreaseStackSize()
   {
      changeStackSize(-1);
   }
   
   // ===== Constants
   
   public int addConstant(Object constant)
   {
      int result = consts.indexOf(constant);
      if (result == -1)
      {
         result = consts.size();
         consts.append(constant);
      }
      return(result);
   }
   
   // ===== Instruction management
   
   public int currentInsnPos()
   {
      return(code.size() - 1);
   }
   
   private void appendOp(int op)
   {
      code.append(op);
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
      return(currentInsnPos());
   }
   
   public int opPop()
   {
      decreaseStackSize();
      appendOp(OP_POP);
      return(currentInsnPos());
   }
   
   public int opDup()
   {
      increaseStackSize();
      appendOp(OP_DUP);
      return(currentInsnPos());
   }
   
   public int opLoadLocal()
   {
      increaseStackSize();
      appendOp(OP_LOAD_LOCAL);
      return(currentInsnPos());
   }
   
   public int opStoreLocal()
   {
      decreaseStackSize();
      appendOp(OP_STORE_LOCAL);
      return(currentInsnPos());
   }
   
   public int opLoadOuter()
   {
      increaseStackSize();
      appendOp(OP_LOAD_OUTER);
      return(currentInsnPos());
   }
   
   public int opStoreOuter()
   {
      decreaseStackSize();
      appendOp(OP_STORE_OUTER);
      return(currentInsnPos());
   }
   
   public int opLoadGlobal()
   {
      increaseStackSize();
      appendOp(OP_LOAD_GLOBAL);
      return(currentInsnPos());
   }
   
   public int opStoreGlobal()
   {
      decreaseStackSize();
      appendOp(OP_STORE_GLOBAL);
      return(currentInsnPos());
   }
   
   public int opLoadIndex()
   {
      decreaseStackSize();
      appendOp(OP_LOAD_INDEX);
      return(currentInsnPos());
   }
   
   public int opStoreIndex()
   {
      changeStackSize(-2);
      appendOp(OP_STORE_INDEX);
      return(currentInsnPos());
   }
   
   public int opNLoad()
   {
      increaseStackSize();
      appendOp(OP_NLOAD);
      return(currentInsnPos());
   }
   
   public int opCLoad(int constIndex)
   {
      increaseStackSize();
      appendOp_C(OP_CLOAD, constIndex);
      return(currentInsnPos());
   }
   
   public int opILoad(long value)
   {
      increaseStackSize();
      switch ((int)value)
      {
         case -1:
         {
            appendOp(OP_ILOADM1);
         } break;
         case 0:
         {
            appendOp(OP_ILOAD0);
         } break;
         case 1:
         {
            appendOp(OP_ILOAD1);
         } break;
         case 2:
         {
            appendOp(OP_ILOAD2);
         } break;
         case 3:
         {
            appendOp(OP_ILOAD3);
         } break;
         case 4:
         {
            appendOp(OP_ILOAD4);
         } break;
         case 5:
         {
            appendOp(OP_ILOAD5);
         } break;
         default:
         {
            int cIndex = addConstant(LayeInt.valueOf(value));
            opCLoad(cIndex);
         } break;
      }
      return(currentInsnPos());
   }
   
   public int opFLoad(double value)
   {
      increaseStackSize();
      if (value == -1.0)
      {
         appendOp(OP_FLOADM1);
      }
      else if (value == 0.0)
      {
         appendOp(OP_FLOAD0);
      }
      else if (value == 1.0)
      {
         appendOp(OP_FLOAD0);
      }
      else if (value == 2.0)
      {
         appendOp(OP_FLOAD0);
      }
      else
      {
         int cIndex = addConstant(LayeFloat.valueOf(value));
         opCLoad(cIndex);
      }
      return(currentInsnPos());
   }
   
   public int opBLoad(boolean value)
   {
      increaseStackSize();
      appendOp(value ? OP_BLOADT : OP_BLOADF);
      return(currentInsnPos());
   }
   
   public int opClosure(FunctionPrototype prototype)
   {
      increaseStackSize();
      int nIndex = nested.size();
      nested.append(prototype);
      appendOp_C(OP_CLOSURE, nIndex);
      return(currentInsnPos());
   }
   
   // TODO(sekai): opType
   
   public int opCloseOuters(int index)
   {
      appendOp_C(OP_CLOSE_OUTERS, index);
      return(currentInsnPos());
   }
   
   public int opInvoke(int nargs)
   {
      changeStackSize(-nargs);
      appendOp_C(OP_INVOKE, nargs);
      return(currentInsnPos());
   }
   
   public int opInvokeMethod(int nargs)
   {
      changeStackSize(-nargs - 1);
      appendOp_C(OP_INVOKE_METHOD, nargs);
      return(currentInsnPos());
   }
   
   // TODO(sekai): opInvokeBase
   
   public int opJump(int to)
   {
      appendOp_C(OP_JUMP, to);
      return(currentInsnPos());
   }
   
   public int opJumpEq(int to)
   {
      changeStackSize(-2);
      appendOp_C(OP_JUMP_EQ, to);
      return(currentInsnPos());
   }
   
   public int opJumpNeq(int to)
   {
      changeStackSize(-2);
      appendOp_C(OP_JUMP_NEQ, to);
      return(currentInsnPos());
   }
   
   public int opJumpTrue(int to)
   {
      decreaseStackSize();
      appendOp_C(OP_JUMP_TRUE, to);
      return(currentInsnPos());
   }
   
   public int opJumpFalse(int to)
   {
      decreaseStackSize();
      appendOp_C(OP_JUMP_FALSE, to);
      return(currentInsnPos());
   }
   
   public int opCompEq()
   {
      decreaseStackSize();
      appendOp(OP_COMP_EQ);
      return(currentInsnPos());
   }
   
   public int opCompNeq()
   {
      decreaseStackSize();
      appendOp(OP_COMP_NEQ);
      return(currentInsnPos());
   }
   
   public int opPrefix(Operator op)
   {
      int oIndex = addConstant(op);
      appendOp_C(OP_PREFIX, oIndex);
      return(currentInsnPos());
   }
   
   public int opInfix(Operator op)
   {
      decreaseStackSize();
      int oIndex = addConstant(op);
      appendOp_C(OP_INFIX, oIndex);
      return(currentInsnPos());
   }
   
   public int opList(int count)
   {
      changeStackSize(1 - count);
      appendOp_C(OP_LIST, count);
      return(currentInsnPos());
   }
   
   public int opTuple(int count)
   {
      changeStackSize(1 - count);
      appendOp_C(OP_TUPLE, count);
      return(currentInsnPos());
   }
}
