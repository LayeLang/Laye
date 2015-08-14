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
class OuterValueInfo
{
   public static
   enum Type
   {
      LOCAL, OUTER
   }
   
   public final Identifier name;
   public final int pos;
   public Type type;
   
   public OuterValueInfo(Identifier name, int pos, Type type)
   {
      this.name = name;
      this.pos = pos;
      this.type = type;
   }

   public OuterValueInfo(OuterValueInfo other)
   {
      this(other.name, other.pos, other.type);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + pos;
      result = prime * result + ((type == null) ? 0 : type.hashCode());
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
      if (!(obj instanceof OuterValueInfo))
      {
         return false;
      }
      OuterValueInfo other = (OuterValueInfo) obj;
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
      if (pos != other.pos)
      {
         return false;
      }
      if (type != other.type)
      {
         return false;
      }
      return true;
   }
}
