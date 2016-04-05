package com.kindabear.radiople.network;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyLog;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class FileRequest<T> extends GsonRequest {

    private MultipartEntityBuilder mMultipartEntity = MultipartEntityBuilder.create();
    private HttpEntity mHttpEntity = null;

    public FileRequest(Context context, int method, String url, Class clazz, Response.Listener responseListener, CommonResponseListener commonListener, Response.ErrorListener errorListener) {
        super(context, method, url, clazz, responseListener, commonListener, errorListener);

        mMultipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    }

    public void setFile(String key, File file) {
        mMultipartEntity.addPart(key, new FileBody(file));
        mHttpEntity = mMultipartEntity.build();
    }

    @Override
    public String getBodyContentType() {
        return mHttpEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mHttpEntity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }
}
