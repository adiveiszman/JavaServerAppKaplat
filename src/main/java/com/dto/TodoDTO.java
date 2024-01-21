package com.dto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;

public class TodoDTO {
    private static int nextId = 1;
    private int id;
    private String title;
    private String content;
    private long dueDate;
    private String status;
    private static Logger log = LogManager.getLogger("todo-logger");

    public TodoDTO(String title, String content, long dueDate) {
        this.id = nextId;
        this.title = title;
        this.content = content;
        this.dueDate = dueDate;
        this.status = "PENDING";
    }

    public static void promoteNextId() {
        nextId++;
    }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public long getDueDate() {
        return this.dueDate;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
