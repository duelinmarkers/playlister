(defproject playlister "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http-lite "0.3.0"]
                 [cheshire "5.5.0"]]
  :main ^:skip-aot playlister.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
