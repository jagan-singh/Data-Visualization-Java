package vilij.components;

import java.io.IOException;

/**
 * Defines the behavior of the core actions to be handled by an application.
 *
 * @author Ritwik Banerjee
 */
public interface ActionComponent {

    void handleNewRequest() throws IOException;

    void handleSaveRequest();

    void handleLoadRequest();

    void handleExitRequest();

    void handlePrintRequest();

}
