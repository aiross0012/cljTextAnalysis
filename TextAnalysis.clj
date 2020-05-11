(ns TextAnalysis)
(import '[java.util.concurrent Executors])
(use 'clojure.java.io)
;(require '[clojure.core.reducers :as r])

(def of "ProbabilityDistribution.txt")

;; This function, if uncommented, will output a map to all n-char
;; substrings in the file (as keys), and a map to all of the characters
;; following that particular substring (as the value)

;; (defn print-results [s n i char-mapping]
;;   (try
;;     (def sub (subs s i (+ i n)))
;;     (def nc (subs s (+ i n) (inc (+ i n))))
;;     (if (nil? (get char-mapping sub))
;;       (do
;;         (def char-mapping (assoc char-mapping sub {nc 1})))
;;       (do
;;         (def c-val (get char-mapping sub))
;;         (if (nil? (get c-val nc))
;;           (do
;;             (def c-val (assoc c-val nc 1))
;;             (def char-mapping (assoc char-mapping sub c-val)))
;;           (do
;;             (def v-val (inc (get c-val nc)))
;;             (def c-val (assoc c-val nc v-val))
;;             (def char-mapping (assoc char-mapping sub c-val))))))
;;     (catch StringIndexOutOfBoundsException e ())))

;; Determines values for the occurrences map
(defn get-occurrences [s n i occurences num-occurences]
  (try
    (def sub (clojure.string/replace (subs s i (+ i n)) " " "[space]"))
    (if (nil? (get occurences sub))
      (do
        (def occurences (assoc occurences sub 1))
        (def num-occurences (inc num-occurences)))
      (do
        (def sub-val (inc (get occurences sub)))
        (def occurences (assoc occurences sub sub-val))
        (def num-occurences (inc num-occurences))))
    (catch StringIndexOutOfBoundsException e ())))

;; Main function, creates the map of the characters, outputs probability distribution,
;; finds the total information in the file
(defn loop-n-chars [s n]
  (def occurences {}) ; Map w/ n-char as the key, number of occurrences as the value
  (def num-occurences 0) ; Variable for number of occurrences of n-chars in this run
  (def total-information 0) ; Variable for total information in this run
  (def i 0) ; Variable for looping
  (println "Looping with " n " character(s)")
  (println "=============================")
  ;; Find number of occurrences for each combination of n-chars
  (doseq [i (range (count s))]
    (do
      (get-occurrences s n i occurences num-occurences)))
  ;; Get Probability Distribution/Total information in file
  (spit of "Probability Distribution:")
  (println "Probability Distribution:")
  (try
    (doseq [[k v] occurences]
      ;; Output the probability distribution
      (def p (/ v num-occurences))
      (print k ": ")
      (println (format "%6f" (float p)))

      ;; These lines output to the ProbabilityDistribution.txt file
      ;; (spit of k :append true)
      ;; (spit of ": " :append true)
      ;; (spit of (format "%6f" (float p)) :append true)
      ;; (spit of "\n" :append true)

      (def total-information (+ total-information (* v (- p) (Math/log p)))))
    (catch NullPointerException e ()))
  (println "\nInformation Summation:")
  (println total-information))

;; Get the time of a single run of the program
(defn get-time [s]
  (let [starttime (System/nanoTime)]
    (loop-n-chars s 1)
    (println)
    (loop-n-chars s 2)
    (println)
    (loop-n-chars s 3)
    (/ (- (System/nanoTime) starttime) 1e9)))

;; Get the average time of the program execution using x 
;; number of threads
(defn get-avg-time [threads]
  (def s (slurp "WarAndPeace.txt"))
  (set-agent-send-off-executor! (Executors/newFixedThreadPool threads))
  (println "Threads: " threads)
  (def t1 (get-time s))
  (def t2 (get-time s))
  (def t3 (get-time s))
  (println)
  (println "Elapsed Time 1: " t1)
  (println "Elapsed Time 2: " t2)
  (println "Elapsed Time 3: " t3)
  (print "Avg. Time of " threads " threads: ")
  (println (/ (+ t1 t2 t3) 3) "\n"))


;; Uncomment any of the below lines to run the program
;; with the provided number of threads

;; (get-avg-time 1)
;; (get-avg-time 2)
;; (get-avg-time 4)
;; (get-avg-time 8)
;; (get-avg-time 16)
;; (get-avg-time 32)
(get-avg-time 64)

;;Kill threadpools
(shutdown-agents)