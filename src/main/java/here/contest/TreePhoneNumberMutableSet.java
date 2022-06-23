package here.contest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class TreePhoneNumberMutableSet implements PhoneNumberMutableSet {

    private final Node root = new Node();

    @Override
    public void add(PhoneNumber phoneNumber) {
        var node = root;
        for (int digit : phoneNumber.digits()) {
            node = node.createLink(digit);
        }
    }

    @Override
    public boolean contains(PhoneNumber phoneNumber) {
        var node = root;
        for (int digit : phoneNumber.digits()) {
            node = node.traverse(digit);
            if (node == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int size() {
        return root.countLinks(0, PhoneNumber.LENGTH);
    }

    private static class Node {

        private final Node[] links = new Node[10];

        Node createLink(int digit) {
            var node = links[digit];
            if (node == null) {
                synchronized (this) {
                    node = links[digit];
                    if (node == null) {
                        links[digit] = node = new Node();
                    }
                }
            }
            return node;
        }

        Node traverse(int digit) {
            var link = links[digit];
            if (link != null) {
                return link;
            }
            synchronized (this) {
                return links[digit];
            }
        }

        int countLinks(int currentDepth, int seekedDepth) {
            if (currentDepth == seekedDepth) {
                return 1;
            }
            Collection<Node> snapshot;
            synchronized (this) {
                snapshot = Arrays.stream(links)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            }
            return snapshot.stream()
                .mapToInt(link -> link.countLinks(currentDepth + 1, seekedDepth))
                .sum();
        }
    }
}
