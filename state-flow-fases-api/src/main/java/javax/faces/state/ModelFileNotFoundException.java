/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ModelFileNotFoundException extends ModelException {


    public ModelFileNotFoundException() {
        super();
    }
    
    public ModelFileNotFoundException(Throwable cause) {
        super(cause);
    }

    public ModelFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelFileNotFoundException(String message) {
        super(message);
    }
    
    
}
