package com.github.danielnickel94.todo;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TodoService service = new TodoService();
        service.load("todos.csv");
        new ConsoleUI(service).run();


    }
}
