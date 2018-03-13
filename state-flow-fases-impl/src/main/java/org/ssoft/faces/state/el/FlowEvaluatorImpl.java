/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.el;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.facelets.compiler.CompilationMessageHolder;
import com.sun.faces.facelets.compiler.CompilationMessageHolderImpl;
import com.sun.faces.facelets.tag.TagLibrary;
import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowContext;
import javax.faces.state.FlowEvaluator;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.FlowExpressionException;
import org.ssoft.faces.state.FlowContextImpl;
import org.w3c.dom.Node;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowEvaluatorImpl implements FlowEvaluator, Serializable {
    
    private static final Pattern inFct = Pattern.compile("In\\(");
    private static final Pattern dataFct = Pattern.compile("Data\\(");
    private final ApplicationAssociate associate;
    private final StateFlowExecutor executor;

    public FlowEvaluatorImpl(StateFlowExecutor executor) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        associate = ApplicationAssociate.getInstance(ctx.getExternalContext());
        this.executor = executor;
    }
    
    @Override
    public Object eval(FlowContext ctx, String expr) throws FlowExpressionException {
        if (expr == null) {
            return null;
        }
        CompilationMessageHolder messageHolder = new CompilationMessageHolderImpl();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext, executor, messageHolder);
        try {
            fcontext.putContext(FlowContext.class, ctx);
            fcontext.putContext(StateFlowExecutor.class, executor);

            String evalExpr = inFct.matcher(expr).replaceAll("In(_ALL_STATES, ");
            evalExpr = dataFct.matcher(evalExpr).replaceAll("Data(_ALL_NAMESPACES, ");
            ValueExpression ve = ef.createValueExpression(context, evalExpr, Object.class);
            return ve.getValue(context);
        } catch (PropertyNotFoundException e) {
            throw new FlowExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } catch (ELException e) {
            throw new FlowExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } finally {
            fcontext.putContext(FlowContext.class, newContext(null));
            messageHolder.processCompilationMessages(fc);
        }
    }

    public void evalSet(FlowContext ctx, String expr, Object value) throws FlowExpressionException {
        if (expr == null) {
            return;
        }
        CompilationMessageHolder messageHolder = new CompilationMessageHolderImpl();

        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext, executor, messageHolder);
        try {
            fcontext.putContext(FlowContext.class, ctx);
            fcontext.putContext(StateFlowExecutor.class, executor);

            String evalExpr = inFct.matcher(expr).replaceAll("In(_ALL_STATES, ");
            evalExpr = dataFct.matcher(evalExpr).replaceAll("Data(_ALL_NAMESPACES, ");
            ValueExpression ve = ef.createValueExpression(context, evalExpr, Object.class);
            ve.setValue(context, value);
        } catch (PropertyNotFoundException e) {
            throw new FlowExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } catch (ELException e) {
            throw new FlowExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } finally {
            fcontext.putContext(FlowContext.class, newContext(null));
            messageHolder.processCompilationMessages(fc);
        }
    }

    @Override
    public Boolean evalCond(FlowContext ctx, String expr) throws FlowExpressionException {
        if (expr == null) {
            return null;
        }
        CompilationMessageHolder messageHolder = new CompilationMessageHolderImpl();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext, executor, messageHolder);
        try {
            fcontext.putContext(FlowContext.class, ctx);
            fcontext.putContext(StateFlowExecutor.class, executor);

            String evalExpr = inFct.matcher(expr).replaceAll("In(_ALL_STATES, ");
            evalExpr = dataFct.matcher(evalExpr).replaceAll("Data(_ALL_NAMESPACES, ");
            ValueExpression ve = ef.createValueExpression(context, evalExpr, Boolean.class);
            return (Boolean) ve.getValue(context);
        } catch (PropertyNotFoundException e) {
            throw new FlowExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } catch (ELException e) {
            throw new FlowExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } finally {
            fcontext.putContext(FlowContext.class, newContext(null));
            messageHolder.processCompilationMessages(fc);
        }
    }

    @Override
    public Node evalLocation(FlowContext ctx, String expr) throws FlowExpressionException {
        if (expr == null) {
            return null;
        }
        CompilationMessageHolder messageHolder = new CompilationMessageHolderImpl();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext, executor, messageHolder);
        try {
            fcontext.putContext(FlowContext.class, ctx);
            fcontext.putContext(StateFlowExecutor.class, executor);

            String evalExpr = inFct.matcher(expr).replaceAll("In(_ALL_STATES, ");
            evalExpr = dataFct.matcher(evalExpr).replaceAll("Data(_ALL_NAMESPACES, ");
            ValueExpression ve = ef.createValueExpression(context, evalExpr, Node.class);
            Node node = (Node) ve.getValue(context);
            return node;
        } catch (PropertyNotFoundException e) {
            throw new FlowExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } catch (ELException e) {
            throw new FlowExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } finally {
            fcontext.putContext(FlowContext.class, newContext(null));
        }
    }

    @Override
    public FlowContext newContext(FlowContext parent) {
        return new FlowContextImpl(parent);
    }
    
    public class ContextWrapper extends ELContext implements Serializable {

        private final ELContext context;
        private final CompositeFunctionMapper functionMapper;
        private final VariableMapper variableMapper;
        private final CompositeELResolver elResolver;

        private ContextWrapper(ELContext context, StateFlowExecutor executor, CompilationMessageHolder messageHolder) {
            super();
            this.context = context;
            functionMapper = new CompositeFunctionMapper();
            variableMapper = new BuiltinVariableMapper(context.getVariableMapper());

            functionMapper.add(new BuiltinFunctionMapper());
            
            com.sun.faces.facelets.compiler.Compiler compiler = associate.getCompiler();
            TagLibrary tagLibrary = compiler.createTagLibrary(messageHolder);
            Map namespaces = executor.getStateMachine().getNamespaces();
            
            functionMapper.add(new TagsFunctionMapper(namespaces, tagLibrary));
            
            functionMapper.add(context.getFunctionMapper());

            elResolver = new CompositeELResolver();
            elResolver.add(new FlowELResolver());
            elResolver.add(context.getELResolver());
        }

        @Override
        public ELResolver getELResolver() {
            return elResolver;
        }

        @Override
        public FunctionMapper getFunctionMapper() {
            return functionMapper;
        }

        @Override
        public VariableMapper getVariableMapper() {
            return variableMapper;
        }

        @Override
        public Object getContext(Class key) {
            Object ret = super.getContext(key);
            if (ret == null) {
                ret = context.getContext(key);
            }
            return ret;
        }
    }

    static class BuiltinVariableMapper extends VariableMapper implements Serializable {

        private final VariableMapper mapper;

        public BuiltinVariableMapper(VariableMapper mapper) {
            super();
            this.mapper = mapper;
        }

        @Override
        public ValueExpression resolveVariable(String variable) {
            return mapper.resolveVariable(variable);
        }

        @Override
        public ValueExpression setVariable(String variable, ValueExpression expression) {
            return mapper.setVariable(variable, expression);
        }
    }
    
}
