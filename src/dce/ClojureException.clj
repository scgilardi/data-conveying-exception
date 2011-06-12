(ns dce.ClojureException
  (:gen-class :extends RuntimeException
              :state state
              :init init
              :constructors {[Object Object]
                             []}))

(defn -init
  [obj env]
  [[] {:obj obj :env env}])

(defn -toString [self]
  (str (.getCanonicalName (class self)) ": " (-> self .state :obj)))

(defmacro throw+ [obj]
  `(throw (dce.ClojureException. ~obj (zipmap '~(keys &env) [~@(keys &env)]))))

(defn- catch-form? [x]
  (and (seq? x) (= 'catch (first x))))

(defn- type-name? [x]
  (or (keyword? x)
      (and (symbol? x) (class? (resolve x)))))

(defmacro try+
  [& body]
  (let [catch-clauses (filter catch-form? body)
        try-body (remove catch-form? body)
        throwable (gensym)
        thrown (gensym)]
    `(try
       ~@try-body
       (catch Throwable ~throwable
         (let [~thrown (if (instance? dce.ClojureException ~throwable)
                         (-> ~throwable .state :obj)
                         ~throwable)
               ~'&thrown-context
               (when (instance? dce.ClojureException ~throwable)
                 (hash-map
                  :env (-> ~throwable .state :env)
                  :stack (into-array (drop 3 (.getStackTrace ~throwable)))))]
           (cond
            ~@(mapcat
               (fn [[_ type-or-pred local-name & catch-body]]
                 [(if (type-name? type-or-pred)
                    `(isa? (type ~thrown) ~type-or-pred)
                    `(~type-or-pred ~thrown))
                  `(let [~local-name ~thrown]
                     ~@catch-body)])
               catch-clauses)
            :else
            (throw ~throwable)))))))
