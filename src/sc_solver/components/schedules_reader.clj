(ns sc-solver.components.schedules-reader
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.util.network-creator :as nc]))

(defn read-schedules [status response-chan]
  (async/go
    (async/>! response-chan (nc/make-network))))

(defrecord Schedules-reader [status response-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (read-schedules status response-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-schedules-reader [msg-response-chan]
  (->Schedules-reader (atom :init) msg-response-chan))