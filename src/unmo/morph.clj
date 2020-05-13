(ns unmo.morph
  (:require [clojure.spec.alpha :as s]
            [integrant.core :as ig])
  (:import com.atilika.kuromoji.ipadic.Tokenizer))

(s/def ::instance (s/nilable (partial instance? Tokenizer)))
(s/def ::tokenizer fn?)

(defmethod ig/pre-init-spec :morph/instance [_]
  (s/spec ::instance))

(defmethod ig/pre-init-spec :morph/tokenizer [_]
  (s/keys :req-un [::instance]))

(s/def ::part (s/tuple string? boolean?))
(s/def ::parts (s/coll-of ::part))
(s/def ::surface string?)
(s/def ::part-lv1 string?)
(s/def ::part-lv2 string?)

(defn noun?
  [[_ noun :as part]]
  {:pre  [(s/valid? ::part part)]
   :post [(s/valid? boolean? %)]}
  noun)

(defn- ->surf-noun
  [[surf lv1 lv2 :as tuple]]
  {:pre  [(s/valid? (s/tuple ::surface ::part-lv1 ::part-lv2) tuple)]
   :post [(s/valid? ::part %)]}
  (let [noun? (boolean (and (some #{lv1} #{"名詞"})
                            (some #{lv2} #{"一般" "固有名詞" "サ変接続" "副詞可能" "形容動詞語幹"})))]
    [surf noun?]))

(def ^:private system (atom nil))

(def ^:private config
  {:morph/instance  nil
   :morph/tokenizer {:instance (ig/ref :morph/instance)}})

(defmethod ig/init-key :morph/instance [_ _]
  (Tokenizer.))

(defmethod ig/init-key :morph/tokenizer
  [_ {:keys [instance]}]
  (fn [s]
    {:pre  [(s/valid? string? s)]
     :post [(s/valid? ::parts %)]}
    (->> (.tokenize instance s)
         (map #(vector (.getSurface %)
                       (.getPartOfSpeechLevel1 %)
                       (.getPartOfSpeechLevel2 %)))
         (mapv ->surf-noun))))

(defn start []
  (reset! system (-> config ig/init))
  :started)

(defn stop []
  (when @system
    (ig/halt! @system))
  (reset! system nil)
  :stopped)

(defn restart []
  (stop)
  (start)
  :restarted)

(defn analyze
  [s]
  {:pre  [(s/valid? string? s)]
   :post [(s/valid? (s/nilable ::parts) %)]}
  (when-let [f (:morph/tokenizer @system)]
    (f s)))
