// Find node ref form node ref string and unlock
foundNode = search.findNode(args["nodeToUnlock"]);
//customLockService.unlockNode(foundNode);
foundNode.unlock();