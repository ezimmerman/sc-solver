# Project
Proof of concept using a graph to solve retail supply chain problems. Given a network of vendors, DCs, and stores, 
the solver will distribute product to satisfy edge max setting, and in order of need of the stores.  In the case of
scarcity with a max set on an edge that will not allow a store to order up to it's target, the store with the greatest
ratio between target and inventory will receive product first.

It will solve for any graph so long as the graph is a DAG (Directed Acyclic Directed) graph.

Edges will have their flow-amounts updated to show the total amount of product that flowed through that edge to supply 
the stores.

A max can be set on any edge and the solver will honor the max.

When the solver is done it will launch a visual of the solved graph with graphviz.  You can inspect the flow-amounts
on the edges to see how much product has been allocated.

## Motivation
To show a graph library can be used to greatly simplify the SC problem space.

## Getting Started
### Prerequisites ###

[Leiningen] (http://leiningen.org)
[graphviz] (http://www.graphviz.org)


### Installing

From working leiningen installation 
- lein install

### Running tests

There are 4 graphs pre-described in sc-solver.test-solver namespace.
- Graph 1 is a simple graph with no constraints.
- Graph graph-dc-store-constrained has a constraint on the dc to store-0 edge
- Graph graph-vendor-dc-constrained Has a constraint on the vendor to dc.  When we flow this graph, we can see that
store-0 gets the 1 product allocated, since it has the greatest need.
- Graph graph-large Is simply a large graph to show the solver can work on any graph.

#### Running sample graphs.
- Start a REPL using `lein repl`.
- Switch to the sc-solver.test-solver name space. 
- Run tests with lein test:
    `(in-ns 'sc-solver.test-solver)`

View any of the graphs before being solved using:
    `(uber/viz-graph graph-1 {:auto-label true})`
    
Run the solver using:
    `(s/flow-graph graph-1)`
    
Tests written on core functions using clojre.test
Tests can be found under sc-sover/test
    
## License

Copyright © 2016 Eric Zimmerman

Distributed under the Eclipse Public License version 1.0.
