(ns bark.view
  (:require [clojure.string :as string]))

; (defn trim-indent
;   "remove common indentation in a multi-line string"
;   [s]
;   (let [indent-width #(- (count %) (count (string/trim %)))
;         lines (string/split s #"\r?\n" -1)
;         min-indent (->> lines
;                         (filter seq)
;                         (map indent-width)
;                         (apply min))]
;     (->> lines
;          (map #(when (seq %) (subs % min-indent)))
;          (string/join \newline))))

; (defn- fill-page-fragment
;   "takes the file path of an html fragment template and some arguments,
;   return a string that is the fragment with the arguments substituted into
;   it"
;   [filepath & args]
;   (as-> filepath v (slurp v) (partial format v) (apply v args)))

; (distinct (re-seq #"\{\{([\w\*\+\!\-'\?\<\>\=\/\.]+)\}\}"
;                   (slurp "./resources/entry.html")))

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

; (defn fill-template-file
;   [filepath variables]
;   (loop [content (slurp filepath)
;          remaining variables]
;     (if (empty? remaining)
;       content
;       (let [k (-> remaining
;                   keys
;                   first)
;             v (-> k
;                   remaining
;                   str)
;             match (->> k
;                        name
;                        (format "{{%s}}"))]
;         (recur (string/replace content match v) (dissoc remaining k))))))

; (defn page-layout
;   "takes a page title and an html body, return the whole html file as a
;   string"
;   ([title body] (fill-page-fragment "./resources/template.html" title body)))

; (defn- bookmark-fragment
;   "takes a map representing a bookmark, transform it into an html fragment"
;   [{:bookmarks/keys [title url]}]
;   (fill-page-fragment "./resources/entry.html" title url))
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

(comment
  (def bookmarks
    [{:url "https://example.com", :title "tesg"}
     {:url "https://another.org", :title "Another"}
     {:url "https://www.google.com", :title "Google"}
     {:url "https://clojure.org", :title "Clojure Main Page"}])
  (println (bookmark-list-body bookmarks)))
