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

import static io.ylf.laye.vm.Instruction.*;

import io.ylf.laye.struct.FunctionPrototype;
import io.ylf.laye.struct.OuterValueInfo;
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
   private final SharedState state;
   
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
   
   public LayeObject invoke(LayeObject target, LayeObject thisValue, LayeObject... args)
   {
      if (target instanceof LayeClosure)
      {
         return(invoke((LayeClosure)target, thisValue, args));
      }
      return(null);
   }
   
   public LayeObject invoke(LayeClosure closure, LayeObject thisValue, LayeObject... args)
   {
      stack.pushFrame(closure, thisValue);
      
      final OuterValue[] openOuters = closure.proto.containsClosures ? 
            new OuterValue[closure.proto.maxStackSize] : null;
      
      int argc = closure.proto.argc;
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
      
      while (top.ip++ < codeLength)
      {
         executeInstruction(code[top.ip], openOuters, captures, top, consts);
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
      
      LayeObject result = top.pop();
      stack.popFrame();
      
      return(result);
   }
   
   private void executeInstruction(int insn, OuterValue[] openOuters, OuterValue[] captures,
         StackFrame top, Object[] consts)
   {
      switch (insn & MAX_OP)
      {
         case OP_POP:
         {
            top.pop();
         } return;
         case OP_DUP:
         {
            top.dup();
         } return;

         case OP_LOAD_LOCAL:
         {
            top.push(top.load((insn >>> POS_A) & MAX_A));
         } return;
         case OP_STORE_LOCAL:
         {
            top.store((insn >>> POS_A) & MAX_A, top.pop());
         } return;
         case OP_LOAD_OUTER:
         {
            top.push(captures[(insn >>> POS_A) & MAX_A].getValue());
         } return;
         case OP_STORE_OUTER:
         {
            captures[(insn >>> POS_A) & MAX_A].setValue(top.pop());
         } return;
         case OP_LOAD_GLOBAL:
         {
            top.push(state.load((String)consts[(insn >>> POS_A) & MAX_A]));
         } return;
         case OP_STORE_GLOBAL:
         {
            state.store((String)consts[(insn >>> POS_A) & MAX_A], top.pop());
         } return;
         case OP_LOAD_INDEX:
         {
            LayeObject index = top.pop();
            top.push(top.pop().load(index));
         } return;
         case OP_STORE_INDEX:
         {
            LayeObject value = top.pop(), index = top.pop();
            top.pop().store(index, value);
         } return;

         case OP_NLOAD:
         {
            top.push(LayeNull.INSTANCE);
         } return;
         case OP_CLOAD:
         {
            top.push((LayeObject)consts[(insn >>> POS_A) & MAX_A]);
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

         case OP_CLOSURE:
         {
            FunctionPrototype proto = (FunctionPrototype)consts[(insn >>> POS_A) & MAX_A];
            LayeClosure closure = new LayeClosure(proto);
            OuterValueInfo[] protoOuters = proto.outerValues;
            for (int i = 0; i < protoOuters.length; i++)
            {
               if (protoOuters[i].type == OuterValueInfo.LOCAL)
               {
                  closure.captures[i] = findOuterValue(top.getLocals(), protoOuters[i].pos, openOuters);
               }
               else
               {
                  closure.captures[i] = top.closure.captures[protoOuters[i].pos];
               }
            }
         } return;
         case OP_TYPE:
         {
         } return;

         case OP_CLOSE_OUTERS:
         {
            for (int i = openOuters.length, a = (insn >>> POS_A) & MAX_A; --i >= a;)
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
            LayeObject[] args = top.popCount((insn >>> POS_A) & MAX_A);
            top.push(invoke(top.pop(), null, args));
         } return;
         case OP_INVOKE_METHOD:
         {
            LayeObject args[] = top.popCount((insn >>> POS_A) & MAX_A), target = top.pop();
            top.push(invoke(target, top.pop(), args));
         } return;
         case OP_INVOKE_SUPER:
         {
         } return;
      }
   }
}
