(ns user
  (:require [clojure.pprint :refer [pprint]]
            [bark.view :as view]
            [bark.system :as system]
            [bark.model :as model]
            [bark.handler :as handler]
            [next.jdbc.sql :as sql]))

(def config
  {:db {:dbtype "sqlite", :dbname "bookmarks.sqlite3"},
   :server {:join? false, :port 8000}})

(defonce state nil)

(defn datasource [] (:bark.system/datasource state))

(defn handler [] (:bark.system/handler state))

(defn server [] (:bark.system/server state))

(defn init-system!
  []
  (when-not state
    (alter-var-root #'state (constantly (system/init-system config)))))

(defn halt-system!
  []
  (when state
    (.stop (:system/server state))
    (alter-var-root #'state (constantly nil))))

(defn reboot-system! [] (halt-system!) (init-system!))

(comment
  (init-system!))

(comment
  (-> (let [bark (model/get-bookmark-by-id (datasource) 10)]
        (view/bookmark-form bark))
      println))

(comment
  (handler/decode-query-str "a="))
