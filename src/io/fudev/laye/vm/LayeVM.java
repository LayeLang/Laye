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

import java.util.Arrays;
import java.util.HashMap;

import io.fudev.laye.LayeException;
import io.fudev.laye.struct.FunctionPrototype;
import io.fudev.laye.struct.Operator;
import io.fudev.laye.struct.OuterValueInfo;
import io.fudev.laye.struct.TypePrototype;

/**
 * The Laye virtual machine.
 * 
 * @author Sekai Kyoretsuna
 */
public
class LayeVM
   extends LayeObject
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
   
   /**
    * Creates a new virtual machine. This virtual machine is initialized with an empty
    * {@link SharedState} to store global variables in.
    */
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
      return "LayeVM:TODO"; // TODO(kai): Add a toString to LayeVM.
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((haltValue == null) ? 0 : haltValue.hashCode());
      result = prime * result + ((parent == null) ? 0 : parent.hashCode());
      result = prime * result + ((stack == null) ? 0 : stack.hashCode());
      result = prime * result + ((state == null) ? 0 : state.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof LayeVM))
      {
         return false;
      }
      LayeVM other = (LayeVM) obj;
      if (haltValue == null)
      {
         if (other.haltValue != null)
         {
            return false;
         }
      }
      else if (!haltValue.equals(other.haltValue))
      {
         return false;
      }
      if (parent == null)
      {
         if (other.parent != null)
         {
            return false;
         }
      }
      else if (!parent.equals(other.parent))
      {
         return false;
      }
      if (stack == null)
      {
         if (other.stack != null)
         {
            return false;
         }
      }
      else if (!stack.equals(other.stack))
      {
         return false;
      }
      if (state == null)
      {
         if (other.state != null)
         {
            return false;
         }
      }
      else if (!state.equals(other.state))
      {
         return false;
      }
      return true;
   }

   CallStack getCallStack()
   {
      return(stack);
   }
   
   /**
    * Attempts to invoke the target LayeObject. This is equivalent to calling the
    * {@link LayeObject#invoke(LayeVM, LayeObject, LayeObject...)} method using this
    * vm as the first argument.
    * 
    * @param target
    *    The object to invoke
    * @param thisObject
    *    The object to invoke {@code target} on as a method. This may be null
    * @param args
    *    The arguments to passed to {@code target} on invocation
    * @return
    *    The result of the invocation. {@link LayeObject#NULL} if no value is returned
    */
   public LayeObject invoke(LayeObject target, LayeObject thisObject, LayeObject... args)
   {
      return(target.invoke(this, thisObject, args));
   }

   /**
    * Invokes the target LayeFunction. This is equivalent to calling the
    * {@link LayeFunction#invoke(LayeVM, LayeObject, LayeObject...)} method using this
    * vm as the first argument.
    * 
    * @param target
    *    The function to invoke
    * @param thisObject
    *    The object to invoke {@code target} on as a method. This may be null
    * @param args
    *    The arguments to passed to {@code target} on invocation
    * @return
    *    The result of the invocation. {@link LayeObject#NULL} if no value is returned
    */
   public LayeObject invoke(LayeFunction target, LayeObject thisObject, LayeObject... args)
   {
      return(target.invoke(this, thisObject, args));
   }

   /**
    * Invokes the target LayeClosure. This is equivalent to calling the
    * {@link LayeClosure#invoke(LayeVM, LayeObject, LayeObject...)} method using this
    * vm as the first argument.
    * 
    * @param target
    *    The function to invoke
    * @param thisObject
    *    The object to invoke {@code target} on as a method. This may be null
    * @param args
    *    The arguments to passed to {@code target} on invocation
    * @return
    *    The result of the invocation. {@link LayeObject#NULL} if no value is returned
    */
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
            if (c == argc - 1 && vargs)
            {
               LayeList vargsList = new LayeList(Arrays.copyOfRange(args, c, args.length));
               arg = vargsList;
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
      TypePrototype[] types = closure.proto.definedTypes;
      
      while (top.ip < codeLength)
      {
         executeInstruction(code[top.ip++], openOuters, captures, top, consts, nested, types);
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
   
   LayeClosure buildClosure(FunctionPrototype proto, OuterValue[] openOuters)
   {
      StackFrame top = stack.getTop();
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
      return(closure);
   }
   
   private void executeInstruction(int insn, OuterValue[] openOuters, OuterValue[] captures,
         StackFrame top, Object[] consts, FunctionPrototype[] nested, TypePrototype[] types)
   {
      //System.out.println(top.stackPointer + ": " + Arrays.toString(top.stack));
      //System.out.println(String.format("0x%02X\n", insn & MAX_OP));
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
         
         case OP_LOAD_LOCAL:
         {
            top.push(top.load(insn >>> POS_C));
         } return;
         case OP_STORE_LOCAL:
         {
            int c;
            LayeObject temp;
            if ((temp = top.load(c = insn >>> POS_C)) instanceof LayeReference)
            {
               ((LayeReference)temp).store(this, top.top());
            }
            else
            {
               top.store(c, top.top());
            }
         } return;
         case OP_LOAD_OUTER:
         {
            top.push(captures[insn >>> POS_C].getValue());
         } return;
         case OP_STORE_OUTER:
         {
            captures[insn >>> POS_C].setValue(this, top.top());
         } return;
         case OP_LOAD_GLOBAL:
         {
            top.push(state.load((String)consts[insn >>> POS_C]));
         } return;
         case OP_STORE_GLOBAL:
         {
            String key = (String)consts[insn >>> POS_C];
            LayeObject temp;
            if ((temp = state.load(key)) instanceof LayeReference)
            {
               ((LayeReference)temp).store(this, top.top());
            }
            else
            {
               state.store(key, top.top());
            }
         } return;
         case OP_LOAD_INDEX:
         {
            LayeObject index = top.pop();
            top.push(top.pop().load(this, index));
         } return;
         case OP_STORE_INDEX:
         {
            LayeObject value = top.pop(), index = top.pop();
            LayeObject target = top.pop(), temp;
            if ((temp = target.load(this, index)) instanceof LayeReference)
            {
               ((LayeReference)temp).store(this, value);
            }
            else
            {
               target.store(this, index, value);
            }
            top.push(value);
         } return;
         case OP_LOAD_FIELD:
         {
            top.push(top.pop().getField(this, (String)consts[insn >>> POS_C]));
         } return;
         case OP_STORE_FIELD:
         {
            String index = (String)consts[insn >>> POS_C];
            LayeObject value = top.pop(), target = top.pop(), temp;
            if (target.hasField(this, index) && (temp = target.getField(this, index)) instanceof LayeReference)
            {
               ((LayeReference)temp).store(this, value);
            }
            else
            {
               target.setField(this, index, value);
            }
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
            top.push(buildClosure(proto, openOuters));
         } return;
         case OP_TYPE:
         {
            TypePrototype proto = types[insn >>> POS_C];
            top.push(new LayeTypeDef(this, proto, openOuters));
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
            top.push(top.pop().invoke(this, null, args));
         } return;
         case OP_INVOKE_METHOD:
         {
            String index = (String)consts[(insn >>> POS_A) & MAX_A];
            LayeObject args[] = top.popCount((insn >>> POS_B) & MAX_B);
            top.push(top.pop().invokeMethod(this, index, args));
         } return;
         case OP_INVOKE_BASE:
         {
            // TODO(kai): invoke base in the vm
         } return;
         case OP_NEW_INSTANCE:
         {
            LayeObject[] args = top.popCount((insn >>> POS_B) & MAX_B);
            top.push(top.pop().instantiate(this, (String)consts[(insn >>> POS_A) & MAX_A], args));
         } return;
         
         case OP_JUMP:
         {
            top.ip = insn >>> POS_C;
         } return;
         case OP_JUMP_EQ:
         {
            if (top.pop().compareEquals(this, top.pop()))
            {
               top.ip = insn >>> POS_C;
            }
         } return;
         case OP_JUMP_NEQ:
         {
            if (!top.pop().compareEquals(this, top.pop()))
            {
               top.ip = insn >>> POS_C;
            }
         } return;
         case OP_JUMP_TRUE:
         {
            if (top.pop().toBool(this))
            {
               top.ip = insn >>> POS_C;
            }
         } return;
         case OP_JUMP_FALSE:
         {
            if (!top.pop().toBool(this))
            {
               top.ip = insn >>> POS_C;
            }
         } return;

         case OP_COMP_EQ:
         {
            top.push(top.pop().compareEquals(this, top.pop()) ?
                  LayeBool.BOOL_TRUE : LayeBool.BOOL_FALSE);
         } return;
         case OP_COMP_NEQ:
         {
            top.push(top.pop().compareEquals(this, top.pop()) ?
                  LayeBool.BOOL_FALSE : LayeBool.BOOL_TRUE);
         } return;

         case OP_PREFIX:
         {
            top.push(top.pop().prefix(this, (Operator)consts[insn >>> POS_C]));
         } return;
         case OP_INFIX:
         {
            LayeObject right = top.pop().deref(this);
            top.push(top.pop().infix(this, (Operator)consts[insn >>> POS_C], right));
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
            top.push(top.pop().toBool(this) ? FALSE : TRUE);
         } return;
         case OP_BOOL_AND:
         {
            LayeObject value = top.top();
            if (value.toBool(this))
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
            if (value.toBool(this))
            {
               top.ip = insn >>> POS_C;
            }
            else
            {
               top.pop();
            }
         } break;
         
         case OP_REF:
         {
            switch ((insn >>> POS_A) & MAX_A)
            {
               case 0: // Global
               {
                  top.push(new LayeGlobalReference(state,
                        (String)consts[(insn >>> POS_B) & MAX_B]));
               } return;
               case 1: // Local
               {
                  top.push(new LayeLocalReference(top, (insn >>> POS_B) & MAX_B));
               } return;
               case 2: // Outer
               {
                  top.push(new LayeOuterReference(captures, (insn >>> POS_B) & MAX_B));
               } return;
               case 3: // Index
               {
                  LayeObject index = top.pop();
                  top.push(new LayeIndexReference(this, top.pop(), index));
               } return;
               case 4: // Field
               {
                  top.push(new LayeFieldReference(this, top.pop(),
                        (String)consts[(insn >>> POS_B) & MAX_B]));
               } return;
               default:
               {
                  // TODO(kai): error when invalid reference type
               }
            }
         } return;
         case OP_DEREF:
         {
            // Force a deref, of course
            LayeObject value = top.pop();
            if (!(value instanceof LayeReference))
            {
               throw new LayeException(this, "attempt to dereference %s.",
                     value.getClass().getSimpleName());
            }
            top.push(value.deref(this));
         } return;
         
         case OP_MATCH:
         {
            @SuppressWarnings("unchecked")
            HashMap<LayeObject, Integer> lookup =
                  (HashMap<LayeObject, Integer>)consts[insn >>> POS_C];
            LayeObject value = top.pop();
            Integer jump = lookup.get(value);
            if (jump == null)
            {
               jump = lookup.get(null);
            }
            top.ip = jump.intValue();
         } return;
         
         case OP_THIS:
         {
            top.push(top.thisObject);
         } return;
      }
   }
}
