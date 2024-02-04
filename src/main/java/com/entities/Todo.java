package com.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name= "todos")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Todo implements Serializable {

    @Id
    @Column(name = "rawid", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "duedate", nullable = false)
    private Long dueDate;

    @Column(name = "state", nullable = false)
    private String state = "PENDING";

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String  content) { this.content = content; }

    public Long getDueDate() { return dueDate; }
    public void setDueDate(Long dueDate) { this.dueDate = dueDate; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    @Override
    public String toString() {
        return "Todo { " +
                "id = " + id +
                ", title = " + title +
                ", content = " + content +
                ", duedate = " + dueDate +
                ", state = '" + state + '\'' +
                '}';
    }
}
