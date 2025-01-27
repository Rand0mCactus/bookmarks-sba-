(ns bark.view
  (:require [clojure.string :as string]))

(defn fill-template-file
  [filepath variables]
  (let [content (slurp filepath)
        reducing (fn [content [match symbol-name]]
                   (->> symbol-name
                        keyword
                        variables
                        str
                        (string/replace content match)))]
    (->> content
         (re-seq #"\{\{([\w\*\+\!\-'\?\<\>\=\/\.]+)\}\}")
         distinct
         (reduce reducing content))))

(defn page-layout
  [title body]
  (fill-template-file "./resources/template.html" {:title title, :body body}))

(defn- bookmark-fragment
  [bookmark]
  (fill-template-file "./resources/entry.html" bookmark))

(defn bookmark-list-body
  "takes a vector of bookmarks (as maps), returns the html body with those bookmarks"
  [bookmarks]
  (->> bookmarks
       (map bookmark-fragment)
       (string/join \newline)
       (assoc {} :_)
       (fill-template-file "./resources/bookmark-list.html")))

(defn bookmark-form
  "returns the html body for adding a new bookmark entry"
  [variables]
  (fill-template-file "./resources/bookmark-form.html" variables))
