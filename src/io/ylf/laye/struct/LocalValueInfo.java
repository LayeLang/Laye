/**
 * Copyright (C) 2015 Sekai Kyoretsuna
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package io.ylf.laye.struct;

/**
 * @author Sekai Kyoretsuna
 */
public
class LocalValueInfo
{
   public static final int IS_UP_VALUE = -1;
   
   public String name;
   public int location;
   public int startOp = 0, endOp = 0;
   public boolean isConst;
   
   public LocalValueInfo(final String name, final int location, final boolean isConst)
   {
      this.location = location;
      this.name = name;
      this.isConst = isConst;
   }
   
   private LocalValueInfo(final String name, final int location, final int startOp, final int endOp,
         final boolean isConst)
   {
      this.location = location;
      this.name = name;
      this.startOp = startOp;
      this.endOp = endOp;
      this.isConst = isConst;
   }
   
   public LocalValueInfo copy()
   {
      return new LocalValueInfo(name, location, startOp, endOp, isConst);
   }
}
