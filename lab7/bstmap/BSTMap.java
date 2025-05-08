package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    @Override
    public Iterator<K> iterator() {
        return null;
    }

    private class Node {
        private K key;
        private V val;
        private Node left, right;
        private int size;

        public Node(K key, V val, int size) {
            this.key = key;
            this.val = val;
            this.left = null;
            this.right = null;
            this.size = size;
        }
    }

    private Node root;
    private Set<K> keySet;

    public BSTMap() {
        root = null;
        keySet = new HashSet<>();
    }

    public int size() {
        return size(root);
    }

    private int size(Node node) {
        if (node == null) {
            return 0;
        }
        return 1 + size(node.left) + size(node.right);
    }

    public void clear() {
        root = null;
    }

    public boolean containsKey(K key) {
        return containsKey(root, key);
    }

    private boolean containsKey(Node node, K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        if (node == null) {
            return false;
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return containsKey(node.left, key);
        } else if (cmp > 0) {
            return containsKey(node.right, key);
        } else {
            return true;
        }
    }

    public V get(K key) {
        return get(root, key);
    }

    private V get(Node node, K key) {
        if (key == null) {
            throw new IllegalArgumentException("calls get() with a null key");
        }
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        } else {
            return node.val;
        }
    }

    public void put(K key, V val) {
        if (root == null) {
            root = new Node(key, val, 1);
        } else {
            put(root, key, val);
        }
    }

    private Node put(Node node, K key, V val) {
        if (key == null) {
            throw new IllegalArgumentException("calls put() with a null key");
        }
        if (node == null) {
            return new Node(key, val, 1);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = put(node.left, key, val);
        } else if (cmp > 0) {
            node.right = put(node.right, key, val);
        } else {
            node.val = val;
        }
        return node;
    }

    public Set<K> keySet() {
        keySet(root);
        return keySet;
    }

    private void keySet(Node node) {
        if (node == null) {
            return;
        }
        this.keySet.add(node.key);
        keySet(node.left);
        keySet(node.right);
    }

    public V remove(K key) {
        V val = get(key);
        root = remove(root, key);
        return val;
    }

    private Node remove(Node node, K key) {
        if (key == null) {
            throw new IllegalArgumentException("calls remove() with a null key");
        }

        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = remove(node.left, key);
        } else if (cmp > 0) {
            node.right = remove(node.right, key);
        } else {
            if (node.left == null && node.right == null) {
                return null;
            } else if (node.left == null) {
                return node.right;
            } else if (node.right == null) {
                return node.left;
            } else {
                Node left = node.left;
                Node rightestOfLeft = left;
                while (rightestOfLeft.right != null) {
                    rightestOfLeft = rightestOfLeft.right;
                }
                rightestOfLeft.right = node.right;
                return left;
            }
        }
        return node;
    }

    public V remove(K key, V val) {
        throw new UnsupportedOperationException();
    }

}
