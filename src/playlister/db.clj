(ns playlister.db
  (:require [clojure.java.jdbc :as sql]
            [cheshire.core :as json]
            [playlister.downloads :as downloads])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(def db {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "data/plays.db"})

(defn recreate [db]
  (let [statements
        [["drop table if exists plays"]
         [(sql/create-table-ddl
           :plays
           [:id :integer :primary :key :autoincrement]
           [:artist_name :text]
           [:track_name :text]
           [:collection_name :text]
           [:start_time :text]
           [:duration :numeric]
           [:npr_play_id :text]
           [:program_name :text]
           [:npr_airing_id :text]
           [:npr_event_id :text]
           [:npr_program_id :text])]]]
    (sql/with-db-connection [conn db]
      (doseq [statement statements]
        (sql/execute! conn statement)))))

(def start-time-format (DateTimeFormatter/ofPattern "MM-dd-yyyy HH:mm:ss"))

(defn start-time->db-time [s]
  (-> s
      (LocalDateTime/parse start-time-format)
      (.format DateTimeFormatter/ISO_LOCAL_DATE_TIME)))

(defn load-from-files [db data-dir]
  (sql/with-db-connection [conn db]
    (doseq [file (downloads/sorted-day-files data-dir)
            :let [parsed-body (json/parse-string (slurp file) true)
                  airings (:onToday parsed-body)]
            airing airings
            :let [{:keys [playlist program program_id _id event_id]} airing]]
      (if (seq playlist)
        (apply sql/insert! conn :plays
               (map (fn [{:keys [_duration _start_time collectionName trackName artistName]
                          npr-play-id :_id}]
                      {:artist_name     artistName
                       :track_name      trackName
                       :collection_name collectionName
                       :start_time      (start-time->db-time _start_time)
                       :duration        _duration
                       :npr_play_id     npr-play-id
                       :program_name    (:name program)
                       :npr_airing_id   _id
                       :npr_event_id    event_id
                       :npr_program_id  program_id})
                    playlist))
        (println "No playlist to insert for" (:name program)
                 "from" (downloads/file->name file))))))

(comment
  (recreate db)
  )
