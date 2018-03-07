package com.hq.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;

/**
 * 用于监听资源审批流程结束事件
 *
 * @author Kevin
 */
public class ResourceApplyProcessEndListener implements ExecutionListener {

    private Expression applyStatus;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String businessKey = execution.getProcessBusinessKey();

        System.out.println("资源申请审批流程已结束！");
        System.out.println("businessKey = " + businessKey);
        System.out.println("applyStatusText = " + applyStatus.getExpressionText());
        System.out.println("applyStatusValue = " + applyStatus.getValue(execution));

        System.out.println("通过业务ID去修改业务字段的值");
    }
}
