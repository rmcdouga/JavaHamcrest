package org.hamcrest.collection;

import static org.hamcrest.Matchers.*;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Matches if collection is truly unmodifiable
 */
public class IsUnmodifiableCollection {


    /**
     * Creates matcher that matches when collection is truly unmodifiable.
     * 
     * Under some circumstances the matcher will attempt to modify the collection to verify that it is unmodifiable.
     * In that case, the test will fail however subsequent tests that happen after the failure will be 
     * operating on the modified collection.  This may be important if multiple tests are executed using
     * JUnit's assertAll() or similar functionality.
     * 
     * @param <E> the type of elements in the collection
     * @return The matcher
     */
    public static <E> Matcher<Collection<? extends E>> isUnmodifiable() {
        return anyOf(isUnmodifiableJdkCollection(), allOf(not(isModifiableJdkCollection()), isUnmodifiableCustomCollection()));
    }

    public static <E> Matcher<Collection<? extends E>> isUnmodifiableJdkCollection() {
        return new IsUnmodifiableJdkCollection<>();
    }

    public static <E> Matcher<Collection<? extends E>> isModifiableJdkCollection() {
        return new IsModifiableJdkCollection<>();
    }

    public static <E> Matcher<Collection<? extends E>> isUnmodifiableCustomCollection() {
        return new IsUnmodifiableCustomCollection<>();
    }

    private static class IsUnmodifiableJdkCollection<E> extends TypeSafeDiagnosingMatcher<Collection<? extends E>> {
        private static final Set<String> KNOWN_UNMODIFIABLE_COLLECTIONS = Set.of("java.util.ImmutableCollections", "java.util.Collections$Unmodifiable", "java.util.Collections$Empty");
  
        @Override
        public void describeTo(Description description) {
            description.appendText("Expected to be unmodifiable JDK collection, but ");
        }

        @Override
        protected boolean matchesSafely(Collection<? extends E> collection, Description mismatchDescription) {
            @SuppressWarnings("rawtypes")
            final Class<? extends Collection> collectionClass = collection.getClass();
            String collectionClassName = collectionClass.getName();
            for (String knownUnmodifiableCollection : KNOWN_UNMODIFIABLE_COLLECTIONS) {
                if (collectionClassName.startsWith(knownUnmodifiableCollection)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class IsModifiableJdkCollection<E> extends TypeSafeDiagnosingMatcher<Collection<? extends E>> {
        private static final Set<String> KNOWN_MODIFIABLE_COLLECTIONS = Set.of("java.util.Arrays$ArrayList");
        @Override
        public void describeTo(Description description) {
            description.appendText("Expected to be unmodifiable JDK collection, but ");
        }
       
        @Override
        protected boolean matchesSafely(Collection<? extends E> collection, Description mismatchDescription) {
            @SuppressWarnings("rawtypes")
            final Class<? extends Collection> collectionClass = collection.getClass();
            String collectionClassName = collectionClass.getName();
            for (String knownModifiableCollection : KNOWN_MODIFIABLE_COLLECTIONS) {
                if (collectionClassName.startsWith(knownModifiableCollection)) {
                    mismatchDescription.appendText(collectionClassName + " is a known modifiable collection");
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Exercises a collection, tests all methods to see if they are modifiable. 
     */
    private interface MethodChecker {
        /**
         * Tries to modify a collection by calling all the methods.
         * 
         * @return true if collection is unable to be modified.
         */
        boolean checkMethods();
    }
    
    
    /**
     * 
     */
    private static class CollectionMethodChecker implements MethodChecker {
        protected static final Object TEST_OBJECT = new Object();
        protected static final Set<Object> TEST_COLLECTION = Collections.singleton(TEST_OBJECT);

        protected final Collection item;
        protected final Description mismatchDescription;
        
        public CollectionMethodChecker(Collection item, Description mismatchDescription) {
            this.item = item;
            this.mismatchDescription = mismatchDescription;
        }

        @Override
        public boolean checkMethods() {
            if (checkMethod_add(item, TEST_OBJECT, mismatchDescription)) return false;
            if (checkMethod_add_all(item, TEST_COLLECTION, mismatchDescription)) return false;
            if (checkMethod_remove(item, TEST_OBJECT, mismatchDescription)) return false;
            if (checkMethod_remove_all(item, TEST_COLLECTION, mismatchDescription)) return false;
            if (checkMethod_retail_all(item, TEST_COLLECTION, mismatchDescription)) return false;
            if (checkMethod_clear(item, mismatchDescription)) return false;
            if (checkMethod_iterator(item, mismatchDescription)) return false;
            
            return true;
        }
        
        private boolean checkMethod_add(Collection item, Object testObject, Description mismatchDescription) {
            try {
                item.add(testObject);
                mismatchDescription.appendText("was able to add a value into the collection");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_add_all(Collection item, Set<Object> singletonList, Description mismatchDescription) {
            try {
                item.addAll(singletonList);
                mismatchDescription.appendText("was able to perform addAll on the collection");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_iterator(Collection item, Description mismatchDescription) {
            try {
                Iterator iterator = item.iterator();
                iterator.remove();
                mismatchDescription.appendText("was able to remove an element from the iterator");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_clear(Collection item, Description mismatchDescription) {
            try {
                item.clear();
                mismatchDescription.appendText("was able to clear the collection");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_retail_all(Collection item, Set<Object> singletonList, Description mismatchDescription) {
            try {
                item.retainAll(singletonList);
                mismatchDescription.appendText("was able to call retainAll on the collection");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_remove_all(Collection item, Set<Object> singletonList, Description mismatchDescription) {
            try {
                item.removeAll(singletonList);
                mismatchDescription.appendText("was able to call removeAll on the collection");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_remove(Collection item, Object testObject, Description mismatchDescription) {
            try {
                item.remove(testObject);
                mismatchDescription.appendText("was able to call remove a value from the collection");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    }

    private static class ListMethodChecker extends CollectionMethodChecker {
        protected final List list;

        public ListMethodChecker(Collection item, Description mismatchDescription) {
            super(item, mismatchDescription);
            if (item instanceof List originalList) {
                this.list = originalList;
            } else {
                throw new IllegalArgumentException("collection provided is not a list.");
            }
      }

        @Override
        public boolean checkMethods() {
            // This is an operation on the original collection, but it is safe, since it sets the same element
            if (checkMethod_set(list, mismatchDescription)) return false;

            if (checkMethod_listIterator_remove(list, mismatchDescription)) return false;
            if (checkMethod_listIterator_set(list, TEST_OBJECT, mismatchDescription)) return false;
            if (checkMethod_listIterator_add(list, TEST_OBJECT, mismatchDescription)) return false;
            if (checkMethod_listIterator_index(list, mismatchDescription)) return false;
            if (checkMethod_add_index(list, TEST_OBJECT, mismatchDescription)) return false;
            if (checkMethod_add_all_index(list, TEST_COLLECTION, mismatchDescription)) return false;
            if (checkMethod_remove_index(list, mismatchDescription)) return false;

            return super.checkMethods();
        }
        
        private boolean checkMethod_remove_index(List item, Description mismatchDescription) {
            try {
                item.remove(0);
                mismatchDescription.appendText("was able to call remove by index from the collection");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_add_all_index(List item, Set<Object> singletonList, Description mismatchDescription) {
            try {
                item.addAll(0, singletonList);
                mismatchDescription.appendText("was able to perform addAll by index on the collection");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_add_index(List item, Object testObject, Description mismatchDescription) {
            try {
                item.add(0, testObject);
                mismatchDescription.appendText("was able to add a value into the list by index");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_listIterator_remove(List item, Description mismatchDescription) {
            List list = item;
            try {
                ListIterator iterator = list.listIterator();
                iterator.remove();
                mismatchDescription.appendText("was able to remove an element from the list iterator");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_listIterator_set(List item, Object testObject, Description mismatchDescription) {
            List list = item;
            try {
                ListIterator iterator = list.listIterator();
                iterator.next();
                iterator.set(testObject);
                mismatchDescription.appendText("was able to set element on the list iterator");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_listIterator_add(List item, Object testObject, Description mismatchDescription) {
            List list = item;
            try {
                ListIterator iterator = list.listIterator();
                iterator.next();
                iterator.add(testObject);
                mismatchDescription.appendText("was able to add element on the list iterator");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_listIterator_index(List item, Description mismatchDescription) {
            List list = item;
            try {
                Iterator iterator = list.listIterator(0);
                iterator.remove();
                mismatchDescription.appendText("was able to remove an element from the list iterator with index");
                return true;
            } catch (Exception ignore) {
            }
            return false;
        }
    
        private boolean checkMethod_set(List list, Description mismatchDescription) {
            if (list.size() > 0) {
                try {
                    list.set(0, list.get(0));
                    mismatchDescription.appendText("was able to set an element of the collection");
                    return true;
                } catch (Exception ignore) {
                }
            }
            return false;
        }
    }
    
    private static class IsUnmodifiableCustomCollection<E> extends TypeSafeDiagnosingMatcher<Collection<? extends E>> {
        private static final Map<Class<? extends Collection>, Object> DEFAULT_COLLECTIONS = new HashMap<>();
        static {
            final List<String> list = Arrays.asList("a", "b", "c");
            DEFAULT_COLLECTIONS.put(Collection.class, list);
            DEFAULT_COLLECTIONS.put(List.class, list);
            DEFAULT_COLLECTIONS.put(Set.class, new HashSet<>(list));
        }


        @Override
        protected boolean matchesSafely(final Collection collection, final Description mismatchDescription) {
            final Class<? extends Collection> collectionClass = collection.getClass();
            final Collection item = getInstanceOfType(collectionClass, collection);
            if (item == null) {
                throw failedToInstantiateItem(collectionClass, null);
            }

            MethodChecker methodChecker = collection instanceof List listCollection ? new ListMethodChecker(item, mismatchDescription) : new CollectionMethodChecker(item, mismatchDescription);
           
            return methodChecker.checkMethods();
        }
    
    
    
        @SuppressWarnings("unchecked")
        private <T> T getInstanceOfType(final Class clazz, Collection collection) {
            if (clazz.isArray()) {
                return (T) Array.newInstance(clazz, 0);
            }
    
            if (clazz.isPrimitive()) {
                if (Byte.TYPE.isAssignableFrom(clazz)) {
                    return (T) Byte.valueOf((byte) 1);
                }
                if (Short.TYPE.isAssignableFrom(clazz)) {
                    return (T) Short.valueOf((short) 1);
                }
                if (Integer.TYPE.isAssignableFrom(clazz)) {
                    return (T) Integer.valueOf(1);
                }
                if (Long.TYPE.isAssignableFrom(clazz)) {
                    return (T) Long.valueOf(1L);
                }
                if (Float.TYPE.isAssignableFrom(clazz)) {
                    return (T) Float.valueOf(1L);
                }
                if (Double.TYPE.isAssignableFrom(clazz)) {
                    return (T) Double.valueOf(1L);
                }
                if (Boolean.TYPE.isAssignableFrom(clazz)) {
                    return (T) Boolean.valueOf(true);
                }
                if (Character.TYPE.isAssignableFrom(clazz)) {
                    return (T) Character.valueOf(' ');
                }
            }
    
            if (clazz.isInterface()) {
                Object defaultCollection = DEFAULT_COLLECTIONS.get(clazz);
                if (defaultCollection != null) {
                    return (T) defaultCollection;
                }
                return null;
            }
    
            // For the most part of implementations there probably won't be any default constructor
            final Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
    
            Constructor<?> constructorForCollection = findConstructorForCollection(declaredConstructors);
    
            Exception lastException = null;
            if (constructorForCollection != null) {
                try {
                    return (T) constructorForCollection.newInstance(collection);
                } catch (Exception e) {
                    lastException = e;
                }
            }
    
            // First take constructor with fewer number of arguments
            Arrays.sort(declaredConstructors, new Comparator<Constructor<?>>() {
                @Override
                public int compare(Constructor<?> o1, Constructor<?> o2) {
                    return Integer.compare(o2.getParameterTypes().length, o1.getParameterTypes().length);
                }
            });
    
            for (Constructor<?> declaredConstructor : declaredConstructors) {
                try {
                    declaredConstructor.setAccessible(true);
                } catch (Exception ignore) {
                    // Since Java 17 it is impossible to make jdk* classes accessible without manipulation with modules:
                    // module java.base does not "opens java.util" to unnamed module
                }
                final int parametersNumber = declaredConstructor.getParameterTypes().length;
    
                Object[] arguments = new Object[parametersNumber];
                for (int argumentIndex = 0; argumentIndex < arguments.length; argumentIndex++) {
                    arguments[argumentIndex] = getInstanceOfType(declaredConstructor.getParameterTypes()[argumentIndex], collection);
                }
                try {
                    return (T) declaredConstructor.newInstance(arguments);
                } catch (Exception e) {
                    lastException = e;
                }
    
            }
            throw failedToInstantiateItem(clazz, lastException);
        }
    
        private Constructor<?> findConstructorForCollection(Constructor<?>[] declaredConstructors) {
            for (Constructor<?> constructor : declaredConstructors) {
                if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].isAssignableFrom(Collection.class)) {
                    return constructor;
                }
            }
            return null;
        }
    
        private <T> IllegalStateException failedToInstantiateItem(Class<T> clazz, Exception e) {
            return new IllegalStateException("Failed to create an instance of <" + clazz + "> class.", e);
        }
    
        @Override
        public void describeTo(Description description) {
            description.appendText("Expected to be unmodifiable collection, but ");
        }
    }
}
