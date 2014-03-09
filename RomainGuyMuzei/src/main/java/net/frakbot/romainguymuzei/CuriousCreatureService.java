package net.frakbot.romainguymuzei;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.gson.Gson;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CuriousCreatureService {

    public static final String TAG = CuriousCreatureService.class.getSimpleName();
    public static final int BATCH_SIZE = 300;
    public static final String PREF_KEY_PHOTOSTREAM = "photostream";

    private final AtomicReference<RetrofitError> mLastError = new AtomicReference<>(null);

    public RetrofitError getLastError() {
        return mLastError.get();
    }

    public List<CuriousCreatureRESTClient.Photo> downloadPhotos() {
        Log.i(TAG, "Downloading updated photostream data");

        RestAdapter restAdapter = new RestAdapter.Builder()
            .setServer("http://api.flickr.com/services/rest")
            .setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addQueryParam("api_key", Config.API_KEY);
                }
            })
            .setErrorHandler(new ErrorHandler() {
                @Override
                public Throwable handleError(RetrofitError retrofitError) {
                    final Response response = retrofitError.getResponse();
                    if (response != null) {
                        int statusCode = response.getStatus();
                        if (retrofitError.isNetworkError() || (500 <= statusCode && statusCode < 600)) {
                            return new RemoteMuzeiArtSource.RetryException();
                        }
                    }
                    mLastError.set(retrofitError);
                    return retrofitError;
                }
            })
            .build();

        mLastError.set(null);       // Clear last error before performing the request

        if (BuildConfig.DEBUG) Log.v(TAG, "Initializing REST client");
        CuriousCreatureRESTClient service = restAdapter.create(CuriousCreatureRESTClient.class);

        if (BuildConfig.DEBUG) Log.v(TAG, "Downloading photos");
        return getRomainsPhotos(service);
    }

    /**
     * Downloads all the information about Romain's photo stream from Flickr.
     *
     * @param service The REST client to use to retrieve the photos.
     *
     * @return The complete photo stream info.
     */
    private List<CuriousCreatureRESTClient.Photo> getRomainsPhotos(CuriousCreatureRESTClient service) {
        // Get the first page (which is probably not the last) and also get metadata out of it
        CuriousCreatureRESTClient.PhotosResponse response = service.getRomainsPhotos(1, 300);
        if (response == null) {
            Log.w(TAG, "Got an empty response from the server");
            return null;
        }

        final CuriousCreatureRESTClient.Photos photos = response.getPhotos();
        final List<CuriousCreatureRESTClient.Photo> photostream = photos.getPhotos();
        if (BuildConfig.DEBUG) Log.v(TAG, "Total photos found: " + photos.getTotal() + "; batch size: " + BATCH_SIZE);

        for (int page = photos.page + 1; page < photos.pages; page++) {
            Log.d(TAG, "Downloading photos batch " + page + " out of " + photos.pages);
            CuriousCreatureRESTClient.PhotosResponse tmpResponse = service.getRomainsPhotos(page, BATCH_SIZE);
            photostream.addAll(tmpResponse.getPhotos().getPhotos());
        }

        return photostream;
    }

    /**
     * Persists a list of photos in the app's shared preferences,
     * storing them as a JSON string.
     *
     * @param context The Context to get the preferences from
     * @param photos  The photos metadata to be persisted
     */
    public static void persistPhotosList(Context context, List<CuriousCreatureRESTClient.Photo> photos) {
        final Gson gson = new Gson();
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit()
          .putString(PREF_KEY_PHOTOSTREAM, gson.toJson(photos))
          .commit();
    }

    /**
     * Loads a list of photos from the app's shared preferences,
     * where they should have been persisted as JSON.
     *
     * @param context The Context to get the preferences from
     *
     * @return Returns the read photos list, if any, or null
     * if there has been any issue.
     */
    public static ArrayList<CuriousCreatureRESTClient.Photo> readSavedPhotosList(Context context) {
        final Gson gson = new Gson();
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        final String json = sp.getString(PREF_KEY_PHOTOSTREAM, null);
        if (json == null) {
            return null;
        }

        ArrayList<CuriousCreatureRESTClient.Photo> list = null;
        try {
            //noinspection unchecked
            list = gson.fromJson(json, ArrayList.class);
        }
        catch (Exception e) {
            Log.e(TAG, "Unable to deserialize the photos list JSON:\n" + json, e);
        }
        return list;
    }
}
