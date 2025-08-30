# Hamcrest 4.0 Release Notes

## What's New
* New `org.hamcrest.io.PathMatchers` class for matching `java.nio.files.Path` objects
* Create custom matchers more succinctly using `matcher()` static factory meothods on `FeatureMatcher` and `TypeSafeDiagnosingMatcher`
* `org.hamcrest.beans` property matchers now support Java record naming conventions in addition to bean naming conventions
* `IsIterableContainingInAnyOrder.containsInAnyOrder()` matcher now supports custom `Comparator`s
* New `IsIterableContainingParallelRuns.containsParallelRunsOf()` matcher is a more general implementation of `containsInRelativeOrder()`
* New `IsUnmodifiableCollection.isUnmodifiable` matcher for testing if a collection is unmodifiable.

 * Various bug fixes