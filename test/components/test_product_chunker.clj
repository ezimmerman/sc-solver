(ns components.test-product-chunker
  (:require [clojure.core.async :as async]
            [clojure.test :refer :all]
            [sc-solver.components.product-chunker :as chunker]
            [sc-solver.util.network-creator :as creator]))

(def response-ch (async/chan))
(def req-ch (async/chan))
(def error-ch (async/chan))
(def component-state (atom :running))
(def msg (creator/network 1 2 4))


(deftest test-chunker
  (do (chunker/process-schedules component-state req-ch response-ch error-ch)
      (async/>!! req-ch msg)
      (is (= 6 (count (async/<!! response-ch))))))

