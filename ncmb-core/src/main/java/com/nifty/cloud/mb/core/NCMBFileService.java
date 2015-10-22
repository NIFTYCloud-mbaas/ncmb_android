package com.nifty.cloud.mb.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * NCMBFileService class
 */
public class NCMBFileService extends NCMBService {

    /** service path for API category */
    public static final String SERVICE_PATH = "files";

    /**
     * Inner class for callback
     */
    abstract class FileServiceCallback extends ServiceCallback {
        /** constructors */
        FileServiceCallback(NCMBFileService service, ExecuteServiceCallback callback) {
            super(service, (CallbackBase) callback);
        }

        FileServiceCallback(NCMBFileService service, GetDataCallback callback) {
            super(service, (CallbackBase) callback);
        }

        FileServiceCallback(NCMBFileService service, SearchFileCallback callback) {
            super(service, (CallbackBase) callback);
        }


        protected NCMBFileService getFileService() {
            return (NCMBFileService) mService;
        }
    }


    /**
     * Constructor
     *
     * @param context NCMBContext
     */
    NCMBFileService(NCMBContext context) {
        super(context);
        mServicePath = SERVICE_PATH;
    }


    /**
     * Upload file data to Nifty cloud mobile backend
     *
     * @param fileName upload file name
     * @param fileData file data to byte[]
     * @param aclJson  saving file acl
     * @return API response
     * @throws NCMBException exception sdk internal or NIFTY Cloud mobile backend
     */
    public JSONObject saveFile(String fileName, byte[] fileData, JSONObject aclJson) throws NCMBException {
        if (!validateFileName(fileName)) {
            throw new NCMBException(NCMBException.GENERIC_ERROR, "fileName is must not be null or empty");
        }
        String url = mContext.baseUrl + mServicePath + "/" + fileName;
        NCMBResponse response = sendRequestFile(url, NCMBRequest.HTTP_METHOD_POST, fileName, fileData, aclJson);
        if (response.statusCode != NCMBResponse.HTTP_STATUS_CREATED) {
            throw new NCMBException(NCMBException.GENERIC_ERROR, "Invalid status code");
        }
        return response.responseData;
    }

    /**
     * Upload file data to Nifty cloud mobile backend in background thread
     *
     * @param fileName upload file name
     * @param fileData file data to byte[]
     * @param aclJson  saving file acl
     * @param callback callback for after file save
     */
    public void saveFileInBackground(String fileName, byte[] fileData, JSONObject aclJson, ExecuteServiceCallback callback) {
        if (!validateFileName(fileName)) {
            callback.done(null, new NCMBException(NCMBException.GENERIC_ERROR, "fileName is must not be null or empty"));
        }

        String url = mContext.baseUrl + mServicePath + "/" + fileName;

        try {
            sendRequestFileAsync(url, NCMBRequest.HTTP_METHOD_POST, fileName, fileData, aclJson, new FileServiceCallback(this, callback) {
                @Override
                public void handleResponse(NCMBResponse response) {

                    ExecuteServiceCallback callback = (ExecuteServiceCallback) mCallback;
                    if (callback != null) {
                        callback.done(response.responseData, null);
                    }
                }

                @Override
                public void handleError(NCMBException e) {
                    ExecuteServiceCallback callback = (ExecuteServiceCallback) mCallback;
                    if (callback != null) {
                        callback.done(null, e);
                    }
                }
            });
        } catch (NCMBException e) {
            //Exception handling for NCMBRequest
            if (callback != null) {
                callback.done(null, e);
            }
        }
    }


    /**
     * Update ACL of file
     *
     * @param fileName update file name
     * @param aclJson  update file acl
     * @return API response
     * @throws NCMBException exception sdk internal or NIFTY Cloud mobile backend
     */
    public JSONObject updateFile(String fileName, JSONObject aclJson) throws NCMBException {
        if (!validateFileName(fileName)) {
            throw new NCMBException(NCMBException.GENERIC_ERROR, "fileName is must not be null or empty");
        }
        String url = mContext.baseUrl + mServicePath + "/" + fileName;
        JSONObject content = new JSONObject();
        try {
            content.put("acl", aclJson);
        } catch (JSONException e) {
            throw new NCMBException(NCMBException.GENERIC_ERROR, e.getMessage());
        }
        NCMBResponse response = sendRequest(url, NCMBRequest.HTTP_METHOD_PUT, content.toString());
        if (response.statusCode != NCMBResponse.HTTP_STATUS_OK) {
            throw new NCMBException(NCMBException.GENERIC_ERROR, "Invalid status code");
        }
        return response.responseData;
    }


    /**
     * Update ACL of file in background thread
     *
     * @param fileName update file name
     * @param aclJson  saving file acl
     * @param callback callback for after file update
     */
    public void updateFileInBackground(String fileName, JSONObject aclJson, ExecuteServiceCallback callback) {
        if (!validateFileName(fileName)) {
            if (callback != null) {
                callback.done(null, new NCMBException(NCMBException.GENERIC_ERROR, "fileName is must not be null or empty"));
            }
        }
        JSONObject content = new JSONObject();
        try {
            content.put("acl", aclJson);
        } catch (JSONException e) {
            if (callback != null) {
                callback.done(null, new NCMBException(NCMBException.GENERIC_ERROR, e.getMessage()));
            }
        }

        String url = mContext.baseUrl + mServicePath + "/" + fileName;
        try {
            sendRequestAsync(url, NCMBRequest.HTTP_METHOD_PUT, content.toString(), null, new FileServiceCallback(this, callback) {

                @Override
                public void handleResponse(NCMBResponse response) {

                    ExecuteServiceCallback callback = (ExecuteServiceCallback) mCallback;
                    if (callback != null) {
                        callback.done(response.responseData, null);
                    }
                }

                @Override
                public void handleError(NCMBException e) {
                    ExecuteServiceCallback callback = (ExecuteServiceCallback) mCallback;
                    if (callback != null) {
                        callback.done(null, e);
                    }
                }
            });
        } catch (NCMBException e) {
            //Exception handling for NCMBRequest
            if (callback != null) {
                callback.done(null, e);
            }
        }
    }

    /**
     * Delete file data to Nifty cloud mobile backend
     *
     * @param fileName delete file name
     * @return API response
     * @throws NCMBException exception sdk internal or NIFTY Cloud mobile backend
     */
    public JSONObject deleteFile(String fileName) throws NCMBException {
        if (!validateFileName(fileName)) {
            throw new NCMBException(NCMBException.GENERIC_ERROR, "fileName is must not be null or empty");
        }

        String url = mContext.baseUrl + mServicePath + "/" + fileName;
        String type = NCMBRequest.HTTP_METHOD_DELETE;
        NCMBResponse response = sendRequest(url, type);
        if (response.statusCode != NCMBResponse.HTTP_STATUS_OK) {
            throw new NCMBException(NCMBException.GENERIC_ERROR, "Invalid status code");
        }
        return response.responseData;
    }

    /**
     * Delete file data to Nifty cloud mobile backend in background thread
     *
     * @param fileName delete file name
     * @param callback callback for after file delete
     */
    public void deleteFileInBackground(String fileName, ExecuteServiceCallback callback) {
        if (!validateFileName(fileName)) {
            if (callback != null) {
                callback.done(null, new NCMBException(NCMBException.GENERIC_ERROR, "fileName is must not be null or empty"));
            }
        }

        String url = mContext.baseUrl + mServicePath + "/" + fileName;
        try {
            sendRequestAsync(url, NCMBRequest.HTTP_METHOD_DELETE, null, null, new FileServiceCallback(this, callback) {

                @Override
                public void handleResponse(NCMBResponse response) {

                    ExecuteServiceCallback callback = (ExecuteServiceCallback) mCallback;
                    if (callback != null) {
                        callback.done(response.responseData, null);
                    }
                }

                @Override
                public void handleError(NCMBException e) {
                    ExecuteServiceCallback callback = (ExecuteServiceCallback) mCallback;
                    if (callback != null) {
                        callback.done(null, e);
                    }
                }
            });
        } catch (NCMBException e) {
            //Exception handling for NCMBRequest
            if (callback != null) {
                callback.done(null, e);
            }
        }
    }

    /**
     * Get file data from Nifty cloud mobile backend
     *
     * @param fileName get file name
     * @return API response
     * @throws NCMBException exception sdk internal or NIFTY Cloud mobile backend
     */
    public byte[] getFile(String fileName) throws NCMBException {
        if (!validateFileName(fileName)) {
            throw new NCMBException(NCMBException.GENERIC_ERROR, "fileName is must not be null or empty");
        }

        String url = mContext.baseUrl + mServicePath + "/" + fileName;
        String type = NCMBRequest.HTTP_METHOD_GET;
        NCMBResponse response = sendRequest(url, type);
        if (response.statusCode != NCMBResponse.HTTP_STATUS_OK) {
            throw new NCMBException(NCMBException.GENERIC_ERROR, "Invalid status code");
        }
        return response.responseByte;
    }

    /**
     * Get file data from Nifty cloud mobile backend in background thread
     *
     * @param fileName get file name
     * @param callback callback for after file get
     */
    public void getFileInBackground(String fileName, GetDataCallback callback) {
        if (!validateFileName(fileName)) {
            if (callback != null) {
                callback.done(null, new NCMBException(NCMBException.GENERIC_ERROR, "fileName is must not be null or empty"));
            }
        }

        String url = mContext.baseUrl + mServicePath + "/" + fileName;
        try {
            sendRequestAsync(url, NCMBRequest.HTTP_METHOD_GET, null, null, new FileServiceCallback(this, callback) {
                @Override
                public void handleResponse(NCMBResponse response) {

                    GetDataCallback callback = (GetDataCallback) mCallback;
                    if (callback != null) {
                        callback.done(response.responseByte, null);
                    }
                }

                @Override
                public void handleError(NCMBException e) {

                    GetDataCallback callback = (GetDataCallback) mCallback;
                    if (callback != null) {
                        callback.done(null, e);
                    }
                }
            });
        } catch (NCMBException e) {
            //Exception handling for NCMBRequest
            if (callback != null) {
                callback.done(null, e);
            }

        }
    }

    /**
     * Get files from Nifty cloud mobile backend
     *
     * @param conditions search conditions
     * @return NCMBFile list
     * @throws NCMBException exception sdk internal or NIFTY Cloud mobile backend
     */
    public List searchFile(JSONObject conditions) throws NCMBException {
        String url = mContext.baseUrl + mServicePath;
        String type = NCMBRequest.HTTP_METHOD_GET;
        NCMBResponse response = sendRequest(url, type, null, conditions);
        if (response.statusCode != NCMBResponse.HTTP_STATUS_OK) {
            throw new NCMBException(NCMBException.GENERIC_ERROR, "Invalid status code");
        }
        return createSearchResults(response.responseData);
    }


    /**
     * Get files from Nifty cloud mobile backend in background thread
     *
     * @param conditions search conditions
     * @param callback   callback for after file get
     */
    public void searchFileInBackground(JSONObject conditions, SearchFileCallback callback) {
        String url = mContext.baseUrl + mServicePath;
        try {
            sendRequestAsync(url, NCMBRequest.HTTP_METHOD_GET, null, conditions, new FileServiceCallback(this, callback) {
                @Override
                public void handleResponse(NCMBResponse response) {

                    SearchFileCallback callback = (SearchFileCallback) mCallback;
                    if (callback != null) {
                        try {
                            callback.done(createSearchResults(response.responseData), null);
                        } catch (NCMBException callbackException) {
                            callback.done(null, callbackException);
                        }

                    }
                }

                @Override
                public void handleError(NCMBException e) {
                    SearchFileCallback callback = (SearchFileCallback) mCallback;
                    if (callback != null) {
                        callback.done(null, e);
                    }
                }
            });
        } catch (NCMBException e) {
            //Exception handling for NCMBRequest
            if (callback != null) {
                callback.done(null, e);
            }

        }
    }

    /**
     * Create search results
     *
     * @param responseData API response data
     * @return NCMBFile list
     * @throws NCMBException
     */
    List<NCMBFile> createSearchResults(JSONObject responseData) throws NCMBException {
        try {
            JSONArray results = responseData.getJSONArray("results");
            List<NCMBFile> array = new ArrayList<>();
            for (int i = 0; i < results.length(); ++i) {
                NCMBFile file = new NCMBFile();
                file.setLocalData(results.getJSONObject(i));
                array.add(file);
            }
            return array;
        } catch (JSONException e) {
            throw new NCMBException(NCMBException.INVALID_JSON, "Invalid JSON format.");
        }
    }


    private boolean validateFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        return true;
    }
}