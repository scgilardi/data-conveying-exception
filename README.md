# Data-Carrying Exception

An exception for carrying data. Based on clojure.contrib.condition.

Differs from c.c.condition in a few ways:

* the exception is a descendant of RuntimeException rather than direct Throwable.
* the exception may be treated as a Clojure map itself.

Also introduces new awesomeness in the form of try+ and throw+. throw+
takes an Exception, a map, or varargs, and constructs a data-conveying
Exception in the latter two cases. try+ can destructure data-conveying
exceptions in catches.

## Usage

    (defn asplode [problem type]
      (dce.Exception/throw+ :message (str "Oh no! " problem) :failure true))
      
    (try+
      (when-not (success?)
        (asplode "failed!"))
      (catch :failure e
        (log/warn e "stuff failed, dude: " (:message e)))
      (catch :catastrophic-failure {:keys [exit-code]}
        (System/exit exit-code))
      (catch java.io.IOException e
        (log/info "whatever; who cares.")))

# Clojure Exception (Another Option)

Provides try+ and throw+ that are a strict superset of the capabilities of try
and catch. Throwing instances of java.lang.Throwable and using catch clauses
that specify Exception classes by name works as it always has. try+ catch
clauses are fully interoperable with java-generated throws.

These are the enhanced capabilities:

* throw+ can throw any java object, not just those with classes derived from
  java.lang.Throwable
** clojure records become an easy way to throw custom exceptions without
using gen-class
** generic maps with :type metadata also work
* catch clauses within try+ can catch any java object specified by
** a class name (e.g., Integer, IllegalArgumentException)
** a keyword representing a clojure type (as used by isa?, derive, etc.)
** an arbitrary predicate: the first clause whose predicate matches is executed
* the binding to the caught exception in a catch clause is subject to destructuring
rather than being required to ba simple symbol
* in a catch clause, the context at the throw site: stack trace and
  &env containing bound locals and their values are accessible via the
  hidden argument: &throw-context

## License

Copyright (C) 2011 Kevin Downey, Stephen Gilardi, and Phil Hagelberg

Distributed under the Eclipse Public License, the same as Clojure.
