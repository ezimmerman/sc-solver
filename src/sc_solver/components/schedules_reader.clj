(ns sc-solver.components.schedules-reader
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.util.network-creator :as nc]))

(defn read-schedules [status response-chan]
  (async/go (while (= @status :running)
              (let [schedules (nc/make-network)]
                (async/>! response-chan schedules)))))

(defrecord Schedules-reader [status response-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component):runing)
    (read-schedules status response-chan)
    component)
  (stop [component]
    (reset! (:status component):stopped)
    component))

(defn new-schedules-reader [msg-response-chan]
  (->Schedules-reader (atom :init) msg-response-chan))