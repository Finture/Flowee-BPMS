package com.finture.spin.groovy.xml.dom

if (input != null) {
    element = S(input)
}

if (newChild != null) {
    newChild = S(newChild)
}

element.replaceChild(existingChild, newChild)
