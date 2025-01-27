(ns bark.model
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

;; see https://www.sqlite.org/schematab.html
(defn schema
  ([datasource] (schema datasource "%"))
  ([datasource object-type]
   (sql/query datasource
              ["SELECT * FROM sqlite_schema WHERE type LIKE ?" object-type])))

(defn table-schema
  [datasource table]
  (sql/query datasource ["PRAGMA table_info(?)" table]))

(defn populate!
  "create a 'bookmarks' table if it doesn't yet exist, returns nothing"
  [datasource]
  (let [table-exists
          (-> datasource
              (sql/query
                ["SELECT * FROM sqlite_schema WHERE name = ? AND type = 'table'"
                 "bookmarks"])
              seq)]
    (when-not table-exists
      (jdbc/execute-one!
        datasource
        ["
        CREATE TABLE bookmarks (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          url TEXT,
          title TEXT
        )"]))))

(defn insert-bookmark!
  "takes a datasource and a map representing a bookmark,
  insert it into the 'bookmarks' table; returns nothing"
  [datasource row]
  (sql/insert! datasource "bookmarks" row))

(defn get-bookmarks
  "returns a vector of maps, each with a bookmark's URL and title"
  [datasource]
  (sql/query datasource ["SELECT * FROM bookmarks ORDER BY id DESC"]))

(defn get-bookmark-by-id
  [datasource id]
  (sql/get-by-id datasource :bookmarks id))

(defn update-bookmark-by-id
  [datasource id title url]
  (sql/update! datasource :bookmarks {:title title, :url url} {:id id}))
