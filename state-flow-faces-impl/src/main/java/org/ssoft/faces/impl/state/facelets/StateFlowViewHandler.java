package org.ssoft.faces.impl.state.facelets;

/*
 * SCXMLViewHandler.java
 *
 * Created on 6 listopad 2007, 21:38
 *
 * To change this template, choose Tools | Template Manager and open the
 * template in the editor.
 */
import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import org.ssoft.faces.impl.state.config.StateWebConfiguration;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewHandler extends ViewHandlerWrapper {

    private final ViewHandler wrapped;
    private final StateWebConfiguration webcfg;

    /**
     *
     * @param wrapped
     */
    public StateFlowViewHandler(ViewHandler wrapped) {
        this.wrapped = wrapped;
        webcfg = StateWebConfiguration.getInstance();
    }

    @Override
    public ViewHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public UIViewRoot createView(FacesContext fc, String viewId) {
        return super.createView(fc, viewId);
    }

    @Override
    public UIViewRoot restoreView(FacesContext fc, String viewId) {
        return super.restoreView(fc, viewId);

    }

    @Override
    public void initView(FacesContext context) throws FacesException {
        super.initView(context);
    }

    @Override
    public void renderView(FacesContext facesContext, UIViewRoot viewRoot) throws IOException, FacesException {
        super.renderView(facesContext, viewRoot);
    }

    @Override
    public void writeState(FacesContext context) throws IOException {
        super.writeState(context);
    }

}
