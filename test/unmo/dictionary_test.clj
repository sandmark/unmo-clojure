(ns unmo.dictionary-test
  (:require [clojure.test :as t]
            [fudje.core :as fj]
            [unmo.dictionary :as dict]
            [fudje.sweet :as fs]))

(def sentences [["あたしはプログラムの女の子です"
                 [["あたし" false] ["は" false] ["プログラム" true] ["の" false] ["女の子" true] ["です" false]]]

                ["あたしが好きなのはおしゃべりと月餅です"
                 [["あたし" false] ["が" false] ["好き" false] ["な" false] ["の" false] ["は" false]
                  ["おしゃべり" true] ["と" false] ["月餅" true] ["です" false]]]

                ["あたしです"
                 [["あたし" false] ["です" false]]]])

(def nouns ["プログラム" "女の子" "おしゃべり" "月餅"])

;; --------------------------------------------------
;; Random Dictionary
;;
(t/deftest study-random-test
  (let [text "test"]
    (t/testing "New word"
      (t/is
       (compatible
        (#'dict/study-random {} text)
        (fs/contains {:random (fs/just #{text})}))))

    (t/testing "A word already added"
      (t/is
       (compatible
        (#'dict/study-random {:random #{text}} text)
        (fs/contains {:random (fs/just #{text})}))))))

;; --------------------------------------------------
;; Pattern Dictionary
;;
(t/deftest study-pattern-test
  (let [[[sentence1 parts1]
         [sentence2 parts2]
         [sentence3 parts3]] sentences
        noun                 (first nouns)]
    (letfn [(study [m]
              (reduce-kv #'dict/study-pattern
                         m
                         {sentence1 parts1, sentence2 parts2}))]
      (t/testing "Data structure"
        (t/is
         (compatible
          (#'dict/study-pattern {} sentence1 parts1)
          (fs/contains-in {:pattern {noun (fs/checker set?)}}))))

      (t/testing "Nouns"
        (t/is
         (compatible
          (-> {} study :pattern keys)
          (fs/just nouns))))

      (t/testing "Sentences"
        (t/is
         (compatible
          (->> {} study :pattern vals set)
          (fs/just #{#{sentence1} #{sentence2}}))))

      (t/testing "Distinction"
        (t/is
         (compatible
          (-> {} study study study)
          (fs/contains-in {:pattern {(last nouns) (fs/checker #(= 1 (count %)))}}))))

      (t/testing "No nouns"
        (t/is
         (compatible
          (#'dict/study-pattern {} sentence3 parts3)
          (fs/contains {:pattern (fs/checker empty?)})))))))

;; --------------------------------------------------
;; Template Dictionary
;;
(t/deftest study-template-test
  (let [[[_ parts1] [_ parts2] [_ no-nouns]] sentences
        template1                            (str "あたしは" dict/template-nounmark
                                                  "の" dict/template-nounmark "です")
        template2                            (str "あたしが好きなのは" dict/template-nounmark
                                                  "と" dict/template-nounmark "です")]
    (t/testing "Data structure"
      (t/is
       (compatible
        (#'dict/study-template {} parts1)
        (fs/contains-in {:template {2
                                    (fs/checker set? (complement empty?))}}))))

    (t/testing "Study"
      (t/is
       (compatible
        (-> {}
            (#'dict/study-template parts1)
            (#'dict/study-template parts2))
        (fs/contains-in
         {:template {2 (fs/just #{template1 template2})}}))))

    (t/testing "No nouns"
      (t/is
       (compatible
        (#'dict/study-template {:template {}} no-nouns)
        (fs/contains {:template (fs/checker empty?)}))))))

;; --------------------------------------------------
;; Markov Dictionary
;;
(t/deftest study-markov-test
  (let [[[_ parts1] [_ parts2] [_ parts3]] sentences]
    (letfn [(study-all [m]
              (reduce-kv #(#'dict/study-markov %1 %3) m [parts1 parts2 parts3]))]
      (t/testing "Data structure with 3 times nested"
        (t/is (= 3 dict/markov-depth))
        (t/is
         (compatible
          (study-all {})
          (fs/contains-in {:markov {:starts     {"あたし" 2}
                                    :dictionary {"女の子" {"です" #{dict/markov-endmark}}
                                                 "月餅"   {"です" #{dict/markov-endmark}}
                                                 "あたし" {"は" ["プログラム"]
                                                           "が" ["好き"]}}}}))))

      (t/testing "Data structure with 5 times nested"
        (fj/mocking [dict/markov-depth => 5]
          (t/is (= 5 dict/markov-depth))
          (t/is
           (compatible
            (study-all {})
            (fs/contains-in
             {:markov
              {:dictionary
               {"あたし"     {"は" {"プログラム" {"の" #{"女の子"}}}}
                "は"         {"プログラム" {"の" {"女の子" #{"です"}}}}
                "プログラム" {"の" {"女の子" {"です" #{dict/markov-endmark}}}}}}})))))

      (t/testing "Addition"
        (let [m (#'dict/study-markov {} parts1)]
          (t/is
           (compatible
            (#'dict/study-markov m parts2)
            (fs/contains-in
             {:markov
              {:dictionary
               (fs/checker #(> (count %)
                               (count (get-in m [:markov :dictionary]))))}})))))

      (t/testing "Min words"
        (t/is (< (count parts3) dict/markov-depth))
        (t/is (compatible (#'dict/study-markov {:markov {}} parts3)
                          (fs/contains {:markov (fs/checker empty?)})))))))
