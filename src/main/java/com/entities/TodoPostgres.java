package com.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name= "todos")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class TodoPostgres implements Serializable, TodoBase {
    @Id
    @Column(name = "rawid", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "duedate", nullable = false)
    private Long duedate;

    @Column(name = "state", nullable = false)
    private String state = "PENDING";

    public TodoPostgres() {}

    public TodoPostgres(int id, String title, String content, Long duedate) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.duedate = duedate;
    }

    @Override
    public Integer getId() { return id; }
    @Override
    public void setId(Integer id) { this.id = id; }

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
                "id = " + id +
                ", title = " + title +
                ", content = " + content +
                ", duedate = " + duedate +
                ", state = '" + state + '\'' +
                '}';
    }
}
