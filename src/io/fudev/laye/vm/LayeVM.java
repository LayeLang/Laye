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

import static io.fudev.laye.vm.Instruction.*;

import io.fudev.laye.struct.FunctionPrototype;
import io.fudev.laye.struct.OuterValueInfo;
import lombok.EqualsAndHashCode;

/**
 * @author Sekai Kyoretsuna
 */
public @EqualsAndHashCode(callSuper = false)
class LayeVM extends LayeObject
{
   private static OuterValue findOuterValue(LayeObject[] locals, int idx, OuterValue[] openUps)
   {
      final int n = openUps.length;
      for (int i = 0; i < n; i++)
      {
         if (openUps[i] != null && openUps[i].getIndex() == idx)
         {
            return openUps[i];
         }
      }
      for (int i = 0; i < n; i++)
      {
         if (openUps[i] == null)
         {
            return openUps[i] = new OuterValue(locals, idx);
         }
      }
      throw new IllegalArgumentException("no space for new upvalue.");
   }
   
   private final LayeVM parent;
   
   private final CallStack stack = new CallStack();
   public final SharedState state;
   
   private LayeObject haltValue;
   
   public LayeVM()
   {
      this.parent = null;
      state = new SharedState(this);
   }
   
   private LayeVM(LayeVM parent)
   {
      state = (this.parent = parent).state;
      state.addSideThread(this);
   }
   
   @Override
   public String toString()
   {
      return "LayeVM:TODO"; // FIXME(sekai): Add a toString to LayeVM.
   }
   
   public LayeObject invoke(LayeObject target, LayeObject thisObject, LayeObject... args)
   {
      if (target instanceof LayeClosure)
      {
         return(invoke((LayeClosure)target, thisObject, args));
      }
      else if (target instanceof LayeFunction)
      {
         return(((LayeFunction)target).invoke(this, thisObject, args));
      }
      throw new IllegalArgumentException("target (" + target.getClass().getSimpleName() + ")");
   }
   
   public LayeObject invoke(LayeClosure closure, LayeObject thisObject, LayeObject... args)
   {
      stack.pushFrame(closure, thisObject);
      
      final OuterValue[] openOuters = closure.proto.nestedClosures.length != 0 ? 
            new OuterValue[closure.proto.maxStackSize] : null;
      
      int argc = closure.proto.numParams;
      boolean vargs = closure.proto.vargs;
      
      StackFrame top = stack.getTop();
      
      for (int c = 0; c < args.length; c++)
      {
         LayeObject arg;
         if (c < argc)
         {
            if (c == args.length - 1 && vargs)
            {
               // FIXME(sekai): create a vargs list
               arg = null;
            }
            else
            {
               arg = args[c];
            }
         }
         else
         {
            arg = LayeNull.INSTANCE;
         }
         top.store(c, arg);
      }
      
      int[] code = closure.proto.code;
      int codeLength = code.length;
      OuterValue[] captures = closure.captures;
      Object[] consts = closure.proto.consts;
      FunctionPrototype[] nested = closure.proto.nestedClosures;
      
      while (top.ip < codeLength)
      {
         executeInstruction(code[top.ip++], openOuters, captures, top, consts, nested);
      }
      
      if (openOuters != null)
      {
         for (int u = openOuters.length; --u >= 0;)
         {
            if (openOuters[u] != null)
            {
               openOuters[u].close();
            }
         }
      }
      
      LayeObject result;
      if (top.hasValue())
      {
         result = top.pop();
      }
      else
      {
         result = NULL;
      }
      stack.popFrame();
      
      return(result);
   }
   
   private void executeInstruction(int insn, OuterValue[] openOuters, OuterValue[] captures,
         StackFrame top, Object[] consts, FunctionPrototype[] nested)
   {
      switch (insn & MAX_OP)
      {
         default:
         {
            throw new IllegalArgumentException();
         }
         
         case OP_NOP:
         { // Do nothing... This is nice, I like breaks<3
         } return;
         
         case OP_POP:
         {
            top.pop();
         } return;
         case OP_DUP:
         {
            top.dup();
         } return;
         
         // TODO(sekai): stores DON'T pop the value.

         case OP_LOAD_LOCAL:
         {
            top.push(top.load(insn >>> POS_C));
         } return;
         case OP_STORE_LOCAL:
         {
            top.store(insn >>> POS_C, top.top());
         } return;
         case OP_LOAD_OUTER:
         {
            top.push(captures[insn >>> POS_C].getValue());
         } return;
         case OP_STORE_OUTER:
         {
            captures[insn >>> POS_C].setValue(top.top());
         } return;
         case OP_LOAD_GLOBAL:
         {
            top.push(state.load((String)consts[insn >>> POS_C]));
         } return;
         case OP_STORE_GLOBAL:
         {
            state.store((String)consts[insn >>> POS_C], top.top());
         } return;
         case OP_LOAD_INDEX:
         {
            LayeObject index = top.pop();
            top.push(top.pop().load(this, index));
         } return;
         case OP_STORE_INDEX:
         {
            LayeObject value = top.pop(), index = top.pop();
            top.pop().store(this, index, value);
            top.push(value);
         } return;

         case OP_NLOAD:
         {
            top.push(LayeNull.INSTANCE);
         } return;
         case OP_CLOAD:
         {
            top.push((LayeObject)consts[insn >>> POS_C]);
         } return;
         
         case OP_ILOADM1:
         {
            top.push(LayeInt.IM1);
         } return;
         case OP_ILOAD0:
         {
            top.push(LayeInt.I0);
         } return;
         case OP_ILOAD1:
         {
            top.push(LayeInt.I1);
         } return;
         case OP_ILOAD2:
         {
            top.push(LayeInt.I2);
         } return;
         case OP_ILOAD3:
         {
            top.push(LayeInt.I3);
         } return;
         case OP_ILOAD4:
         {
            top.push(LayeInt.I4);
         } return;
         case OP_ILOAD5:
         {
            top.push(LayeInt.I5);
         } return;
         
         case OP_FLOADM1:
         {
            top.push(LayeFloat.FM1);
         } return;
         case OP_FLOAD0:
         {
            top.push(LayeFloat.F0);
         } return;
         case OP_FLOAD1:
         {
            top.push(LayeFloat.F1);
         } return;
         case OP_FLOAD2:
         {
            top.push(LayeFloat.F2);
         } return;

         case OP_BLOADT:
         {
            top.push(LayeBool.BOOL_TRUE);
         } return;
         case OP_BLOADF:
         {
            top.push(LayeBool.BOOL_FALSE);
         } return;
         
         case OP_CLOSURE:
         {
            FunctionPrototype proto = nested[insn >>> POS_C];
            LayeClosure closure = new LayeClosure(proto);
            OuterValueInfo[] protoOuters = proto.outerValues;
            for (int i = 0; i < protoOuters.length; i++)
            {
               if (protoOuters[i].type == OuterValueInfo.Type.LOCAL)
               {
                  closure.captures[i] = findOuterValue(top.getLocals(), protoOuters[i].pos, openOuters);
               }
               else
               {
                  closure.captures[i] = top.closure.captures[protoOuters[i].pos];
               }
            }
            top.push(closure);
         } return;
         case OP_TYPE:
         {
            // TODO(sekai): create types in the vm
         } return;

         case OP_CLOSE_OUTERS:
         {
            for (int i = openOuters.length, a = insn >>> POS_C; --i >= a;)
            {
               if (openOuters[i] != null)
               {
                  openOuters[i].close();
                  openOuters[i] = null;
               }
            }
         } return;
         case OP_INVOKE:
         {
            LayeObject[] args = top.popCount(insn >>> POS_C);
            top.push(invoke(top.pop(), null, args));
         } return;
         case OP_INVOKE_METHOD:
         {
            LayeObject args[] = top.popCount(insn >>> POS_C), index = top.pop();
            top.push(top.pop().invokeMethod(this, index, args));
         } return;
         case OP_INVOKE_BASE:
         {
            // TODO(sekai): invoke base in the vm
         } return;
         
         case OP_JUMP:
         {
            top.ip = insn >>> POS_C;
         } return;
         case OP_JUMP_EQ:
         {
            if (top.pop().compareEquals(top.pop()))
            {
               top.ip = insn >>> POS_C;
            }
         } return;
         case OP_JUMP_NEQ:
         {
            if (!top.pop().compareEquals(top.pop()))
            {
               top.ip = insn >>> POS_C;
            }
         } return;
         case OP_JUMP_TRUE:
         {
            if (top.pop().toBool())
            {
               top.ip = insn >>> POS_C;
            }
         } return;
         case OP_JUMP_FALSE:
         {
            if (!top.pop().toBool())
            {
               top.ip = insn >>> POS_C;
            }
         } return;

         case OP_COMP_EQ:
         {
            top.push(top.pop().compareEquals(top.pop()) ?
                  LayeBool.BOOL_TRUE : LayeBool.BOOL_FALSE);
         } return;
         case OP_COMP_NEQ:
         {
            top.push(top.pop().compareEquals(top.pop()) ?
                  LayeBool.BOOL_FALSE : LayeBool.BOOL_TRUE);
         } return;

         case OP_PREFIX:
         {
            top.push(top.pop().prefix(this, (String)consts[insn >>> POS_C]));
         } return;
         case OP_INFIX:
         {
            LayeObject right = top.pop();
            top.push(top.pop().infix(this, (String)consts[insn >>> POS_C], right));
         } return;

         case OP_LIST:
         {
            LayeObject[] elements = top.popCount(insn >>> POS_C);
            top.push(new LayeList(elements));
         } return;
         case OP_TUPLE:
         {
            LayeObject[] elements = top.popCount(insn >>> POS_C);
            top.push(new LayeTuple(elements));
         } return;
         
         case OP_NOT:
         {
            top.push(top.pop().toBool() ? FALSE : TRUE);
         } return;
         case OP_BOOL_AND:
         {
            LayeObject value = top.top();
            if (value.toBool())
            {
               top.pop();
            }
            else
            {
               top.ip = insn >>> POS_C;
            }
         } break;
         case OP_BOOL_OR:
         {
            LayeObject value = top.top();
            if (value.toBool())
            {
               top.ip = insn >>> POS_C;
            }
            else
            {
               top.pop();
            }
         } break;
      }
   }
}
