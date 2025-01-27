(ns bark.main
  (:require [bark.system :as system]))

;; The configuration of the system.
;; Each key is a "component" and the value is the configuration it requires
;; for initialization.
(def config
  {:server {:port 8000, :join? false},
   :db {:dbtype "sqlite", :dbname "bookmarks.sqlite3"}})

(defn -main [] (system/init-system config))
