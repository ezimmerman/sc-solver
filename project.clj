(defproject sc-solver "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ubergraph "0.3.0"]
                 [clj-time "0.13.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [org.clojure/core.async "0.2.395"]
                 [levand/immuconf "0.1.0"]
                 [environ "1.1.0"]
                 [cheshire "5.7.0"]]
  :target-path "target/%s"
  :plugins [[lein-environ "1.1.0"]]
  :main ^:skip-aot sc-solver.main
  :profiles {:uberjar {:aot :all}})
