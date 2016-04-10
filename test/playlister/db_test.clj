(ns playlister.db-test
  (:require [clojure.test :refer :all]
            [playlister.db :refer :all]))

(deftest json-to-db-conversion
  (is (= {:program_name "The DJ or Show Name"
          :artist_name "The Artist"
          :collection_name "The Album"
          :track_name "The Track"
          :start_time "1999-12-31T23:59:59"
          :duration 200000
          :npr_program_id "the program id"
          :npr_airing_id "the airing id"
          :npr_event_id "the event id"
          :npr_play_id "the play id"}
         (airing&play->db-row-map
           {:program {:name "The DJ or Show Name"}
            :program_id "the program id"
            :_id "the airing id"
            :event_id "the event id"}
           {:artistName "The Artist"
            :collectionName "The Album"
            :trackName "The Track"
            :_start_time "12-31-1999 23:59:59"
            :_duration 200000
            :_id "the play id"}))))
