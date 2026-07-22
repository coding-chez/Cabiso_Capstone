package com.example.cabiso_capstone.facade;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.session.SessionManager;
import com.example.cabiso_capstone.session.UserSession;

import java.io.IOException;

/**
 * Structural Facade Pattern
 *
 * Provides controllers with one simplified interface
 * for scene navigation and session-related operations.
 *
 * The controllers no longer need to coordinate
 * MainApplication and SessionManager directly.
 */
public final class ApplicationFacade {

    private ApplicationFacade() {
        // Prevent instantiation.
    }

    /* =====================================================
       AUTHENTICATION VIEWS
       ===================================================== */

    public static void openLogin()
            throws IOException {

        MainApplication.changeScene(
                "login-view.fxml"
        );
    }

    public static void openRegistration()
            throws IOException {

        MainApplication.changeScene(
                "register-view.fxml"
        );
    }

    /* =====================================================
       ADMINISTRATOR VIEWS
       ===================================================== */

    public static void openAdminDashboard()
            throws IOException {

        MainApplication.changeScene(
                "admin-dashboard-view.fxml"
        );
    }

    public static void openTenantManagement()
            throws IOException {

        MainApplication.changeScene(
                "tenant-view.fxml"
        );
    }

    public static void openRoomManagement()
            throws IOException {

        MainApplication.changeScene(
                "room-view.fxml"
        );
    }

    public static void openPaymentManagement()
            throws IOException {

        MainApplication.changeScene(
                "payment-view.fxml"
        );
    }

    /* =====================================================
       TENANT VIEW
       ===================================================== */

    public static void openTenantDashboard()
            throws IOException {

        MainApplication.changeScene(
                "tenant-dashboard-view.fxml"
        );
    }

    /* =====================================================
       SESSION OPERATIONS
       ===================================================== */

    public static UserSession getCurrentSession()
            throws IOException,
            ClassNotFoundException {

        if (!SessionManager.hasValidSession()) {
            return null;
        }

        return SessionManager.loadSession();
    }

    public static boolean hasAdminSession() {

        try {

            UserSession session =
                    getCurrentSession();

            return session != null
                    && session.isActive()
                    && session.isAdmin();

        } catch (
                IOException
                | ClassNotFoundException exception
        ) {

            return false;
        }
    }

    public static boolean hasTenantSession() {

        try {

            UserSession session =
                    getCurrentSession();

            return session != null
                    && session.isActive()
                    && session.isTenant();

        } catch (
                IOException
                | ClassNotFoundException exception
        ) {

            return false;
        }
    }

    /* =====================================================
       LOGOUT
       ===================================================== */

    public static void logout()
            throws IOException {

        SessionManager.deleteSession();

        System.out.println(
                "Session deleted: "
                        + SessionManager
                        .getSessionFilePath()
        );

        openLogin();
    }
}