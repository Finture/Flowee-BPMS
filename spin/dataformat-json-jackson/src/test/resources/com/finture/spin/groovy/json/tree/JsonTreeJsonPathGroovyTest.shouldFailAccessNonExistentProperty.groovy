package com.finture.spin.groovy.json.tree

jsonNode = S(input, "application/json");

jsonNode.jsonPath('$.order.task').elementList();