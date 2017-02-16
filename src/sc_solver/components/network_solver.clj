(ns sc-solver.components.network-solver
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.solver :as solver]
            [clojure.tools.logging :as log]))


;Todo externalize the number of processes.
(defn process-graphs [status msg-chan response-chan]
  (async/pipeline 4 response-chan (map solver/flow-graph) msg-chan))

(defrecord Network-solver [status msg-chan response-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (process-graphs status msg-chan response-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

;Todo clean up, we don't have a process here anymore.
(defn new-network-solver [msg-request-chan msg-response-chan]
  (->Network-solver (atom :init) msg-request-chan msg-response-chan))