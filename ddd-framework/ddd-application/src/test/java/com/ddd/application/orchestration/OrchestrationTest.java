package com.ddd.application.orchestration;

import com.ddd.application.command.ICommand;
import com.ddd.application.command.ICommandBus;
import com.ddd.application.query.IQuery;
import com.ddd.application.query.IQueryBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 业务编排框架单元测试（Java 8兼容）
 *
 * @author anthem37
 * @date 2025/8/15 19:45:00
 */
class OrchestrationTest {

    private TestCommandBus commandBus;
    private TestQueryBus queryBus;
    private Orchestration orchestration;

    @BeforeEach
    void setUp() {
        commandBus = new TestCommandBus();
        queryBus = new TestQueryBus();
        orchestration = new Orchestration("test-orchestration", "测试编排", commandBus, queryBus);
    }

    @Test
    void testConstructor() {
        assertEquals("test-orchestration", orchestration.getId());
        assertEquals("测试编排", orchestration.getName());
    }

    @Test
    void testAddGeneric() {
        Orchestration result = orchestration.addGeneric("gen1", "通用节点", 
            ctx -> "generic result");
        
        assertSame(orchestration, result);
    }

    @Test
    void testExecute_SimpleGenericFlow() {
        // 构建简单流程
        orchestration
            .addGeneric("node1", "节点1", ctx -> "result1")
            .addGeneric("node2", "节点2", ctx -> "result2")
            .connect("node1", "node2");
        
        // 执行
        Orchestration.Result result = orchestration.execute();
        
        // 验证
        assertTrue(result.isSuccess());
        assertNull(result.getErrorMessage());
        assertNotNull(result.getResults());
        assertEquals("result1", result.getResults().get("node1"));
        assertEquals("result2", result.getResults().get("node2"));
    }

    @Test
    void testExecute_ConditionalFlow() {
        // 构建条件流程
        orchestration
            .addCondition("cond1", "条件", ctx -> true)
            .addGeneric("node1", "节点1", ctx -> "result1")
            .addGeneric("end", "结束", ctx -> "end-result")
            .connectWhenTrue("cond1", "node1")
            .connect("node1", "end");
        
        // 执行
        Orchestration.Result result = orchestration.execute();
        
        // 验证
        assertTrue(result.isSuccess());
        assertEquals(true, result.getResults().get("cond1"));
        assertEquals("result1", result.getResults().get("node1"));
        assertEquals("end-result", result.getResults().get("end"));
    }

    @Test
    void testExecute_WithContext() {
        // 构建流程
        orchestration.addGeneric("node1", "动态节点", 
            ctx -> "input-" + ctx.getVariable("input", String.class));
        
        // 创建上下文
        Orchestration.Context context = new Orchestration.Context("test-orchestration");
        context.setVariable("input", "test-value");
        
        // 执行
        Orchestration.Result result = orchestration.execute(context);
        
        // 验证
        assertTrue(result.isSuccess());
        assertEquals("input-test-value", result.getResults().get("node1"));
    }

    @Test
    void testExecute_EmptyOrchestration() {
        // 空编排执行会失败
        Orchestration.Result result = orchestration.execute();
        
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("编排中没有定义任何节点"));
    }

    @Test
    void testExecute_NodeExecutionFailure() {
        // 构建会失败的节点
        orchestration.addGeneric("fail", "失败节点", ctx -> {
            throw new RuntimeException("节点执行失败");
        });
        
        // 执行
        Orchestration.Result result = orchestration.execute();
        
        // 验证
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("节点执行失败"));
    }

    @Test
    void testContext_VariableOperations() {
        Orchestration.Context context = new Orchestration.Context("test");
        
        // 设置和获取变量
        context.setVariable("key1", "value1");
        context.setVariable("key2", 123);
        
        assertEquals("value1", context.getVariable("key1", String.class));
        assertEquals(Integer.valueOf(123), context.getVariable("key2", Integer.class));
        assertNull(context.getVariable("nonexistent", String.class));
    }

    @Test
    void testContext_ResultOperations() {
        Orchestration.Context context = new Orchestration.Context("test");
        
        // 设置和获取结果
        context.setResult("node1", "result1");
        context.setResult("node2", 456);
        
        assertEquals("result1", context.getResult("node1", String.class));
        assertEquals(Integer.valueOf(456), context.getResult("node2", Integer.class));
        assertNull(context.getResult("nonexistent", String.class));
        
        // 获取所有结果
        Map<String, Object> allResults = context.getAllResults();
        assertEquals(2, allResults.size());
        assertEquals("result1", allResults.get("node1"));
        assertEquals(456, allResults.get("node2"));
    }

    @Test
    void testResult_Success() {
        Map<String, Object> results = new HashMap<>();
        results.put("node1", "result1");
        
        Orchestration.Result result = Orchestration.Result.success("test", 
            java.time.LocalDateTime.now().minusSeconds(1), 
            java.time.LocalDateTime.now(), 
            results);
        
        assertTrue(result.isSuccess());
        assertNull(result.getErrorMessage());
        assertEquals(results, result.getResults());
        assertTrue(result.getExecutionTimeMillis() >= 0);
    }

    @Test
    void testResult_Failure() {
        Orchestration.Result result = Orchestration.Result.failure("test", "error message",
            java.time.LocalDateTime.now().minusSeconds(1),
            java.time.LocalDateTime.now());
        
        assertFalse(result.isSuccess());
        assertEquals("error message", result.getErrorMessage());
        assertNull(result.getResults());
        assertTrue(result.getExecutionTimeMillis() >= 0);
    }

    @Test
    void testToPlantUML() {
        orchestration
            .addGeneric("start", "开始", ctx -> "start")
            .addCondition("check", "检查", ctx -> true)
            .addGeneric("end", "结束", ctx -> "end")
            .connect("start", "check")
            .connectWhenTrue("check", "end");
        
        String uml = orchestration.toPlantUML();
        
        assertNotNull(uml);
        assertTrue(uml.contains("@startuml"));
        assertTrue(uml.contains("@enduml"));
        assertTrue(uml.contains("测试编排"));
        assertTrue(uml.contains("start"));
        assertTrue(uml.contains("check"));
        assertTrue(uml.contains("end"));
    }

    // 测试用的命令总线实现
    private static class TestCommandBus implements ICommandBus {
        @Override
        @SuppressWarnings("unchecked")
        public <R> R send(ICommand<R> command) {
            return (R) "command-result";
        }

        @Override
        public <R> CompletableFuture<R> sendAsync(ICommand<R> command) {
            return CompletableFuture.completedFuture(send(command));
        }

        @Override
        public int getHandlerCount() {
            return 1;
        }
    }

    // 测试用的查询总线实现
    private static class TestQueryBus implements IQueryBus {
        @Override
        @SuppressWarnings("unchecked")
        public <R> R send(IQuery<R> query) {
            return (R) "query-result";
        }

        @Override
        public <R> CompletableFuture<R> sendAsync(IQuery<R> query) {
            return CompletableFuture.completedFuture(send(query));
        }

        @Override
        public int getHandlerCount() {
            return 1;
        }
    }
}