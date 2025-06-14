module com.mangareader.prototype {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.web;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires java.net.http;
    requires java.sql;
    requires java.prefs;

    opens com.mangareader.prototype to javafx.fxml;
    opens com.mangareader.prototype.ui to javafx.fxml;
    opens com.mangareader.prototype.model to com.fasterxml.jackson.databind;
    opens com.mangareader.prototype.source to com.fasterxml.jackson.databind;
    opens com.mangareader.prototype.service.impl to com.fasterxml.jackson.databind;

    exports com.mangareader.prototype;
    exports com.mangareader.prototype.ui;
    exports com.mangareader.prototype.model;
    exports com.mangareader.prototype.source;
    exports com.mangareader.prototype.source.impl;
}