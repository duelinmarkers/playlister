(ns playlister.download
  (:require [clj-http.lite.client :as client]
            [cheshire.core :as json]
            [clojure.java.io :as io])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(def song-start-end-time-format (DateTimeFormatter/ofPattern "MM/dd/yyyy HH:mm:ss"))
(def filename-date-time-format (DateTimeFormatter/ofPattern "yyyy-MM-dd-HHmmss"))

(defn parse-time [^CharSequence s]
  (LocalDateTime/parse s song-start-end-time-format))

(defn format-time-for-filename [^LocalDateTime t]
  (.format t filename-date-time-format))

(defn tracks-url [{:keys [datestamp limit]}]
  (str "https://api.composer.nprstations.org/v1/widget/50ef24ebe1c8a1369593d032/tracks?format=json"
       "&datestamp=" (if (instance? LocalDateTime datestamp)
                       (.format datestamp DateTimeFormatter/ISO_LOCAL_DATE_TIME)
                       datestamp)
       "&limit=" limit
       "&hide_itunes=true"))

(defn http-get [url]
  (println "Requesting" url)
  (let [started-at (System/currentTimeMillis)
        response (client/get url)]
    (println "Response received in" (- (System/currentTimeMillis) started-at)
             "ms, status" (:status response)
             ", content-length" (get-in response [:headers "content-length"]))
    response))

(defn download-tracks [data-dir url-options]
  (let [response (http-get (tracks-url url-options))
        parsed-body (json/parse-string (:body response) true)
        latest-start-time (-> parsed-body :tracklist :results first :song :_start_time parse-time)
        file-name (str "tracks-" (format-time-for-filename latest-start-time) ".json")]
    (spit (io/file data-dir file-name)
          (:body response))
    (println "Wrote" file-name "to" data-dir)))

(defn latest-tracks [data-dir]
  (download-tracks data-dir {:limit 300}))

(defn earliest-tracks-file [data-dir]
  (-> (io/file data-dir)
      (.listFiles (reify java.io.FilenameFilter
                    (accept [_ _ name]
                      (boolean (re-find #"tracks-.+\.json" name)))))
      (->> (sort-by #(.getName %)))
      first))

(defn backfill-1 [data-dir]
  (if-let [earliest-file (earliest-tracks-file data-dir)]
    (let [parsed-body (json/parse-string (slurp earliest-file) true)
          earliest-start-time (-> parsed-body :tracklist :results last :song :_start_time parse-time)
          datestamp (-> earliest-start-time (.minusSeconds 1))]
      (download-tracks data-dir {:datestamp datestamp :limit 300}))
    (println "No tracks file found in" data-dir)))

(comment
  (backfill-1 "./data")
  )
