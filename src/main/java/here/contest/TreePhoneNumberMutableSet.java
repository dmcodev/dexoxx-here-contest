package here.contest;

import java.util.Arrays;
import java.util.Objects;

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

        synchronized Node createLink(int digit) {
            if (links[digit] == null) {
                links[digit] = new Node();
            }
            return traverse(digit);
        }

        synchronized Node traverse(int digit) {
            return links[digit];
        }

        int countLinks(int currentDepth, int seekedDepth) {
            if (currentDepth == seekedDepth) {
                return 1;
            }
            return Arrays.stream(linksSnapshot())
                .filter(Objects::nonNull)
                .mapToInt(link -> link.countLinks(currentDepth + 1, seekedDepth))
                .sum();
        }

        private synchronized Node[] linksSnapshot() {
            return links.clone();
        }
    }
}
