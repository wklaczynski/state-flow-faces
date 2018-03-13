/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.annotation;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

@Target({TYPE})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface FlowAction {
    String value();
    String namespaceURI() default "http://xmlns.jcp.org/flow/custom";
}


