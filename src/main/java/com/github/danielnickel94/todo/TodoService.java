package com.github.danielnickel94.todo;

import java.util.ArrayList;
import java.util.List;

public class TodoService {
    private final List<Todo> todos = new ArrayList<>();
    private int nextId = 1;

    public int add(String text) {
        Todo t = new Todo(nextId++, text);
        todos.add(t);
        return t.getId();
    }

    public boolean updateText(int id, String newText) {
        Todo t = find(id);
        if (t == null) return false;
        t.setText(newText);
        return true;
    }

    public boolean remove(int id) {
        Todo t = find(id);
        return t != null && todos.remove(t);
    }

    public boolean markDone(int id) {
        Todo t = find(id);
        if (t == null) return false;
        t.setDone(true);
        return true;
    }

    public List<Todo> list() {
        return List.copyOf(todos); // read-only copy
    }

    private Todo find(int id) {
        for (Todo t : todos) if (t.getId() == id) return t;
        return null;
    }
}

