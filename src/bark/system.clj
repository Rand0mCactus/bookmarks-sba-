;; This namespace contains code related to how the system is initialized.
(ns bark.system
  (:require [bark.routing :as routing]
            [next.jdbc :as jdbc]
            [ring.adapter.jetty :as jetty]))

(defn init-db
  "takes a 'db-spec' config map, returns a datasource"
  [config]
  (jdbc/get-datasource config))

(defn init-server
  "takes a handler and a server config map, returns a server instance"
  [handler config]
  (jetty/run-jetty #(handler %) config))

(defn init-system
  "takes a system config map, initialize both the datasource(?) and the server,
  and returns them as a map"
  [{db-config :db, server-config :server}]
  (let [datasource (init-db db-config)
        handler (routing/wrap-datasource routing/routing-handler datasource)
        server (init-server handler server-config)]
    #::{:datasource datasource, :handler handler, :server server}))
