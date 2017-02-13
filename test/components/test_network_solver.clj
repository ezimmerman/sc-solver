(ns components.test-network-solver
  (:require
    [clojure.test :refer :all]
    [sc-solver.components.network-solver :as solver]
    [sc-solver.test-solver :as test-solver]
    [clojure.core.async :as async]
    [ubergraph.core :as uber]))

(def response-ch (async/chan))
(def req-ch (async/chan))
(def component-state (atom :running))

(deftest test-network-solver
         (do (solver/process-graphs component-state req-ch response-ch)
             (async/>!! req-ch test-solver/graph-1)
             (is (= 5 (uber/count-nodes (async/<!! response-ch))))))