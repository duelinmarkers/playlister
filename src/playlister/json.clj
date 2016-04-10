(ns playlister.json
  (:require cheshire.core)
  (:import (java.time.format DateTimeFormatter)
           (java.time LocalDateTime)))

(def start-time-format (DateTimeFormatter/ofPattern "MM-dd-yyyy HH:mm:ss"))

(defn start-time->local-date-time [s]
  (LocalDateTime/parse s start-time-format))

(defn parse [s]
  (cheshire.core/parse-string s true))