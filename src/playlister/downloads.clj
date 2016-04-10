(ns playlister.downloads
  (:require [clj-http.lite.client :as client]
            [clojure.java.io :as io])
  (:import (java.time LocalDate)
           (java.time.format DateTimeFormatter)
           (java.io File FilenameFilter)))

(def date-format DateTimeFormatter/ISO_LOCAL_DATE)
(def day-filename-pattern #"day-(\d\d\d\d-\d\d-\d\d)\.json")

(defn format-local-date [^LocalDate local-date]
  (.format local-date date-format))

(defn day-url [date]
  (str "https://api.composer.nprstations.org/v1/widget/50ef24ebe1c8a1369593d032/day?date="
       date
       "&format=json&hide_amazon=true&hide_itunes=true&hide_arkiv=true"))

(defn http-get [url]
  (println "Requesting" url)
  (let [started-at (System/currentTimeMillis)
        response (client/get url)]
    (println "Response received in" (- (System/currentTimeMillis) started-at)
             "ms, status" (:status response)
             ", content-length" (get-in response [:headers "content-length"]))
    response))

(defn download-day [data-dir date]
  (let [response (http-get (day-url date))
        filename (str "day-" date ".json")]
    (assert (re-find day-filename-pattern filename)
            (str filename " should match pattern " day-filename-pattern))
    (spit (io/file data-dir filename)
          (:body response))
    (println "Wrote" filename "to" data-dir)))

(defn day-filename->local-date [s]
  (when-let [[_ date-string] (re-find day-filename-pattern s)]
    (LocalDate/parse date-string date-format)))

(defn file->name [^File f] (.getName f))

(defn sorted-day-files [data-dir]
  (-> (io/file data-dir)
      (.listFiles (reify FilenameFilter
                    (accept [_ _ name]
                      (boolean (re-find day-filename-pattern name)))))
      (->> (sort-by file->name))))

(defn ^LocalDate earliest-day-file-date [data-dir]
  (-> (sorted-day-files data-dir)
      (some->
        first
        file->name
        day-filename->local-date)))

(defn backfill-1 [data-dir]
  (if-let [earliest-date (earliest-day-file-date data-dir)]
    (let [date (-> earliest-date (.minusDays 1) format-local-date)]
      (download-day data-dir date))
    (println "No tracks file found in" data-dir)))

(comment
  (backfill-1 "./data")
  )
