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
package io.fudev.laye.struct;

/**
 * @author Sekai Kyoretsuna
 */
public
class LocalValueInfo
{
   private static final int IS_OUTER_VALUE = -1;
   
   public Identifier name;
   public int location;
   public int startOp = 0, endOp = 0;
   
   public LocalValueInfo(Identifier name, int location)
   {
      this.location = location;
      this.name = name;
   }
   
   public LocalValueInfo(LocalValueInfo that)
   {
      this(that.name, that.location, that.startOp, that.endOp);
   }
   
   public LocalValueInfo(Identifier name, int location, int startOp, int endOp)
   {
      this.name = name;
      this.location = location;
      this.startOp = startOp;
      this.endOp = endOp;
   }

   public void markAsOuterValue()
   {
      endOp = IS_OUTER_VALUE;
   }
   
   public boolean isOuterValue()
   {
      return(endOp == IS_OUTER_VALUE);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + endOp;
      result = prime * result + location;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + startOp;
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
      if (!(obj instanceof LocalValueInfo))
      {
         return false;
      }
      LocalValueInfo other = (LocalValueInfo) obj;
      if (endOp != other.endOp)
      {
         return false;
      }
      if (location != other.location)
      {
         return false;
      }
      if (name == null)
      {
         if (other.name != null)
         {
            return false;
         }
      }
      else if (!name.equals(other.name))
      {
         return false;
      }
      if (startOp != other.startOp)
      {
         return false;
      }
      return true;
   }
}
