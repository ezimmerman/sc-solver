{:dev  {:env {:dcs            "5"
              :stores         "20"
              :products       "1000"
              :days           "30"
              :plans          "./plans/"
              :solver-procs   "6"
              :assemble-procs "4"}}
 :test {:env {:dcs      "1"
              :stores   "2"
              :products "1"
              :days     "1"
              :plans    "./plans"
              :solver-procs   "6"
              :assemble-procs "4"}}
 :prod {:env {:dcs      "5"
              :stores   "20"
              :products "1"
              :days     "1000"}}}