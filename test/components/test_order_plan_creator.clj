(ns components.test-order-plan-creator
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.solver :as solver]
            [clojure.test :refer :all]
            [sc-solver.util.network-creator :as graph-creator]
            [sc-solver.components.order-plan-creator :as opc]
            [clj-time.core :as t]
            [ubergraph.core :as uber]
            [sc-solver.test-solver :as test-solver]))

(def response-ch (async/chan))
(def req-ch (async/chan))
(def error-ch (async/chan))
(def component-state (atom :running))


(deftest test-assemble-network
  (let [graph (uber/add-attr test-solver/graph-1 :vendor :day (t/today-at-midnight))
        graph-plus-day (solver/increment-vendor-date graph)]
    (do (opc/process-graphs component-state req-ch response-ch error-ch)
       (async/>!! req-ch [graph graph-plus-day])
       (is (= 2 (count (async/<!! response-ch)))))))