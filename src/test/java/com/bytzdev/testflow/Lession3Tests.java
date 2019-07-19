package com.bytzdev.testflow;

import org.assertj.core.util.Maps;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
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
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Lession3Tests {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Before
    public void beforeTest() throws SQLException {
        Server.createWebServer().start();
    }

    @Test
    public void testExclusiveGatewayA() {
        deploy("lesson3/exclusive-gateway-a.bpmn20.xml");

        ProcessInstance pi = start("exclusive-gateway", Maps.newHashMap("score", 90));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

        pi = start("exclusive-gateway", Maps.newHashMap("score", 70));

        tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

        pi = start("exclusive-gateway", Maps.newHashMap("score", 40));

        tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    }

    @Test
    public void testExclusiveGatewayB() {
        deploy("lesson3/exclusive-gateway-b.bpmn20.xml");

        ProcessInstance pi = start("exclusive-gateway", Maps.newHashMap("score", 90));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

        pi = start("exclusive-gateway", Maps.newHashMap("score", 70));

        tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

        pi = start("exclusive-gateway", Maps.newHashMap("score", 40));

        tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    }

    @Test
    public void testExclusiveGatewayC() {
        deploy("lesson3/exclusive-gateway-c.bpmn20.xml");

        ProcessInstance pi = start("exclusive-gateway", Maps.newHashMap("score", 90));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

        pi = start("exclusive-gateway", Maps.newHashMap("score", 70));

        tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

        pi = start("exclusive-gateway", Maps.newHashMap("score", 40));

        tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    }

    @Test
    public void testParallelGateway() {
        deploy("lesson3/parallel-gateway.bpmn20.xml");

        ProcessInstance pi = start("parallel-gateway", Maps.newHashMap("score", 90));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

        pi = start("parallel-gateway", Maps.newHashMap("score", 70));

        tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

        pi = start("parallel-gateway", Maps.newHashMap("score", 40));

        tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    }

    @Test
    public void testInclusiveGateway() {
        deploy("lesson3/inclusive-gateway.bpmn20.xml");

        ProcessInstance pi = start("inclusive-gateway", Maps.newHashMap("score", 90));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

        pi = start("inclusive-gateway", Maps.newHashMap("score", 70));

        tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

        pi = start("inclusive-gateway", Maps.newHashMap("score", 40));

        tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    }

    @Test
    public void testDevOps() {
        deploy("lesson3/devops-build.bpmn20.xml");
        deploy("lesson3/devops-release.bpmn20.xml");

        // 发送代码签入信号
        runtimeService.signalEventReceived("code_checkin");

        // 查build流程，因已经结束从历史查
        HistoricProcessInstance buildPi = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("devops-build").singleResult();

        // 查release流程
        ProcessInstance releasePi = runtimeService.createProcessInstanceQuery().processDefinitionKey("devops-release").singleResult();

        // 查人工审核节点任务并完成
        Task task = taskService.createTaskQuery().processInstanceId(releasePi.getProcessInstanceId()).singleResult();
        complete(task.getId());

        // 查release流程，因已经结束从历史查
        HistoricProcessInstance releaseHiPi = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("devops-release").singleResult();

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

    private ProcessInstance start(String processKey, Map<String, Object> vars) {
        return runtimeService.startProcessInstanceByKey(processKey, vars);
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