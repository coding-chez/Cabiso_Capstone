package com.example.cabiso_capstone.session;

import java.io.IOException;
import java.nio.file.Paths;

public final class SessionManager {

    private static SessionStorage storage =
            new FileSessionStorage(
                    Paths.get("session.txt"));

    private SessionManager() {
    }

    public static void saveSession(UserSession userSession) throws IOException {
        storage.save(userSession);
    }

    public static UserSession loadSession() throws IOException, ClassNotFoundException {
        return storage.load();
    }

    public static boolean hasSession() {
        return storage.exists();
    }

    public static boolean hasValidSession() {

        try {
            UserSession session = storage.load();

            return session != null && session.isActive() && (session.isAdmin() || session.isTenant());

        } catch (IOException | ClassNotFoundException | ClassCastException exception) {
            return false;
        }
    }

    public static void deleteSession()
            throws IOException {

        storage.delete();
    }

    public static String getSessionFilePath() {
        return storage
                .getPath()
                .toString();
    }

    public static void setStorage(SessionStorage newStorage) {

        if (newStorage == null) {
            throw new IllegalArgumentException(
                    "Session storage cannot be null."
            );
        }

        storage = newStorage;
    }
}