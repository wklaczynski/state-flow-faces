/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

import javax.el.FunctionMapper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface FunctionMapperHolder {

    /**
     * Set the {@link FunctionMapper} to use.
     *
     * @param functionMapper The path resolver to use.
     */
    void setFunctionMapper(FunctionMapper functionMapper);

    /**
     * Get the {@link FunctionMapper}.
     *
     * @return The path resolver in use.
     */
    FunctionMapper getFunctionMapper();

}
