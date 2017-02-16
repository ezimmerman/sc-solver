(ns sc-solver.main
  (:require [immuconf.config :as ic]
            [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.components.product-chunker :as chunker]
            [sc-solver.components.schedules-reader :as reader]
            [sc-solver.components.assemble-network :as assemble]
            [sc-solver.components.network-solver :as solver]
            [sc-solver.components.order-plan-creator :as creator]
            [sc-solver.components.order-plan-writer :as writer])
  (:gen-class))

(defn system []
  (let [schedules-reader-response-chan (async/chan 10)
        product-chunker-resp-chan (async/chan 10)
        assemble-network-resp-chan (async/chan 10)
        network-solver-resp-chan (async/chan 10)
        order-plan-creator-resp-chan (async/chan 10)]
    (component/system-map
      :schedules-reader (reader/new-schedules-reader schedules-reader-response-chan)
      :product-chunker (chunker/new-product-chunker schedules-reader-response-chan product-chunker-resp-chan)
      :assemble-network (assemble/new-assemble-network product-chunker-resp-chan assemble-network-resp-chan)
      :network-solver (solver/new-network-solver assemble-network-resp-chan network-solver-resp-chan)
      :order-plan-creator (creator/new-order-plan-creator network-solver-resp-chan order-plan-creator-resp-chan)
      :order-plan-writer (writer/new-order-plan-writer order-plan-creator-resp-chan)))
  )

(defn start-app []
  (component/start-system (system)))

(defn stop-app [app])

(defn -main
  "App entry point"
  []
  (start-app))