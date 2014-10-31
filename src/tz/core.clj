(ns tz.core
  (:require [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(def cli-options
  [["-h" "--help" "Display this message."]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn usage [options-summary]
  (->> ["tz - time zone converter"
        ""
        "Usage: tz \"[yyMMdd] (hh)[:mm] [am|pm] (pst|cet|gmt+HH:MM) [cet|pst|gmt+HH:MM]\""
        "Outputs: yyMMdd HH[:mm] [am|pm] z"
        ""
        "Example:"
        "    tz \"7 pm pdt gmt+02:00\""
        "    tz \"23:00 gmt+07:00 cet\""
        ""
        "Options:"
        options-summary]
  (str/join \newline)))

(def dayFormatter (new java.text.SimpleDateFormat "yyMMdd"))

(def dateFormat24HH (new java.text.SimpleDateFormat "yyMMdd HH z"))
(def dateFormat24HHMM (new java.text.SimpleDateFormat "yyMMdd HH:mm z"))

(def dateFormat12HH
  (new java.text.SimpleDateFormat "yyMMdd hh a z" java.util.Locale/UK))
(def dateFormat12HHMM
  (new java.text.SimpleDateFormat "yyMMdd hh:mm a z" java.util.Locale/UK))

(defn numeric?
  "Check if sequence is numeric."
  [s]
  (if-let [s (seq s)]
    (let [s (if (= (first s) \-) (next s) s)
          s (drop-while #(Character/isDigit %) s)
          s (if (= (first s) \.) (next s) s)
          s (drop-while #(Character/isDigit %) s)]
      (empty? s))))

(defn tokenize
  "Tokenize input. <yyMMdd> <nmbr> <am/pm> <tz>"
  [input]
  (-> input
    str/upper-case
    (str/replace #"\s+" " ")
    (str/split #" ")))

(defn convertDate
  "Converts a given date-time string to current time-zone Date object."
  [input]
  (def is12H (or (.contains input "am") (.contains input "pm")))

  (def tokenized (tokenize input))

  ;; Enforce at least H z
  (cond
    (< (count tokenized) 2) (exit 1 "Invalid input."))

  (try
    (def yearMonthDay (subs input 0 6))
    (catch Exception e (def yearMonthDay (str "nonsense"))))

  ;; Check if we should use today or given date.
  (if (numeric? yearMonthDay)
      (def fromDate yearMonthDay)
      (def fromDate (.format dayFormatter (new java.util.Date))))

  ;; [hh:mm timezone:offset]
  (def timeParts
    (if (numeric? yearMonthDay)
      (drop 1 tokenized)
      tokenized))

  ;; Construct time string; need to circumvent optional arg (date).
  (def fromTime (str/join " "
    (if is12H
      (take 3 timeParts)
      (take 2 timeParts))))

  (def fromZone
    (if is12H
      (nth timeParts 2)
      (nth timeParts 1)))

  (def toZone
    (if is12H
      (if (> (count timeParts) 3)
        (nth timeParts 3)
        (.getID (java.util.TimeZone/getDefault)))
      (if (> (count timeParts) 2)
        (nth timeParts 2)
        (.getID (java.util.TimeZone/getDefault)))))

  ;; No optional time formats..
  (if is12H
    (if (.contains (first timeParts) ":")
      (def dateFormat dateFormat12HHMM)
      (def dateFormat dateFormat12HH))
    (if (.contains (first timeParts) ":")
      (def dateFormat dateFormat24HHMM)
      (def dateFormat dateFormat24HH)))

  (.setTimeZone dateFormat (java.util.TimeZone/getTimeZone fromZone))

  (try
    (def from (.parse dateFormat (str fromDate " " fromTime)))
    (catch Exception e (exit 1 "Parse error.")))

  (print (str (.format dateFormat from) " --> " ))

  (def newDate (.parse dateFormat (str fromDate " " fromTime)))

  (.setTimeZone dateFormat (java.util.TimeZone/getTimeZone toZone))
  (.format dateFormat newDate))

;; 'z' may be GMT+/-HH:MM, or PST/CEST/CET/PDT/GMT/UTC etc..
(defn -main
  "Convert any date/time to current time zone. Input: yyMMdd? hh(:mm)? a? z"
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]

    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      (:stdin options) (exit 1 "wtf")
      (not= (count arguments) 1) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))

    (println (convertDate (first arguments)))))
