(ns playlister.db
  (:require [clojure.java.jdbc :as sql]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [playlister.download :as download])
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
           [:end_time :text]
           [:duration :numeric]
           [:npr_song_id :text]
           [:npr_episode_id :text]
           [:npr_program_id :text])]]]
    (sql/with-db-connection [conn db]
      (doseq [statement statements]
        (sql/execute! conn statement)))))

(defn tracks-files [data-dir]
  (-> (io/file data-dir)
      (.listFiles (reify java.io.FilenameFilter
                    (accept [_ _ name]
                      (boolean (re-find #"tracks-.+\.json" name)))))
      (->> (sort-by #(.getName %)))))

(defn json-time->db-time [s]
  (-> s
      (LocalDateTime/parse download/song-start-end-time-format)
      (.format DateTimeFormatter/ISO_LOCAL_DATE_TIME)))

(defn load-from-files [db data-dir]
  (sql/with-db-connection [conn db]
    (doseq [file (tracks-files data-dir)]
      (let [parsed-body (json/parse-string (slurp file) true)
            results (-> parsed-body :tracklist :results)]
        (apply sql/insert! conn :plays
           (map (fn [{:keys [song song_id]}]
                  {:artist_name (:artistName song)
                   :track_name (:trackName song)
                   :collection_name (:collectionName song)
                   :start_time (json-time->db-time (:_start_time song))
                   :end_time (json-time->db-time (:_end_time song))
                   :duration (:_duration song)
                   :npr_song_id song_id
                   :npr_episode_id (:_episode_id song)
                   :npr_program_id (:_program_id song)})
                results))))))

(comment
  (recreate db)
  )
