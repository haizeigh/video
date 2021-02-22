package com.westwell.server.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

@Data
public class FaceCollectionChangeEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */

    private String colleKey;

    public FaceCollectionChangeEvent(Object source) {
        super(source);
    }

    public FaceCollectionChangeEvent(Object source, String colleKey) {
        super(source);
        this.colleKey = colleKey;
    }
}
