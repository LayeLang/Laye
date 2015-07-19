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
package io.ylf.laye.log;

import java.io.PrintStream;

import io.ylf.laye.LogMessageID;
import io.ylf.laye.lexical.Location;

/**
 * @author Sekai Kyoretsuna
 */
public class DetailLogger
{
   private PrintStream out;
   private PrintStream err;
   
   private int errorCount = 0;
   private int warningCount = 0;
   
   public DetailLogger()
   {
      out = System.out;
      err = System.err;
   }
   
   public DetailLogger(PrintStream out, PrintStream err)
   {
      this.out = out;
      this.err = err;
   }
   
   public boolean hasErrors()
   {
      return errorCount > 0;
   }
   
   public int getErrorCount()
   {
      return errorCount;
   }
   
   public boolean hasWarnings()
   {
      return warningCount > 0;
   }
   
   public int getWarningCount()
   {
      return warningCount;
   }
   
   private String getHeader(Location location)
   {
      StringBuilder result = new StringBuilder();
      result.append(location.toString());
      return result.append(": ").toString();
   }
   
   /**
    * @param message
    */
   public void log(String message)
   {
      out.println(message);
   }
   
   /**
    * @param message
    * @param args
    */
   public void logf(String format, Object... args)
   {
      out.printf(format, args);
   }
   
   /**
    * Logs a warning message, incrementing the warning count.
    * @param location
    * @param message
    */
   public void logWarning(Location location, LogMessageID id, String message)
   {
      warningCount++;
      out.printf("[Warning:%d] ", id.id);
      if (location != null)
      {
         out.print(getHeader(location));
      }
      out.println(message);
   }
   
   /**
    * Logs a warning message, formatted, incrementing the warning count.
    * @param location
    * @param message
    * @param args
    */
   public void logWarningf(Location location, LogMessageID id, String format, Object... args)
   {
      warningCount++;
      out.printf("[Warning:%d] ", id.id);
      if (location != null)
      {
         out.print(getHeader(location));
      }
      out.printf(format, args);
   }
   
   /**
    * Logs an error message, incrementing the error count.
    * @param location
    * @param message
    */
   public void logError(Location location, LogMessageID id, String message)
   {
      errorCount++;
      err.printf("[Error:%d] ", id.id);
      if (location != null)
      {
         err.print(getHeader(location));
      }
      err.println(message);
   }
   
   /**
    * Logs an error message, formatted, incrementing the error count.
    * @param location
    * @param message
    * @param args
    */
   public void logErrorf(Location location, LogMessageID id, String format, Object... args)
   {
      errorCount++;
      err.printf("[Error:%d] ", id.id);
      if (location != null)
      {
         err.print(getHeader(location));
      }
      err.printf(format, args);
   }
}
