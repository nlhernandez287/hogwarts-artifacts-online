package edu.tcu.cs.hogwartsartifactsonline.system.exception;

public class ObjectNotFoundException extends RuntimeException{
    public ObjectNotFoundException(String objectName, String id) {
        super("Could not find %s with Id %s".formatted(objectName, id));
    }

    public ObjectNotFoundException(String objectName, Integer id) {
        super("Could not find %s with Id %d".formatted(objectName, id));
    }
}
