The solution contains a set implementation based on a tree. Each digit of a phone number is represented as a node in the tree. Every node can point to 10 children nodes (we have 10 digits) and phone number existence check is `O(1)` because every time we will be traversing the tree max `9` times (with every digit we go deeper one level).

The implementation is also thread safe.