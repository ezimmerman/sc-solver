(ns sc-solver.components.network-solver
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.solver :as solver]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]))

(def solver-procs (read-string (env :solver-procs)))

(defn process-graphs [msg-chan response-chan]
  (async/pipeline solver-procs response-chan (map solver/flow-graph) msg-chan))

(defrecord Network-solver [msg-chan response-chan]
  component/Lifecycle
  (start [component]
    (process-graphs msg-chan response-chan)
    component)
  (stop [component]
    component))


(defn new-network-solver [msg-request-chan msg-response-chan]
  (->Network-solver msg-request-chan msg-response-chan))