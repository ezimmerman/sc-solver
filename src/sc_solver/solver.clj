(ns sc-solver.solver
  (:require [ubergraph.core :as uber]
            [ubergraph.alg :as alg]
            [clj-time.core :as t]
            [sc-solver.services.forecast :as f]))

(def store "store")

(def dc "dc")

(def vendor "vendor")

(defn get-flow-amount [graph edge]
  (uber/attr graph edge :flow-amount))

(defn get-edge-max [graph edge]
  (uber/attr graph edge :max))

(defn get-inventory [graph node]
  (uber/attr graph node :inventory))

(defn get-existing-inventory [graph edge]
  (uber/attr graph (uber/dest edge) :inventory))

(defn get-target-inventory [graph edge]
  (uber/attr graph (uber/dest edge) :target))

(defn get-location
  [graph node]
  (uber/attr graph node :location))

(defn is-vendor-node?
  [graph node]
  (= (uber/attr graph node :type) vendor))

(defn get-vendor [graph]
  (first (filter #(is-vendor-node? graph %) (uber/nodes graph))))

(defn get-day
  [graph]
  (uber/attr graph (get-vendor graph) :day))

(defn get-product
  [graph]
  (uber/attr graph (get-vendor graph) :product))

(defn get-edge-dest-type
  "Given an edge returns the type of the destination."
  [graph edge]
  (uber/attr graph (uber/dest edge) :type))

(defn is-store-node?
  [graph node]
  (= (uber/attr graph node :type) store))

(defn is-dest-node-store? [graph edge]
  (if (= (get-edge-dest-type graph edge) store) true false))

;todo make this dynamic not hard coded.
(defn get-graph-start-node [graph]
  :vendor)

(defn get-store-edges [graph]
  (filter #(is-dest-node-store? graph %) (uber/edges graph)))

(defn get-store-nodes [graph]
  (filter #(is-store-node? graph %) (uber/nodes graph)))

(defn incremment-flow-amount
  [graph edge]
  (uber/add-attr graph edge :flow-amount (+ (uber/attr graph edge :flow-amount) 1)))

(defn incremment-flow-amounts
  "Given a path, get the edges and adds to their flow amounts.  Returns a graph."
  [path graph]
  (let [edges (alg/edges-in-path path)]
    (reduce incremment-flow-amount graph edges)))

(defn utility-fn
  "Given an edge calcuate the utility of the next product"
  [graph]
  (fn [edge] (let [dest (get-edge-dest-type graph edge)]
               (case dest
                 "store" (let [total-inventory (+ (get-flow-amount graph edge) (get-existing-inventory graph edge))
                               target-inventory (get-target-inventory graph edge)]
                           (cond
                             (>= total-inventory target-inventory) (Integer/MAX_VALUE)
                             :else (/ target-inventory (- target-inventory total-inventory))))
                 "dc" 0))))

(defn is-edge-under-max-constraint? [graph edge]
  (let [max (get-edge-max graph edge)
        flow-amount (get-flow-amount graph edge)]
    (cond
      (nil? max) true
      (< flow-amount max) true
      :else false)))

(defn is-edges-to-end-node-valid?
  "Given a node, check to see if the path to it only includes edges below max."
  [graph node]
  (let [path (alg/shortest-path graph (get-graph-start-node graph) node)
        edges (alg/edges-in-path path)]
    (reduce #(if-not (is-edge-under-max-constraint? graph %2) (reduced false) %) true edges)))

(defn is-edge-valid-fn
  "Return a function that given an edge will check to see if dest is store if so, check to see if
  it's utility is < 0 "
  [graph]
  (fn [edge]
    (let [utility-fn (utility-fn graph)]
      (cond
        (= Integer/MAX_VALUE (utility-fn edge)) false
        (and (is-dest-node-store? graph edge) (is-edge-under-max-constraint? graph edge) (not= Integer/MAX_VALUE (utility-fn edge))) (< 0 (utility-fn edge))
        (and (is-dest-node-store? graph edge) (not (is-edge-under-max-constraint? graph edge))) false
        :else true))))

(defn is-end-node-fn
  "Return a function that determines if we have an end node.  Currently assumes a single inbound edge."
  [graph]
  (fn [node]
    (let [utility-fn (utility-fn graph)]

      (cond
        (and (is-store-node? graph node) (is-edges-to-end-node-valid? graph node)) (< 0 (utility-fn (first (uber/in-edges graph node))))
        :else false))))

(defn have-end-nodes? [g]
  (let [nodes (filter #(is-store-node? g %) (uber/nodes g))]
    (reduce #(if (is-edges-to-end-node-valid? g %2) (reduced true) %) false nodes)))

(defn flow-graph
  "Given a graph recur to solve it until we can't send any more product to stores."
  [graph]
  (loop [g graph]
    (if (have-end-nodes? g)
      (let [path (alg/shortest-path g {:start-node (get-graph-start-node g) :end-node? (is-end-node-fn g) :cost-fn (utility-fn g) :edge-filter (is-edge-valid-fn g)})]
        (if (nil? path)
          g
          (recur (incremment-flow-amounts path g))))
      g)))

(defn increment-vendor-date
  "Increment the Vendors date by one day."
  [graph]
  (let [vendor-node (get-vendor graph)]
    (uber/add-attr graph vendor-node :day (t/plus (uber/attr graph vendor-node :day) (t/days 1)))))

(defn update-nodes-inventory
  "Return a graph with new inventory. Update the inventory on the node with the new inventory."
  [graph node inventory]
  (uber/add-attr graph node :inventory inventory))

(defn update-store-inventory
  "Return a graph with updated store inventory. Get previous days inventory from the store node
  (the dest on the edge-to-store) subtract sales forecast add previous flow amount."
  [graph edge-to-store]
  (let [yesterday-inventory (get-existing-inventory graph edge-to-store)
        vendor (get-vendor graph)
        store (uber/dest edge-to-store)
        sales-forecast (f/get-forecast (get-product graph) (get-location graph store) (get-day graph) yesterday-inventory)]
    (update-nodes-inventory graph store (- (+ yesterday-inventory (get-flow-amount graph edge-to-store)) sales-forecast))))

(defn update-inventory
  "Return a graph with updated inventory. Roll the inventory over from previous day.
  Update with sales forecast and deliveries."
  [graph]
  (let [store-edges (get-store-edges graph)]
    (reduce #(update-store-inventory %1 %2) graph store-edges)))

(defn clear-flow-amount
  [graph edge]
  (uber/add-attr graph edge :flow-amount nil))

(defn clear-flow-amounts
  [graph]
  (reduce #(clear-flow-amount %1 %2) graph (get-store-nodes graph)))

(defn next-day
  "Returns next day graph. On new graph: 1. update vendor date 2. Change inventory = previous-inventory - sales-forecast + flow amount.
  3. clear flow amounts on edges."
  [previous-day-graph]
  (->> previous-day-graph
       (increment-vendor-date)
       (update-inventory)
       (clear-flow-amounts)))

;todo revisit implementation.  It's got a bad smell.
(defn flow-for-days
  "Solve each day starting at first day to days. Solving the next day requires the previous day.
    Return vector of solved graphs."
  [graph days]
  (loop [g graph
         graphs []]
    (if (< (count graphs) days)
      (let [next-graph (->> g
                            (next-day)
                            (flow-graph))
            added-graphs (conj graphs next-graph)]
        (recur next-graph added-graphs))
      graphs)))


