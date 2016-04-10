(ns playlister.db
  (:require [clojure.java.jdbc :as sql]
            [playlister.json :as json]
            [playlister.downloads :as downloads])
  (:import [java.time.format DateTimeFormatter]))

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

(defn local-date-time->db-time [s]
  (.format s DateTimeFormatter/ISO_LOCAL_DATE_TIME))

(defn airing&play->db-row-map
  [{:keys [program program_id _id event_id]}
   {:keys [_duration _start_time collectionName trackName artistName] npr-play-id :_id}]
  {:artist_name     artistName
   :track_name      trackName
   :collection_name collectionName
   :start_time      (-> _start_time
                        json/start-time->local-date-time
                        local-date-time->db-time)
   :duration        _duration
   :npr_play_id     npr-play-id
   :program_name    (:name program)
   :npr_airing_id   _id
   :npr_event_id    event_id
   :npr_program_id  program_id})

(defn load-from-files [db data-dir]
  (sql/with-db-connection [conn db]
    (doseq [file (downloads/sorted-day-files data-dir)
            :let [parsed-body (json/parse (slurp file))
                  airings (:onToday parsed-body)]
            {:keys [playlist program] :as airing} airings]
      (if (seq playlist)
        (apply sql/insert! conn :plays
               (map (partial airing&play->db-row-map airing)
                    playlist))
        (println "No playlist to insert for" (:name program)
                 "from" (downloads/file->name file))))))

(comment
  (recreate db)
  (load-from-files db "./data")
  )
