package com.example.cabiso_capstone.session;

import java.io.IOException;
import java.nio.file.Path;

public interface SessionStorage {

    void save(UserSession session) throws IOException;

    UserSession load()
            throws IOException, ClassNotFoundException;

    boolean exists();

    void delete() throws IOException;

    Path getPath();
}