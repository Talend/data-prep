package org.talend.dataprep.helper.utils;

/**
 * URL construction helper class.
 *
 * @author vferreira
 * @since 20/07/17
 */
public class DataPrepWebInfo {

    public static final String API = "/api";
    public static final String DATASETS = "/datasets";
    public static final String PREPARATIONS = "/preparations";
    public static final String API_ACTIONS = "actions";
    public static final String API_DETAILS = "details";
    public static final String API_UPLOAD = "upload";
    public static final String API_EXPORT = "export";
    public static final String API_HISTORY = "history";

    public static final String API_TCOMP = "tcomp";
    public static final String TCOMP_DATASTORE = "/datastores";
    public static final String JDBC_DATASTORE = "/tcomp-JDBCDatastore";

    public static final String API_DATASETS = API + DATASETS + "/";
    public static final String API_UPLOAD_DATASETS_NAME = API_UPLOAD + API + DATASETS + "?name=";
    public static final String API_DATASETS_NAME = API + DATASETS + "?name=";
    public static final String API_PREPARATIONS = API + PREPARATIONS + "/";
    public static final String API_PREPARATIONS_FOLDER = API + PREPARATIONS + "?folder=";
    public static final String API_FULLRUN_EXPORT = API + "/" + API_EXPORT + "/async";


    public static final String API_TCOMP_DB_CONNECTION = API + "/" + API_TCOMP + TCOMP_DATASTORE + JDBC_DATASTORE + "/test";
    public static final String API_TCOMP_DB_IMPORT = API + "/" + API_TCOMP + TCOMP_DATASTORE + JDBC_DATASTORE + "/dataset";
    public static final String JDBC_DATASTORE_NAME = "JDBCDatastore";
    public static final String JDBC_DATASET_NAME = "JDBCDataset";


}
