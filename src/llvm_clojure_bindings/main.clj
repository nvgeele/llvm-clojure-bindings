(ns llvm-clojure-bindings.main
  (:require [llvm-clojure-bindings.example-ast])
  (:use [llvm-clojure-bindings.ast]
        [llvm-clojure-bindings.parser]
        [llvm-clojure-bindings.llvm]
        [clojure.java.shell :only [sh]]))

(defn compile-ir
  [ir-file]
  (let [{:keys [out err exit]}
        (sh "/usr/local/opt/llvm/bin/llc" "-filetype=obj" "-o" "main.o" "main.ir")]
    (when (not= exit 0)
      (throw (Exception. (str "Compiler Exit Code:" exit))))
    (when (= exit 0)
      (println "Producing object file..."))))

(defn link-and-run [obj-file]
  (let [{:keys [out err exit]}
        (sh "/usr/bin/cc" obj-file)]
    (when (not= exit 0)
      (throw (Exception. (str "CC Exit Code: " exit))))
    (when (= exit 0)
      (println "Linking binary...")
      (println "Executing binary a.out:")
      (println (:out (sh "./a.out"))))))

(defn run-test [ast]
  (let [[b mymod] (create)]
    (populate! b mymod ast)
    (println (repeat 30 \=))
    (LLVMDumpModule mymod)
    (println (repeat 30 \=))
    (LLVMWriteBitcodeToFile mymod "main.ir")
    (try
      (do
        (compile-ir "main.ir")
        (link-and-run "main.o"))
      (catch Exception e
        (println (str (.getMessage e))))
      (finally
        (LLVMDisposeModule mymod)))
    (println "Finished")))

(defn -main []
  (let [input (slurp "input.program")]
    (run-test (parse-str input))))
