package com.hq.cloud.activiti.demo;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
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
    private RepositoryService repositoryService;

    /**
     * 发布流程测试
     * @throws FileNotFoundException
     */
    @Test
    public void deployProcessTest() throws FileNotFoundException {
        String bpmFileName = "ResApply_1.zip";
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
        runtimeService.startProcessInstanceByKey("myProcess","911");
    }

    /**
     * 查询流程测试
     */
    @Test
    public void searchTaskTest(){
        List<Task> list = taskService.createTaskQuery()
                .orderByTaskCreateTime().asc()
                .list();

        System.out.println(list.size());

        for(Task task : list){
            System.out.println(task.getId());
        }
    }

    @Test
    public void getBusinessKeyTest(){
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId("5001").singleResult();
        System.out.println(pi.getBusinessKey());

    }


    /**
     * 完成任务测试
     */
    @Test
    public void completeTaskTest(){
        List<Task> list = taskService.createTaskQuery().list();
        Task task = list.get(0);

        taskService.complete(task.getId());
    }
}
