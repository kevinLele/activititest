package com.hq.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class MyExecutionListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String businessKey = execution.getProcessBusinessKey();

        System.out.println("MyExecutionListener:");
        System.out.println("businessKey = " + businessKey);
    }
}
