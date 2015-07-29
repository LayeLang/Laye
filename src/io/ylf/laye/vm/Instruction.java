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

/**
 * @author Sekai Kyoretsuna
 */
public final
class Instruction
{
   public static final int SIZE_OP = 8;
   public static final int SIZE_A  = 12;
   public static final int SIZE_B  = 12;
   public static final int SIZE_C  = SIZE_A + SIZE_B;

   public static final int POS_OP = 0;
   public static final int POS_A  = SIZE_OP;
   public static final int POS_B  = POS_A + SIZE_A;
   public static final int POS_C  = POS_A;

   public static final int MAX_OP = (1 << SIZE_OP) - 1;
   public static final int MAX_A  = (1 << SIZE_A) - 1;
   public static final int MAX_B  = (1 << SIZE_B) - 1;
   public static final int MAX_C  = (1 << SIZE_C) - 1;
   
   // ===== Op Codes
   
   public static final byte OP_NOP              = 0x00;
   
   public static final byte OP_POP              = 0x01;
   public static final byte OP_DUP              = 0x02;

   public static final byte OP_LOAD_LOCAL       = 0x03;
   public static final byte OP_STORE_LOCAL      = 0x04;
   public static final byte OP_LOAD_OUTER       = 0x05;
   public static final byte OP_STORE_OUTER      = 0x06;
   public static final byte OP_LOAD_GLOBAL      = 0x07;
   public static final byte OP_STORE_GLOBAL     = 0x08;
   public static final byte OP_LOAD_INDEX       = 0x09;
   public static final byte OP_STORE_INDEX      = 0x0A;

   public static final byte OP_NLOAD            = 0x0B;
   public static final byte OP_CLOAD            = 0x0C;
   
   public static final byte OP_ILOADM1          = 0x0D;
   public static final byte OP_ILOAD0           = 0x0E;
   public static final byte OP_ILOAD1           = 0x0F;
   public static final byte OP_ILOAD2           = 0x10;
   public static final byte OP_ILOAD3           = 0x11;
   public static final byte OP_ILOAD4           = 0x12;
   public static final byte OP_ILOAD5           = 0x13;
   
   public static final byte OP_FLOADM1          = 0x14;
   public static final byte OP_FLOAD0           = 0x15;
   public static final byte OP_FLOAD1           = 0x16;
   public static final byte OP_FLOAD2           = 0x17;

   public static final byte OP_BLOADT           = 0x18;
   public static final byte OP_BLOADF           = 0x19;

   public static final byte OP_CLOSURE          = 0x1A;
   public static final byte OP_TYPE             = 0x1B;
   
   public static final byte OP_CLOSE_OUTERS     = 0x1C;
   public static final byte OP_INVOKE           = 0x1D;
   public static final byte OP_INVOKE_METHOD    = 0x1E;
   public static final byte OP_INVOKE_BASE      = 0x1F;

   public static final byte OP_JUMP             = 0x20;
   public static final byte OP_JUMP_EQ          = 0x21;
   public static final byte OP_JUMP_NEQ         = 0x22;
   public static final byte OP_JUMP_TRUE        = 0x23;
   public static final byte OP_JUMP_FALSE       = 0x24;

   public static final byte OP_COMP_EQ          = 0x25;
   public static final byte OP_COMP_NEQ         = 0x26;

   public static final byte OP_PREFIX           = 0x27;
   public static final byte OP_INFIX            = 0x28;
   
   private Instruction()
   {
   }
}
