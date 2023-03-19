package com.zhang.autotouch.dialog.message;

public class Response {
    String command;
    String content;

    public Response(String command,String content){
        this.command=command;this.content=content;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
