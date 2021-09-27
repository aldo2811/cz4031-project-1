package com.cz4031;

/**
 * Utility class to help with implementation
 */
public class Util {

    /**
     * Find index to insert an element to an array
     * @param arr array to be inserted into
     * @param k element to be inserted
     * @param <T> class of element, must implement Comparable interface
     * @return index to insert
     */
    public static <T extends Comparable<T>> int findIndexToInsert(T[] arr, T k) {
        int lo = 0, hi = arr.length-1, mid;
        while (lo < hi) {
            mid = lo + (hi - lo) / 2;
            int cmp = k.compareTo(arr[mid]);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return k.compareTo(arr[lo]) > 0 ? lo+1 : lo;
    }

    /**
     * Insert an element to a specific index in an array, shift the elements affected by the insertion
     * and delete last element in the array
     * @param arr array to be inserted into
     * @param t element to be inserted
     * @param pos index to insert
     * @param <T> class type of element
     */
    public static <T> void insertAndShift(T[] arr, T t, int pos) {
        for (int i = arr.length - 1; i > pos; --i) {
            arr[i] = arr[i-1];
        }
        arr[pos] = t;
    }

    /**
     * Delete an element on the specified index in an array, then shift the elements accordingly
     * @param arr array to be deleted from
     * @param pos position of element to be deleted
     * @param <T> class type of element
     */
    public static <T> void deleteAndShift(T[] arr, int pos) {
        for (int i = pos; i < arr.length - 1; ++i) {
            arr[i] = arr[i+1];
        }
        arr[arr.length - 1] = null;
    }
}
