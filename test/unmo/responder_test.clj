(ns unmo.responder-test
  (:require [unmo.responder :as sut]
            [clojure.test :as t]
            [clojure.string :as str]))

(def analysis
  {"晴れた天気だ"     [["晴れ" false] ["た" false] ["天気" true] ["だ" false]]
   "今日は天気がいい" [["今日" true] ["は" false] ["天気" true] ["が" false] ["いい" false]]
   "今日の天気は快晴" [["今日" true] ["の" false] ["天気" true] ["は" false] ["快晴" true]]

   "あたしはプログラムの女の子です"
   [["あたし" false] ["は" false] ["プログラム" true] ["の" false] ["女の子" true] ["です" false]]

   "あたしが好きなのはおしゃべりと月餅です"
   [["あたし" false] ["が" false] ["好き" false] ["な" false] ["の" false] ["は" false]
    ["おしゃべり" true] ["と" false] ["月餅" true] ["です" false]]})

(def markov-dictionary
  {:markov {:starts     {"あたし" 2}
            :dictionary {"あたし"     {"は" #{"プログラム"}, "が" #{"好き"}},
                         "は"         {"プログラム" #{"の"}, "おしゃべり" #{"と"}},
                         "女の子"     {"です" #{"%ENDMARK%"}},
                         "おしゃべり" {"と" #{"月餅"}},
                         "な"         {"の" #{"は"}},
                         "月餅"       {"です" #{"%ENDMARK%"}},
                         "と"         {"月餅" #{"です"}},
                         "プログラム" {"の" #{"女の子"}},
                         "好き"       {"な" #{"の"}},
                         "が"         {"好き" #{"な"}},
                         "の"         {"女の子" #{"です"}, "は" #{"おしゃべり"}}}}})

(t/deftest response-what-test
  (t/testing "Fixed response"
    (t/is (= "test？"
             (sut/response-what {:input "test"})))))

(t/deftest response-random-test
  (t/testing "Nil when empty"
    (t/is (nil? (sut/response-random {})))
    (t/is (nil? (sut/response-random {:dictionary {:random #{}}}))))

  (t/testing "Any"
    (t/is (= "Hello"
             (sut/response-random {:dictionary {:random #{"Hello"}}})))))

(t/deftest response-pattern-test
  (let [dictionary {:pattern {"チョコ(レート)?" #{"%match%おいしいよね"}
                              "天気"            #{"明日晴れるといいなー" "今日もいい天気"}}}]
    (t/testing "Regex replacement"
      (t/is (= "チョコおいしいよね"
               (sut/response-pattern {:input      "チョコ食べたい"
                                      :dictionary dictionary})))
      (t/is (= "チョコレートおいしいよね"
               (sut/response-pattern {:input      "チョコレート食べたい"
                                      :dictionary dictionary}))))

    (t/testing "Choose randomly when matched"
      (t/is (some #{(sut/response-pattern {:input "今日は天気が悪い" :dictionary dictionary})}
                  #{"明日晴れるといいなー" "今日もいい天気"})))

    (t/testing "Nil when not matched"
      (t/is (nil? (sut/response-pattern {:input "パターン一致なし" :dictionary dictionary}))))

    (t/testing "Nil when empty"
      (t/is (nil? (sut/response-pattern {:input "パターン一致なし" :dictionary nil}))))))

(t/deftest response-template-test
  (let [dictionary {:template {1 #{"あたしは%noun%です" "あなたは%noun%です"}
                               2 #{"%noun%の%noun%は？"}}}]
    (t/testing "Replace %noun% with the given noun"
      (t/is (some #{(sut/response-template {:parts      (analysis "晴れた天気だ")
                                             :dictionary dictionary})}
                  #{"あたしは天気です" "あなたは天気です"}))
      (t/is (some #{(sut/response-template {:parts      (analysis "今日は天気がいい")
                                             :dictionary dictionary})}
                  #{"天気の今日は？" "今日の天気は？"})))

    (t/testing "Nil when not matched"
      (t/is (nil? (sut/response-template {:parts      (analysis "今日の天気は快晴")
                                          :dictionary dictionary}))))

    (t/testing "Nil when empty"
      (t/is (nil? (sut/response-template {:parts [] :dictionary {}}))))))

(t/deftest response-markov-test
  (t/testing "When starts with a word in the dictionary"
    (let [response (sut/response-markov {:parts      (analysis "あたしはプログラムの女の子です")
                                         :dictionary markov-dictionary})]
      (t/is (str/starts-with? response "あたし"))
      (t/is (str/ends-with? response "です"))))

  (t/testing "When starts with a word not in the dictionary"
    (let [response (sut/response-markov {:parts      (analysis "今日は天気がいい")
                                         :dictionary markov-dictionary})]
      (t/is (str/starts-with? response "あたし"))
      (t/is (str/ends-with? response "です"))))

  (t/testing "Basic markov patterns"
    (let [response (sut/response-markov {:parts      (analysis "あたしはプログラムの女の子です")
                                         :dictionary markov-dictionary})]
      (t/is (some #{response}
                  #{"あたしはプログラムの女の子です"
                     "あたしが好きなの女の子です"
                     "あたしが好きなのはおしゃべりと月餅です"}))))

  (t/testing "Nil when the dictionary is empty"
    (t/is (nil? (sut/response-markov {:parts      (analysis "あたしはプログラムの女の子です")
                                      :dictionary {}})))))
