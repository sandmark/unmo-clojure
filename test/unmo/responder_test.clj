(ns unmo.responder-test
  (:require [unmo.responder :as sut]
            [clojure.test :as t]
            [clojure.string :as str]))

(def analysis
  {"晴れた天気だ"     [["晴れ" "動詞,一般,*,*,下一段-ラ行,連用形-一般"]
                       ["た" "助動詞,*,*,*,助動詞-タ,連体形-一般"]
                       ["天気" "名詞,普通名詞,一般,*,*,*"]
                       ["だ" "助動詞,*,*,*,助動詞-ダ,終止形-一般"]]
   "今日は天気がいい" [["今日" "名詞,普通名詞,副詞可能,*,*,*"]
                       ["は" "助詞,係助詞,*,*,*,*"]
                       ["天気" "名詞,普通名詞,一般,*,*,*"]
                       ["が" "助詞,格助詞,*,*,*,*"]
                       ["いい" "形容詞,非自立可能,*,*,形容詞,終止形-一般"]]
   "今日の天気は快晴" [["今日" "名詞,普通名詞,副詞可能,*,*,*"]
                       ["の" "助詞,格助詞,*,*,*,*"]
                       ["天気" "名詞,普通名詞,一般,*,*,*"]
                       ["は" "助詞,係助詞,*,*,*,*"]
                       ["快晴" "名詞,普通名詞,一般,*,*,*"]]

   "あたしはプログラムの女の子です"
   [["あたし" "代名詞,*,*,*,*,*"]
    ["は" "助詞,係助詞,*,*,*,*"]
    ["プログラム" "名詞,普通名詞,サ変可能,*,*,*"]
    ["の" "助詞,格助詞,*,*,*,*"]
    ["女の子" "名詞,普通名詞,一般,*,*,*"]
    ["です" "助動詞,*,*,*,助動詞-デス,終止形-一般"]]

   "あたしが好きなのはおしゃべりと月餅です"
   [["あたし" "代名詞,*,*,*,*,*"]
    ["が" "助詞,格助詞,*,*,*,*"]
    ["好き" "形状詞,一般,*,*,*,*"]
    ["な" "助動詞,*,*,*,助動詞-ダ,連体形-一般"]
    ["の" "助詞,準体助詞,*,*,*,*"]
    ["は" "助詞,係助詞,*,*,*,*"]
    ["おしゃべり" "名詞,普通名詞,サ変可能,*,*,*"]
    ["と" "助詞,格助詞,*,*,*,*"]
    ["月餅" "名詞,普通名詞,一般,*,*,*"]
    ["です" "助動詞,*,*,*,助動詞-デス,終止形-一般"]]})

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
