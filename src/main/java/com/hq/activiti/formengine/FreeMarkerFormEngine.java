package com.hq.activiti.formengine;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.form.FormEngine;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class FreeMarkerFormEngine implements FormEngine {

    @Override
    public String getName() {
        return "FreeMarker";
    }

    @Override
    public Object renderStartForm(StartFormData startForm) {
        if (startForm.getFormKey() == null) {
            return null;
        }

        String formTemplateString = getFormTemplateString(startForm, startForm.getFormKey());
        System.out.println("formTemplateString = " + formTemplateString);

        return "renderStartForm 测试";
    }

    @Override
    public Object renderTaskForm(TaskFormData taskForm) {
        if (taskForm.getFormKey() == null) {
            return null;
        }

        String formTemplateString = getFormTemplateString(taskForm, taskForm.getFormKey());
        System.out.println("formTemplateString = " + formTemplateString);

        TaskEntity task = (TaskEntity) taskForm.getTask();
        Map<String, Object> instanceVariables = task.getActivityInstanceVariables();
        Map<String, Object> taskLocalVariables = task.getTaskLocalVariables();

        return "renderTaskForm 测试";
    }

    protected String getFormTemplateString(FormData formInstance, String formKey) {
        String deploymentId = formInstance.getDeploymentId();

        ResourceEntity resourceStream = Context
                .getCommandContext()
                .getResourceEntityManager()
                .findResourceByDeploymentIdAndResourceName(deploymentId, formKey);

        if (resourceStream == null) {
            throw new ActivitiObjectNotFoundException("Form with formKey '" + formKey + "' does not exist", String.class);
        }

        byte[] resourceBytes = resourceStream.getBytes();
        String encoding = "UTF-8";
        String formTemplateString;

        try {
            formTemplateString = new String(resourceBytes, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new ActivitiException("Unsupported encoding of :" + encoding, e);
        }

        return formTemplateString;
    }
}
