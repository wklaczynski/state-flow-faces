/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

