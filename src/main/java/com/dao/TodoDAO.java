package com.dao;

import com.controller.TodoController;
import com.dto.TodoDTO;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

public class TodoDAO {
    private static final List<TodoDTO> DB = new ArrayList<>();
    private static final Logger todoLogger = LoggerFactory.getLogger(TodoDAO.class);

    public static void addNewTODO(TodoDTO newTodo) {
        todoLogger.info("Creating new TODO with Title [{}] | request #{}", newTodo.getTitle(), TodoController.requestNumber);
        todoLogger.debug("Currently there are {} TODOs in the system. New TODO will be assigned with id {} | request #{}",
                DB.size(), newTodo.getId(), TodoController.requestNumber);
        DB.add(newTodo);
    }

    public static boolean isTitleExists(String title) {
        return DB.stream().anyMatch(todo -> todo.getTitle().equals(title));
    }

    public static boolean isDueDateInTheFuture(long dueDate) {
        Date date = new Date(dueDate);
        Date today = new Date();

        return date.before(today);
    }

    public static int getTodosCount(String status) {
        int count = 0;

        if (status == null || status.equals("ALL")) {
            count = DB.size();
        } else if (status.equals("PENDING")) {
            count = (int) DB.stream().filter(todo -> "PENDING".equals(todo.getStatus())).count();
        } else if (status.equals("LATE")) {
            count = (int) DB.stream().filter(todo -> "LATE".equals(todo.getStatus())).count();
        } else if (status.equals("DONE")) {
            count = (int) DB.stream().filter(todo -> "DONE".equals(todo.getStatus())).count();
        }

        todoLogger.info("Total TODOs count for state {} is {} | request #{}", status, count, TodoController.requestNumber);
        return count;
    }

    public static List<TodoDTO> getTodos(String status, String sortBy) {
        List<TodoDTO> todos = new ArrayList<>();

        if (!status.equals("ALL")) {
            for (TodoDTO todo : DB) {
                if (status.equals(todo.getStatus())) {
                    todos.add(todo);
                }
            }
        } else {
            todos.addAll(DB);
        }

        switch (sortBy) {
            case "TITLE":
                todos.sort(Comparator.comparing(TodoDTO::getTitle));
                break;
            case "DUE_DATE":
                todos.sort(Comparator.comparing(TodoDTO::getDueDate));
                break;
            default:
                todos.sort(Comparator.comparingInt(TodoDTO::getId));
                break;
        }

        todoLogger.info("Extracting todos content. Filter: {} | Sorting by: {} | request #{}", status, sortBy, TodoController.requestNumber);
        todoLogger.debug("There are a total of {} todos in the system. The result holds {} todos | request #{}",
                DB.size(), todos.size(), TodoController.requestNumber);
        return todos;
    }

    public static boolean isTodoExistsById(int id) {
        return DB.stream().anyMatch(todo -> todo.getId() == id);
    }

    public static boolean isStatusValid(String status) {
        return "PENDING".equals(status) || "LATE".equals(status) || "DONE".equals(status);
    }

    public static String updateTodoStatus(int id, String newStatus) {
        String oldStatus = "";

        for (TodoDTO todo : DB) {
            if (todo.getId() == id) {
                oldStatus = todo.getStatus();
                todo.setStatus(newStatus);
            }
        }

        todoLogger.debug("Todo id [{}] state change: {} --> {} | request #{}",
                id, oldStatus, newStatus, TodoController.requestNumber);
        return oldStatus;
    }

    public static List<Integer> getAllExistingTodosIds() {
        List<Integer> result = new ArrayList<>();

        for(TodoDTO todo : DB) {
            result.add(todo.getId());
        }

        return result;
    }

    public static List<Integer> deleteTodoById(int id) {
        List<Integer> result = null;

        Optional<TodoDTO> todoOptional = DB.stream().filter(todo -> todo.getId() == id).findFirst();
        if (todoOptional.isPresent()) {
            DB.remove(todoOptional.get());
            result = getAllExistingTodosIds();
        }

        todoLogger.info("Removing todo id {} | request #{}", id, TodoController.requestNumber);
        todoLogger.debug("After removing todo id [{}] there are {} TODOs in the system | request #{}",
                id, DB.size(), TodoController.requestNumber);

        return result;
    }

    public static String getLogInStructure(String logLevel, String logMsg, String RequestNumber) {
        LocalDateTime now = LocalDateTime.now();

        return now.toString() + " " + logLevel + ": " + logMsg + " | request #" + RequestNumber;
    }

}
