(ns sc-solver.main
  (:require [immuconf.config :as ic]
            [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [sc-solver.components.product-chunker :as chunker]
            [sc-solver.components.schedules-reader :as reader]
            [sc-solver.components.assemble-network :as assemble])
  (:gen-class))

(defn system []
  (let [schedules-reader-chan (async/chan 10)
        schedules-reader-response-chan (async/chan 10)
        product-chunker-chan (async/chan 10)
        product-chunker-resp-chan (async/chan 10)
        assemble-network-chan (async/chan 10)
        assemble-network-resp-chan (async/chan 10)]
    (component/system-map
      :schedules-reader (reader/new-schedules-reader schedules-reader-response-chan)
      :product-chunker (chunker/new-product-chunker product-chunker-chan product-chunker-resp-chan)
      :assemble-network (assemble/new-assemble-network assemble-network-chan assemble-network-resp-chan)))
  )

(defn start-app []
  (component/start-system (system)))

(defn stop-app [app])

(defn -main
  "App entry point"
  []
  (start-app ))