(ns components.test-schedules-reader
  (:require [clojure.test :refer :all]
            [clojure.core.async :as async]
            [sc-solver.components.schedules-reader :as reader]))

(def response-ch (async/chan))
(def component-state (atom :running))


(deftest test-filter
         (do (reader/read-schedules component-state response-ch)
             (is (= 0 (get (first (async/<!! response-ch)) :product)))))

(deftest print-filter
  (do (reader/read-schedules component-state response-ch)
      (println (async/<!! response-ch))))
