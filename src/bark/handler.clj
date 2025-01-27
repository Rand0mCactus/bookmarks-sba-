(ns bark.handler
  (:require [clojure.pprint :refer [pprint]]
            [bark.model :as model]
            [bark.view :as view]
            [clojure.string :as string]))

;;; Parsing
(defn query-str->map
  "takes a string in the form of \"a=x&b=y\", returns the map {:a \"x\", :b \"y\"}"
  [s]
  (let [pairs (string/split s #"&")
        split-pair #(string/split % #"=" -1)
        reducing (fn [m v] (assoc m (keyword (v 0)) (v 1)))]
    (->> pairs
         (map split-pair)
         (reduce reducing {}))))

(defn decode-url
  "decodes a URL-encoded string e.g. \"a%3Ab+%26+%2Bc\" -> \"a:b & +c\""
  ([s] (if (empty? s) s (decode-url s "")))
  ([remaining final]
   (if (empty? remaining)
     final
     (let [c (first remaining)]
       (case c
         \% (let [decoded (-> remaining
                              (subs 1 3)
                              (Integer/parseInt 16)
                              char)]
              (recur (subs remaining 3) (str final decoded)))
         \+ (recur (subs remaining 1) (str final \space))
         (recur (subs remaining 1) (str final c)))))))

(comment
  (decode-url "a%3Ab+%26+%2Bc"))

(defn decode-query-str
  "takes a URL-encoed query string, returns a map with decoded values
  e.g. \"q=a%3Ab+%26+%2Bc&sort=relevance&t=week\"
        -> {:q \"a:b & +c\", :sort \"relevance\", :t \"week\"}"
  [s]
  (-> s
      query-str->map
      (update-vals decode-url)))

(query-str->map "a=sdfsf&bfs-d=1")

;;; Middleware

(defn decode-body-query
  [handler]
  (fn [{:keys [content-type], :as req}]
    (if (= "application/x-www-form-urlencoded" content-type)
      (-> req
          (update :body slurp)
          (update :body decode-query-str)
          handler)
      (handler req))))

(defn wrap-datasource
  "takes a handler and a datasource,
  returns a new handler that add the datasource to the request map before
  passing the request map to the old handler"
  [handler datasource]
  (fn [request]
    (-> request
        (assoc :datasource datasource)
        (handler))))

;;; Handlers
(defn echo-handler
  "takes a request and returns the formatted request map as response body"
  [req]
  {:status 200,
   :headers {},
   :body (-> req
             ; (dissoc :headers)
             pprint
             with-out-str)})

(defn routing-dispatcher
  [{:keys [request-method uri]}]
  [request-method (string/replace uri #"\d+" ":id")])

(defmulti routing-handler
  (fn [{:keys [request-method uri]}] [request-method
                                      (string/replace uri #"\d+" ":id")]))

; "if the request isn't matched by any other method,
; returns the formatted request map back"
(defmethod routing-handler :default
  default
  [req]
  (-> req
      (assoc :handler "default")
      (assoc :dispatched-value (routing-dispatcher req))
      echo-handler))

; "returns a page showing all stored bookmarks"
(defmethod routing-handler [:get "/"]
  bookmark-list
  [req]
  (->> req
       :datasource
       model/get-bookmarks
       view/bookmark-list-body
       (view/page-layout "All Bookmarks")
       (assoc {:status 200, :headers {}} :body)))

; "returns a page for adding a new bookmark entry"
(defmethod routing-handler [:get "/bookmark/new"]
  new-bookmark-form
  [_req]
  {:status 200,
   :headers {},
   :body (view/page-layout "New Bookmark"
                           (view/bookmark-form {:post-url "/save"}))})

; "returns a page for editing an existing bookmark entry"
(defmethod routing-handler [:get "/bookmark/:id"]
  edit-bookmark-form
  [{:keys [uri datasource]}]
  (let [id (->> uri
                (re-find #"\d+")
                Integer/parseInt)
        bookmark (model/get-bookmark-by-id datasource id)
        title (->> bookmark
                   :bookmarks/title
                   (str "Edit - "))]
    {:status 200,
     :headers {},
     :body (view/page-layout title
                             (view/bookmark-form (assoc bookmark
                                                   :post-url (str "/save/" id))))}))

(defn h
  [{:keys [datasource body]}]
  (model/insert-bookmark! datasource body)
  {:status 303, :headers {"Location" "/"}, :body ""})

; "takes a POST request containing the bookmark form inputs,
; then adds it to the database"
(defmethod routing-handler [:post "/save"]
  save-bookmark
  [req]
  ((-> h
       decode-body-query)
    req))

(comment
  (decode-query-str "title=clojure&url=https%3A%2F%2Fclojure.org"))
