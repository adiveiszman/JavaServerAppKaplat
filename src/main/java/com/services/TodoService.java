package com.services;

import com.controller.TodoController;
import com.entities.Todo;
import com.repositories.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TodoService {
    private static final Logger todoLogger = LoggerFactory.getLogger(TodoService.class);
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public void newTODO(Todo todo) {
        todoLogger.info("Creating new TODO with Title [{}] | request #{}", todo.getTitle(), TodoController.requestNumber);
        todoLogger.debug("Currently there are {} TODOs in the system. New TODO will be assigned with id {} | request #{}",
                todoRepository.count(), todo.getId(), TodoController.requestNumber);
        todoRepository.save(todo);
    }

//    public int getIdForNewTodo() {
////        Object result = entityManager.createQuery("SELECT MAX(t.rawid) FROM Todo t").getSingleResult();
////        return (result != null) ? ((Number) result).intValue() : 0;
//        return todoRepository.findMaxRawid() + 1;
//    }

    public boolean isTitleExists(String title) {
        return !todoRepository.findByTitle(title).isEmpty();
    }

    public static boolean isDueDateInTheFuture(long dueDate) {
        Date date = new Date(dueDate);
        Date today = new Date();

        return date.before(today);
    }

    public int getTodosCount(String status) {
        int count = 0;

        if (status == null || status.equals("ALL")) {
            count = (int) todoRepository.count();
        } else if (status.equals("PENDING") || status.equals("LATE") || status.equals("DONE")) {
            count = todoRepository.countByState(status);
        }

        todoLogger.info("Total TODOs count for state {} is {} | request #{}", status, count, TodoController.requestNumber);
        return count;
    }

    public List<Todo> getTodos(String state, String sortBy) {
        List<Todo> filteredTodos;
        List<Todo> todosFromDB = todoRepository.findAll();

        if (!state.equals("ALL")) {
            filteredTodos = todosFromDB
                    .stream()
                    .filter(todo -> state.equals(todo.getState()))
                    .collect(Collectors.toList());
        } else {
            filteredTodos = new ArrayList<>(todosFromDB);
        }

        switch (sortBy) {
            case "TITLE":
                filteredTodos.sort(Comparator.comparing(Todo::getTitle));
                break;
            case "DUE_DATE":
                filteredTodos.sort(Comparator.comparing(Todo::getDueDate));
                break;
            default:
                filteredTodos.sort(Comparator.comparingInt(Todo::getId));
                break;
        }

        todoLogger.info("Extracting todos content. Filter: {} | Sorting by: {} | request #{}", state, sortBy, TodoController.requestNumber);
        todoLogger.debug("There are a total of {} todos in the system. The result holds {} todos | request #{}",
                todosFromDB.size(), filteredTodos.size(), TodoController.requestNumber);
        return filteredTodos;
    }

    public boolean existenceById(int id) {
        return todoRepository.findById(id).isPresent();
    }

    public static boolean isStateValid(String state) {
        return "PENDING".equals(state) || "LATE".equals(state) || "DONE".equals(state);
    }

    public String updateTodoState(int id, String newState) {
        Optional<Todo> optionalTodo = todoRepository.findById(id);
        String oldState = optionalTodo.map(Todo::getState).orElse(null);

        optionalTodo.ifPresent(todo -> {
            todo.setState(newState);
            todoRepository.save(todo);
        });

        todoLogger.debug("Todo id [{}] state change: {} --> {} | request #{}",
                id, oldState, newState, TodoController.requestNumber);
        return oldState;
    }

    public List<Integer> deleteTodoById(int id) {
        List<Integer> result = null;

        todoRepository.deleteById(id);
        result = todoRepository.findAll().stream().map(Todo::getId)
                .collect(Collectors.toList());

        todoLogger.info("Removing todo id {} | request #{}", id, TodoController.requestNumber);
        todoLogger.debug("After removing todo id [{}] there are {} TODOs in the system | request #{}",
                id, result.size(), TodoController.requestNumber);

        return result;
    }

    public static String getLogInStructure(String logLevel, String logMsg, String RequestNumber) {
        LocalDateTime now = LocalDateTime.now();

        return now.toString() + " " + logLevel + ": " + logMsg + " | request #" + RequestNumber;
    }
}
