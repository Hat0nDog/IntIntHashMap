import java.util.Arrays;
import java.util.Objects;

//甚至懒得放package
public final class IntIntHashMap {

    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_NULL_VALUE = 0;

    static class Node {
        public final int hash;
        public final int key;
        public int value;
        Node next;

        Node(int hash, int key, int value, Node next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Node) {
                return key == ((Node) o).key && value == ((Node) o).value;
            }
            return false;
        }
    }

    public static int intHash(int key) {
        return key ^ (key >>> 16);
    }

    public int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    private final int nullValue;

    private Node[] table;

    private int[] keys;

    private int size;

    private final float loadFactor;

    public IntIntHashMap() {
        this(DEFAULT_NULL_VALUE, DEFAULT_INITIAL_CAPACITY,  DEFAULT_LOAD_FACTOR);
    }

    public IntIntHashMap(int nullValue) {
        this(nullValue, DEFAULT_INITIAL_CAPACITY,  DEFAULT_LOAD_FACTOR);
    }

    public IntIntHashMap(int nullValue, int initialCapacity, float loadFactor) {
        this.nullValue = nullValue;
        this.loadFactor = loadFactor;
        this.table = new Node[tableSizeFor(initialCapacity)];
        this.keys = new int[table.length];
    }

    public int size() {
        return size;
    }
    public final boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(int key) {
        return getNode(intHash(key), key) != null;
    }

    public int get(int key) {
        Node e;
        return (e = getNode(intHash(key), key)) == null ? nullValue : e.value;
    }

    public void put(int key, int value) {
        putVal(intHash(key), key, value);
        if(size >= table.length * loadFactor) {
            resize();
        }
    }

    private Node getNode(int hash, int key) {
        int idx = (table.length - 1) & hash;
        if(table[idx] == null) {
            return null;
        }
        if(table[idx].next == null) {
            return table[idx];
        }
        Node node = table[idx];
        while(node != null) {
            if(node.key == key) {
                return node;
            }
            node = node.next;
        }
        return null;
    }

    private void putVal(int hash, int key, int value) {
        int idx = (table.length - 1) & hash;
        if(table[idx] == null) {
            table[idx] = new Node(hash, key, value, null);
            keys[size++] = key;
        } else {
            Node node = table[idx];
            while(node != null && node.key != key) {
                node = node.next;
            }
            if(node == null) {
                table[idx] = new Node(hash, key, value, table[idx]);
                keys[size++] = key;
            } else {
                node.value = value;
            }
        }
    }

    private void resize() {
        Node[] newTab = new Node[table.length << 1];
        for (Node node : table) {
            if (node == null) {
                continue;
            }
            while (node != null) {
                Node nextNode = node.next;
                int idx = (newTab.length - 1) & node.hash;
                if (newTab[idx] == null) {
                    newTab[idx] = node;
                    newTab[idx].next = null;
                } else {
                    node.next = newTab[idx];
                    newTab[idx] = node;
                }
                node = nextNode;
            }
        }
        table = newTab;
        keys = Arrays.copyOf(keys, table.length);
    }

    private int[] keys() {
        return keys;
    }

}
