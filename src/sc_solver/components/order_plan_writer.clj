(ns sc-solver.components.order-plan-writer
  (:require [cheshire.core :refer :all]
            [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]))
;Todo remove hard coding.
(def op-directory "/Users/ezimmerman/clojure/sc-solver/plans/")

;Todo remove UUID and create sensible file names.
(defn write-order-plans [ops]
  (let [file-name (str op-directory "order-plan" (str (java.util.UUID/randomUUID)) ".json")
        op-json (generate-string ops)]
    (spit file-name op-json)))

;(defn write-order-plans [ops]
;  (map write-plan (first ops)))

(defn process-order-plans [status msg-chan]
  (async/go (while (= @status :running)
              (write-order-plans (async/<! msg-chan)))
            (async/close! msg-chan)))

(defrecord Order-plan-writer [status msg-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (process-order-plans status msg-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-order-plan-writer [msg-request-chan]
  (->Order-plan-writer (atom :init) msg-request-chan))