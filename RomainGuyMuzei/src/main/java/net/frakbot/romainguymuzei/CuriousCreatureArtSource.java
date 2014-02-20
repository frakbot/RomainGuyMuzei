/*
 * Copyright 2014 Frakbot (Sebastiano Poggi, Francesco Pontillo)
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.frakbot.romainguymuzei;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.apps.muzei.api.UserCommand;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import net.frakbot.romainguymuzei.CuriousCreatureService.*;

import java.util.List;
import java.util.Random;

public class CuriousCreatureArtSource extends RemoteMuzeiArtSource {
    private static final String TAG = CuriousCreatureArtSource.class.getSimpleName();
    private static final String SOURCE_NAME = "CuriousCreatureArtSource";

    private static final int ROTATE_TIME_MILLIS = 3 * 60 * 60 * 1000; // rotate every 3 hours
    private static final int COMMAND_SHARE_ARTWORK = 1337;

    public CuriousCreatureArtSource() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(new UserCommand(BUILTIN_COMMAND_ID_NEXT_ARTWORK),
                        new UserCommand(COMMAND_SHARE_ARTWORK, getString(R.string.share)));
    }

    @Override
    protected void onCustomCommand(int id) {
        if (id == COMMAND_SHARE_ARTWORK) {
            Artwork currentArtwork = getCurrentArtwork();
            if (currentArtwork == null) {
                Log.w(TAG, "No current artwork, can't share.");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CuriousCreatureArtSource.this,
                                       R.string.source_error_no_artwork_to_share,
                                       Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            String detailUrl = currentArtwork.getViewIntent().getDataString();
            String artist = currentArtwork.getByline().trim();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "My Android wallpaper today is '"
                                                    + currentArtwork.getTitle().trim()
                                                    + "' by " + artist
                                                    + ". #Muzei\n\n"
                                                    + detailUrl);
            shareIntent = Intent.createChooser(shareIntent, "Share artwork");
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(shareIntent);
        }
        else {
            super.onCustomCommand(id);
        }
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : null;

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
                    int statusCode = retrofitError.getResponse().getStatus();
                    if (retrofitError.isNetworkError()
                        || (500 <= statusCode && statusCode < 600)) {
                        return new RetryException();
                    }
                    scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
                    return retrofitError;
                }
            })
            .build();

        Log.i(TAG, "Updating photos list");
        CuriousCreatureService service = restAdapter.create(CuriousCreatureService.class);
        PhotosResponse response = service.getRomainsPhotos();

        if (response == null || response.photos == null) {
            throw new RetryException();
        }

        final List<Photo> photosList = response.photos.getPhoto();
        if (photosList.isEmpty()) {
            Log.w(TAG, "No photos returned from API.");
            scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
            return;
        }

        Random random = new Random();
        Photo photo;
        String token;
        while (true) {
            photo = photosList.get(random.nextInt(photosList.size()));
            token = photo.id;
            if (photosList.size() <= 1 || !TextUtils.equals(token, currentToken)) {
                break;
            }
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "Publishing artwork: " + photo);
        publishArtwork(new Artwork.Builder()
                           .title(photo.title)
                           .byline("Romain Guy")
                           .imageUri(Uri.parse(photo.url_o))
                           .token(token)
                           .viewIntent(new Intent(Intent.ACTION_VIEW,
                                                  Uri.parse("http://www.flickr.com/photos/24046097%40N00/" + photo.id)))
                           .build());

        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
    }
}

