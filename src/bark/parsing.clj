(ns bark.parsing
  (:require [clojure.string :as string]))

(defn- query-params->map
  "takes a string in the form of \"a=x&b=y\",
  returns the map {\"a\" \"x\", \"b\" \"y\"}"
  [s]
  (let [pairs (string/split s #"&")
        split-pair #(string/split % #"=" -1)
        reducing (fn [m v] (assoc m (v 0) (v 1)))]
    (->> pairs
         (map split-pair)
         (reduce reducing {}))))

(defn- percent-decode
  "decodes a percent-encoded string e.g. \"a%3Ab+%26+%2Bc\" -> \"a:b & +c\""
  ([s] (if (empty? s) s (percent-decode s "")))
  ([tail result]
   (if (empty? tail)
     result
     (let [c (first tail)]
       (case c
         \% (let [decoded (-> tail
                              (subs 1 3)
                              (Integer/parseInt 16)
                              char)]
              (recur (subs tail 3) (str result decoded)))
         \+ (recur (subs tail 1) (str result \space))
         (recur (subs tail 1) (str result c)))))))

(defn uri-query->map
  "takes a URL-encoed query string, returns a map with decoded values
  e.g. \"q=a%3Ab+%26+%2Bc&sort=relevance&t=week\"
        -> {\"q\" \"a:b & +c\", \"sort\" \"relevance\", \"t\" \"week\"}"
  [s]
  (-> s
       query-params->map
       (update-keys percent-decode)
       (update-vals percent-decode)))
