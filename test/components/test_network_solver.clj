(ns components.test-network-solver
  (:require
    [clojure.test :refer :all]
    [sc-solver.components.network-solver :as solver]
    [sc-solver.test-solver :as test-solver]
    [clojure.core.async :as async]
    [ubergraph.core :as uber]))

(def response-ch (async/chan))
(def error-ch (async/chan))
(def req-ch (async/chan))


(deftest test-network-solver
  (do (solver/process-graphs  req-ch response-ch error-ch)
      (async/>!! req-ch test-solver/graph-1)
      (is (= 5 (uber/count-nodes (async/<!! response-ch))))))