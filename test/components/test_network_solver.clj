(ns components.test-network-solver
  (:require
    [clojure.test :refer :all]
    [sc-solver.components.network-solver :as solver]
    [sc-solver.test-solver :as test-solver]
    [clojure.core.async :as async]
    [ubergraph.core :as uber]
    [clj-time.core :as t]))

(def response-ch (async/chan))
(def error-ch (async/chan))
(def req-ch (async/chan))


(deftest test-network-solver
  ;We'll get back a vector on the response channel.  Should contain graphs
  ;equal to the number of days configured in the profiles.clj days file.
  (let [graph-plus-day (uber/add-attr test-solver/graph-1 :vendor :day (t/today-at-midnight))]
    (do (solver/process-graphs req-ch response-ch error-ch)
        (async/>!! req-ch graph-plus-day)
        (is (= 10 (count (async/<!! response-ch)))))))