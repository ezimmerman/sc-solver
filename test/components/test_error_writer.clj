(ns components.test-error-writer
  (:require
    [clojure.test :refer :all]
    [sc-solver.components.error-writer :as writer]
    [clojure.core.async :as async]))


(def error-ch (async/chan))


(deftest test-error-writer
  (do (writer/process-errors  error-ch)
      (async/>!! "error")
      (is (= 5 (uber/count-nodes (async/<!! response-ch))))))