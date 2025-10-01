package com.github.danielnickel94.todo;

public class Todo {
    private final int id;
    private String text;
    private boolean done;

    public Todo(int id, String text) {
        this.id = id;
        this.text = text;
        this.done = false;
    }

    public Todo(int id, String text, boolean done) {
        this.id = id;
        this.text = text;
        this.done = done;
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public boolean isDone() { return done; }

    public void setText(String text) { this.text = text; }
    public void setDone(boolean done) { this.done = done; }

    @Override
    public String toString() {
        String status = done ? "✔" : " ";
        return String.format("[%s] #%d %s", status, id, text);
    }
}

