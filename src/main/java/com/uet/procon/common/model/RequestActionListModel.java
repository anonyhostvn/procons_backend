package com.uet.procon.common.model;

import java.io.Serializable;
import java.util.List;

public class RequestActionListModel implements Serializable {

    private List<RequestActionModel> actions;

    public List<RequestActionModel> getActions() {
        return actions;
    }

    public void setActions(List<RequestActionModel> actions) {
        this.actions = actions;
    }
}
