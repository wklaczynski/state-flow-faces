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
package javax.faces.state.execute;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.faces.context.FacesContext;
import javax.faces.state.scxml.SCXMLExecutor;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ExecutorController implements Externalizable {

    private transient SCXMLExecutor _executor;
    private transient String _path;

    public SCXMLExecutor getExecutor() {
        return _executor;
    }

    public void setExecutor(SCXMLExecutor executor) {
        this._executor = executor;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(_path);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _path = in.readUTF();
    }

    public String getExecutePath(FacesContext context) {
        if (_path == null && _executor != null) {
            _path = _executor.getId();
        }
        return _path;
    }

}
