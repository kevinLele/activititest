package com.hq.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class MyTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String taskId = delegateTask.getId();
        String taskDefinitionKey = delegateTask.getTaskDefinitionKey();

        System.out.println("MyTaskListener:");
        System.out.println("taskId = " + taskId);
        System.out.println("taskDefinitionKey = " + taskDefinitionKey);
    }
}
