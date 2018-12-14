(ns unmo.responder)

(defmulti response :responder)

(defmethod response :what [{:keys [input]}]
  (str input "ってなに？"))

(defmethod response :default [{:keys [responder]}]
  (throw (IllegalArgumentException.
          (str "Responder " responder " が存在しません。"))))
