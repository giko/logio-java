package org.kluge.logging.model;

/**
 * Created by giko on 10/13/14.
 */
public class LogMessage {
    //my_stream|my_node|info|this is log message
    private String stream;
    private LogNode node;
    private String level;
    private String message;

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public LogNode getNode() {
        return node;
    }

    public void setNode(LogNode node) {
        this.node = node;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
