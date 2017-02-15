(ns components.test-order-plan-creator
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.test-solver :as solver]
            [clojure.test :refer :all]
            [sc-solver.util.network-creator :as graph-creator]
            [sc-solver.components.order-plan-creator :as opc]))

(def response-ch (async/chan))
(def req-ch (async/chan))
(def component-state (atom :running))
(def graph solver/graph-1)


(deftest test-assemble-network
  (do (opc/process-graphs component-state req-ch response-ch)
      (async/>!! req-ch graph)
      (is (= 0 (:qty (first (async/<!! response-ch)))))))