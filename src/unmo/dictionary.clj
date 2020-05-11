(ns unmo.dictionary
  (:require [sudachi-clj.helper :as helper]))

(def template-nounmark "%noun%")
(def markov-endmark "%ENDMARK%")
(def markov-depth 3)

(defn- study-random
  "Returns a new map with the input added to a vector, referred by :random key.
  If the input already exists, just returns dictionary.

  (study-random {:random #{}}
                 こんにちは)              => {:random #{こんにちは}}

  (study-random {:random #{こんにちは}}
                 こんにちは)              => {:random #{こんにちは}}"
  [dictionary input]
  (update dictionary :random (fnil conj #{}) input))

(defn- study-pattern
  "Returns a new map with the input and the parts added to a map,
  referred by :pattern key. The pattern dictionary is formed
  {noun #{sentences-includes-noun...}}

  For instance:
      if the `input` was あたしはプログラムの女の子です and
      if the `parts` was [[プログラム 名詞] [女の子 名詞]] roughly, then

      result will be {:pattern {プログラム #{あたしはプログラムの女の子です}
                                女の子    #{あたしはプログラムの女の子です}}}

  Note that `parts` could have more informations though
  this function handles only nouns, not verbs nor others.
  When the `input` has a noun which is already in the dictionary,
  it'll be added to the value vector, keeping uniqueness."
  [dictionary input parts]
  (let [nouns (->> parts (filter helper/noun?) (mapv first))]  ; this must be vector for `reduce-kv`
    (letfn [(update-noun [m _ v]
              (update m v (fnil conj #{}) input))]
      (update dictionary :pattern #(reduce-kv update-noun % nouns)))))

(defn- study-template
  "Returns a new map with the parts as a template added to a map,
  referred by :template key. The template dictionary is formed
  {nouns-count [templates ...]}. If the parts has no nouns then
  just returns the dictionary.

  The template is a string built from parts, nouns replaced with
  a %noun% mark, i.e, あたしはプログラムです will be あたしは%noun%です.
  In this case the sentence has only one noun so the result will be:

  {:template {1 #{あたしは%noun%です}}}

  Note that the key of the template dictionary is a count of nouns."
  [dictionary parts]
  (letfn [(->noun [[word _ :as part]]
            (if (helper/noun? part) template-nounmark word))]
    (let [nouns-count (->> parts (filter helper/noun?) count)
          template    (->> parts (map ->noun) (apply str))]
      (if (zero? nouns-count)
        dictionary
        (update-in dictionary [:template nouns-count] (fnil conj #{}) template)))))

(defn- parts->slices
  "Returns a vector with enough words to build the Markov dictionary
  based on the depth."
  [parts depth]
  (letfn [(enough? [coll]
            (> (count coll) (- depth 2)))]
    (->> parts
         (map first)
         (partition-all depth 1)
         (filter enough?)
         (into []))))

(defn- study-markov
  "Returns a new map with Markov analysis results of the parts
  merged into the dictionary's :markov key. If the depth and the endmark
  are supplied (each defaults to `markov-depth` and `markov-endmark`),
  endmark is used at the end of the sentence, and the resulting dictionary
  is also nested with the depth value.

  If the given parts is 'あたしはプログラムの女の子です',
  the Markov dictionary will be as follows:

  {:markov {:starts     {あたし 1}
            :dictionary {あたし     {は #{プログラム}}
                         は         {プログラム #{の}}
                         プログラム {の #{女の子}}
                         の         {女の子 #{です}}
                         女の子     {です #{%ENDMARK%}}}}}

  In case of the depth is 3 and the endmark is %ENDMARK%,
  the third nesting shows a vector that stores the 'next words'.
  In the same way, if depth is 4 then the vector appears in the fourth nest,
  and so on.

  The :starts map stores the first word in the sentence and its frequency,
  and the :dictionary map is a nested map that stores a vector of next words
  according to the depth."
  ([dictionary parts]
   (study-markov dictionary parts markov-depth markov-endmark))

  ([dictionary parts depth endmark]
   (letfn [(build-markov [m _ words]
             (let [index (dec depth)
                   ks    (take index words)
                   suf   (nth words index endmark)]
               (update-in m ks (fnil conj #{}) suf)))]
     (if (< (count parts) depth)
       dictionary
       (-> dictionary
           (update-in [:markov :starts (ffirst parts)] (fnil inc 0))
           (update-in [:markov :dictionary]
                      #(reduce-kv build-markov % (parts->slices parts depth))))))))

(defn study
  "文字列inputと形態素解析結果partsを受け取り、辞書dictionaryに保存したものを返す。"
  [dictionary input parts]
  (-> dictionary
      (study-random input)
      (study-pattern input parts)
      (study-template parts)
      (study-markov parts)))
