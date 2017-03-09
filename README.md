# Project
Proof of concept using a graph to solve allocation and replenishment problems. Given a network of vendors, DCs, and stores,
the solver will distribute product to satisfy edge max setting, and in order of need of the stores.  In the case of
scarcity with a max set on an edge that will not allow a store to order up to it's target, the store with the greatest
ratio between target and inventory will receive product first.

It will solve for any graph so long as the graph is a DAG (Directed Acyclic graph) with a single vendor.

Edges will have their flow-amounts updated to show the total amount of product that flowed through that edge to supply 
the stores.

A max can be set on any edge and the solver will honor the max.

There are a couple of ways to run this.  This first is to execute the sample graphs found in the test-solver namespace.
A visual can be launched of a sample graph before the graph is solved, and after the graph is solved.

The second way of running this is auto solving generated networks and saving them to the file system.

See below for details on how to run each.

## Motivation
To show a graph library can be used to greatly simplify the allocation/replenishment problem space.

## Getting Started  with visual sample
### Prerequisites ###

[clojure] (https://clojure.org)
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
    `(uber/viz-graph (s/flow-graph graph-1) {:auto-label true})`
    
When the solver is done it will launch a visual of the solved graph with graphviz.  You can inspect the flow-amounts
on the edges to see how much product has been allocated.    

## Getting Started with solving networks and writing order plans.    

### Installing

From working leiningen installation
- lein install

#### Running solving generated order plans.
- Start a REPL using `lein with-profile default,dev repl`
- Switch to the main namespace using `(in-ns 'sc-solver.main)`
- In the repl execute `(start-app)` Start app doesn't exit at this time, you will know sc-solver is done when
the number of plans saved in the configured plans location equals the number of products configured (see below for configuration).
On unix you can check the number of plans written with `ls *.json | wc -l` in the saved plans directory.

Not supported yet, but would be easy to do is to output recommended distribution quantities (Dcs to stores).

##### Configuration options.
sc-solver/profiles.clj provides a number of configuration options for the dev, and test leiningen profiles.
The sc-solver.util.network-creator namespace will create a random network based on the profiles settings.
Your can control the width of the network by configuring the number of DCs, stores, and products in the network.
The network will always be a single vendor, to the number of configured dcs to stores.  Dcs and Stores will be spread evenly
across the network.  :products determines the number of products you are solving for so a value of 1000 would create 1000
networks to solve for. Days configuration is currently not supported, it will solve for a single day.  The location of where the plans are saved is
configured by :plans.  :Solver-procs and :assemble-procs determines the number of concurrent processes that are used for that logic.
Solving the network is expensive, so turning up or down the solver-procs number has a big impact on cpu usage and time to solve
the ordering problem.

The default settings for the dev env will create 1000 networks of a single vendor to 5 DCs and 20 stores, and will save them to
the sc-solver/plans directory.  The format of the saved plans is a ship date, source, destination, and order qty in json format.
    
Tests written on core functions using clojre.test
Tests can be found under sc-sover/test
    
## License

Copyright © 2016 Eric Zimmerman

Distributed under the Eclipse Public License version 1.0.
