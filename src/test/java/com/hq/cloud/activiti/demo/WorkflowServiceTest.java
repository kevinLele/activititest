package com.hq.cloud.activiti.demo;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Administrator
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:activiti.cfg.xml")
public class WorkflowServiceTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FormService formService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;

    /**
     * 启动流程
     *
     * @param processKey
     * @param businessKey
     * @param variables
     */
    public String startProcess(String userId, String processKey, String businessKey,
                               String comment, Map<String, Object> variables) {
        //设置当前人，启动流程时会将该UserId记录为流程发起人
        Authentication.setAuthenticatedUserId(userId);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, businessKey, variables);
        taskService.addComment(null, processInstance.getId(), comment);

        return processInstance.getId();
    }

    public long getTodoTaskListCount(String userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId).count();
    }

    /**
     * 获取待办任务列表
     */
    public void getTodoTaskListByPage(String userId, int startRowNum, int pageSize) {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskCreateTime().desc()
                .listPage(startRowNum, pageSize);

        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务办理人：" + task.getAssignee());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("流程定义ID：" + task.getProcessDefinitionId());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());

                String processInstanceId = task.getProcessInstanceId();
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                System.out.println("业务ID: " + processInstance.getBusinessKey());

                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("===========================================");
            }
        }
    }

    public long getDoneTaskListCount(String userId) {
        return historyService
                .createHistoricTaskInstanceQuery()//创建历史任务查询
                .taskAssignee(userId)//指定办理人
                .finished()
                .count();
    }

    /**
     * 获取已办任务列表
     *
     * @param userId
     * @param startRowNum
     * @param pageSize
     */
    public void getDoneTaskListByPage(String userId, int startRowNum, int pageSize) {
        List<HistoricTaskInstance> list = historyService
                .createHistoricTaskInstanceQuery()//创建历史任务查询
                .taskAssignee(userId)//指定办理人
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .listPage(startRowNum, pageSize);

        if (list != null && list.size() > 0) {
            for (HistoricTaskInstance historicTaskInstance : list) {
                HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(historicTaskInstance.getProcessInstanceId()).finished()
                        .singleResult();

                System.out.println(historicTaskInstance.getStartTime() + "\n"
                        + historicTaskInstance.getEndTime() + "\n"
                        + historicTaskInstance.getDurationInMillis());
                System.out.println("任务ID：" + historicTaskInstance.getId());
                System.out.println("任务名称：" + historicTaskInstance.getName());
                System.out.println("流程实例ID：" + historicTaskInstance.getProcessInstanceId());
                System.out.println("业务ID: " + historicProcessInstance.getBusinessKey());
                System.out.println("流程定义ID：" + historicTaskInstance.getProcessDefinitionId());
                System.out.println("执行对象ID：" + historicTaskInstance.getExecutionId());
                System.out.println("任务办理人：" + historicTaskInstance.getAssignee());
                System.out.println("业务ID：" + historicProcessInstance.getBusinessKey());
                System.out.println("==========================================================");
            }
        }
    }


    public void completeTask(String opearatorId, String taskId, String comment, Map<String, Object> variables) {
        Authentication.setAuthenticatedUserId(opearatorId);
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        taskService.addComment(taskId, processInstance.getProcessInstanceId(), comment);
        taskService.complete(taskId, variables);
    }

    @Test
    public void startProcessTest() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("ownerUserId", "zhaoliu");
        variables.put("adminUserId", "wangwu");
        variables.put("operation", "nopass");
        startProcess("zhangsan", "ResourceApplyBaseProcess", "businessKey01",
                "测试批注", variables);
    }

    @Test
    public void getTodoTaskListCountTest() {
        long count = getTodoTaskListCount("lisi");
        System.out.println("count = " + count);
    }

    @Test
    public void getTodoTaskListByPageTest() {
        getTodoTaskListByPage("wangwu", 0, 10);
    }

    @Test
    public void getDoneTaskListCountTest() {
        long count = getDoneTaskListCount("wangwu");
        System.out.println("count = " + count);
    }

    @Test
    public void getDoneTaskListByPageTest() {
        getDoneTaskListByPage("wangwu", 0, 10);
    }

    @Test
    public void completeTaskTest() {
        Map<String, Object> variables = new HashMap<>();
        //variables.put("ownerUserId", "zhaoliu");
        //variables.put("pass", "true");
        variables.put("operation", "pass");
        completeTask("管理员", "5009", "基本同意", variables);
    }

    @Test
    public void getTaskVariables() {
        Task task = taskService.createTaskQuery()
                .taskId("5006")
                .includeTaskLocalVariables()
                .includeProcessVariables()
                .singleResult();
        Map<String, Object> localVariables = task.getTaskLocalVariables();
        System.out.println("localVariables = " + localVariables);

        Map<String, Object> processVariables = task.getProcessVariables();
        System.out.println("processVariables = " + processVariables);
    }

    public void addTaskComment(String taskId, String message) {
        taskService.addComment(taskId, null, "test task comment");
    }

    public void addProcessInstanceComment(String taskId, String processInstanceId, String message) {
        taskService.addComment(null, processInstanceId, "test task comment");
    }

    @Test
    public void getTaskComment() {
        List<Comment> taskcomments = taskService.getTaskComments("12509");
        String processInstanceId = taskService.createTaskQuery().taskId("12509").singleResult().getProcessInstanceId();
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        List<Comment> processComments = taskService.getProcessInstanceComments(pi.getProcessInstanceId());

        System.out.println("taskcomments = " + taskcomments);
        System.out.println("processComments = " + processComments);
    }


}
