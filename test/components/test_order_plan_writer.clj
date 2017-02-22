(ns components.test-order-plan-writer
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [clojure.test :refer :all]
            [sc-solver.components.order-plan-writer :as opw]
            [sc-solver.domain :as domain])
  (:import (java.util Date)))

(def req-ch (async/chan))
(def error-ch (async/chan))
(def component-state (atom :running))
(def op-0 (domain/->Orderplan 0 (Date.) :vendor-0 :dc-0 5))
(def op-1 (domain/->Orderplan 0 (Date.) :vendor-0 :dc-2 10))

(deftest test-write-order-plan
  (do (opw/process-order-plans component-state req-ch error-ch)
      (async/>!! req-ch [op-0 op-1])))