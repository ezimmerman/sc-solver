(ns sc-solver.components.product-chunker
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

; Component that expects a map that contains a key :product
; groups the same products in a single vector.
; Expects a vector of schedules on the msg-chan.
; Puts a map of schedules keyed by product.  For instance {1: [schedule 1 schedule 2 etc]}

(defn process-schedules [status msg-chan response-chan error-chan]
  (async/go (while (= @status :running)
              (let [msg (async/<! msg-chan)
                    grouped (try (group-by :product msg)
                                 (catch Exception e (async/>! error-chan e)))]
                (doseq [product grouped] (async/>! response-chan (val product)))))
            (async/close! msg-chan)))

(defrecord Product-chunker [status msg-chan response-chan error-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (process-schedules status msg-chan response-chan error-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-product-chunker [msg-request-chan msg-response-chan error-chan]
  (->Product-chunker (atom :init) msg-request-chan msg-response-chan error-chan))