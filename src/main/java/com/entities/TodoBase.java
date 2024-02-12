package com.entities;

public interface TodoBase {

    Integer getId();
    void setId(Integer id);

    String getTitle();
    void setTitle(String title);

    String getContent();
    void setContent(String content);

    Long getDuedate();
    void setDuedate(Long duedate);

    String getState();
    void setState(String state);
}