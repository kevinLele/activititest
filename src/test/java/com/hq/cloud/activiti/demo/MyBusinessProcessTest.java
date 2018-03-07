package com.hq.cloud.activiti.demo;

import com.hq.activiti.test.entity.Resource;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * Created by Administrator on 9/8/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:activiti.cfg.xml")
public class MyBusinessProcessTest {

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
     * 发布流程测试
     *
     * @throws FileNotFoundException
     */
    @Test
    public void deployProcessTest() throws FileNotFoundException {
        //String bpmFileName = "helloworld.zip";
        String bpmFileName = "ResourceApply.zip";
        String deployName = "deployName1";
        String filePath = getClass().getClassLoader().getResource("./BPM/" + bpmFileName).getFile();
        File bpmFile = new File(filePath);

        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(bpmFile));
        repositoryService.createDeployment()
                .name(deployName)
                .addZipInputStream(zipInputStream)
                .deploy();
    }

    /**
     * 启动流程测试
     */
    @Test
    @Deployment
    public void startProcessTest() {
        //设置当前人
        identityService.setAuthenticatedUserId("zhangsan");

        Map<String, Object> values = new HashMap<String, Object>();
        ArrayList<String> users = new ArrayList();
        ArrayList<String> userids = new ArrayList<String>();

        userids.add("ddd");
        userids.add("eee");
        userids.add("fff");
        userids.add("ggg");

        values.put("users", "aaa2");
        values.put("userid", userids);
        values.put("groupid", "g1");
        values.put("directManagerId", "ddd");

        runtimeService.startProcessInstanceByKey("ResourceApplyProcess", "aab", values);
    }

    /**
     * 查询流程测试
     */
    @Test
    public void searchTaskTest() {
        List<Task> list = taskService.createTaskQuery().taskCandidateOrAssigned("ddd")
                .orderByTaskCreateTime().asc()
                .list();

        System.out.println(list.size());

        for (Task task : list) {
            System.out.println("---------------------------------------");
            System.out.println(task.getId());
            System.out.println(task.getProcessDefinitionId());
            System.out.println(task.getExecutionId());
        }
    }

    @Test
    public void getBusinessKeyTest() {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId("5001").singleResult();
        System.out.println(pi.getBusinessKey());

    }


    /**
     * 完成任务测试
     */
    @Test
    public void completeTaskTest() {
        Map<String, Object> variables = new HashMap<String, Object>();
        //variables.put("directManagerPass", "true");

        taskService.complete("52503", variables);
    }

    @Test
    public void addGroup() {
        String groupId = "g1";
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setId(groupId);
        groupEntity.setName("name:" + groupId);

        identityService.saveGroup(groupEntity);
    }

    @Test
    public void addUser() {
        String userId = "ddd";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("fn:" + userId);
        userEntity.setLastName("ln:" + userId);

        identityService.saveUser(userEntity);
    }

    @Test
    public void addMembership() {
        identityService.createMembership("ddd", "g1");
    }

    @Test
    public void generateDiagram() {
        String processDefinitionId = "ResourceApplyProcess:1:17504";

        try {
            FileOutputStream fos = new FileOutputStream(new File("test.png"));

            // 根据流程定义ID获得BpmnModel
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

            InputStream imageStream = new DefaultProcessDiagramGenerator()
                    .generateDiagram(bpmnModel, "png",
                            processEngineConfiguration.getActivityFontName(),
                            processEngineConfiguration.getLabelFontName(),
                            processEngineConfiguration.getAnnotationFontName(),
                            processEngineConfiguration.getClassLoader(),
                            1.0);
            IOUtils.copy(imageStream, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void generateProcessTracking() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File("testTracking.png"));
            this.processTracking("ResourceApplyProcess:6:25005", "35001", fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 流程是否已经结束
     *
     * @param processInstanceId 流程实例ID
     * @return
     */
    public boolean isFinished(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().finished()
                .processInstanceId(processInstanceId).count() > 0;
    }

    /**
     * 获得高亮线
     *
     * @param processDefinitionEntity   流程定义实体
     * @param historicActivityInstances 历史活动实体
     * @return 线ID集合
     */
    public List<String> getHighLightedFlows(
            ProcessDefinitionEntity processDefinitionEntity,
            List<HistoricActivityInstance> historicActivityInstances) {

        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
        for (int i = 0; i < historicActivityInstances.size(); i++) {// 对历史流程节点进行遍历
            ActivityImpl activityImpl = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i)
                            .getActivityId());// 得 到节点定义的详细信息
            List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点
            if ((i + 1) >= historicActivityInstances.size()) {
                break;
            }
            ActivityImpl sameActivityImpl1 = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i + 1)
                            .getActivityId());// 将后面第一个节点放在时间相同节点的集合里
            sameStartTimeNodes.add(sameActivityImpl1);
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances
                        .get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances
                        .get(j + 1);// 后续第二个节点
                if (activityImpl1.getStartTime().equals(
                        activityImpl2.getStartTime())) {// 如果第一个节点和第二个节点开始时间相同保存
                    ActivityImpl sameActivityImpl2 = processDefinitionEntity
                            .findActivity(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {// 有不相同跳出循环
                    break;
                }
            }
            List<PvmTransition> pvmTransitions = activityImpl
                    .getOutgoingTransitions();// 取出节点的所有出去的线
            for (PvmTransition pvmTransition : pvmTransitions) {// 对所有的线进行遍历
                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition
                        .getDestination();// 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }

    public void processTracking(String processDefinitionId, String executionId,
                                OutputStream out) throws Exception {
        // 当前活动节点、活动线
        List<String> activeActivityIds = new ArrayList<String>(), highLightedFlows = new ArrayList<String>();

        /**
         * 获得当前活动的节点
         */
        if (this.isFinished(executionId)) {// 如果流程已经结束，则得到结束节点
            activeActivityIds.add(historyService
                    .createHistoricActivityInstanceQuery()
                    .executionId(executionId).activityType("endEvent")
                    .singleResult().getActivityId());
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            activeActivityIds = runtimeService
                    .getActiveActivityIds(executionId);
        }
        /**
         * 获得当前活动的节点-结束
         */

        /**
         * 获得活动的线
         */
        // 获得历史活动记录实体（通过启动时间正序排序，不然有的线可以绘制不出来）
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery().executionId(executionId)
                .orderByHistoricActivityInstanceStartTime().asc().list();
        // 计算活动线
        highLightedFlows = this
                .getHighLightedFlows(
                        (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                                .getDeployedProcessDefinition(processDefinitionId),
                        historicActivityInstances);
        /**
         * 获得活动的线-结束
         */

        /**
         * 绘制图形
         */
        if (null != activeActivityIds) {
            InputStream imageStream = null;
            try {
                // 根据流程定义ID获得BpmnModel
                BpmnModel bpmnModel = repositoryService
                        .getBpmnModel(processDefinitionId);

                // 输出资源内容到相应对象
                imageStream = new DefaultProcessDiagramGenerator()
                        .generateDiagram(bpmnModel,
                                "png",
                                activeActivityIds,
                                highLightedFlows,
                                processEngineConfiguration.getActivityFontName(),
                                processEngineConfiguration.getLabelFontName(),
                                processEngineConfiguration.getAnnotationFontName(),
                                processEngineConfiguration.getClassLoader(),
                                1.0);
                IOUtils.copy(imageStream, out);
            } finally {
                IOUtils.closeQuietly(imageStream);
            }
        }
    }

    /**
     * 获取任务的外部表单
     */
    @Test
    public void getRenderedTaskForm() {
        Object renderedTaskForm = formService.getRenderedTaskForm("");

        //将生成的外部表单返回到前端进行显示
    }

    /**
     * 通过任务表单来提交任务
     */
    @Test
    public void completeTaskByRenderedTaskForm() {
        Map<String, String> formValues = new HashMap<>();
        Task task = taskService.createTaskQuery().taskId("52503").singleResult();
        Map<String, Object> localVariables = task.getTaskLocalVariables();
        Map<String, Object> processVariables = task.getProcessVariables();

        formService.submitTaskFormData("", formValues);
    }

    /**
     * 获取发布包里的资源文件
     */
    @Test
    public void getResourceFile() {
        InputStream is = repositoryService.getResourceAsStream("7501", "forms/start.form");

        try {
            IOUtils.copy(is, System.out);
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取启动流程的表单KEY
     */
    @Test
    public void getStartTaskFormKey() {
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition("ResourceApplyProcess:4:7506");
        boolean hasStartFormKey = processDefinition.hasStartFormKey();
        System.out.println("hasStartFormKey = " + hasStartFormKey);

        String startFormKey = formService.getStartFormKey(processDefinition.getId());
        System.out.println("startFormKey = " + startFormKey);


    }

    /***********************************************************************************/

    /**
     * 获取动态生成的启动表单
     */
    @Test
    public void getStartTaskForm() {
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey("ResourceApplyProcess")
                .latestVersion().singleResult();

        boolean hasStartFormKey = processDefinition.hasStartFormKey();

        if (hasStartFormKey) {
            String startFormKey = formService.getStartFormKey(processDefinition.getId());
            InputStream is = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), startFormKey);
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

            try {
                Template template = new Template(null, new InputStreamReader(is), cfg);
                Map<String, Object> root = new HashMap<>();
                List<Resource> resources = new ArrayList<>();
                root.put("resources", resources);
                resources.add(new Resource("name1", "des1", "owner1"));
                resources.add(new Resource("name2", "des2", "owner2"));
                resources.add(new Resource("name3", "des3", "owner3"));

                StringWriter writer = new StringWriter();
                template.process(root, writer);
                System.out.println(writer.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TemplateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 提交启动表单来启动流程
     * 注意：需要将表单模板中或流程中需要使用的变量（同时这些变量不是表单中用户可以填入的变量）在启动流程时就注入，
     * 因为在任务阶段应该只负责填表单和提交表单（保持任务处理阶段的处理逻辑是通用的）
     */
    @Test
    public void submitStartTaskForm() {
        //设置当前人
        identityService.setAuthenticatedUserId("zhangsan");


        //1.提交表单的方式
        // 从表单获取表单的信息然后构造variables
        /*Map<String, String> formValues = new HashMap<>();
        formValues.put("directManagerId", "ddd");
        formValues.put("directManagerPass", "true");
        formValues.put("deptManagerId", "eee");
        formValues.put("deptManagerPass", "false");

        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey("ResourceApplyProcess")
                .latestVersion().singleResult();

        formService.submitStartFormData(processDefinition.getId(), "aaabbb", formValues);*/

        /* ********************************************************************* */
        //2.直接启动流程的方式
        // 从表单获取表单的信息然后构造variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("directManagerId", "ddd");

        runtimeService.startProcessInstanceByKey("ResourceApplyProcess", "bbbccc1", variables);
    }

    /**
     * 流程开始时的回调
     */
    public void processStartCallback() {
        //将表单模板中或流程中需要使用的变量（同时这些变量不是表单中用户可以填入的变量）在
        //回调方法中注入到Activiti流程引擎中
    }

    /**
     * 流程结束时的回调
     */
    public void processEndCallback() {
        // 通过业务系统流程已结束，业务系统可以进行接下来的业务逻辑处理
    }

    /**
     * 获取待办列表
     */
    @Test
    public void getTodoList() {
        String assignee = "ddd";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime().desc()
                .listPage(1, 10);

        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务办理人：" + task.getAssignee());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("流程定义ID：" + task.getProcessDefinitionId());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("===========================================");
            }
        }
    }

    /**
     * 获取已完成列表
     */
    @Test
    public void getDoneList() {
        String assignee = "ddd"; //历史任务办理人
        List<HistoricTaskInstance> list = historyService
                .createHistoricTaskInstanceQuery()//创建历史任务查询
                .taskAssignee(assignee)//指定办理人
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .list();

        long count = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee).count();
        System.out.println("count = " + count);

        if (list != null && list.size() > 0) {
            for (HistoricTaskInstance historicTaskInstance : list) {
                HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(historicTaskInstance.getProcessInstanceId())
                        .singleResult();

                System.out.println(historicTaskInstance.getStartTime() + "\n"
                        + historicTaskInstance.getEndTime() + "\n"
                        + historicTaskInstance.getDurationInMillis());
                System.out.println("任务ID：" + historicTaskInstance.getId());
                System.out.println("任务名称：" + historicTaskInstance.getName());
                System.out.println("流程实例ID：" + historicTaskInstance.getProcessInstanceId());
                System.out.println("流程定义ID：" + historicTaskInstance.getProcessDefinitionId());
                System.out.println("执行对象ID：" + historicTaskInstance.getExecutionId());
                System.out.println("任务办理人：" + historicTaskInstance.getAssignee());
                System.out.println("业务ID：" + historicProcessInstance.getBusinessKey());
                System.out.println("==========================================================");
            }
        }
    }

    /**
     * 查看待办任务的申请信息
     */
    @Test
    public void getTaskFormInfo() {
        String taskId = "62507";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        String formKey = task.getFormKey();
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        InputStream is = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), formKey);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

        try {
            Template template = new Template(null, new InputStreamReader(is), cfg);
            Map<String, Object> root = new HashMap<>();
            List<Resource> resources = new ArrayList<>();
            root.put("resources", resources);
            resources.add(new Resource("name4", "des1", "owner1"));
            resources.add(new Resource("name5", "des2", "owner2"));
            resources.add(new Resource("name6", "des3", "owner3"));

            StringWriter writer = new StringWriter();
            template.process(root, writer);
            System.out.println(writer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提交表单，任务向流程的下一步任务转移
     */
    @Test
    public void submitTaskForm() {
        String taskId = "92506";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        // 从表单获取表单的信息然后构造variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("finalManagerPass", "true");
        variables.put("resourceOwnerId", "eee");
        variables.put("administratorId", "ddd");

        taskService.complete(task.getId(), variables);
    }
}
