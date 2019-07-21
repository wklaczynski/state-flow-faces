/*
 * Copyright 2019 Waldemar Kłaczyński.
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
package javax.faces.state.component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayDeque;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.state.scxml.SCXMLExecutor;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ExecutorController implements Externalizable {

    public static final String EXECUTOR_CONTROLLER_KEY = "javax.faces.component.EXECUTOR_CONTROLLER_KEY";
    private static final String _CURRENT_EXECUTOR_STACK_KEY
                                = "javax.faces.state.controller.CURRENT_EXECUTOR_STACK_KEY";

    private int _isPushedAsCurrentRefCount = 0;

    private transient SCXMLExecutor _executor;

    public SCXMLExecutor getExecutor() {
        return _executor;
    }

    public void setExecutor(SCXMLExecutor executor) {
        this._executor = executor;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.write(_isPushedAsCurrentRefCount);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _isPushedAsCurrentRefCount = in.readInt();
    }

    public void pushControllerToEl(FacesContext context, ExecutorController controller) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (null == controller) {
            controller = this;
        }

        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<ExecutorController> controllerStack = getControllerStack(_CURRENT_EXECUTOR_STACK_KEY,
                contextAttributes);

        controllerStack.push(controller);
        controller._isPushedAsCurrentRefCount++;
    }

    public void popControllerFromEl(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (_isPushedAsCurrentRefCount < 1) {
            return;
        }

        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<ExecutorController> controllerStack = getControllerStack(_CURRENT_EXECUTOR_STACK_KEY,
                contextAttributes);

        for (ExecutorController topController = controllerStack.peek();
                topController != this;
                topController = controllerStack.peek()) {
            if (topController instanceof ExecutorController) {
                ((ExecutorController) topController).popControllerFromEl(context);

            } else {
                controllerStack.pop();
            }
        }

        controllerStack.pop();
        _isPushedAsCurrentRefCount--;

    }

    public static ExecutorController getControllerStack(FacesContext context) {
        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<ExecutorController> controllerStack = getControllerStack(_CURRENT_EXECUTOR_STACK_KEY,
                contextAttributes);

        return (ExecutorController) controllerStack.peek();
    }

    public static ArrayDeque<ExecutorController> getControllerStack(String keyName, Map<Object, Object> contextAttributes) {
        ArrayDeque<ExecutorController> stack = (ArrayDeque<ExecutorController>) contextAttributes.computeIfAbsent(keyName, (t) -> {
            return new ArrayDeque<>();
        });

        return stack;
    }

}
