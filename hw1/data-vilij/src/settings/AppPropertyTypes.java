package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    CONFIG_ICON,
    CONFIG_TOOLTIP,
    PLAY_ICON,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    INVALID_FORMAT_TITLE,
    INVALID_FORMAT,
    DUPLICATES_TITLE,
    DUPLICATES,
    CHART_TITLE,
    LEFT_PANE_TITLE,
    LEFT_PANE_TITLEFONT,
    LEFT_PANE_TITLESIZE,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    EDIT,
    DONE,
    ALGO_VBOX_TITLE,
    CLUSTERING,
    CLASSIFICATION,
    RANDOM_CLASSIFICATION,
    RANDOM_CLUSTERING,
    MAX_ITERATIONS,
    UPDATE_INTERVAL,
    LABELS,
    CONTINUE,
    CONFIG_ERROR_TITLE,
    CONFIG_ERROR
}
