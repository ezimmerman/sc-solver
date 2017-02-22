(ns components.test-error-writer
  (:require
    [clojure.test :refer :all]
    [sc-solver.components.error-writer :as writer]
    [clojure.core.async :as async]))


(def error-ch (async/chan))


(deftest test-error-writer
  (do (writer/process-errors  (atom :running) error-ch)
      (async/>!! error-ch "error" )))