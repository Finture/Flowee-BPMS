package com.finture.spin.groovy.json.tree

node = S(input, "application/json");
customers = node.prop("customers");

customers.removeLast(null);