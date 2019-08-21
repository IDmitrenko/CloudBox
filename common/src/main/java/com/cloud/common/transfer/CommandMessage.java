package com.cloud.common.transfer;

public class CommandMessage extends AbstractMessage {
    public static final int CMD_MSG_AUTH_OK = 19643971;
    public static final int CMD_MSG_REQUEST_FILE_DOWNLOAD = 198816036;
    public static final int CMD_MSG_REQUEST_FILES_LIST = 196617051;
    public static final int CMD_MSG_REQUEST_SERVER_DELETE_FILE = 198922066;

    private int type;
    private Object[] attachment;

    public int getType() {
        return type;
    }

    public Object[] getAttachment() {
        return attachment;
    }

    public CommandMessage(int type, Object... attachment) {
        this.type = type;
        this.attachment = attachment;
    }
}
