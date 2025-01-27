(ns bark.handler
  (:require [clojure.pprint :refer [pprint]]
            [bark.model :as model]
            [bark.view :as view]))

(defn ok-response
  "takes a request body and returns a response map with status 200"
  [body]
  {:status 200, :headers {}, :body body})

(defn redirect-response
  ([] (redirect-response "/"))
  ([url] {:status 303, :headers {"Location" url}, :body ""}))

(defn echo-handler
  "takes a request and returns the formatted request map as response body"
  [req]
  {:status 200,
   :headers {},
   :body (-> req
             ; (dissoc :headers)
             pprint
             with-out-str)})

(defn list-page
  "returns a page showing all stored bookmarks"
  [{:keys [datasource], :as _req}]
  (->> datasource
       model/get-bookmarks
       view/bookmark-list-body
       (view/page-layout "All Bookmarks")
       ok-response))

(defn new-page
  "returns a page for adding a new bookmark entry"
  []
  (->> {:post-url "/save"}
       view/bookmark-form
       (view/page-layout "New Bookmark")
       ok-response))

(defn edit-page
  "returns a page for editing an existing bookmark entry"
  [{:keys [datasource id], :as _req}]
  (let [{:bookmarks/keys [title], :as bookmark}
          (model/get-bookmark-by-id datasource id)
        page-title (str "Edit - " title)
        form-action-path (str "/save/" id)]
    (if (nil? bookmark)
      (echo-handler _req)
      (->> (assoc bookmark :post-url form-action-path)
           view/bookmark-form
           (view/page-layout page-title)
           ok-response))))

(defn save-bookmark!
  "save a bookmark into the database, and returns to the main page"
  [{:keys [datasource body], :as _req}]
  (model/insert-bookmark! datasource body)
  (redirect-response))

(defn update-bookmark!
  "update an existing bookmark in the database, and returns to the main page"
  [{:keys [datasource id], {:strs [title url]} :body, :as _req}]
  (model/update-bookmark-by-id! datasource id title url)
  (redirect-response))
