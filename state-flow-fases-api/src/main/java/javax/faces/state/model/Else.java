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
public class Else extends ElseIf {

    /**
     * &lt;else/&gt; is equivalent to &lt;elseif cond="true" /&gt;.
     */
    public Else() {
        super();
        setCond("true");
    }

}

