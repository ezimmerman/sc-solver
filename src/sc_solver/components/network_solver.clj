(ns sc-solver.components.network-solver
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.solver :as solver]))



(defn process-graphs [status msg-chan response-chan]
  (async/go (while (= @status :running)
              (let [graph (async/<! msg-chan)
                    solved-graph (solver/flow-graph graph)]
                (async/>! response-chan solved-graph)))
            (async/close! msg-chan)))

(defrecord Network-solver [status msg-chan response-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component):running)
    (process-graphs status msg-chan response-chan)
    component)
  (stop [component]
    (reset! (:status component):stopped)
    component))

(defn new-network-solver [msg-request-chan msg-response-chan]
  (->Network-solver (atom :init) msg-request-chan msg-response-chan))