module COMP2211 {
    requires java.sql;
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.swing;
    requires transitive org.jfree.jfreechart;
    exports org.example;
}
