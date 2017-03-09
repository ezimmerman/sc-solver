(ns sc-solver.components.schedules-reader
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.util.network-creator :as nc]))

; This component is would need to be swapped out in product.
; It's purpose is to create schedules.  This schedules reader
; Simpley uses the network-creator to create arbitrary schedules.
; We would want to swap this out for something that pulls in schedules
; from a feed, or persistence store.
; Data format put on channel is:

(defn read-schedules [status response-chan error-chan]
  (async/go
    (async/>! response-chan (try (nc/make-network)
                                 (catch Exception e (async/>! error-chan e))))))

(defrecord Schedules-reader [status response-chan error-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (read-schedules status response-chan error-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-schedules-reader [msg-response-chan error-chan]
  (->Schedules-reader (atom :init) msg-response-chan error-chan))