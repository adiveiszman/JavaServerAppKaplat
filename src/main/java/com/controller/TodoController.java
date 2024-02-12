package com.controller;

import com.entities.TodoBase;
import com.services.TodoService;
import com.entities.TodoPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TodoController {
    public static int requestNumber = 0;
    private static final Logger requestLogger = LoggerFactory.getLogger(TodoController.class);
    private static final Logger todoLogger = LoggerFactory.getLogger(TodoService.class);
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/todo/health")
    @ResponseBody
    public String health() {
        long startTime = System.currentTimeMillis();
        requestNumber++;
        logInfoIncomingRequest(requestNumber, "/todo/health", "GET");
        logDebugIncomingRequest(requestNumber, startTime);

        return "OK";
    }

    @PostMapping("/todo")
    @ResponseBody
    public ResponseEntity<String> createNewTODO(@RequestBody TodoPostgres newTodo) {
        long startTime = System.currentTimeMillis();
        requestNumber++;
        logInfoIncomingRequest(requestNumber, "/todo","POST");
        JSONObject responseJson = new JSONObject();

        if (todoService.isTitleExists(newTodo.getTitle())) {
            todoLogger.error("Error: TODO with the title [{}] already exists in the system | request #{}", newTodo.getTitle(), requestNumber);
            responseJson.put("errorMessage", "Error: TODO with the title [" + newTodo.getTitle() + "] already exists in the system");
            logDebugIncomingRequest(requestNumber, startTime);

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(responseJson.toString());
        }

        if (TodoService.isDueDateInTheFuture(newTodo.getDuedate())) {
            todoLogger.error("Error: Can't create new TODO that its due date is in the past | request #{}", requestNumber);
            responseJson.put("errorMessage", "Error: Can't create new TODO that its due date is in the past");
            logDebugIncomingRequest(requestNumber, startTime);

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(responseJson.toString());
        }

        todoService.newTODO(newTodo.getTitle(), newTodo.getContent(), newTodo.getDuedate());
        responseJson.put("result", newTodo.getId());

        logDebugIncomingRequest(requestNumber, startTime);

        return ResponseEntity.ok().body(responseJson.toString());
    }

    @GetMapping("/todo/size")
    @ResponseBody
    public ResponseEntity<String> getTodosCount(@RequestParam(required = false) String status,
                                                @RequestParam(name = "persistenceMethod") String persistenceMethod) {
        long startTime = System.currentTimeMillis();
        requestNumber++;
        logInfoIncomingRequest(requestNumber, "/todo/size","GET");
        JSONObject responseJson = new JSONObject();

        if (status != null && !status.equals("ALL") && !status.equals("PENDING") && !status.equals("LATE") && !status.equals("DONE")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        responseJson.put("result", todoService.getTodosCount(status, persistenceMethod));
        logDebugIncomingRequest(requestNumber, startTime);

        return ResponseEntity.ok().body(responseJson.toString());
    }

    @GetMapping("/todo/content")
    @ResponseBody
    public ResponseEntity<List<? extends TodoBase>> getTodosData(@RequestParam(name = "status") String status,
                                                           @RequestParam(name = "sortBy", defaultValue = "rawid") String sortBy,
                                                           @RequestParam(name = "persistenceMethod") String persistenceMethod) {
        long startTime = System.currentTimeMillis();
        requestNumber++;
        logInfoIncomingRequest(requestNumber, "/todo/content","GET");
        List<? extends TodoBase> todos = todoService.getTodosContent(status, sortBy, persistenceMethod);
        logDebugIncomingRequest(requestNumber, startTime);

        return ResponseEntity.ok().body(todos);
    }

    @RequestMapping(value = "/todo", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<String> updateTodoStatus(@RequestParam("id") Integer id,
                                                   @RequestParam("status") String state) {
        long startTime = System.currentTimeMillis();
        requestNumber++;
        logInfoIncomingRequest(requestNumber, "/todo","PUT");
        JSONObject responseJson = new JSONObject();

        todoLogger.info("Update TODO id [{}] state to {} | request #{}", id, state, requestNumber);

        if (!todoService.existenceById(id)) {
            todoLogger.error("Error: no such TODO with id {} | request #{}", id, requestNumber);
            responseJson.put("errorMessage", "Error: no such TODO with id " + id);
            logDebugIncomingRequest(requestNumber, startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(responseJson.toString());
        }

        if (!TodoService.isStateValid(state)) {
            logDebugIncomingRequest(requestNumber, startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        responseJson.put("result", todoService.updateTodoState(id, state));
        logDebugIncomingRequest(requestNumber, startTime);

        return ResponseEntity.ok().body(responseJson.toString());
    }

    @RequestMapping(value = "/todo", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<String> deleteTodoById(@RequestParam("id") int id) {
        long startTime = System.currentTimeMillis();
        requestNumber++;
        logInfoIncomingRequest(requestNumber, "/todo","DELETE");
        JSONObject responseJson = new JSONObject();

        if (!todoService.existenceById(id)) {
            todoLogger.error("Error: no such TODO with id {} | request #{}", id, requestNumber);
            responseJson.put("errorMessage", "Error: no such TODO with id " + id);
            logDebugIncomingRequest(requestNumber, startTime);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(responseJson.toString());
        }

        responseJson.put("result", String.valueOf(todoService.deleteTodoById(id)));
        logDebugIncomingRequest(requestNumber, startTime);

        return ResponseEntity.ok().body(responseJson.toString());
    }

    @GetMapping("/logs/level")
    @ResponseBody
    public String getLoggerLevel(@RequestParam(name = "logger-name") String loggerName) {
        long startTime = System.currentTimeMillis();
        requestNumber++;
        logInfoIncomingRequest(requestNumber, "/logs/level", "GET");

        if (!"request-logger".equals(loggerName) && !"todo-logger".equals(loggerName)) {
            return "Failure: No such logger with name " + loggerName;
        }

        Logger logger = LoggerFactory.getLogger(loggerName);
        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
        ch.qos.logback.classic.Level level = logbackLogger.getEffectiveLevel();

        logDebugIncomingRequest(requestNumber, startTime);

        return "Success: " + level;
    }

    @RequestMapping(value = "/logs/level", method = RequestMethod.PUT)
    @ResponseBody
    public String updateLoggerLevel(@RequestParam(name = "logger-name") String loggerName,
                                                    @RequestParam(name = "logger-level") String newLevel) {
        long startTime = System.currentTimeMillis();
        requestNumber++;
        logInfoIncomingRequest(requestNumber, "/logs/level","PUT");
        Logger loggerToUpdate = null;

        if (!"request-logger".equals(loggerName) && !"todo-logger".equals(loggerName)) {
            return "Failure: No such logger with name " + loggerName;
        }

        ch.qos.logback.classic.Level level = ch.qos.logback.classic.Level.toLevel(newLevel, null);
        if (level == null) {
            return "Failure: No such logger level " + newLevel;
        }

        if("request-logger".equals(loggerName)) {
            loggerToUpdate = LoggerFactory.getLogger("com.controller.TodoController");
        } else {
            loggerToUpdate = LoggerFactory.getLogger("com.dao.TodoDAO");
        }

        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) loggerToUpdate;
        logbackLogger.setLevel(ch.qos.logback.classic.Level.toLevel(newLevel));

        logDebugIncomingRequest(requestNumber, startTime);

        return "Success: " + newLevel;
    }

    private void logInfoIncomingRequest(int requestNumber, String endPoint, String verb) {
        requestLogger.info("#{} | resource: {} | HTTP Verb {} | request #{}", requestNumber, endPoint, verb, requestNumber);
    }

    private void logDebugIncomingRequest(int requestNumber, long startTime) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        requestLogger.debug("request #{} duration: {}ms | request #{}", requestNumber, duration, requestNumber);
    }
}

