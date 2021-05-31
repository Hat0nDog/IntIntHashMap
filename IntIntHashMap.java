
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
/**
1.支持链表转红黑树
2.支持遍历
3.支持单个key读，增改
4.不支持删key
5.刷题够了
**/
public final class IntIntHashMap {
    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    private static final float LOAD_FACTOR = 5f;
    private static final int TREEIFY_THRESHOLD = 4;
    public static final int NULL_VALUE = Integer.MIN_VALUE;
    private MapNode[] table;
    private int[] keys;
    private int size;
    public IntIntHashMap() {
        this.table = new MapNode[DEFAULT_INITIAL_CAPACITY];
        this.keys = new int[(int) (table.length * LOAD_FACTOR)];
    }
    public static int intHash(int key) {
        return key ^ (key >>> 16);
    }
    public int size() {
        return size;
    }
    public int get(int key) {
        return Optional.ofNullable(getNode(intHash(key))).map(it -> it.val(key)).orElse(NULL_VALUE);
    }
    public void put(int key, int value) {
        putVal(intHash(key), key, value);
        if (size >= table.length * LOAD_FACTOR) {
            resize();
        }
    }
    private MapNode getNode(int hash) {
        int idx = (table.length - 1) & hash;
        return table[idx];
    }
    private void putVal(int hash, int key, int val) {
        int idx = (table.length - 1) & hash;
        if (table[idx] == null) {
            table[idx] = new LinkedMapNode(hash, key, val);
            keys[size++] = key;
        } else {
            if(table[idx].add(hash, key, val)) {
                keys[size++] = key;
                if(table[idx].size() >= TREEIFY_THRESHOLD && table[idx] instanceof LinkedMapNode) {
                    treeify(idx);
                }
            }
        }
    }
    private void resize() {
        MapNode[] newTab = new MapNode[table.length << 1];
        for (MapNode mapNode : table) {
            if (mapNode == null) {
                continue;
            }
            Node[] nodes = mapNode.array();
            for(Node node : nodes) {
                int idx = (newTab.length - 1) & node.hash();
                if(newTab[idx] == null) {
                    newTab[idx] = new LinkedMapNode(node);
                } else if(newTab[idx] instanceof LinkedMapNode && node instanceof LinkedNode) {
                    ((LinkedMapNode)newTab[idx]).add((LinkedNode)node);
                } else if(newTab[idx] instanceof RBTreeMapNode && node instanceof RBTreeNode) {
                    ((RBTreeMapNode)newTab[idx]).add(node);
                } else {
                    newTab[idx].add(node.hash(), node.key(), node.val());
                }
            }
        }
        table = newTab;
        keys = Arrays.copyOf(keys, (int) (table.length * LOAD_FACTOR));
    }
    private int[] keys() {
        return Arrays.copyOf(keys, size);
    }
    private void treeify(int idx) {
        LinkedMapNode mapNode = (LinkedMapNode) table[idx];
        Node[] array = mapNode.array();
        RBTreeMapNode rbTreeMapNode = new RBTreeMapNode(array[0].hash(), array[0].key(), array[0].val());
        //noinspection StatementWithEmptyBody
        for(int i = 1; i < array.length; rbTreeMapNode.add(array[i++]));
    }
    interface MapNode {
        int val(int key);
        boolean add(int hash, int key, int val);
        Node[] array();
        int size();
    }
    interface Node {
        int hash(); int key(); int val();
    }
    private static class LinkedMapNode implements MapNode {
        private LinkedNode root;
        private int size;
        public LinkedMapNode(int hash, int key, int val) {
            root = new LinkedNode(hash, key, val, null);
            size = 1;
        }
        public LinkedMapNode(Node node) {
            if(node instanceof  LinkedNode) {
                root = (LinkedNode) node;
                root.next = null;
            } else {
                root = new LinkedNode(node.hash(), node.key(), node.val(), null);
            }
            size = 1;
        }
        public int size() { return size; }
        public int val(int key) { return Optional.ofNullable(getNode(key)).map(Node::val).orElse(IntIntHashMap.NULL_VALUE); }
        public void add(LinkedNode node) {
            LinkedNode _node = getNode(node.key());
            if(Objects.nonNull(_node)) {
                _node.val = node.val();
            } else {
                node.next = root;
                root = node;
                ++size;
            }
        }
        public boolean add(int hash, int key, int val) {
            LinkedNode _node = getNode(key);
            if(Objects.nonNull(_node)) {
                _node.val = val;
                return false;
            } else {
                root = new LinkedNode(hash, key, val, root);
                ++size;
                return true;
            }
        }
        public Node[] array() {
            Node[] ans = new Node[size];
            int length = 0;
            LinkedNode tmp = root;
            while(tmp != null) {
                ans[length++] = tmp;
                tmp = tmp.next;
            }
            return ans;
        }
        private LinkedNode getNode(int key) {
            LinkedNode tmp = root;
            while(tmp != null) {
                if(tmp.key == key) {
                    return tmp;
                }
                tmp = tmp.next;
            }
            return null;
        }
    }
    private static class LinkedNode implements Node{
        final int hash;
        final int key;
        int val;
        LinkedNode next;
        public LinkedNode(int hash, int key, int val, LinkedNode next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }
        public int hash() {  return hash;  }
        public int key() { return key; }
        public int val() { return val; }
    }
    private static class RBTreeMapNode implements MapNode {
        RBTreeNode root;
        int size;
        public RBTreeMapNode(int hash, int key, int val) {
            root = new RBTreeNode(hash, key, val);
            root.red = false;
            size = 1;
        }
        private Node getNode(RBTreeNode node, int key) {
            if(node.key == key) {
                return node;
            }
            if(node.key > key && node.left != null) {
                return getNode(node.left, key);
            }
            if(node.key < key & node.right != null) {
                return getNode(node.right, key);
            }
            return null;
        }
        public int val(int key) { return Optional.ofNullable(getNode(root, key)).map(Node::val).orElse(NULL_VALUE); }
        public void add(Node node) {
            if(node instanceof RBTreeNode) {
                RBTreeNode newNode = (RBTreeNode) node;
                newNode.left = null;
                newNode.right = null;
                add(root, newNode);
            } else {
                add(root, node.hash(), node.key(), node.val());
            }
        }
        public boolean add(int hash, int key, int val) {
            return add(root, hash, key, val);
        }
        private boolean add(RBTreeNode node, int hash, int key, int val) {
            if(node.key == key) {
                node.val = val;
                return false;
            }
            if(node.key > key) {
                if(node.left == null) {
                    node.left = new RBTreeNode(hash, key, val, node);
                    ++size;
                    fix(node.left);
                    return true;
                } else {
                    return add(node.left, hash, key, val);
                }
            } else {
                if(node.right == null) {
                    node.right = new RBTreeNode(hash, key, val, node);
                    ++size;
                    fix(node.right);
                    return true;
                } else {
                    return add(node.right, hash, key, val);
                }
            }
        }
        private boolean add(RBTreeNode node, RBTreeNode newNode) {
            if(node.key == newNode.key) {
                node.val = newNode.val;
                return false;
            }
            if(node.key > newNode.key) {
                if(node.left == null) {
                    node.left = newNode;
                    ++size;
                    newNode.parent = node;
                    fix(newNode);
                    return true;
                } else {
                    return add(node.left, newNode);
                }
            } else {
                if(node.right == null) {
                    node.right = newNode;
                    ++size;
                    newNode.parent = node;
                    fix(newNode);
                    return true;
                } else {
                    return add(node.left, newNode);
                }
            }
        }
        private void fix(RBTreeNode node) {
            if(node.parent == null) {
                node.red = false;
                root = node;
                return;
            }
            RBTreeNode parent = node.parent;
            if(!parent.red) { return; }
            RBTreeNode uncle = parent == parent.parent.left ? parent.parent.right : parent.parent.left;
            boolean uncleRed = Optional.ofNullable(uncle).map(it -> it.red).orElse(false);
            if(uncleRed) {
                uncle.red = false;
                parent.red = false;
                parent.parent.red = true;
                fix(parent.parent);
            } else {
                if(parent == parent.parent.left) {
                    if(node == parent.right) {
                        rotateLeft(parent);
                        parent = node;
                        node = parent.left;
                    }
                    parent.red = false;
                    parent.parent.red = true;
                    rotateRight(parent.parent);
                } else {
                    if(node == parent.left) {
                        rotateRight(parent);
                        parent = node;
                        node = parent.right;
                    }
                    parent.red = false;
                    parent.parent.red = true;
                    rotateLeft(parent.parent);
                }
                fix(node);
            }
        }
        private void rotateRight(RBTreeNode node) {
            RBTreeNode parent = node.parent;
            RBTreeNode left = node.left;
            node.left = left.right;
            if(left.right != null) {
                left.right.parent = node;
            }
            left.right = node;
            rotate(node, parent, left);
        }
        private void rotateLeft(RBTreeNode node) {
            RBTreeNode parent = node.parent;
            RBTreeNode right = node.right;
            node.right = right.left;
            if(right.left != null) {
                right.left.parent = node;
            }
            right.left = node;
            rotate(node, parent, right);
        }
        private void rotate(RBTreeNode node, RBTreeNode parent, RBTreeNode sub) {
            node.parent = sub;
            sub.parent = parent;
            if(parent == null) {
                root = sub;
            } else {
                if(parent.left == node) {
                    parent.left = sub;
                } else {
                    parent.right = sub;
                }
            }
        }
        public Node[] array() {
            RBTreeNode[] nodes = new RBTreeNode[size];
            nodes[0] = root;
            int length = 1;
            int idx = 0;
            while(length < size) {
                if(nodes[idx].left != null) {
                    nodes[length++] = nodes[idx].left;
                }
                if(nodes[idx].right != null) {
                    nodes[length++] = nodes[idx].right;
                }
                ++idx;
            }
            return nodes;
        }
        public int size() { return size; }
    }
    private static class RBTreeNode implements Node {
        boolean red;
        final int hash;
        final int key;
        int val;
        RBTreeNode left;
        RBTreeNode right;
        RBTreeNode parent;
        public RBTreeNode(int hash, int key, int val) {
            this(hash, key, val, null);
        }
        public RBTreeNode(int hash, int key, int val, RBTreeNode parent) {
            red = true;
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.parent = parent;
        }
        public int hash() {
            return hash;
        }
        public int key() {
            return key;
        }
        public int val() {
            return val;
        }
    }
}
