/**
 * Copyright (C) 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formModeler.core.model;


import java.util.Map;
import java.util.TreeMap;

import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.ProjectService;
import org.jbpm.formModeler.api.client.FormEditorContext;
import org.jbpm.formModeler.api.client.FormEditorContextManager;
import org.jbpm.formModeler.api.model.Form;
import org.jbpm.formModeler.api.model.RangeProvider;
import org.jbpm.formModeler.core.rendering.SubformFinderService;
import org.jbpm.formModeler.editor.service.FormModelerService;
import org.kie.commons.io.IOService;
import org.kie.workbench.common.services.datamodeller.util.FileUtils;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

public class RangeProviderForm implements RangeProvider {

    @Inject
    @Named("ioStrategy")
    IOService ioService;

    @Inject
    private Paths paths;

    @Inject
    private ProjectService projectService;

    @Inject
    protected FormModelerService formModelerService;

    @Inject
    protected FormEditorContextManager formEditorContextManager;

    @Override
    public String getType() {
        return Form.RANGE_PROVIDER_FORM;
    }

    @Override
    public Map getRangesMap(String namespace) {
        TreeMap treeMap = new TreeMap<String,String> ();

        FormEditorContext context = formEditorContextManager.getRootEditorContext(namespace);

        if (context == null) return treeMap;

        Path currentForm = (Path) context.getPath();
        String currentFormDirUri = getFormDirUri(currentForm);
        String currentFormName = currentForm.getFileName();

        Project project = projectService.resolveProject(currentForm);

        FileUtils utils  = FileUtils.getInstance();

        List<org.kie.commons.java.nio.file.Path> nioPaths = new ArrayList<org.kie.commons.java.nio.file.Path>();
        nioPaths.add(paths.convert(project.getRootPath()));

        Collection<FileUtils.ScanResult> forms = utils.scan(ioService, nioPaths, "form", true);

        String resourcesPath = paths.convert(projectService.resolveProject(currentForm).getRootPath()).resolve(SubformFinderService.MAIN_RESOURCES_PATH).toUri().getPath();

        Path formPath;
        String formDirUri;
        String formName;
        for (FileUtils.ScanResult form : forms) {
            formPath = paths.convert(form.getFile());
            formDirUri = getFormDirUri(formPath);
            formName = formPath.getFileName();

            if (currentFormDirUri.equals(formDirUri) && !formName.startsWith(".") && !currentFormName.equals(formName)) {
                treeMap.put(formPath.getFileName(), formPath.getFileName());
            }
        }
        return treeMap;
    }

    private String getFormDirUri(Path formPath) {
        return formPath.toURI().substring(0, formPath.toURI().lastIndexOf(formPath.getFileName()) - 1);
    }
}