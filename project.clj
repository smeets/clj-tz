(defproject app "0.1.0"
  :description "Time-Zone converter."
  :url "http://github.com/MurlocBrand/tz"
  :main tz.core
  :bin { :name "tz" }
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]])
