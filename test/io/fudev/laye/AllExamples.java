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
package io.fudev.laye;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

import io.fudev.laye.api.LayeScript;
import io.fudev.laye.file.ScriptFile;
import io.fudev.laye.vm.LayeClosure;

/**
 * @author Sekai Kyoretsuna
 */
public final 
class AllExamples
{
   private static final boolean preCompile = false;

   public static void main(String[] unused) throws IOException
   {
      LayeScript script = new LayeScript();
      File examplesDirectory = new File("./examples/");
      File[] examples = examplesDirectory.listFiles();

      if (examples != null)
      {
         if (preCompile)
         {
            LayeClosure[] compileScripts = new LayeClosure[examples.length];
            
            int i = 0;
            long nanoStart = System.nanoTime();
            for (File example : examples)
            {
               ScriptFile file = ScriptFile.fromFile(example.getPath());
               compileScripts[i++] = script.compile(file);
            }
            long nanoEnd = System.nanoTime(), total = (nanoEnd - nanoStart);
            System.out.println(total + " nanoseconds to compile all examples! (" +
                  (total / 1_000_000) + " milliseconds)");
            
            i = 0;
            nanoStart = System.nanoTime();
            for (File example : examples)
            {
               System.out.println("> Running " + example.getPath());
               script.invoke(compileScripts[i++]);
               System.out.println();
            }
            nanoEnd = System.nanoTime();
            total = (nanoEnd - nanoStart);
            System.out.println(total + " nanoseconds to run all examples! (" +
                  (total / 1_000_000) + " milliseconds)");
         }
         else
         {
            long nanoStart = System.nanoTime();
            for (File example : examples)
            {
               ScriptFile file = ScriptFile.fromFile(example.getPath());
               System.out.println("> Running " + example.getPath());
               script.doFile(file);
               System.out.println();
            }
            long nanoEnd = System.nanoTime(), total = (nanoEnd - nanoStart);
            System.out.println(total + " nanoseconds to run all examples! (" +
                  (total / 1_000_000) + " milliseconds)");
         }
      }
   }
}