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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import io.fudev.laye.LayeException;
import io.fudev.laye.kit.io.LayeFile;
import io.fudev.laye.vm.LayeKit;
import io.fudev.laye.vm.LayeObject;
import io.fudev.laye.vm.LayeVM;

/**
 * @author Sekai Kyoretsuna
 */
public
class KitIO
   extends LayeKit
{
   public KitIO(LayeVM vm)
   {
      setField(vm, "open", this::open);
   }
   
   // TODO(kai): this will be replaced with "new io.File(path, mode, charset)" when that gets implemented.
   public LayeObject open(LayeVM vm, LayeObject thisObject, LayeObject[] args)
   {
      String filePath = null;
      String mode = "r+";
      Charset charset = Charset.defaultCharset();
      
      switch (args.length)
      {
         case 0:
         {
            throw new LayeException(vm, "Expected file path.");
         }
         case 3: charset = Charset.forName(args[2].checkString(vm));
         case 2: mode = args[1].checkString(vm);
         case 1: filePath = args[0].checkString(vm);
                 break;
      }
      
      try
      {
         return(new LayeFile(vm, new File(filePath), mode, charset));
      }
      catch (IOException e)
      {
         throw new LayeException(vm, "failed to open file %s", filePath);
      }
   }
}
