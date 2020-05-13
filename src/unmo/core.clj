(ns unmo.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [fipp.edn :as fipp]
            [unmo.dictionary :as dict]
            [unmo.morph :as morph]
            [unmo.responder :as resp]
            [unmo.version :as ver]))

(def dictionary-file "dict.edn")

(def responders {:what     resp/response-what
                 :random   resp/response-random
                 :pattern  resp/response-pattern
                 :template resp/response-template
                 :markov   resp/response-markov})

(defn save-dictionary
  "Saves the result of pprint the map dictionary to the specified filename."
  [dictionary filename]
  (let [data (with-out-str
               (binding [*print-length* false]
                 (fipp/pprint dictionary)))]
    (spit filename data :encoding "UTF-8")))

(defn load-dictionary
  "Returns a dictionary map read from the specified filename."
  [filename]
  (if (.exists (io/as-file filename))
    (-> filename (slurp :encoding "UTF-8") read-string)
    {}))

(defn- rand-responder
  "Returns the one of keywords, :what :random :pattern :template :markov,
  which is determined by the probability.

  :what     10%
  :random   20%
  :pattern  30%
  :template 20%
  :markov   20%"
  []
  (let [n (rand-int 100)]
    (cond (<  0 n 10) :what
          (< 11 n 30) :random
          (< 31 n 60) :pattern
          (< 61 n 80) :template
          :else       :markov)))

(defn- dialogue
  "Takes an input, its morphological analysis results, a dictionary, and
  returns a sentence generated by the responder. If the responder returned nil,
  calls :what responder and returns the result sentence."
  ([input parts dictionary]
   (dialogue input parts dictionary (rand-responder)))

  ([input parts dictionary res-key]
   (if-let [res ((res-key responders) {:input      input
                                       :dictionary dictionary
                                       :parts      parts})]
     (str (str/capitalize (name res-key)) "> " res)
     (dialogue input parts dictionary :what))))

(defn -main [& args]
  (morph/start)
  (println (format "Unmo version %s launched." ver/unmo-version))
  (print "> ")
  (flush)

  (loop [input      (read-line)
         dictionary (load-dictionary dictionary-file)]
    (if (str/blank? input)
      (do (println "Shutting down...")
          (morph/stop)
          (save-dictionary dictionary dictionary-file)
          (println "Quit."))
      (let [parts (morph/analyze input)
            res   (dialogue input parts dictionary)]
        (println res)
        (print "> ")
        (flush)
        (recur (read-line) (dict/study dictionary input parts))))))
