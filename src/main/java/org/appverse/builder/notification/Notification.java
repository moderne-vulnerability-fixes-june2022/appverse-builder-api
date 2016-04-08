package org.appverse.builder.notification;

import org.appverse.builder.web.rest.dto.UserDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by panthro on 01/02/16.
 */
public class Notification {

    public enum Event {
        BUILD_REQUEST_FAILED,
        BUILD_REQUEST_FINISHED,
        BUILD_REQUEST_CANCELLED,
        BUILD_REQUEST_STARTED
    }

    private Map<String, Object> modelMap = new HashMap<>();

    private String title;

    private UserDTO user;

    private Event event;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Map<String, Object> getModelMap() {
        return modelMap;
    }

    public void setModelMap(Map<String, Object> modelMap) {
        this.modelMap = modelMap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Notification{" +
            "modelMap=" + modelMap +
            ", title='" + title + '\'' +
            ", user=" + user +
            ", event=" + event +
            '}';
    }
}
