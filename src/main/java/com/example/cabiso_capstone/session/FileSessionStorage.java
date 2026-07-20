package com.example.cabiso_capstone.session;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSessionStorage
        implements SessionStorage {

    private final Path sessionFile;

    public FileSessionStorage(Path sessionFile) {

        if (sessionFile == null) {
            throw new IllegalArgumentException(
                    "Session file path cannot be null."
            );
        }

        this.sessionFile = sessionFile;
    }

    @Override
    public void save(UserSession session)
            throws IOException {

        if (session == null) {
            throw new IllegalArgumentException(
                    "User session cannot be null."
            );
        }

        try (
                ObjectOutputStream outputStream =
                        new ObjectOutputStream(
                                Files.newOutputStream(
                                        sessionFile
                                )
                        )
        ) {
            outputStream.writeObject(session);
        }
    }

    @Override
    public UserSession load()
            throws IOException, ClassNotFoundException {

        if (!exists()) {
            return null;
        }

        try (
                ObjectInputStream inputStream =
                        new ObjectInputStream(
                                Files.newInputStream(
                                        sessionFile
                                )
                        )
        ) {
            return (UserSession)
                    inputStream.readObject();
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(sessionFile);
    }

    @Override
    public void delete() throws IOException {
        Files.deleteIfExists(sessionFile);
    }

    @Override
    public Path getPath() {
        return sessionFile.toAbsolutePath();
    }
}