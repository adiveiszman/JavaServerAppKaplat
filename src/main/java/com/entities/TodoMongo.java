package com.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document(collection = "todos")
public class TodoMongo implements Serializable, TodoBase {
    private Integer rawid;
    private String title;
    private String content;
    private Long duedate;
    private String state = "PENDING";

    public TodoMongo() {}

    public TodoMongo(Integer id, String title, String content, Long duedate) {
        this.rawid = id;
        this.title = title;
        this.content = content;
        this.duedate = duedate;
    }

    @Override
    public Integer getId() { return rawid; }
    @Override
    public void setId(Integer id) { this.rawid = id; }

    @Override
    public String getTitle() { return title; }
    @Override
    public void setTitle(String title) { this.title = title; }

    @Override
    public String getContent() { return content; }
    @Override
    public void setContent(String  content) { this.content = content; }

    @Override
    public Long getDuedate() { return duedate; }
    @Override
    public void setDuedate(Long duedate) { this.duedate = duedate; }

    @Override
    public String getState() { return state; }
    @Override
    public void setState(String state) { this.state = state; }

    @Override
    public String toString() {
        return "Todo { " +
                "id = " + rawid +
                ", title = " + title +
                ", content = " + content +
                ", duedate = " + duedate +
                ", state = '" + state + '\'' +
                '}';
    }
}
