package com.github.danielnickel94.todo;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        TodoService service = new TodoService();
        boolean running = true;

        while (running) {
            System.out.println("\n--- ToDo Menü ---");
            System.out.println("1) Hinzufügen");
            System.out.println("2) Verändern");
            System.out.println("3) Löschen");
            System.out.println("4) Anzeigen");
            System.out.println("5) Als erledigt markieren");
            System.out.println("6) Speichern");
            System.out.println("7) Laden");
            System.out.println("0) Beenden");
            System.out.print("Auswahl: ");

            String auswahl = sc.nextLine().trim();

            switch (auswahl) {
                case "1": { // Hinzufügen
                    System.out.print("Text der Erledigung: ");
                    String text = sc.nextLine();
                    int id = service.add(text);
                    System.out.println("Hinzugefügt mit ID " + id);
                    break;
                }
                case "2": { // Verändern
                    System.out.print("ID zum Ändern: ");
                    int id = parseInt(sc.nextLine());
                    System.out.print("Neuer Text: ");
                    String neu = sc.nextLine();
                    System.out.println(service.updateText(id, neu) ? "Geändert." : "ID nicht gefunden.");
                    break;
                }
                case "3": { // Löschen
                    System.out.print("ID zum Löschen: ");
                    int id = parseInt(sc.nextLine());
                    System.out.println(service.remove(id) ? "Gelöscht." : "ID nicht gefunden.");
                    break;
                }
                case "4": { // Anzeigen
                    if (service.list().isEmpty()) {
                        System.out.println("(keine Erledigungen)");
                    } else {
                        System.out.println("\nAktuelle Erledigungen:");
                        service.list().forEach(System.out::println);
                    }
                    break;
                }
                case "5": { // Als erledigt markieren
                    System.out.print("ID als erledigt markieren: ");
                    int id = parseInt(sc.nextLine());
                    System.out.println(service.markDone(id) ? "Erledigt markiert." : "ID nicht gefunden.");
                    break;
                }
                case "6": {
                    service.save("todos.csv");
                    System.out.println("Gespeichert in todos.csv");
                    break;
                }
                case "7": {
                    service.load("todos.csv");
                    System.out.println("Todos aus todos.csv geladen.");
                    break;
                }
                case "0":
                    running = false;
                    System.out.println("Programm beendet");
                    break;
                default:
                    System.out.println("Ungültige Eingabe!");
            }
        }
        sc.close();
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}
