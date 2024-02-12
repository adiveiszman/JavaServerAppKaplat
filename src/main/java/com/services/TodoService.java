package com.services;

import com.controller.TodoController;
import com.entities.TodoBase;
import com.entities.TodoMongo;
import com.entities.TodoPostgres;
import com.repositories.TodoMongoRepository;
import com.repositories.TodoPostgresRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TodoService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public enum PersistenceMethod {
        POSTGRES, MONGO
    }

    private static final Logger todoLogger = LoggerFactory.getLogger(TodoService.class);
    private final TodoPostgresRepository postgresRepository;
    private final TodoMongoRepository mongoRepository;
    private final int nextId;

    public TodoService(TodoPostgresRepository postgresRepository, TodoMongoRepository mongoRepository) {
        this.postgresRepository = postgresRepository;
        this.mongoRepository = mongoRepository;
        this.nextId = postgresRepository.findAll().stream().map(TodoPostgres::getId).max(Integer::compareTo)
                .orElse(0) + 1;
    }

    public void newTODO(String title, String content, Long dueDate) {
        TodoPostgres postgresTodo = new TodoPostgres(nextId, title, content, dueDate);
        TodoMongo mongoTodo = new TodoMongo(nextId, title, content, dueDate);

        postgresRepository.save(postgresTodo);
        mongoRepository.save(mongoTodo);

        todoLogger.info("Creating new TODO with Title [{}] | request #{}", title, TodoController.requestNumber);
        todoLogger.debug("Currently there are {} TODOs in the system. New TODO will be assigned with id {} | request #{}",
                postgresRepository.count(), nextId, TodoController.requestNumber);
    }

    public boolean isTitleExists(String title) {
        return !postgresRepository.findByTitle(title).isEmpty();
    }

    public static boolean isDueDateInTheFuture(long dueDate) {
        Date date = new Date(dueDate);
        Date today = new Date();

        return date.before(today);
    }

    public int getTodosCount(String state, String persistenceMethod) {
        int count = 0;

        if (state == null || state.equals("ALL")) {
            count = PersistenceMethod.POSTGRES.name().equals(persistenceMethod) ?
                    (int) postgresRepository.count() : (int) mongoRepository.count();
        } else if (state.equals("PENDING") || state.equals("LATE") || state.equals("DONE")) {
            count = PersistenceMethod.POSTGRES.name().equals(persistenceMethod) ?
                    postgresRepository.countByState(state) : mongoRepository.countByState(state);
        }

        todoLogger.info("Total TODOs count for state {} is {} | request #{}", state, count, TodoController.requestNumber);
        return count;
    }

    public List<? extends TodoBase> getTodosContent(String state, String sortBy, String persistenceMethod) {
        List<? extends TodoBase> filteredTodos;
        List<? extends TodoBase> todosFromDB =  PersistenceMethod.POSTGRES.name().equals(persistenceMethod) ?
                postgresRepository.findAll() : mongoRepository.findAll();

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
                filteredTodos.sort(Comparator.comparing(TodoBase::getTitle));
                break;
            case "DUE_DATE":
                filteredTodos.sort(Comparator.comparing(TodoBase::getDuedate));
                break;
            default:
                filteredTodos.sort(Comparator.comparing(TodoBase::getId));
                break;
        }

        todoLogger.info("Extracting todos content. Filter: {} | Sorting by: {} | request #{}", state, sortBy, TodoController.requestNumber);
        todoLogger.debug("There are a total of {} todos in the system. The result holds {} todos | request #{}",
                todosFromDB.size(), filteredTodos.size(), TodoController.requestNumber);
        return filteredTodos;
    }

    /*public List<TodoPostgres> getTodos(String state, String sortBy, String persistenceMethod) {
        List<TodoPostgres> todosFromDBPostgres;
        List<TodoMongo> todosFromDBMongo;
        List<TodoPostgres> filteredTodosPostgres;
        List<TodoMongo> filteredTodosMongo;

        if(PersistenceMethod.POSTGRES.name().equals(persistenceMethod)) {
            todosFromDBPostgres = postgresRepository.findAll();
            if (!state.equals("ALL")) {
                filteredTodosPostgres = todosFromDBPostgres
                        .stream()
                        .filter(todo -> state.equals(todo.getState()))
                        .collect(Collectors.toList());
            } else {
                filteredTodosPostgres = new ArrayList<>(todosFromDBPostgres);
            }

            switch (sortBy) {
                case "TITLE":
                    filteredTodosPostgres.sort(Comparator.comparing(TodoPostgres::getTitle));
                    break;
                case "DUE_DATE":
                    filteredTodosPostgres.sort(Comparator.comparing(TodoPostgres::getDueDate));
                    break;
                default:
                    filteredTodosPostgres.sort(Comparator.comparingInt(TodoPostgres::getId));
                    break;
            }
        } else {
            todosFromDBMongo = mongoRepository.findAll();
            if (!state.equals("ALL")) {
                filteredTodosMongo = todosFromDBMongo
                        .stream()
                        .filter(todo -> state.equals(todo.getState()))
                        .collect(Collectors.toList());
            } else {
                filteredTodosMongo = new ArrayList<>(todosFromDBMongo);
            }

            switch (sortBy) {
                case "TITLE":
                    filteredTodosMongo.sort(Comparator.comparing(TodoMongo::getTitle));
                    break;
                case "DUE_DATE":
                    filteredTodosMongo.sort(Comparator.comparing(TodoMongo::getDueDate));
                    break;
                default:
                    filteredTodosMongo.sort(Comparator.comparingInt(TodoMongo::getId));
                    break;
            }
        }

        todoLogger.info("Extracting todos content. Filter: {} | Sorting by: {} | request #{}", state, sortBy, TodoController.requestNumber);
        todoLogger.debug("There are a total of {} todos in the system. The result holds {} todos | request #{}",
                todosFromDB.size(), filteredTodos.size(), TodoController.requestNumber);
        return filteredTodos;
    }*/

/*    private List<Object> sortTodosList(List<TodoPostgres> filteredTodosPostgres, String sortBy) {

    }*/

    public boolean existenceById(int id) {
        return postgresRepository.findById(id).isPresent();
    }

    public static boolean isStateValid(String state) {
        return "PENDING".equals(state) || "LATE".equals(state) || "DONE".equals(state);
    }

    public String updateTodoState(Integer id, String newState) {
        Optional<TodoPostgres> optionalTodoPostgres = postgresRepository.findById(id);
        Optional<TodoMongo> optionalTodoMongo = mongoRepository.findByRawid(id);

        String oldState = optionalTodoPostgres.map(TodoBase::getState).orElse(null);

        optionalTodoPostgres.ifPresent(todo -> {
            todo.setState(newState);
            postgresRepository.save(todo);
        });

        optionalTodoMongo.ifPresent(todo -> {
            todo.setState(newState);
            Query query = new Query(Criteria.where("rawid").is(id));
            Update update = new Update().set("state", newState);
            mongoTemplate.updateFirst(query, update, TodoMongo.class);
            //mongoRepository.save(todo);
        });

        todoLogger.debug("Todo id [{}] state change: {} --> {} | request #{}",
                id, oldState, newState, TodoController.requestNumber);
        return oldState;
    }

    public List<Integer> deleteTodoById(int id) {
        List<Integer> result;

        postgresRepository.deleteById(id);
        mongoRepository.deleteById(id);
        result = postgresRepository.findAll().stream().map(TodoPostgres::getId)
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
