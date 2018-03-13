/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Final extends State {

    /**
     * Default no-args constructor for Digester. Sets
     * <code>isFinal</code> property of this <code>State</code>
     * to be <code>true</code>.
     */
    public Final() {
        super();
        this.setFinal(true);
    }

}
