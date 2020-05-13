(ns unmo.responder
  (:require [clojure.string :as str]
            [unmo.dictionary :as dict]
            [unmo.morph :as morph]))

(def markov-word-max 30)
(def what-suffix "？")
(def pattern-matcher #"%match%")
(def template-matcher (re-pattern dict/template-nounmark))

(defn response-what
  "Returns a string with a question mark appended to the end of
  the :input value of the given map."
  [{:keys [input]}]
  (str input what-suffix))

(defn response-random
  "Returns a random value from the set, :dictionary -> :random, of the given map.
  When the set is empty, returns nil."
  [{{random :random} :dictionary}]
  (-> random seq rand-nth))

(defn response-pattern
  "Searches the dictionary (:dictionary :pattern) for a pattern that
  matches the given input, and returns a random response corresponding to the pattern,
  replacing %match% with the matched string."
  [{input :input {pattern :pattern} :dictionary}]
  (letfn [(match [[r phrases]]
            (when-let [matched (-> r re-pattern (re-find input) first)]
              [matched phrases]))]
    (when-let [[matched phrases] (first (keep match pattern))]
      (let [phrase (-> phrases seq rand-nth)]
        (str/replace phrase pattern-matcher matched)))))

(defn response-template
  "Returns a string in which %noun% is replaced with a noun contained in parts
  of the template with the same number of nouns contained in the given parts.
  When no template is matched, returns nil.

  For example, if the parts is built from 'あたしはプログラムの女の子です',
  the nouns included are 'プログラム' and '女の子', two nouns. And if
  the template dictionary is {2 #{'%noun%はいい%noun%'}},
  the result is 'プログラムはいい女の子'."
  [{parts :parts {dictionary :template} :dictionary}]
  (let [nouns (->> parts (filter morph/noun?) (map first))]
    (when-let [template (-> dictionary
                            (get (count nouns))
                            seq
                            rand-nth)]
      (reduce #(str/replace-first %1 template-matcher %2) template nouns))))

(defn response-markov
  "Returns a sentence generated based on a Markov chain starting from
  the first word of the input (beginning of the parts). If the first word
  doesn't exist in the dictionary, randomly picks a word from the dictionary's
  :starts map and generates a sentence.

  For example, if parts starts with 'あたし' and the dictionary looks like this:

  {:markov {:starts     {あたし 2}
            :dictionary {あたし     {は #{プログラム}, が #{好き}},
                         は         {プログラム #{の}, おしゃべり #{と}},
                         女の子     {です #{%ENDMARK%}},
                         おしゃべり {と #{月餅}},
                         な         {の #{は}},
                         月餅       {です #{%ENDMARK%}},
                         と         {月餅 #{です}},
                         プログラム {の #{女の子}},
                         好き       {な #{の}},
                         が         {好き #{な}},
                         の         {女の子 #{です}, は #{おしゃべり}}}}}

  sentences that start with 'あたし' and end with 'です' are:

  #{あたしはプログラムの女の子です
     あたしが好きなの女の子です
     あたしが好きなのはおしゃべりと月餅です}

  TODO: Fix hardcode assuming Markov dictionary depth is 3"
  [{parts :parts {{starts :starts dictionary :dictionary} :markov} :dictionary}]
  (letfn [(choose-suffix [word]
            (let [prefix (-> dictionary (get word) keys rand-nth)
                  suffix (-> dictionary (get-in [word prefix]) seq rand-nth)]
              [prefix suffix]))

          (generate [[word :as words] _]
            (let [[prefix suffix] (choose-suffix word)]
              (if (= suffix dict/markov-endmark)
                (reduced (conj words prefix))
                (conj words prefix suffix))))]
    (when (seq dictionary)
      (let [start (if (contains? starts (ffirst parts))
                    (list (ffirst parts))
                    (list (-> starts keys rand-nth)))]
        (->> (range markov-word-max)
             (reduce generate start)
             reverse
             (apply str))))))
