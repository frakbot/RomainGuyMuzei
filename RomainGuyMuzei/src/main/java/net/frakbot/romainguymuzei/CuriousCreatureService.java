/*
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

import com.google.gson.annotations.Expose;
import retrofit.http.GET;

import java.util.ArrayList;
import java.util.List;

interface CuriousCreatureService {
    @GET("/?method=flickr.people.getPublicPhotos&user_id=24046097%40N00&extras=url_o&format=json&nojsoncallback=1")
    PhotosResponse getRomainsPhotos();

    static class PhotosResponse {

        @Expose
        Photos_ photos;
        @Expose
        String stat;

        public Photos_ getPhotos() {
            return photos;
        }

        public void setPhotos(Photos_ photos) {
            this.photos = photos;
        }

        public String getStat() {
            return stat;
        }

        public void setStat(String stat) {
            this.stat = stat;
        }

    }

    public class Photos_ {

        @Expose
        Integer page;
        @Expose
        Integer pages;
        @Expose
        Integer perpage;
        @Expose
        String total;
        @Expose
        List<Photo> photo = new ArrayList<Photo>();

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getPages() {
            return pages;
        }

        public void setPages(Integer pages) {
            this.pages = pages;
        }

        public Integer getPerpage() {
            return perpage;
        }

        public void setPerpage(Integer perpage) {
            this.perpage = perpage;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public List<Photo> getPhoto() {
            return photo;
        }

        public void setPhoto(List<Photo> photo) {
            this.photo = photo;
        }

    }

    static class Photo {

        @Expose
        String id;
        @Expose
        String owner;
        @Expose
        String secret;
        @Expose
        String server;
        @Expose
        Integer farm;
        @Expose
        String title;
        @Expose
        Integer ispublic;
        @Expose
        Integer isfriend;
        @Expose
        Integer isfamily;
        @Expose
        String url_o;
        @Expose
        String height_o;
        @Expose
        String width_o;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public Integer getFarm() {
            return farm;
        }

        public void setFarm(Integer farm) {
            this.farm = farm;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getIspublic() {
            return ispublic;
        }

        public void setIspublic(Integer ispublic) {
            this.ispublic = ispublic;
        }

        public Integer getIsfriend() {
            return isfriend;
        }

        public void setIsfriend(Integer isfriend) {
            this.isfriend = isfriend;
        }

        public Integer getIsfamily() {
            return isfamily;
        }

        public void setIsfamily(Integer isfamily) {
            this.isfamily = isfamily;
        }

        public String getUrl_o() {
            return url_o;
        }

        public void setUrl_o(String url_o) {
            this.url_o = url_o;
        }

        public String getHeight_o() {
            return height_o;
        }

        public void setHeight_o(String height_o) {
            this.height_o = height_o;
        }

        public String getWidth_o() {
            return width_o;
        }

        public void setWidth_o(String width_o) {
            this.width_o = width_o;
        }

        @Override
        public String toString() {
            return String.format("[\"%1$s\" - URL: %2$s]", title, url_o);
        }
    }

    static class User {
        String fullname;
    }
}
