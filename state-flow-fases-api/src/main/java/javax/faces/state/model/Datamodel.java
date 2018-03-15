/*
 * Copyright 2018 Waldemar Kłaczyński.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.faces.state.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Datamodel implements Serializable {

   /**
    * Serial version UID.
    */
   private static final long serialVersionUID = 1L;

   /**
    * The set of &lt;data&gt; elements, parsed as Elements, that are
    * children of this &lt;datamodel&gt; element.
    */
   private List data;

   /**
    * Constructor.
    */
   public Datamodel() {
       this.data = new ArrayList();
   }

   /**
    * Get all the data children of this datamodel.
    *
    * @return Returns the data.
    */
   public final List getData() {
       return data;
   }

   /**
    * Add a Data.
    *
    * @param datum The data child to be added.
    */
   public final void addData(final Data datum) {
       if (datum != null) {
           data.add(datum);
       }
   }

}

