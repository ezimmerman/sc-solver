(ns sc-solver.components.product-chunker
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]))

; Component that expects a map that contains a key :product
; groups the same products in a single vector.

(defn process-schedules [status msg-chan response-chan]
  (async/go (while (= @status :running)
              (let [msg (async/<! msg-chan)
                    grouped (group-by :product msg)]
                (doseq [product grouped] (async/>! response-chan (val product)))))
            (async/close! msg-chan)))

(defrecord Product-chunker [status msg-chan response-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component):runing)
    (process-schedules status msg-chan response-chan)
    component)
  (stop [component]
    (reset! (:status component):stopped)
    component))

(defn new-product-chunker [msg-request-chan msg-response-chan]
  (->Product-chunker (atom :init) msg-request-chan msg-response-chan))