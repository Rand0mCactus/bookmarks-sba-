(ns bark.routing
  (:require [clojure.string :as string]
            [bark.handler :as handler]
            [bark.parsing :as parsing]))

;;; Middleware
(defn decode-body-query
  [handler]
  (fn [{:keys [content-type], :as req}]
    (if (= "application/x-www-form-urlencoded" content-type)
      (-> req
          (update :body slurp)
          (update :body parsing/uri-query->map)
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

;; Routing
(defmulti routing-handler
  (fn [{:keys [request-method uri]}] [request-method
                                      (string/replace uri #"\d+" ":id")]))

; "if the request isn't matched by any other method,
; returns the formatted request map back"
(defmethod routing-handler :default default [req] (handler/echo-handler req))

(defmethod routing-handler [:get "/"]
  list-page-handler
  [req]
  (handler/list-page req))

(defmethod routing-handler [:get "/bookmark/new"]
  new-page-handler
  [_req]
  (handler/new-page))

(defmethod routing-handler [:get "/bookmark/:id"]
  edit-page-handler
  [req]
  (handler/edit-page req))

(defmethod routing-handler [:post "/save"]
  save-bookmark-handler
  [req]
  ((-> handler/save-bookmark!
       decode-body-query)
    req))

(defmethod routing-handler [:post "/save/:id"]
  [req]
  ((-> handler/update-bookmark!
       decode-body-query)
   req))
