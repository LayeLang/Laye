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

import java.util.HashMap;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import net.fudev.faxlib.collections.List;

/**
 * @author Sekai Kyoretsuna
 */
public @RequiredArgsConstructor
class SharedState
{
   final LayeVM mainThread;
   
   private final HashMap<String, LayeObject> shared = new HashMap<>();
   private final HashMap<String, LayeKit> registeredKits = new HashMap<>();
   private final List<LayeVM> sideThreads = new List<>();
   
   public LayeObject load(String key)
   {
      LayeObject value = shared.getOrDefault(key, registeredKits.getOrDefault(key, null));
      return(value != null ? value : LayeObject.NULL);
   }

   public void store(String key, LayeObject object)
   {
      shared.put(key, object);
   }
   
   public void addSideThread(LayeVM thread)
   {
      sideThreads.append(thread);
   }
   
   public void eachSideThread(Consumer<? super LayeVM> consumer)
   {
      sideThreads.forEach(consumer);
   }

   public void registerKit(String key, LayeKit kit)
   {
      registeredKits.put(key, kit);
   }
   
   public void useAll(String kitKey)
   {
      LayeKit kit = registeredKits.get(kitKey);
      kit.fields.forEach((key, value) ->
      {
         shared.put(key.image, value);
      });
   }
   
   public void use(String kitKey, String... indices)
   {
      LayeKit kit = registeredKits.get(kitKey);
      for (String index : indices)
      {
         shared.put(index, kit.getField(mainThread, index));
      }
   }
}
