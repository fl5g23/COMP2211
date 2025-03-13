module COMP2211 {
    requires transitive java.sql;
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.swing;
    requires transitive org.jfree.jfreechart;
    exports org.example;
    exports org.example.Models;
    exports org.example.Views;
    exports org.example.Controllers;
}
