(ns sc-solver.solver
  (:require [ubergraph.core :as uber]
            [ubergraph.alg :as alg]))

(def store "store")

(def dc "dc")

(defn get-flow-amount [graph edge]
  (uber/attr graph edge :flow-amount))

(defn get-edge-max [graph edge]
  (uber/attr graph edge :max))

(defn get-existing-inventory [graph edge]
  (uber/attr graph (uber/dest edge) :inventory))

(defn get-target-inventory [graph edge]
  (uber/attr graph (uber/dest edge) :target))

(defn get-edge-dest-type
  "Given an edge returns the type of the destination."
  [graph edge]
  (uber/attr graph (uber/dest edge) :type))

(defn is-store-node?
  [graph node]
  (= (uber/attr graph node :type) store))

(defn is-dest-node-store? [graph edge]
  (if (= (get-edge-dest-type graph edge) store) true false))

(defn get-graph-start-node [graph]
  :vendor-0)

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
                           (* (/ (- target-inventory total-inventory) target-inventory) -1))
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
        (and (is-dest-node-store? graph edge) (is-edge-under-max-constraint? graph edge)) (> 0 (utility-fn edge))
        (and (is-dest-node-store? graph edge) (not (is-edge-under-max-constraint? graph edge))) false
        :else true))))

(defn is-end-node-fn
  "Return a function that determines if we have an end node.  Currently assumes a single inbound edge."
  [graph]
  (fn [node]
    (let [utility-fn (utility-fn graph)]

      (cond
        (and (is-store-node? graph node) (is-edges-to-end-node-valid? graph node)) (> 0 (utility-fn (first (uber/in-edges graph node))))
        :else false))))

(defn have-end-nodes? [g]
  (let [nodes (filter #(is-store-node? g %)  (uber/nodes g))]
    (reduce #(if (is-edges-to-end-node-valid? g %2) (reduced true) %) false nodes)))

(defn flow-graph
  "Given a graph continue to solve it until we can't send product to stores."
  [graph]
  (loop [g graph]
    (if (have-end-nodes? g)
      (let [path (alg/shortest-path g {:start-node (get-graph-start-node g) :end-node? (is-end-node-fn g) :cost-fn (utility-fn g) :edge-filter (is-edge-valid-fn g)})]
       (if (nil? path)
         (uber/viz-graph g {:auto-label true})
         (recur (incremment-flow-amounts path g))))
      (uber/viz-graph g {:auto-label true}))))

