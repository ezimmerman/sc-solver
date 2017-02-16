(ns components.test-assemble-network
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [ubergraph.core :as uber]
            [ubergraph.alg :as alg]
            [clojure.test :refer :all]
            [sc-solver.components.product-chunker :as chunker]
            [sc-solver.util.network-creator :as creator]
            [sc-solver.components.assemble-network :as assemble]))

(def response-ch (async/chan))
(def req-ch (async/chan))
(def component-state (atom :running))
(def msg (val (first (group-by :product (creator/network 1 2 4)))))


(deftest test-assemble-network
  (do (assemble/process-schedules component-state req-ch response-ch)
      (async/>!! req-ch msg)
      (is (= 7 (uber/count-nodes (async/<!! response-ch))))))