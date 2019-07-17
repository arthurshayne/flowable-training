package com.bytzdev.testflow;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.h2.tools.Server;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Lession2Tests {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Before
    public void beforeTest() throws SQLException {
        Server.createWebServer().start();
    }

    @Test
    public void testMultipleInstance() {
        deploy("lesson2/consensus.bpmn20.xml");

        ProcessInstance pi = start("consensus");

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

        for (Task task : tasks) {
            taskService.complete(task.getId());
        }
    }

    @Test
    public void testGateway() {
        deploy("lesson2/parallelenjoy.bpmn20.xml");
        ProcessInstance pi = start("parallelenjoy");

        // 完成【开车到4S店】
        Task task = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
        taskService.complete(task.getId());


        // 完成 【车去保养】
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
        taskService.complete(tasks.stream().filter(p -> "车去保养".equals(p.getName())).findFirst().get().getId());

        // 完成 【我去玩耍】
        taskService.complete(tasks.stream().filter(p -> "我去玩耍".equals(p.getName())).findFirst().get().getId());

        // 完成【开车回家】
        task = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
        taskService.complete(task.getId());
    }

    @Test
    public void testInlineSub() {
        deploy("lesson2/inlinesub.bpmn20.xml");
        ProcessInstance pi = start("inlinesub");

        // 完成【主流】
        Task task = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
        taskService.complete(task.getId());

        // 完成【非主流】
        task = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
        taskService.complete(task.getId());
    }

    @Test
    public void testCallSub() {
        deploy("lesson2/callsub.bpmn20.xml");
        deploy("lesson2/inlinesub.bpmn20.xml");
        ProcessInstance pi = start("callsub");

        // 完成【大主流】
        Task task = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
        taskService.complete(task.getId());

        ProcessInstance subpi = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getProcessInstanceId()).singleResult();

        // 完成【主流】
        task = taskService.createTaskQuery().processInstanceId(subpi.getProcessInstanceId()).singleResult();
        taskService.complete(task.getId());

        // 完成【非主流】
        task = taskService.createTaskQuery().processInstanceId(subpi.getProcessInstanceId()).singleResult();
        taskService.complete(task.getId());
    }

    @Test
    public void claim() {
        taskService.claim("22e2f944-5a7e-11e9-82c4-f48c508a7d6c", "assingeem");
    }

    private void deploy(String classPathResource) {
        repositoryService
                .createDeployment()
                .addClasspathResource(classPathResource)
                .deploy();
    }

    private ProcessInstance start(String processKey) {
        return runtimeService.startProcessInstanceByKey(processKey);
    }

    private void complete(String taskId) {
        taskService.complete(taskId);
    }
}

//      SELECT
//        EXE.ID_ EXEID,
//        EXE.PROC_INST_ID_,
//        EXE.PARENT_ID_,
//        EXE.IS_ACTIVE_,
//        TSK.ID_ TSKID,
//        TSK.NAME_
//      FROM ACT_RU_EXECUTION  EXE
//      LEFT JOIN ACT_RU_TASK TSK ON
//        EXE.ID_ = TSK.EXECUTION_ID_
//      ORDER BY PARENT_ID_