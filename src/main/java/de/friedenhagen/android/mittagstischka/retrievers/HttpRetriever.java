/**
 * Copyright 2010 Mirko Friedenhagen 
 */

package de.friedenhagen.android.mittagstischka.retrievers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import de.friedenhagen.android.mittagstischka.model.Eatery;

/**
 * @author mirko
 * 
 */
public class HttpRetriever implements Retriever {

    public static final String MITTAGSTISCH_API = "http://mittagstisch-ka.de/app/";

    public static final String TAG = HttpRetriever.class.getSimpleName();

    public String etag;

    public final static URI MITTAGSTISCH_INDEX;

    private final HttpClient httpClient;

    static {
        try {
            MITTAGSTISCH_INDEX = new URI(MITTAGSTISCH_API + "index");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Message:", e);
        }
    }

    /**
     * 
     */
    public HttpRetriever() {
        httpClient = new DefaultHttpClient();
    }

    private String retrieveString(HttpGet whatToGet) throws ApiException {
        return IOUtils.toUtf8String(retrieveBytes(whatToGet));
    }

    /**
     * @param whatToGet
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     * @throws ApiException
     */
    private byte[] retrieveBytes(HttpGet whatToGet) throws ApiException {
        try {
            final HttpResponse response;
            response = httpClient.execute(whatToGet);
            final StatusLine statusLine = response.getStatusLine();
            final Header[] etags = response.getHeaders("ETag");
            etag = etags[0].getValue();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new ApiException("Status-Line:" + statusLine);
            }
            final byte[] byteArray = EntityUtils.toByteArray(response.getEntity());
            return byteArray;
        } catch (ClientProtocolException e) {
            throw new ApiException("Message:", e);
        } catch (IOException e) {
            throw new ApiException("Message:", e);
        }
    }

    /**
     * @return the etag
     */
    public String getEtag() {
        return etag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Eatery> retrieveEateries() throws ApiException {
        final String response = retrieveString(new HttpGet(MITTAGSTISCH_INDEX));
        return Eatery.fromJsonArray(retrieveEateries(response));
    }

    /** {@inheritDoc} */
    public JSONArray retrieveEateries(final String response) throws ApiException {
        try {
            return new JSONArray(response);
        } catch (JSONException e) {
            throw new ApiException("Could not parse " + response, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String retrieveEateryContent(Integer id) throws ApiException {
        return retrieveString(new HttpGet(MITTAGSTISCH_API + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] retrieveEateryPicture(Integer id) throws ApiException {
        final HttpGet imageGet = new HttpGet(MITTAGSTISCH_API + id + ".jpg");
        return retrieveBytes(imageGet);
    }

}
